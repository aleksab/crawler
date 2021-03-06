package no.hioa.crawler.util;

import java.net.URI;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Link utility methods. All links in the crawler should be without http:// and without www. All links should also not end with /
 */
public class LinkUtil
{
	private static final Logger	logger	= LoggerFactory.getLogger("fileLogger");

	/**
	 * Determine which "level" a link has. This assumes that each sub level will add a / to it's path. So test.com/level1 is on level 1 while
	 * test.com/level1/level2 is on level 2.
	 * 
	 * @param link
	 * @return
	 */
	public static int determineLinkLevel(String link)
	{
		link = normalizeLink(link, true);

		if (link == null || link.length() == 0)
			return -1;

		return StringUtils.countMatches(link, "/");
	}

	/**
	 * Returns the top level domain (.no, .com, .org) for a domain.
	 * 
	 * @param link
	 * @return
	 */
	public static String getTopLevelDomain(String link)
	{
		link = normalizeDomain(link);

		if (link == null || link.length() == 0)
			return null;

		link = StringUtils.substringAfterLast(link, ".");

		if (link.length() == 0)
			return null;
		else
			return link;
	}

	/**
	 * Will return the normalized domain name of a link.
	 * 
	 * @param link
	 * @return
	 */
	public static String normalizeDomain(String link)
	{
		try
		{
			link = normalizeLink(link, true);

			if (link == null || link.length() == 0)
				return null;

			if (link.startsWith("http://"))
				link = StringUtils.substringAfter(link, "http://");

			if (link.startsWith("https://"))
				link = StringUtils.substringAfter(link, "https://");

			// we need http in front for the method to work
			URI uri = new URI("http://" + link);
			link = uri.getHost();

			return normalizeLink(link, true);
		}
		catch (Exception ex)
		{
			logger.error("Illegal link: {}", link);
			throw new IllegalArgumentException("Illegal link");
		}
	}

	/**
	 * Will normalize a link and remove spaces, sessions and anchors. Will also make sure that the link starts with http://
	 * 
	 * @param link
	 * @return
	 */
	public static String normalizeLink(String link, boolean removeDynamicPages)
	{
		if (link == null || link.length() == 0)
			throw new IllegalArgumentException("Empty link");

		link = link.trim();
		link = StringUtils.substringBefore(link, " ");

		// Needed?
		link = link.replaceAll("&#38;", "&");
		link = link.replaceAll("&amp;", "&");

		// Remove page anchor links - still the same page
		link = StringUtils.substringBeforeLast(link, "#");

		// Remove sessionids - still the same page
		link = StringUtils.substringBeforeLast(link, ";jsessionid");

		// Remove @ as part of email domains or username authentication
		if (StringUtils.contains(link, "@"))
			link = StringUtils.substringAfterLast(link, "@");

		// Remove dynamic parameters (everything after ?)
		if (removeDynamicPages)
			link = StringUtils.substringBefore(link, "?");

		// remove http://
		if (StringUtils.startsWithIgnoreCase(link, "http://"))
			link = StringUtils.removeStartIgnoreCase(link, "http://");

		// remove https://
		if (StringUtils.startsWithIgnoreCase(link, "https://"))
			link = StringUtils.removeStartIgnoreCase(link, "https://");

		// remove www.
		if (StringUtils.startsWithIgnoreCase(link, "www."))
			link = StringUtils.removeStartIgnoreCase(link, "www.");

		// remove last /
		if (link.endsWith("/"))
			link = StringUtils.substringBeforeLast(link, "/");

		return link;
	}
}
