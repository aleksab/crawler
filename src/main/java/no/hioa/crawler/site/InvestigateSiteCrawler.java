package no.hioa.crawler.site;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import no.hioa.crawler.model.Link;
import no.hioa.crawler.service.DefaultCrawler;
import no.hioa.crawler.service.LevelQueueManager;
import no.hioa.crawler.util.LinkUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class InvestigateSiteCrawler extends DefaultCrawler
{
	private static final Logger	logger			= LoggerFactory.getLogger("fileLogger");
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	private Link				site			= null;
	private int					pageCounter		= 0;
	private HashSet<Link>		externalLinks	= new HashSet<>();

	@Parameter(names = "-site", description = "Site to crawel", required = true)
	private String				url				= null;

	@Parameter(names = "-maxLevel", description = "Max level to crawl", required = false)
	private int					maxLevel		= 2;

	@Parameter(names = "-ignoreLinks", description = "Which links to ignore (comma seperated)", required = false)
	private String				ignore			= "";

	@Parameter(names = "-maxTime", description = "Max time in minutes", required = false)
	private double				maxTimeInMin	= 2;

	private boolean				shouldAbort		= false;
	private List<String>		ignoreList		= null;
	private long				startTime		= 0;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		InvestigateSiteCrawler crawler = new InvestigateSiteCrawler(args);
		crawler.crawlSite();
	}

	public InvestigateSiteCrawler(String[] args) throws IOException
	{
		new JCommander(this, args);

		this.site = new Link(url);
		this.setDomain(site);

		ignoreList = new LinkedList<>();

		if (!StringUtils.isEmpty(ignore))
		{
			String[] splits = ignore.split(",");
			for (String ignore : splits)
			{
				ignoreList.add(StringUtils.trim(ignore));
			}
			logger.info("Ignore list: " + ignoreList);
		}

		this.setQueueManager(new LevelQueueManager(site, Collections.singletonList(site), maxLevel));
	}

	/**
	 * Crawl the site and save stats to a file. The crawler will not exit before all found links have been crawled.
	 */
	public void crawlSite()
	{
		logger.info("Starting to crawl " + site.getLink());
		consoleLogger.info("Starting to crawl " + site.getLink());
		startTime = System.currentTimeMillis();

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
			try
			{
				Link domain = new Link(LinkUtil.normalizeDomain(link.getLink()));
				if (!site.getLink().equalsIgnoreCase(domain.getLink()) && !externalLinks.contains(domain))
				{
					externalLinks.add(domain);
				}
			}
			catch (Exception ex)
			{
				logger.warn("Could not check if link belongs to domain: {}", link.getLink());
			}
		}
	}

	protected boolean shouldIgnoreLink(String link)
	{
		for (String ignore : ignoreList)
		{
			if (StringUtils.containsIgnoreCase(link, ignore))
				return true;
		}

		return false;
	}

	protected boolean shouldFollowDynamicLinks()
	{
		return true;
	}

	protected boolean shouldAbort()
	{
		long elapsedTime = (System.currentTimeMillis() - startTime) / (1000 * 60);

		if (elapsedTime > maxTimeInMin)
		{
			logger.info("Max time elapsed: {}", elapsedTime);
			return true;
		}
		else
			return shouldAbort;
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

		newFile = new File("target/" + site.getLink().toLowerCase() + "-visited-links.txt");
		logger.info("Saving visited links to file {}", newFile);

		try (PrintWriter writer = new PrintWriter(newFile, "ISO-8859-1"))
		{
			for (Link link : getQueueManager().getAllVisitedLinks())
				writer.write(link.getLink() + "\n");
		}
		catch (IOException ex)
		{
			logger.error("Could not save links to file " + newFile, ex);
		}

		newFile = new File("target/" + site.getLink().toLowerCase() + "-known-links.txt");
		logger.info("Saving known links to file {}", newFile);

		try (PrintWriter writer = new PrintWriter(newFile, "ISO-8859-1"))
		{
			for (Link link : getQueueManager().getAllKnownLinks())
				writer.write(link.getLink() + "\n");
		}
		catch (IOException ex)
		{
			logger.error("Could not save links to file " + newFile, ex);
		}
	}
}
