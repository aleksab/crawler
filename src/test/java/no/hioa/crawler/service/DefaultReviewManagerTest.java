package no.hioa.crawler.service;

import java.io.File;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.Test;

public class DefaultReviewManagerTest
{
	private DefaultReviewManager manager = null;

	@Before
	public void setup() throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		manager = new DefaultReviewManager("target/test");
	}
	
	@Test
	public void testGetReviews()
	{
		manager.getReview(new File("src/test/resoures/"));
	}
}
