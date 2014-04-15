package no.hio.crawler.filmweb;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;

import no.hio.crawler.model.Link;
import no.hio.crawler.model.Page;
import no.hio.crawler.service.QueueManager;
import no.hio.crawler.util.LinkUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Queue manager for filmweb is very simple. It uses a FIFO ordering of queues and all new links are accepted as long as they are part of the filmweb
 * domain.
 */
public class FilmwebQueueManager implements QueueManager
{
	private static final Logger			logger		= LoggerFactory.getLogger(FilmwebQueueManager.class);

	private ConcurrentLinkedQueue<Link>	queue		= null;
	private HashSet<Link>				knownLinks	= null;

	public FilmwebQueueManager(List<Link> seeds)
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
	public void updateQueue(Map<Page, List<Link>> result)
	{
		logger.info("New result {}", result);
		for (Page page : result.keySet())
		{
			for (Link link : result.get(page))
			{
				String domain = LinkUtil.normalizeDomain(link.getLink());
				if ("filmweb.no".equalsIgnoreCase(domain) && !knownLinks.contains(link))
				{
					logger.info("Adding link {} to queue", link);
					knownLinks.add(link);
					queue.add(link);
				}
			}
		}
	}

}
