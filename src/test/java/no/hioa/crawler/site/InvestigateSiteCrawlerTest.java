package no.hioa.crawler.site;

import no.hioa.crawler.model.Link;

import org.apache.log4j.PropertyConfigurator;
import org.jsoup.nodes.Document;
import org.junit.Before;
import org.junit.Test;

public class InvestigateSiteCrawlerTest
{
	private InvestigateSiteCrawler	crawler	= null;

	@Before
	public void setup() throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		crawler = new InvestigateSiteCrawler(new String[] { "-site", "prognett.no" });
	}

	@Test
	public void testCrawlDocument()
	{
		Document document = crawler.fetchContent(new Link("prognett.no/no/tjenester/ny-nettside"));
		System.out.println(document.head());
	}
	
	@Test
	public void testCrawlImageDocument()
	{
		Document document = crawler.fetchContent(new Link("frie-ytringer.com/bannere/FrieYtringerBanner728x90.jpg"));
		System.out.println(document.head());
	}
}
