package no.hio.crawler.service;

import java.util.List;

import no.hio.crawler.model.Page;

/**
 * The content manager is responsible for saving the results from a crawl phase.
 */
public interface ContentManager
{
	/**
	 * Saves the results from a crawl phase.
	 * 
	 * @param pages
	 * @return
	 */
	public boolean savePages(List<Page> pages);
}
