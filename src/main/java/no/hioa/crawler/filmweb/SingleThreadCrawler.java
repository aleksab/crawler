package no.hioa.crawler.filmweb;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import no.hioa.crawler.model.Link;
import no.hioa.crawler.model.Page;
import no.hioa.crawler.service.ContentManager;
import no.hioa.crawler.service.FileContentManager;
import no.hioa.crawler.service.QueueManager;
import no.hioa.crawler.util.LinkUtil;

import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SingleThreadCrawler implements Runnable
{
	private static final Logger	logger			= LoggerFactory.getLogger("fileLogger");
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	private static final String	USER_AGENT		= "Mozilla/5.0 (Linux 3.0.0-13-virtual x86_64) Crawler (ab@prognett.no)";
	private static final int	PAGE_TIMEOUT	= 1000 * 10;

	private QueueManager		qm				= null;
	private ContentManager		cm				= null;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");

		SingleThreadCrawler crawler = new SingleThreadCrawler(new FilmwebQueueManager(Collections.singletonList(new Link("filmweb.no"))),
				new FileContentManager("target/output"));
		crawler.run();
	}

	public SingleThreadCrawler(QueueManager qm, ContentManager cm)
	{
		super();
		this.qm = qm;
		this.cm = cm;
	}

	public void printStats()
	{
		File outputDir = new File("target/output");
		consoleLogger.info("Pages saved: " + outputDir.listFiles().length);
	}

	@Override
	public void run()
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
					cm.savePages(Collections.singletonList(page));
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
		StringBuffer content = new StringBuffer(document.html());
		Set<Link> links = extractLinks(document);
		logger.info("Found {} links on the page {}", links.size(), link.getLink());

		return new CrawlResult(content, links);
	}

	Document fetchContent(Link link)
	{
		try
		{
			return Jsoup.connect("http://" + link.getLink()).timeout(PAGE_TIMEOUT).userAgent(USER_AGENT).followRedirects(true).get();
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
				links.add(new Link(LinkUtil.normalizeLink(link, true)));
			}
		}

		return links;
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
