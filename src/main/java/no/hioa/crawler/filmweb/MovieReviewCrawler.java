package no.hioa.crawler.filmweb;

import java.util.LinkedList;
import java.util.List;

import no.hioa.crawler.model.Link;
import no.hioa.crawler.model.Review;
import no.hioa.crawler.service.DefaultCrawler;
import no.hioa.crawler.service.DefaultReviewManager;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MovieReviewCrawler extends DefaultCrawler
{
	private static final Logger logger = LoggerFactory.getLogger("fileLogger");
	private static final Logger consoleLogger = LoggerFactory.getLogger("stdoutLogger");

	private static final int TIMEOUT = 1000 * 15;

	private DefaultReviewManager reviewManager = new DefaultReviewManager("target/movie");

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");

		MovieReviewCrawler crawler = new MovieReviewCrawler();
		crawler.crawlMovieReviews();
	}

	public MovieReviewCrawler()
	{
		super(new Link("filmweb.no"));
		this.reviewManager = new DefaultReviewManager("target/movie");
	}

	/**
	 * Crawl the filmweb and save review results to a file. The crawler will not
	 * exit before all found links have been crawled.
	 */
	public void crawlMovieReviews()
	{
		consoleLogger.info("Starting to crawl filmweb.no");

		startCrawling();

		String output = "target/movie-reviews.xml";
		consoleLogger.info("Crawler completed, saving to file {}", output);

		reviewManager.generateXml(output);
	}

	protected void crawlDocument(Document document)
	{
		if (doesPageHaveExternalReviews(document))
		{
			List<Review> reviews = getReviews(document);
			logger.info("Found {} reviews", reviews.size());
			reviewManager.saveReviews(reviews);
		}
	}

	protected boolean shouldIgnoreLink(String link)
	{
		return false;
	}

	protected boolean shouldFollowDynamicLinks()
	{
		return false;
	}
	
	boolean doesPageHaveExternalReviews(Document doc)
	{
		Elements elements = doc.select("div.newspaper_review");
		return (elements.size() != 0);
	}

	@SuppressWarnings("deprecation")
	List<Review> getReviews(Document doc)
	{
		List<Review> reviews = new LinkedList<>();

		for (Element element : doc.select("div.newspaper_review"))
		{
			try
			{
				String externalLink = element.select("div.text > a").first().attr("href");
				String ratingText = element.select("div.rating").first().attr("alt");
				String title = "";
				String author = element.select("div.text > a").first().attr("alt");
				String date = null;

				ratingText = StringUtils.substringAfterLast(ratingText, " ");
				int rating = Integer.valueOf(ratingText);
				reviews.add(new Review(externalLink, rating, title, "", author, date));
			} catch (Exception ex)
			{
				logger.error("Could not get external review for element " + element, ex);
			}
		}

		// start a crawling thread per external review
		List<ExternalReviewCrawler> crawlers = new LinkedList<>();
		for (Review review : reviews)
		{
			ExternalReviewCrawler crawler = new ExternalReviewCrawler(review);
			crawler.start();
			crawlers.add(crawler);
		}

		long startTime = System.currentTimeMillis();
		while (true)
		{
			// avoid eternal loop
			long elapsedTime = System.currentTimeMillis() - startTime;
			if (elapsedTime > TIMEOUT)
			{
				logger.warn("Threads are taking too long to complete, aborting");
				for (ExternalReviewCrawler crawler : crawlers)
					crawler.destroy();
				break;
			}

			// continue until all threads are done
			for (ExternalReviewCrawler crawler : crawlers)
			{
				if (crawler.isAlive())
				{
					try
					{
						Thread.sleep(200);
					} catch (InterruptedException ex)
					{
						logger.error("Could not sleep", ex);
					}

					continue;
				}
			}

			for (ExternalReviewCrawler crawler : crawlers)
			{
				reviews.add(crawler.getReview());
			}

			break;
		}

		return reviews;
	}
}
