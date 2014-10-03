package no.hioa.crawler.site;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Set;

import no.hioa.crawler.model.Link;
import no.hioa.crawler.service.DefaultCrawler;
import no.hioa.crawler.util.LinkUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class SiteCrawler extends DefaultCrawler
{
	private static final Logger	logger			= LoggerFactory.getLogger("fileLogger");
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	private Link				site			= null;
	private int					pageCounter		= 0;
	private HashSet<Link>		externalLinks	= new HashSet<>();

	@Parameter(names = "-site", description = "Site to crawel", required = true)
	private String				url				= null;

	@Parameter(names = "-save", description = "Should we save pages? Default is false", required = false)
	private boolean				shouldSavePages	= false;

	@Parameter(names = "-output", description = "Where to store pages", required = false)
	private String				folder			= "target/";

	private String				outputFolder	= null;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		SiteCrawler crawler = new SiteCrawler(args);
		crawler.crawlSite();
	}

	public SiteCrawler(String[] args) throws IOException
	{
		new JCommander(this, args);

		this.site = new Link(url);
		this.setDomain(site);

		if (shouldSavePages)
		{
			outputFolder = folder + "/" + removeNoneAlphaNumeric(site.getLink());
			FileUtils.forceMkdir(new File(outputFolder));
		}
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

		if (shouldSavePages)
		{
			savePage(document);
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

	private void savePage(Document document)
	{
		try
		{
			String file = outputFolder + "/" + removeNoneAlphaNumeric(document.title()) + "_" + System.currentTimeMillis();
			FileUtils.writeStringToFile(new File(file), document.html(), "UTF-8");
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
