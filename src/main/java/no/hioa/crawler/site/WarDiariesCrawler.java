package no.hioa.crawler.site;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import no.hioa.crawler.model.Link;
import no.hioa.crawler.model.Page;
import no.hioa.crawler.service.DefaultCrawler;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class WarDiariesCrawler extends DefaultCrawler
{
	private static final Logger	logger			= LoggerFactory.getLogger("fileLogger");
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	private Link				site			= null;
	private int					pageCounter		= 0;
	private HashSet<Link>		externalLinks	= new HashSet<>();

	@Parameter(names = "-save", description = "Should we save pages? Default is false", required = false)
	private boolean				shouldSavePages	= false;

	@Parameter(names = "-output", description = "Where to store pages", required = false)
	private String				folder			= "target/";

	private String				outputFolder	= null;
	private boolean				shouldAbort		= false;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		WarDiariesCrawler crawler = new WarDiariesCrawler(args);
		crawler.crawlSite();
	}

	public WarDiariesCrawler(String[] args) throws IOException
	{
		new JCommander(this, args);

		this.site = new Link("wardiaries.wikileaks.org");
		this.setDomain(site);

		if (shouldSavePages)
		{
			outputFolder = folder + "/" + removeNoneAlphaNumeric(site.getLink());
			FileUtils.forceMkdir(new File(outputFolder));
		}
	}

	/**
	 * Crawl the site and save stats to a file. The crawler will not exit before
	 * all found links have been crawled.
	 */
	public void crawlSite()
	{
		logger.info("Starting to crawl " + site.getLink());
		consoleLogger.info("Starting to crawl " + site.getLink());

		// first find all pages
		for (int i = 1; i <= 48357; i++)
		{
			long startTime = System.currentTimeMillis();

			Link pageLink = new Link("https://wardiaries.wikileaks.org/search/?sort=date&p=" + i, false);
			Document document = fetchContent(pageLink);

			Set<Link> links = new HashSet<>();
			for (Element element : document.select("a[href]"))
			{
				String link = element.attr("abs:href");
				if (!StringUtils.isEmpty(link) && !shouldIgnoreLink(link))
				{
					links.add(new Link(link, !shouldFollowDynamicLinks()));
				}
			}

			Page page = new Page(pageLink, null);
			getQueueManager().updateQueue(Collections.singletonMap(page, links));

			consoleLogger.info("Found {} links on page {}", links.size(), pageLink);
			beNice(startTime);
		}

		// get content of all pages
		Link link = getQueueManager().getNextLink();
		while (link != null)
		{
			logger.info("Got link {} from QM", link.getLink());
			long startTime = System.currentTimeMillis();

			try
			{
				// crawl link
				Document document = fetchContent(link);

				if (shouldSavePages)
				{
					savePage(document, link.getLink());
				}
			}
			catch (Exception ex)
			{
				logger.error("Could not crawl link " + link.getLink(), ex);
			}

			// get next link
			link = getQueueManager().getNextLink();

			beNice(startTime);
		}

		saveStats();

		logger.info("Done crawling " + site.getLink());
	}

	protected void crawlDocument(Document document, Link url)
	{

	}

	protected boolean shouldIgnoreLink(String link)
	{
		if (link.contains("/id/"))
			return false;
		else
			return true;
	}

	protected boolean shouldFollowDynamicLinks()
	{
		return true;
	}

	protected boolean shouldAbort()
	{
		return shouldAbort;
	}

	void saveStats()
	{
		File newFile = new File("target/" + removeNoneAlphaNumeric(site.getLink()) + "-stats.txt");
		logger.info("Saving stats to file {}", newFile);

		try (PrintWriter writer = new PrintWriter(newFile, "ISO-8859-1"))
		{
			writer.write("Site: " + site.getLink() + "\n");
			writer.append("Pages: " + pageCounter + "\n");
			writer.append("External Links: " + externalLinks.size() + "\n");
		}
		catch (IOException ex)
		{
			logger.error("Could not save stats to file " + newFile, ex);
		}

		newFile = new File("target/" + removeNoneAlphaNumeric(site.getLink()) + "-links.txt");
		logger.info("Saving links to file {}", newFile);

		try (PrintWriter writer = new PrintWriter(newFile, "ISO-8859-1"))
		{
			for (Link link : externalLinks)
				writer.write(link.getLink() + "\n");
		}
		catch (IOException ex)
		{
			logger.error("Could not save links to file " + newFile, ex);
		}
	}

	void savePage(Document document, String url)
	{
		try
		{
			StringBuffer buffer = new StringBuffer();
			buffer.append("URL: ").append(url).append("\n");
			buffer.append(document.html());

			String file = outputFolder + "/" + System.currentTimeMillis() + ".html";
			FileUtils.writeStringToFile(new File(file), buffer.toString(), "UTF-8");
		}
		catch (IOException ex)
		{
			logger.error("Could not save page", ex);
		}
	}

	private String removeNoneAlphaNumeric(String input)
	{
		return input.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
	}
}
