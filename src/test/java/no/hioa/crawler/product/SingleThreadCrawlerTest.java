package no.hioa.crawler.product;

import java.util.List;

import no.hioa.crawler.model.Link;
import no.hioa.crawler.model.Review;

import org.apache.log4j.PropertyConfigurator;
import org.jsoup.nodes.Document;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class SingleThreadCrawlerTest
{
	private ProductReviewCrawler crawler = null;

	@Before
	public void setup() throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		crawler = new ProductReviewCrawler(ProductReviewType.KOMPLETT);
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
		List<Review> reviews = crawler.getReviews(document);
		Assert.assertEquals(28, reviews.size());
	}
}
