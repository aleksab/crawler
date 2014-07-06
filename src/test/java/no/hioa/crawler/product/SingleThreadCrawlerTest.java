package no.hioa.crawler.product;

import java.util.LinkedList;
import java.util.List;

import no.hioa.crawler.model.Link;

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
		crawler = new SingleThreadCrawler(ProductReviewType.KOMPLETT);
	}

	@Test
	public void testDoesPageHasReviews()
	{
		Document document = crawler.fetchContent(new Link("https://www.komplett.no/asus-rt-n66u-dark-knight-11n-n900-router/746279"));
		Assert.assertTrue(crawler.doesPageHasReviews(document));
	}

	@Test
	public void testDoesPageHasReviews2()
	{
		Document document = crawler.fetchContent(new Link("https://www.komplett.no/k/kd.aspx?bn=10444"));
		Assert.assertFalse(crawler.doesPageHasReviews(document));
	}

	@Test
	public void testGetReviews()
	{
		Document document = crawler.fetchContent(new Link("https://www.komplett.no/asus-rt-n66u-dark-knight-11n-n900-router/746279"));
		List<ProductReview> reviews = crawler.getReviews(document);
		Assert.assertEquals(28, reviews.size());
	}	
	
	@Test
	public void testSaveReviews()
	{		
		List<ProductReview> reviews = new LinkedList<>();
		reviews.add(new ProductReview("link", 5, "test review", "review content", "author", "01.01.2014"));
		crawler.saveReviews(reviews);
	}	
}

