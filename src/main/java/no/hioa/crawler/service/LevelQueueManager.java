package no.hioa.crawler.service;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
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
public class LevelQueueManager implements QueueManager
{
	private static final Logger			logger			= LoggerFactory.getLogger("fileLogger");
	private static final Logger			consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	private ConcurrentLinkedQueue<Link>	queue			= null;
	private HashSet<Link>				knownLinks		= null;
	private HashSet<Link>				visitedLinks	= null;
	private Link						crawlDomain		= null;
	private int							maxLevel		= 0;

	/**
	 * Constructor
	 * 
	 * @param crawlDomain
	 *            the domain to crawl against
	 * @param seeds
	 *            list of start pages
	 */
	public LevelQueueManager(Link crawlDomain, List<Link> seeds, int maxLevel)
	{
		super();
		queue = new ConcurrentLinkedQueue<>();
		this.knownLinks = new HashSet<>();
		this.visitedLinks = new HashSet<>();
		this.crawlDomain = crawlDomain;
		this.maxLevel = maxLevel;

		knownLinks.addAll(seeds);
		queue.addAll(seeds);
	}

	@Override
	public Link getNextLink()
	{
		Link link = queue.poll();
		
		if (link != null)			
			visitedLinks.add(link);
		
		return link;
	}

	@Override
	public List<Link> getAllKnownLinks()
	{
		List<Link> list = new LinkedList<>();
		for (Link link : knownLinks)
		{
			list.add(link);
		}

		return list;
	}

	@Override
	public List<Link> getAllVisitedLinks()
	{
		List<Link> list = new LinkedList<>();
		for (Link link : visitedLinks)
		{
			list.add(link);
		}

		return list;
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
						if (isCorrectLevel(link, maxLevel))
						{
							logger.info("Adding link {} to queue", link);
							knownLinks.add(link);
							queue.add(link);
						}
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

	boolean isCorrectLevel(Link link, int maxLevel)
	{
		int level = LinkUtil.determineLinkLevel(link.getLink());
		return (level <= maxLevel);
	}
}
