package no.hioa.crawler.util;

import org.junit.Assert;
import org.junit.Test;

public class RegexUtilTest
{
	@Test
	public void testYearRegex()
	{
		String match = RegexUtil.matchRegex(".*(20\\d\\d).*", " 2015 ");
		Assert.assertEquals("2015", match);
	}

	@Test
	public void testYearRegex2()
	{
		String match = RegexUtil.matchRegex(".*(20\\d\\d).*", " oe</a><a class=\"nolink\"> \n on April 6, 2012 at 15:45 in </a><a href=\"http:/ ");
		Assert.assertEquals("2012", match);
	}

	@Test
	public void testYearRegex3()
	{
		String match = RegexUtil.matchRegex(".*(20\\d\\d).*", " 2012 2013 ");
		Assert.assertEquals("2013", match);
	}
}
