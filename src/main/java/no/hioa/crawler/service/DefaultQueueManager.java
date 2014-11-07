package no.hioa.crawler.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import no.hioa.crawler.model.Link;
import no.hioa.crawler.model.Page;
import no.hioa.crawler.util.LinkUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This default queue manager only adds link for a specified domain.
 */
public class DefaultQueueManager implements QueueManager
{
	private static final Logger			logger			= LoggerFactory.getLogger("fileLogger");
	private static final Logger			consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	private ConcurrentLinkedQueue<Link>	queue			= null;
	private HashSet<Link>				knownLinks		= null;
	private Link						crawlDomain		= null;

	/**
	 * Constructor
	 * 
	 * @param crawlDomain
	 *            the domain to crawl against
	 * @param seeds
	 *            list of start pages
	 */
	public DefaultQueueManager(Link crawlDomain, List<Link> seeds)
	{
		super();
		queue = new ConcurrentLinkedQueue<>();
		knownLinks = new HashSet<>();
		this.crawlDomain = crawlDomain;

		knownLinks.addAll(seeds);
		queue.addAll(seeds);
	}

	@Override
	public Link getNextLink()
	{
		return queue.poll();
	}

	@Override
	public List<Link> getNextLinks(int numberOfLinks)
	{
		return Collections.singletonList(queue.poll());
	}

	@Override
	public void updateQueue(Map<Page, Set<Link>> result)
	{
		for (Page page : result.keySet())
		{
			for (Link link : result.get(page))
			{
				try
				{
					String domain = LinkUtil.normalizeDomain(link.getLink());
					if (crawlDomain.getLink().equalsIgnoreCase(domain) && !knownLinks.contains(link))
					{
						logger.info("Adding link {} to queue", link);
						knownLinks.add(link);
						queue.add(link);
					}
				}
				catch (Exception ex)
				{
					logger.error("Could not add link to queue: " + link.getLink());
					knownLinks.add(link);
				}
			}
		}

		logger.info("Queue size ({}): {}, knownLinks: {}", crawlDomain.getLink(), queue.size(), knownLinks.size());
		consoleLogger.info("Queue size ({}): {}, knownLinks: {}", crawlDomain.getLink(), queue.size(), knownLinks.size());
	}
}
