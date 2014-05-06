package no.hioa.crawler.filmweb;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Crawls all reviews for the already precrawled filmweb movies. For a given film all reviews will be crawled in parallell (since they are on
 * different domains).
 */
public class ParallellReviewCrawler
{
	private static final Logger	logger			= LoggerFactory.getLogger("fileLogger");
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	private static final String	USER_AGENT		= "Mozilla/5.0 (Linux 3.0.0-13-virtual x86_64) Crawler (ab@prognett.no)";
	private static final int	PAGE_TIMEOUT	= 1000 * 10;

	private File				inputDir		= null;
	private File				outputDir		= null;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");

		ParallellReviewCrawler crawler = new ParallellReviewCrawler(new File("target/output"), new File("target/output_reviews"));
		crawler.crawlFilmwebReviews();
	}

	public ParallellReviewCrawler(File inputDir, File outputDir)
	{
		super();
		this.inputDir = inputDir;
		this.outputDir = outputDir;
	}

	@SuppressWarnings("deprecation")
	public void crawlFilmwebReviews() throws IOException, InterruptedException
	{
		FileUtils.forceMkdir(outputDir);

		consoleLogger.info("Extracting external reviews for filmweb");
		Map<File, Set<Review>> externalReviews = getExternalReviews(inputDir);

		long progress = 0;
		long totalReviews = externalReviews.size();
		consoleLogger.info("Starting to crawl {} reviews", totalReviews);

		long lastSleepTime = 5000;
		for (File file : externalReviews.keySet())
		{
			consoleLogger.info("Reviews processed: {} / {}", progress++, totalReviews);

			Set<Review> reviews = externalReviews.get(file);
			logger.info("Crawling external reviews ({}) for {}", reviews.size(), file.getName());

			beNice(lastSleepTime);
			lastSleepTime = System.currentTimeMillis();

			// start a crawling thread per external review
			List<ReviewCrawler> crawlers = new LinkedList<>();
			for (Review review : reviews)
			{
				ReviewCrawler crawler = new ReviewCrawler(review);
				crawler.start();
				crawlers.add(crawler);
			}

			long startTime = System.currentTimeMillis();
			while (true)
			{
				// avoid eternal loop
				long elapsedTime = System.currentTimeMillis() - startTime;
				if (elapsedTime > (PAGE_TIMEOUT + 5000))
				{
					logger.warn("Threads are taking too long to complete, aborting");
					for (ReviewCrawler crawler : crawlers)
						crawler.destroy();
					break;
				}

				// continue until all threads are done
				for (ReviewCrawler crawler : crawlers)
				{
					if (crawler.isAlive())
					{
						Thread.sleep(100);
						continue;
					}
				}

				// save content for each review
				int counter = 1;
				for (ReviewCrawler crawler : crawlers)
				{
					String fileName = StringUtils.substringBefore(file.getName(), ".page") + "_" + counter + ".page";
					saveContent(crawler.getReview(), crawler.getContent(), fileName);
					counter++;
				}

				break;
			}
		}
	}

	void saveContent(Review review, String content, String fileName)
	{
		if (content == null)
		{
			logger.warn("Content is empty for review {}, abort saving", review);
			return;
		}

		Path newFile = Paths.get(outputDir.getAbsolutePath(), fileName);
		logger.info("Saving content for review {} to file {}", review, newFile);

		try (BufferedWriter writer = Files.newBufferedWriter(newFile, Charset.defaultCharset()))
		{
			writer.append("URL: " + review.link + "\n");
			writer.append("RATING: " + review.rating + "\n");
			writer.append("NAME: " + review.name + "\n");
			writer.append(content);
		}
		catch (IOException ex)
		{
			logger.error("Could not save review " + review + " to file " + newFile, ex);
		}
	}

	/**
	 * Get all external links for reviews based on already crawler filmweb pages.
	 * 
	 * @return
	 */
	public Map<File, Set<Review>> getExternalReviews(File folder)
	{
		Map<File, Set<Review>> movies = new HashMap<>();

		for (File file : folder.listFiles())
		{
			FileContent content = getFileContent(file);
			Set<Review> externalReviews = getExternalReviews(content);

			if (!externalReviews.isEmpty())
			{
				logger.info("File {} has {} external reviews", file, externalReviews.size());
				movies.put(file, externalReviews);
			}
		}

		logger.info("Preprocessing done.");
		logger.info("Movies with external reviews: " + movies.keySet().size());
		logger.info("External reviews: " + getTotalReviws(movies));

		return movies;
	}

	/**
	 * When we search for external reviews we are looking for the div class newspaper_review.
	 * 
	 * @param link
	 * @param content
	 * @return
	 */
	Set<Review> getExternalReviews(FileContent fileContent)
	{
		Set<Review> reviews = new HashSet<>();
		Document doc = Jsoup.parse(fileContent.content);

		for (Element element : doc.select("div.newspaper_review"))
		{
			try
			{
				String ratingText = element.select("div.rating").first().attr("alt");
				String name = element.select("div.text > a").first().attr("alt");
				String externalLink = element.select("div.text > a").first().attr("href");

				ratingText = StringUtils.substringAfterLast(ratingText, " ");
				int rating = Integer.valueOf(ratingText);
				reviews.add(new Review(externalLink, rating, name));
			}
			catch (Exception ex)
			{
				logger.error("Could not get review for link " + fileContent.link, ex);
			}
		}

		return reviews;
	}

	/**
	 * First line contains the URL so we discard this when returning file content.
	 * 
	 * @param file
	 * @return
	 */
	FileContent getFileContent(File file)
	{
		String link = null;
		StringBuffer buffer = new StringBuffer();

		try (Scanner scanner = new Scanner(new FileInputStream(file), "UTF-8"))
		{
			if (scanner.hasNextLine())
			{
				link = scanner.nextLine();
			}

			while (scanner.hasNextLine())
			{
				String input = scanner.nextLine().toLowerCase();
				buffer.append(input);
			}
		}
		catch (Exception ex)
		{
			logger.error("Could not read content for file " + file.getAbsolutePath(), ex);
		}

		return new FileContent(link, buffer.toString());
	}

	/**
	 * Be nice and sleep a minimum time between 1 and 4 seconds.
	 * 
	 * @param startTime
	 */
	void beNice(long lastSleepTime)
	{
		try
		{
			long time = 1000 + (long) (Math.random() * 3000);
			long elapsedTime = System.currentTimeMillis() - lastSleepTime;

			if (elapsedTime > time)
				logger.info("We have already slept for {}, so no need to be nice and sleep more", elapsedTime);
			else
			{
				logger.info("Waiting for {} ms", time);
				Thread.sleep(time);
			}
		}
		catch (InterruptedException ex)
		{
			logger.error("Could not sleep", ex);
		}
	}

	private int getTotalReviws(Map<File, Set<Review>> movies)
	{
		int totalReviews = 0;
		for (File file : movies.keySet())
		{
			Set<Review> reviews = movies.get(file);
			totalReviews += reviews.size();
		}

		return totalReviews;
	}

	private class ReviewCrawler extends Thread
	{
		private Review	review	= null;
		private String	content	= null;

		public ReviewCrawler(Review review)
		{
			super();
			this.review = review;
		}

		@Override
		public void run()
		{
			Document document = fetchContent(review.link);
			if (document != null)
			{
				logger.info("Got content for {}", review.link);
				content = document.html();
			}
			else
				logger.warn("Could not get content for {}", review.link);
		}

		public Review getReview()
		{
			return review;
		}

		public String getContent()
		{
			return content;
		}

		private Document fetchContent(String link)
		{
			try
			{
				if (!StringUtils.startsWith(link, "http://"))
					link = "http://" + link;

				return Jsoup.connect(link).timeout(PAGE_TIMEOUT).userAgent(USER_AGENT).followRedirects(true).get();
			}
			catch (Exception ex)
			{
				logger.warn("Could not fetch content for link " + link, ex);
				return null;
			}
		}
	}

	public class Review
	{
		public String	link;
		public int		rating;
		public String	name;

		public Review(String link, int rating, String name)
		{
			super();
			this.link = link;
			this.rating = rating;
			this.name = name;
		}

		@Override
		public boolean equals(Object obj)
		{
			return EqualsBuilder.reflectionEquals(this, obj);
		}

		@Override
		public String toString()
		{
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}
	}

	private class FileContent
	{
		public String	link;
		public String	content;

		public FileContent(String link, String content)
		{
			super();
			this.link = link;
			this.content = content;
		}
	}
}
