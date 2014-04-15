package no.hio.crawler.filmweb;

import java.util.Collections;

import no.hio.crawler.model.Link;
import no.hio.crawler.service.FileContentManager;

import org.apache.log4j.PropertyConfigurator;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SingleThreadCrawlerTest
{
	private SingleThreadCrawler	crawler	= null;

	@Before
	public void setup() throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		crawler = new SingleThreadCrawler(new FilmwebQueueManager(Collections.singletonList(new Link("filmweb.no"))), new FileContentManager("target/data"));
	}

	@Test
	public void testCrawlLink()
	{
		Document document = crawler.fetchContent(new Link("filmweb.no/filmnytt/article1158547.ece"));
		Assert.assertNotNull(document);
	}

}
