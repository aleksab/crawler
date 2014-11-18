package no.hioa.crawler.site;

import java.io.FileReader;
import java.io.FileWriter;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
		crawler = new FacebookCrawler(new String[] { "-group", "asd" });
	}

	@Test
	public void testCrawlDocument()
	{

	}

	@Test
	public void saveGroup1() throws Exception
	{
		String link = "https://www.facebook.com/britainfirstgb";
		String file = "src/test/resources/no/hioa/crawler/site/group1.document";
		Document document = Jsoup.connect(link).timeout(1000 * 15).userAgent("Mozilla/5.0 (Linux 3.0.0-13-virtual x86_64) Crawler (ab@prognett.no)").followRedirects(
				false).get();

		IOUtils.write(document.html(), new FileWriter(file));
	}

	@Test
	public void saveGroup2() throws Exception
	{
		String link = "https://www.facebook.com/NorgesFrihetspartinfp ";
		String file = "src/test/resources/no/hioa/crawler/site/group2.document";
		Document document = Jsoup.connect(link).timeout(1000 * 15).userAgent("Mozilla/5.0 (Linux 3.0.0-13-virtual x86_64) Crawler (ab@prognett.no)").followRedirects(
				false).get();

		IOUtils.write(document.html(), new FileWriter(file));
	}

	@Test
	public void getGroup1() throws Exception
	{
		String file = "src/test/resources/no/hioa/crawler/site/group1.document";
		Document document = getDocument(file);
		Assert.assertEquals(123, document.html().length());
	}

	@Test
	public void getGroup2() throws Exception
	{
		String file = "src/test/resources/no/hioa/crawler/site/group2.document";
		Document document = getDocument(file);
		Assert.assertEquals(123, document.html().length());
	}

	public static Document getDocument(String file) throws Exception
	{
		String html = IOUtils.toString(new FileReader(file));
		return Jsoup.parse(html);
	}
}
