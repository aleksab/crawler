package no.hioa.crawler.filmweb;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import no.hioa.crawler.model.Link;
import no.hioa.crawler.model.Page;
import no.hioa.crawler.service.DefaultQueueManager;
import no.hioa.crawler.service.QueueManager;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FilmwebQueueManagerTest
{
	private QueueManager qm = null;
	private List<Link> seeds = null;

	@Before
	public void setup() throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");

		seeds = new LinkedList<>();
		seeds.add(new Link("filmweb.no"));
		seeds.add(new Link("filmweb.no/test"));

		qm = new DefaultQueueManager(new Link("filmweb.no"), seeds);
	}

	@Test
	public void testGetNextLink()
	{
		Assert.assertEquals(new Link("filmweb.no"), qm.getNextLink());
	}

	@Test
	public void testGetNextLink2()
	{
		Assert.assertEquals(new Link("filmweb.no"), qm.getNextLink());
		Assert.assertEquals(new Link("filmweb.no/test"), qm.getNextLink());
	}

	@Test
	public void testGetNextLinks()
	{
		List<Link> links = qm.getNextLinks(5);
		Assert.assertEquals(1, links.size());
		Assert.assertEquals(new Link("filmweb.no"), links.get(0));
	}

	@Test
	public void testUpdateQueue()
	{
		Set<Link> links = new HashSet<>();
		links.add(new Link("filmweb.no/test2"));
		links.add(new Link("filmweb.no/test3"));

		Map<Page, Set<Link>> result = new HashMap<>();
		Page page = new Page(new Link("filmweb.no/test"), new StringBuffer("Bla"));
		result.put(page, links);

		qm.updateQueue(result);

		Assert.assertEquals(new Link("filmweb.no"), qm.getNextLink());
		Assert.assertEquals(new Link("filmweb.no/test"), qm.getNextLink());
		Assert.assertEquals(new Link("filmweb.no/test3"), qm.getNextLink());
		Assert.assertEquals(new Link("filmweb.no/test2"), qm.getNextLink());

	}
}
