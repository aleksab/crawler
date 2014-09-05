package no.hioa.crawler.site;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import no.hioa.crawler.model.Link;
import no.hioa.crawler.service.DefaultCrawler;
import no.hioa.crawler.util.LinkUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SiteCrawler extends DefaultCrawler
{
	private static final Logger	logger			= LoggerFactory.getLogger("fileLogger");
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	private Link				site			= null;
	private int					pageCounter		= 0;
	private HashSet<Link>		externalLinks	= new HashSet<>();							;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");

		if (args.length != 1)
			throw new IllegalArgumentException("Please provide a site to crawl");

		SiteCrawler crawler = new SiteCrawler(new Link(args[0]));
		crawler.crawlSite();
	}

	public SiteCrawler(Link site) throws IOException
	{
		super(site);

		this.site = site;
	}

	/**
	 * Crawl the site and save stats to a file. The crawler will not exit before all found links have been crawled.
	 */
	public void crawlSite()
	{
		logger.info("Starting to crawl " + site.getLink());
		consoleLogger.info("Starting to crawl " + site.getLink());

		startCrawling();

		saveStats();

		logger.info("Done crawling " + site.getLink());
	}

	protected void crawlDocument(Document document)
	{
		pageCounter++;

		Set<Link> links = new HashSet<>();
		for (Element element : document.select("a[href]"))
		{
			String link = element.attr("abs:href");
			if (!StringUtils.isEmpty(link) && !shouldIgnoreLink(link))
			{
				links.add(new Link(link, !shouldFollowDynamicLinks()));
			}
		}

		for (Link link : links)
		{
			Link domain = new Link(LinkUtil.normalizeDomain(link.getLink()));
			if (!site.getLink().equalsIgnoreCase(domain.getLink()) && !externalLinks.contains(domain))
			{
				externalLinks.add(domain);
			}
		}
	}

	protected boolean shouldIgnoreLink(String link)
	{
		return false;
	}

	protected boolean shouldFollowDynamicLinks()
	{
		return true;
	}

	void saveStats()
	{
		File newFile = new File("target/" + site.getLink().toLowerCase() + "-stats.txt");
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

		newFile = new File("target/" + site.getLink().toLowerCase() + "-links.txt");
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
}
