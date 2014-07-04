package no.hioa.crawler.komplett;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import no.hioa.crawler.model.Link;
import no.hioa.crawler.model.Page;
import no.hioa.crawler.service.QueueManager;
import no.hioa.crawler.util.LinkUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Queue manager for komplett is very simple. It uses a FIFO ordering of queues and all new links are accepted as long as they are part of the komplett
 * domain.
 */
public class KomplettQueueManager implements QueueManager
{
	private static final Logger			logger			= LoggerFactory.getLogger("fileLogger");
	private static final Logger			consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	private ConcurrentLinkedQueue<Link>	queue			= null;
	private HashSet<Link>				knownLinks		= null;

	public KomplettQueueManager(List<Link> seeds)
	{
		super();
		queue = new ConcurrentLinkedQueue<>();
		knownLinks = new HashSet<>();

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
				String domain = LinkUtil.normalizeDomain(link.getLink());
				if ("mpx.no".equalsIgnoreCase(domain) && !knownLinks.contains(link))
				{
					logger.info("Adding link {} to queue", link);
					knownLinks.add(link);
					queue.add(link);
				}
			}
		}

		consoleLogger.info("Queue size: {}, knownLinks: {}", queue.size(), knownLinks.size());
	}

}
