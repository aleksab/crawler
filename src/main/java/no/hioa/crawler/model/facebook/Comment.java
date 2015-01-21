package no.hioa.crawler.model.facebook;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class Comment
{
	private String	id;
	private Entity	from;
	private String	message;
	private String	can_remove;
	private String	created_time;
	private String	like_count;
	private String	user_likes;

	public String getId()
	{
		return id;
	}

	public void setId(String id)
	{
		this.id = id;
	}

	public Entity getFrom()
	{
		return from;
	}

	public void setFrom(Entity from)
	{
		this.from = from;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getCan_remove()
	{
		return can_remove;
	}

	public void setCan_remove(String can_remove)
	{
		this.can_remove = can_remove;
	}

	public String getCreated_time()
	{
		return created_time;
	}

	public void setCreated_time(String created_time)
	{
		this.created_time = created_time;
	}

	public String getLike_count()
	{
		return like_count;
	}

	public void setLike_count(String like_count)
	{
		this.like_count = like_count;
	}

	public String getUser_likes()
	{
		return user_likes;
	}

	public void setUser_likes(String user_likes)
	{
		this.user_likes = user_likes;
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
