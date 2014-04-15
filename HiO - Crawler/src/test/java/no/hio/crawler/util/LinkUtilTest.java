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

	@Test
	public void testNormalizeDomain2()
	{
		Assert.assertEquals(null, LinkUtil.normalizeDomain("http://www.test.com/[Url]"));
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

	@Test
	public void testnormalizeDomainWithoutHttpBug()
	{
		Assert.assertEquals(null, LinkUtil.normalizeDomain("JavaScript:showSearchPanel();"));
	}

	@Test
	public void testnormalizeDomainWithoutHttp()
	{
		Assert.assertEquals(null, LinkUtil.normalizeDomain("//:test.com/"));
	}

	@Test
	public void testnormalizeDomainWithoutHttp2()
	{
		Assert.assertEquals(null, LinkUtil.normalizeDomain("//test.com/"));
	}

	@Test
	public void testnormalizeDomainWithoutHttp3()
	{
		Assert.assertEquals(null, LinkUtil.normalizeDomain("/test.com/"));
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

	@Test
	public void testnormalizeDomainInvalid3()
	{
		Assert.assertEquals(null, LinkUtil.normalizeDomain(""));
	}

	@Test
	public void testnormalizeDomainInvalid4()
	{
		Assert.assertEquals(null, LinkUtil.normalizeDomain(null));
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

	@Test
	public void testDetermineLinkLevelInvalid()
	{
		Assert.assertEquals(-1, LinkUtil.determineLinkLevel(""));
	}

	@Test
	public void testDetermineLinkLevelInvalid2()
	{
		Assert.assertEquals(-1, LinkUtil.determineLinkLevel(null));
	}

	@Test
	public void testnormalizeLink()
	{
		Assert.assertEquals("test.com/test/test", LinkUtil.normalizeLink("http://test.com/test/test/"));
	}

	@Test
	public void testnormalizeLink2()
	{
		Assert.assertEquals("test.com/test/test/?param=id", LinkUtil.normalizeLink("http://test.com/test/test/?param=id"));
	}

	@Test
	public void testnormalizeLink3()
	{
		Assert.assertEquals("test.com/test/test", LinkUtil.normalizeLink("http://test.com/test/test#test"));
	}

	@Test
	public void testnormalizeLink4()
	{
		Assert.assertEquals("test.com/test/test?test=test1&test2=test3",
				LinkUtil.normalizeLink("http://test.com/test/test?test=test1&test2=test3;jsessionid=123123"));
	}

	@Test
	public void testnormalizeLink5()
	{
		Assert.assertEquals("test.com/test/test", LinkUtil.normalizeLink("      http://test.com/test/test/          "));
	}

	@Test
	public void testnormalizeLink6()
	{
		Assert.assertEquals("test.com/test/test", LinkUtil.normalizeLink("HTTP://test.com/test/test/"));
	}

	@Test
	public void testnormalizeLink7()
	{
		Assert.assertEquals("test.com/test/test", LinkUtil.normalizeLink("HTTP://www.test.com/test/test/"));
	}

	@Test
	public void testnormalizeLink8()
	{
		Assert.assertEquals("test.com/test/test", LinkUtil.normalizeLink("www.test.com/test/test/"));
	}

	@Test
	public void testnormalizeLinkInvalid()
	{
		Assert.assertEquals(null, LinkUtil.normalizeLink(""));
	}

	@Test
	public void testnormalizeLinkInvalid2()
	{
		Assert.assertEquals(null, LinkUtil.normalizeLink(null));
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
		Assert.assertEquals(null, LinkUtil.getTopLevelDomain("test"));
	}

	@Test
	public void testGetTopLevelDomain5()
	{
		Assert.assertEquals(null, LinkUtil.getTopLevelDomain("www.test..com"));
	}

	@Test
	public void testGetTopLevelDomain6()
	{
		Assert.assertEquals(null, LinkUtil.getTopLevelDomain(null));
	}

	@Test
	public void testGetTopLevelDomain7()
	{
		Assert.assertEquals(null, LinkUtil.getTopLevelDomain(""));
	}
}
