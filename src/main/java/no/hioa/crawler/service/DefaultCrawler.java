package no.hioa.crawler.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import no.hioa.crawler.model.Link;
import no.hioa.crawler.model.Page;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class DefaultCrawler
{
	private static final Logger	logger			= LoggerFactory.getLogger("fileLogger");

	private static final String	USER_AGENT		= "Mozilla/5.0 (Linux 3.0.0-13-virtual x86_64) Crawler (ab@prognett.no)";
	private static final int	PAGE_TIMEOUT	= 1000 * 15;

	private QueueManager		qm;

	public DefaultCrawler(QueueManager queueManager)
	{
		super();
		this.qm = queueManager;
	}

	public DefaultCrawler(Link link)
	{
		super();
		this.qm = new DefaultQueueManager(link, Collections.singletonList(link));
	}

	public DefaultCrawler()
	{
		super();
	}

	public void setQueueManager(QueueManager queueManager)
	{
		this.qm = queueManager;
	}

	public QueueManager getQueueManager()
	{
		return qm;
	}

	public void setDomain(Link link)
	{
		this.qm = new DefaultQueueManager(link, Collections.singletonList(link));
	}

	protected abstract void crawlDocument(Document document, Link url);

	protected abstract boolean shouldIgnoreLink(String link);

	protected abstract boolean shouldFollowDynamicLinks();

	protected final void startCrawling()
	{
		logger.info("Starting crawler");

		// get first link to start with
		Link link = qm.getNextLink();

		while (link != null)
		{
			if (shouldAbort())
			{
				logger.info("Aborting crawling of site");
				break;
			}

			logger.info("Got link {} from QM", link.getLink());
			long startTime = System.currentTimeMillis();

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

			beNice(startTime);
		}
	}

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

	protected boolean shouldAbort()
	{
		return false;
	}

	CrawlResult crawlLink(Link link)
	{
		Document document = fetchContent(link);
		if (document == null)
			return null;

		logger.info("Got content for {}", link.getLink());

		try
		{
			crawlDocument(document, link);
		}
		catch (Exception ex)
		{
			logger.error("Could not crawl document from link " + link.getLink(), ex);
		}

		StringBuffer content = new StringBuffer(document.html());
		Set<Link> links = extractLinks(document);
		logger.info("Found {} links on the page {}", links.size(), link.getLink());

		return new CrawlResult(content, links);
	}

	void beNice(long startTime)
	{
		try
		{
			long elapsedTime = System.currentTimeMillis() - startTime;

			// sleep a random time between 1 and 4 seconds
			long time = 1000 + (long) (Math.random() * 3000) - elapsedTime;

			if (time > 0)
			{
				logger.info("Waiting for {} ms", time);
				Thread.sleep(time);
			}
			else
				logger.info("No need to wait since elapsedTime was over limit");
		}
		catch (InterruptedException ex)
		{
			logger.error("Could not sleep", ex);
		}
	}

	Set<Link> extractLinks(Document doc)
	{
		Set<Link> links = new HashSet<>();
		for (Element element : doc.select("a[href]"))
		{
			String link = element.attr("abs:href");
			if (!StringUtils.isEmpty(link) && !shouldIgnoreLink(link))
			{
				links.add(new Link(link, !shouldFollowDynamicLinks()));
			}
		}

		return links;
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
