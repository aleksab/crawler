package no.hioa.crawler.model;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * Model for a page with content and links.
 */
public class Page
{
	private Link			url		= null;
	private StringBuffer	content	= null;

	public Page(Link url, StringBuffer content)
	{
		super();
		this.url = url;
		this.content = content;
	}

	public Link getUrl()
	{
		return url;
	}

	public void setUrl(Link url)
	{
		this.url = url;
	}

	public StringBuffer getContent()
	{
		return content;
	}

	public void setContent(StringBuffer content)
	{
		this.content = content;
	}

	@Override
	public boolean equals(Object obj)
	{
		return EqualsBuilder.reflectionEquals(this, obj);
	}

	@Override
	public String toString()
	{
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
