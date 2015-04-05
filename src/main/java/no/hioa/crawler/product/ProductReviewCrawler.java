package no.hioa.crawler.product;

import java.io.IOException;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import no.hioa.crawler.model.Link;
import no.hioa.crawler.model.ReviewType;
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

public class ProductReviewCrawler extends DefaultCrawler
{
	private static final Logger		logger			= LoggerFactory.getLogger("fileLogger");
	private static final Logger		consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	private ReviewType				type			= null;
	private DefaultReviewManager	reviewManager	= null;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");

		ProductReviewCrawler crawler = new ProductReviewCrawler(ReviewType.getEnum(args[0]));
		crawler.generateReviewResults();
	}

	public ProductReviewCrawler(ReviewType type) throws IOException
	{
		super(new Link(type.getUrl()));

		this.type = type;
		this.reviewManager = new DefaultReviewManager("target/" + type.getName().toLowerCase());
	}

	public void generateReviewResults()
	{
		String output = "target/" + type.getName().toLowerCase() + "-reviews.xml";
		consoleLogger.info("Crawler completed, saving to file {}", output);

		reviewManager.generateXml(output);
	}
	
	/**
	 * Crawl the product view page and save review results to a file. The crawler will not exit before all found links have been crawled.
	 */
	public void crawlProductReviews()
	{
		consoleLogger.info("Starting to crawl " + type.getUrl());

		startCrawling();

		String output = "target/" + type.getName().toLowerCase() + "-reviews.xml";
		consoleLogger.info("Crawler completed, saving to file {}", output);

		reviewManager.generateXml(output);
	}

	protected void crawlDocument(Document document, Link url)
	{
		if (doesPageHasReviews(document))
		{
			List<Review> reviews = getReviews(document);
			logger.info("Found {} reviews", reviews.size());
			reviewManager.saveReviews(reviews);
		}
	}

	protected boolean shouldIgnoreLink(String link)
	{
		if (StringUtils.containsIgnoreCase(link, "/search"))
			return true;
		if (StringUtils.containsIgnoreCase(link, "/signin.aspx?"))
			return true;
		if (StringUtils.containsIgnoreCase(link, "/feed.aspx?"))
			return true;
		if (StringUtils.containsIgnoreCase(link, "/aboutme.aspx?"))
			return true;
		if (StringUtils.containsIgnoreCase(link, "/mlf/"))
			return true;
		if (StringUtils.containsIgnoreCase(link, ".pdf"))
			return true;
		if (StringUtils.containsIgnoreCase(link, ".jpg"))
			return true;
		if (StringUtils.containsIgnoreCase(link, ":8083"))
			return true;
		if (StringUtils.containsIgnoreCase(link, "action="))
			return true;

		return false;
	}

	protected boolean shouldFollowDynamicLinks()
	{
		return true;
	}

	boolean doesPageHasReviews(Document doc)
	{
		Elements elements = doc.select("a[class*=reviews]");
		return (elements.size() != 0);
	}

	List<Review> getReviews(Document document)
	{
		try
		{
			Elements elements = document.select("a[class*=reviews]");
			if (elements.size() != 1)
			{
				logger.error("Could not get link to reviews");
				return Collections.emptyList();
			}
			else
			{
				String sku = document.select("span[itemprop=sku]").first().text();
				String reviewLink = "https://www." + type.getUrl() + "/Review.aspx/AjaxList/" + sku + "/";
				logger.info("Sku {} has review link {}", sku, reviewLink);

				return getAllReviews(reviewLink, "");
			}
		}
		catch (Exception ex)
		{
			logger.error("Unexpected error", ex);
			return Collections.emptyList();
		}
	}

	List<Review> getAllReviews(String reviewBase, String reviewPage)
	{
		String reviewLink = reviewBase + reviewPage;
		logger.info("Fetching reviews from {}", reviewLink);
		List<Review> reviews = new LinkedList<>();

		Document reviewContent = fetchContent(new Link(reviewLink));

		Elements reviewElements = reviewContent.select("li[class=review]");

		for (Element element : reviewElements)
		{
			Review review = getReview(reviewLink, element);
			if (review != null)
				reviews.add(review);
		}

		Elements buttonElements = reviewContent.select("a[class=button]:containsOwn(>)");
		if (buttonElements.size() > 0)
		{
			reviews.addAll(getAllReviews(reviewBase, buttonElements.get(0).attr("href")));
		}

		return reviews;
	}

	Review getReview(String link, Element element)
	{
		try
		{
			String title = element.select("div[class=review-description] > h3").first().text();
			String content = element.select("div[class=review-description] > p").first().html();
			String author = element.select("div[class=review-info] > div[class=name] > a > strong[class=author]").first().text();
			String date = element.select("div[class=review-info] > div[class=date]").first().text();
			int rating = Integer.valueOf(element.select("div[class=review-info] > div[class=score] > img").first().attr("alt"));

			return new Review(link, rating, title, content, author, date, type);
		}
		catch (Exception ex)
		{
			logger.error("Unexpected error when fetching review from " + link, ex);
			return null;
		}
	}
}
