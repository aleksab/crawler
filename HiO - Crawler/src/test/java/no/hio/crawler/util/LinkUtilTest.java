package no.hio.crawler.util;

import org.junit.Assert;
import org.junit.Test;

public class LinkUtilTest
{
	@Test
	public void testNormalizeDomain()
	{
		Assert.assertEquals("test.com", LinkUtil.normalizeDomain("http://www.test.com/"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testNormalizeDomain2()
	{
		LinkUtil.normalizeDomain("http://www.test.com/[Url]");
	}

	@Test
	public void testnormalizeDomainWithPath()
	{
		Assert.assertEquals("test.com", LinkUtil.normalizeDomain("http://www.test.com/test/test2"));
	}

	@Test
	public void testnormalizeDomainWithArguments()
	{
		Assert.assertEquals("test.com", LinkUtil.normalizeDomain("http://www.test.com/test?param=1&param2=2"));
	}

	@Test
	public void testnormalizeDomainWithoutWww()
	{
		Assert.assertEquals("test.com", LinkUtil.normalizeDomain("http://test.com/"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testnormalizeDomainWithoutHttpBug()
	{
		LinkUtil.normalizeDomain("JavaScript:showSearchPanel();");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testnormalizeDomainWithoutHttp()
	{
		LinkUtil.normalizeDomain("//:test.com/");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testnormalizeDomainWithoutHttp2()
	{
		LinkUtil.normalizeDomain("//test.com/");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testnormalizeDomainWithoutHttp3()
	{
		LinkUtil.normalizeDomain("/test.com/");
	}

	@Test
	public void testnormalizeDomainWithoutHttp4()
	{
		Assert.assertEquals("test.com", LinkUtil.normalizeDomain("test.com"));
	}

	@Test
	public void testnormalizeDomainInvalid()
	{
		Assert.assertEquals("httpfoo", LinkUtil.normalizeDomain("httpfoo/bar"));
	}

	@Test
	public void testnormalizeDomainUppercase()
	{
		Assert.assertEquals("example.com", LinkUtil.normalizeDomain("HTTP://example.com/"));
	}

	@Test
	public void testnormalizeDomainWithoutHttp5()
	{
		Assert.assertEquals("example.com", LinkUtil.normalizeDomain("example.com/"));
	}

	@Test
	public void testnormalizeDomainInvalid2()
	{
		Assert.assertEquals("www", LinkUtil.normalizeDomain("www/foo"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testnormalizeDomainInvalid3()
	{
		LinkUtil.normalizeDomain("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testnormalizeDomainInvalid4()
	{
		LinkUtil.normalizeDomain(null);
	}

	@Test
	public void testDetermineLinkLevel()
	{
		Assert.assertEquals(0, LinkUtil.determineLinkLevel("http://test.com/"));
	}

	@Test
	public void testDetermineLinkLevel2()
	{
		Assert.assertEquals(1, LinkUtil.determineLinkLevel("http://test.com/test/"));
	}

	@Test
	public void testDetermineLinkLevel3()
	{
		Assert.assertEquals(1, LinkUtil.determineLinkLevel("http://test.com/test"));
	}

	@Test
	public void testDetermineLinkLevel4()
	{
		Assert.assertEquals(2, LinkUtil.determineLinkLevel("http://test.com/test/test/"));
	}

	@Test
	public void testDetermineLinkLevel5()
	{
		Assert.assertEquals(2, LinkUtil.determineLinkLevel("http://test.com/test/test"));
	}

	@Test
	public void testDetermineLinkLevel6()
	{
		Assert.assertEquals(5, LinkUtil.determineLinkLevel("http://test.com/test/test/test/test/test"));
	}

	@Test
	public void testDetermineLinkLevel7()
	{
		Assert.assertEquals(0, LinkUtil.determineLinkLevel("test.com"));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDetermineLinkLevelInvalid()
	{
		LinkUtil.determineLinkLevel("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testDetermineLinkLevelInvalid2()
	{
		LinkUtil.determineLinkLevel(null);
	}

	@Test
	public void testnormalizeLink()
	{
		Assert.assertEquals("test.com/test/test", LinkUtil.normalizeLink("http://test.com/test/test/", true));
	}

	@Test
	public void testnormalizeLink2()
	{
		Assert.assertEquals("test.com/test/test", LinkUtil.normalizeLink("http://test.com/test/test/?param=id", true));
	}

	@Test
	public void testnormalizeLink3()
	{
		Assert.assertEquals("test.com/test/test", LinkUtil.normalizeLink("http://test.com/test/test#test", true));
	}

	@Test
	public void testnormalizeLink4()
	{
		Assert.assertEquals("test.com/test/test", LinkUtil.normalizeLink("http://test.com/test/test?test=test1&test2=test3;jsessionid=123123", true));
	}

	@Test
	public void testnormalizeLink5()
	{
		Assert.assertEquals("test.com/test/test", LinkUtil.normalizeLink("      http://test.com/test/test/          ", true));
	}

	@Test
	public void testnormalizeLink6()
	{
		Assert.assertEquals("test.com/test/test", LinkUtil.normalizeLink("HTTP://test.com/test/test/", true));
	}

	@Test
	public void testnormalizeLink7()
	{
		Assert.assertEquals("test.com/test/test", LinkUtil.normalizeLink("HTTP://www.test.com/test/test/", true));
	}

	@Test
	public void testnormalizeLink8()
	{
		Assert.assertEquals("test.com/test/test", LinkUtil.normalizeLink("www.test.com/test/test/", true));
	}

	@Test
	public void testnormalizeLink9()
	{
		Assert.assertEquals("test.com/test/test?test=test1&test2=test3",
				LinkUtil.normalizeLink("http://test.com/test/test?test=test1&test2=test3;jsessionid=123123", false));
	}
	
	@Test
	public void testnormalizeLink10()
	{
		Assert.assertEquals("test.com/test/test/?param=id", LinkUtil.normalizeLink("http://test.com/test/test/?param=id", false));
	}
	
	@Test
	public void testnormalizeLink11()
	{
		Assert.assertEquals("filmweb.no", LinkUtil.normalizeLink("mailto:kundeservice@filmweb.no", false));
	}

	@Test(expected = IllegalArgumentException.class)
	public void testnormalizeLinkInvalid()
	{
		LinkUtil.normalizeLink("", true);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testnormalizeLinkInvalid2()
	{
		LinkUtil.normalizeLink(null, true);
	}

	@Test
	public void testGetTopLevelDomain()
	{
		Assert.assertEquals("com", LinkUtil.getTopLevelDomain("http://www.test.com"));
	}

	@Test
	public void testGetTopLevelDomain2()
	{
		Assert.assertEquals("com", LinkUtil.getTopLevelDomain("test.com"));
	}

	@Test
	public void testGetTopLevelDomain3()
	{
		Assert.assertEquals("uk", LinkUtil.getTopLevelDomain("www.test.co.uk/"));
	}

	@Test
	public void testGetTopLevelDomain4()
	{
		LinkUtil.getTopLevelDomain("test");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetTopLevelDomain5()
	{
		LinkUtil.getTopLevelDomain("www.test..com");
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetTopLevelDomain6()
	{
		LinkUtil.getTopLevelDomain(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void testGetTopLevelDomain7()
	{
		LinkUtil.getTopLevelDomain("");
	}
}
