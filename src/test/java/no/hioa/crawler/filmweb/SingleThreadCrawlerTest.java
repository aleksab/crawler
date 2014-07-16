package no.hioa.crawler.filmweb;

import no.hioa.crawler.model.Link;

import org.apache.log4j.PropertyConfigurator;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SingleThreadCrawlerTest
{
	private MovieReviewCrawler crawler = null;

	@Before
	public void setup() throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		crawler = new MovieReviewCrawler();
	}

	@Test
	public void testCrawlLink()
	{
		Document document = crawler.fetchContent(new Link("filmweb.no/filmnytt/article1158547.ece"));
		Assert.assertNotNull(document);
	}

}
