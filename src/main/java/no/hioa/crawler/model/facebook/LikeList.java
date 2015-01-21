package no.hioa.crawler.model.facebook;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class LikeList
{
	private List<Entity>	data;
	private LikePaging		paging;

	public List<Entity> getData()
	{
		return data;
	}

	public void setData(List<Entity> data)
	{
		this.data = data;
	}

	public LikePaging getPaging()
	{
		return paging;
	}

	public void setPaging(LikePaging paging)
	{
		this.paging = paging;
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
