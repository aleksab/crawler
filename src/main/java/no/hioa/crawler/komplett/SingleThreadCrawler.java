package no.hioa.crawler.komplett;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import no.hioa.crawler.model.Link;
import no.hioa.crawler.model.Page;
import no.hioa.crawler.service.QueueManager;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleThreadCrawler
{
	private static final Logger	logger			= LoggerFactory.getLogger("fileLogger");
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	private static final String	USER_AGENT		= "Mozilla/5.0 (Linux 3.0.0-13-virtual x86_64) Crawler (ab@prognett.no)";
	private static final int	PAGE_TIMEOUT	= 1000 * 10;

	private QueueManager		qm				= null;
	private String				folder			= null;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");

		SingleThreadCrawler crawler = new SingleThreadCrawler(new KomplettQueueManager(Collections.singletonList(new Link("mpx.no"))),
				"target/mpx");
		crawler.crawlKomplett();
	}

	public SingleThreadCrawler(QueueManager qm, String folder) throws IOException
	{
		super();
		this.qm = qm;
		this.folder = folder;
		
		FileUtils.forceMkdir(new File(folder));
	}

	public void printStats()
	{
		File outputDir = new File("target/mpx");
		consoleLogger.info("Pages saved: " + outputDir.listFiles().length);
	}

	/**
	 * Crawl the komplett and store review content to a file. The crawler will not exit before all found links have been crawled.
	 */
	public void crawlKomplett()
	{		
		// get first link to start with
		Link link = qm.getNextLink();

		while (link != null)
		{
			logger.info("Got link {} from QM", link.getLink());

			beNice();

			try
			{
				// crawl link
				CrawlResult result = crawlLink(link);

				// report result
				if (result != null)
				{
					Page page = new Page(link, result.content);
					qm.updateQueue(Collections.singletonMap(page, result.links));
				}
			}
			catch (Exception ex)
			{
				logger.error("Could not crawl link " + link.getLink(), ex);
			}

			// get next link
			link = qm.getNextLink();
		}
	}

	CrawlResult crawlLink(Link link)
	{
		Document document = fetchContent(link);
		if (document == null)
			return null;

		logger.info("Got content for {}", link.getLink());

		if (doesPageHasReviews(document))
		{
			List<ProductReview> reviews = getReviews(document);
			logger.info("Found {} reviews", reviews.size());			
			saveReviews(reviews);
		}

		StringBuffer content = new StringBuffer(document.html());
		Set<Link> links = extractLinks(document);
		logger.info("Found {} links on the page {}", links.size(), link.getLink());

		return new CrawlResult(content, links);
	}

	void saveReviews(List<ProductReview> reviews)
	{
		for (ProductReview review : reviews)
		{
			Path newFile = Paths.get(folder, System.currentTimeMillis() + ".review");
			logger.info("Saving review to file {}", newFile);
			try (BufferedWriter writer = Files.newBufferedWriter(newFile, Charset.defaultCharset()))
			{
				writer.append("URL: " + review.getLink() + "\n");
				writer.append("AUTHOR: " + review.getAuthor() + "\n");
				writer.append("DATE: " + review.getDate() + "\n");
				writer.append("RATING: " + review.getRating() + "\n");
				writer.append("TITLE: " + review.getName() + "\n");
				writer.append(review.getContent());
			}
			catch (IOException ex)
			{
				logger.error("Could not save review " + review + " to file " + newFile, ex);
			}
		}
	}

	boolean doesPageHasReviews(Document doc)
	{
		Elements elements = doc.select("a[class*=reviews]");
		return (elements.size() != 0);
	}

	List<ProductReview> getReviews(Document document)
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
				String reviewLink = "https://www.mpx.no/Review.aspx/AjaxList/" + sku + "/";
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

	List<ProductReview> getAllReviews(String reviewBase, String reviewPage)
	{
		String reviewLink = reviewBase + reviewPage;
		logger.info("Fetching reviews from {}", reviewLink);
		List<ProductReview> reviews = new LinkedList<>();

		Document reviewContent = fetchContent(new Link(reviewLink));

		Elements reviewElements = reviewContent.select("li[class=review]");

		for (Element element : reviewElements)
		{
			ProductReview review = getReview(reviewLink, element);
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

	ProductReview getReview(String link, Element element)
	{
		try
		{
			String title = element.select("div[class=review-description] > h3").first().text();
			String content = element.select("div[class=review-description] > p").first().html();
			String author = element.select("div[class=review-info] > div[class=name] > a > strong[class=author]").first().text();
			String date = element.select("div[class=review-info] > div[class=date]").first().text();
			int rating = Integer.valueOf(element.select("div[class=review-info] > div[class=score] > img").first().attr("alt"));

			return new ProductReview(link, rating, title, content, author, date);
		}
		catch (Exception ex)
		{
			logger.error("Unexpected error when fetching review from " + link, ex);
			return null;
		}
	}

	Document fetchContent(Link link)
	{
		try
		{
			String properLink = link.getLink();
			if (!StringUtils.startsWithIgnoreCase(properLink, "http://") && !StringUtils.startsWithIgnoreCase(properLink, "https://"))
				properLink = "http://" + properLink;
			return Jsoup.connect(properLink).timeout(PAGE_TIMEOUT).userAgent(USER_AGENT).followRedirects(true).get();
		}
		catch (Exception ex)
		{
			logger.warn("Could not fetch content for link " + link.getLink(), ex);
			return null;
		}
	}

	Set<Link> extractLinks(Document doc)
	{
		Set<Link> links = new HashSet<>();
		for (Element element : doc.select("a[href]"))
		{
			String link = element.attr("abs:href");
			if (link != null)
			{
				if (!shouldIgnoreLink(link))
					links.add(new Link(link, false));
			}
		}

		return links;
	}
	
	boolean shouldIgnoreLink(String link)
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

	// TODO: this can be smarter. subtract time used in case of timeout from pages etc
	void beNice()
	{
		try
		{
			// sleep a random time between 1 and 4 seconds
			long time = 1000 + (long) (Math.random() * 3000);
			logger.info("Waiting for {} ms", time);
			Thread.sleep(time);
		}
		catch (InterruptedException ex)
		{
			logger.error("Could not sleep", ex);
		}
	}

	private class CrawlResult
	{
		public StringBuffer	content;
		public Set<Link>	links;

		public CrawlResult(StringBuffer content, Set<Link> links)
		{
			super();
			this.content = content;
			this.links = links;
		}
	}
}
