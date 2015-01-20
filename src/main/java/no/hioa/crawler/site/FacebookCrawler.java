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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

public class FacebookCrawler
{
	private static final Logger	logger			= LoggerFactory.getLogger("fileLogger");
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	private Link				group			= null;

	@Parameter(names = "-group", description = "Facebook group to crawel", required = true)
	private String				url				= null;

	@Parameter(names = "-save", description = "Should we save pages? Default is false", required = false)
	private boolean				shouldSavePages	= false;

	@Parameter(names = "-output", description = "Where to store pages", required = false)
	private String				folder			= "target/";

	@Parameter(names = "-maxSize", description = "Max size of files", required = false)
	private double				maxSizeMb		= 10 * 1024 * 2014;

	private String				outputFolder	= null;
	private boolean				shouldAbort		= false;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		FacebookCrawler crawler = new FacebookCrawler(args);
		crawler.crawlGroup();
	}

	public FacebookCrawler(String[] args) throws IOException
	{
		new JCommander(this, args);

		this.group = new Link(url);

		if (shouldSavePages)
		{
			outputFolder = folder + "/" + removeNoneAlphaNumeric(group.getLink());
			FileUtils.forceMkdir(new File(outputFolder));
		}
	}

	/**
	 * Crawl the group and save stats to a file. The crawler will not exit
	 * before all found links have been crawled.
	 */
	public void crawlGroup()
	{
		logger.info("Starting to crawl group " + group.getLink());
		consoleLogger.info("Starting to crawl group " + group.getLink());

		Document document = fetchContent(group);
		
		if (document != null)
			savePage(document);
		
		logger.info("Done crawling group " + group.getLink());
	}

	private static final String	USER_AGENT		= "Mozilla/5.0 (Linux 3.0.0-13-virtual x86_64)";
	private static final int	PAGE_TIMEOUT	= 1000 * 15;
	
	public Document fetchContent(Link link)
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
	
	void savePage(Document document)
	{
		try
		{
			String file = outputFolder + "/" + System.currentTimeMillis() + ".html";
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
