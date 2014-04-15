package no.hio.crawler.service;

import java.util.List;
import java.util.Map;
import java.util.Set;

import no.hio.crawler.model.Link;
import no.hio.crawler.model.Page;

/**
 * The queue manager is responsible to organizing the queue for a crawler. This includes adding seeds, prioritizing links in the queue and deciding if
 * new links are to be added to the queue.
 */
public interface QueueManager
{
	/**
	 * Get next link to be crawled.
	 * 
	 * @return
	 */
	public Link getNextLink();

	/**
	 * Get next links to be crawled.
	 * 
	 * @param numberOfLinks
	 *            number of links to return
	 * @return
	 */
	public List<Link> getNextLinks(int numberOfLinks);

	/**
	 * Update queue with results from a crawler.
	 * 
	 * @param result
	 * @return
	 */
	public void updateQueue(Map<Page, Set<Link>> result);
}
