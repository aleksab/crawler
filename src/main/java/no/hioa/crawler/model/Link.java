package no.hioa.crawler.model;

import no.hioa.crawler.util.LinkUtil;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Model for a link.
 */
public class Link
{
	private String	link	= "";

	public Link(String link)
	{
		super();
		this.link = LinkUtil.normalizeLink(link, true);
	}

	public Link(String link, boolean removeDynamicPages)
	{
		super();
		this.link = LinkUtil.normalizeLink(link, removeDynamicPages);
	}

	public String getLink()
	{
		return link;
	}

	public void setLink(String link)
	{
		this.link = link;
	}

	@Override
	public boolean equals(Object obj)
	{
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public int hashCode()
	{
		return HashCodeBuilder.reflectionHashCode(this);
	}

	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
