package no.hioa.crawler.filmweb;

import java.util.LinkedList;
import java.util.List;

import no.hioa.crawler.filmweb.parser.AdressaParser;
import no.hioa.crawler.filmweb.parser.BtParser;
import no.hioa.crawler.filmweb.parser.CineParser;
import no.hioa.crawler.filmweb.parser.DagbladetParser;
import no.hioa.crawler.filmweb.parser.FilmMagasinetParser;
import no.hioa.crawler.filmweb.parser.IgnoreParser;
import no.hioa.crawler.filmweb.parser.NordlysParser;
import no.hioa.crawler.filmweb.parser.NrkParser;
import no.hioa.crawler.filmweb.parser.OsloByParser;
import no.hioa.crawler.filmweb.parser.Side3Parser;
import no.hioa.crawler.filmweb.parser.Tv2Parser;
import no.hioa.crawler.filmweb.parser.VgParser;
import no.hioa.crawler.model.Link;
import no.hioa.crawler.model.Review;
import no.hioa.crawler.model.ReviewType;
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
	private static final Logger			logger			= LoggerFactory.getLogger("fileLogger");
	private static final Logger			consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	private static final int			TIMEOUT			= 1000 * 15;

	private DefaultReviewManager		reviewManager	= null;
	private List<ExternalContentParser>	contentParsers	= null;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");

		MovieReviewCrawler crawler = new MovieReviewCrawler();
		crawler.generateReviewResults();
	}

	public MovieReviewCrawler()
	{
		super(new Link(ReviewType.FILMWEB.getUrl()));
		this.reviewManager = new DefaultReviewManager("target/" + ReviewType.FILMWEB.getName().toLowerCase());

		contentParsers = new LinkedList<>();
		contentParsers.add(new DagbladetParser());
		contentParsers.add(new Side3Parser());
		contentParsers.add(new VgParser());
		contentParsers.add(new CineParser());
		contentParsers.add(new FilmMagasinetParser());
		contentParsers.add(new BtParser());
		contentParsers.add(new IgnoreParser());
		contentParsers.add(new OsloByParser());
		contentParsers.add(new Tv2Parser());
		contentParsers.add(new AdressaParser());
		contentParsers.add(new NordlysParser());
		contentParsers.add(new NrkParser());
	}

	public void generateReviewResults()
	{
		String output = "target/" + ReviewType.FILMWEB.getName().toLowerCase() + "-reviews.xml";
		consoleLogger.info("Crawler completed, saving to file {}", output);

		reviewManager.generateXml(output);
	}
		
	/**
	 * Crawl the filmweb and save review results to a file. The crawler will not exit before all found links have been crawled.
	 */
	public void crawlMovieReviews()
	{
		consoleLogger.info("Starting to crawl " + ReviewType.FILMWEB.getUrl());

		startCrawling();

		String output = "target/" + ReviewType.FILMWEB.getName().toLowerCase() + "-reviews.xml";
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
		if (StringUtils.containsIgnoreCase(link, "/rss/"))
			return true;
		if (StringUtils.containsIgnoreCase(link, ".pdf"))
			return true;
		if (StringUtils.containsIgnoreCase(link, ".jpg"))
			return true;
		
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
		List<Review> externalReviews = new LinkedList<>();
		List<Review> reviews = new LinkedList<>();

		String title = doc.select("div.view_omtale > div > div.info > h1").first().text();

		for (Element element : doc.select("div.newspaper_review"))
		{
			try
			{
				String externalLink = element.select("div.text > a").first().attr("href");
				String ratingText = element.select("div.rating").first().attr("alt");
				String author = element.select("div.text > a").first().attr("alt");
				String date = "";

				ratingText = StringUtils.substringAfterLast(ratingText, " ");
				int rating = Integer.valueOf(ratingText);
				externalReviews.add(new Review(externalLink, rating, title, "", author, date, ReviewType.FILMWEB));
			}
			catch (Exception ex)
			{
				logger.error("Could not get external review for element " + element, ex);
			}
		}

		// start a crawling thread per external review
		List<ExternalReviewCrawler> crawlers = new LinkedList<>();
		for (Review review : externalReviews)
		{
			ExternalReviewCrawler crawler = new ExternalReviewCrawler(review, contentParsers);
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
					}
					catch (InterruptedException ex)
					{
						logger.error("Could not sleep", ex);
					}

					continue;
				}
			}

			for (ExternalReviewCrawler crawler : crawlers)
			{
				if (crawler.hasParsedContent())
					reviews.add(crawler.getReview());
				else if (crawler.shouldIgnore())
					logger.warn("Link ignored " + crawler.getReview().getLink());
				else
					logger.warn("Could not get external content for " + crawler.getReview().getLink());
			}

			break;
		}

		return reviews;
	}
}
