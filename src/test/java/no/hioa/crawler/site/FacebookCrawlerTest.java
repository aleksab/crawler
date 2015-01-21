package no.hioa.crawler.site;

import no.hioa.crawler.model.facebook.GroupFeed;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * https://www.facebook.com/groups/verdier.i.sentrum https://www.facebook.com/NorgesFrihetspartinfp https://www.facebook.com/britainfirstgb
 */
public class FacebookCrawlerTest
{
	private FacebookCrawler	crawler	= null;

	@Before
	public void setup() throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		crawler = new FacebookCrawler(new String[] { "-groupId", "1", "-token", "2" });
	}

	@Test
	public void testParseJson()
	{
		GroupFeed feed = crawler.parseJson("src/test/resources/no/hioa/crawler/site/group1.json");
		Assert.assertEquals("438852196217474_548708421898517", feed.getData().get(0).getId());
	}
}
