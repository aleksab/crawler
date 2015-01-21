package no.hioa.crawler.model.facebook;

import java.util.List;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class Post
{
	private String		id;
	private Entity		from;
	private ToList		to;
	private String		message;
	private String		picture;
	private String		link;
	private String		name;
	private String		caption;
	private String		description;
	private String		icon;
	private List<Link>	actions;
	private Privacy		privacy;
	private String		type;
	private String		created_time;
	private String		updated_time;
	private LikeList	likes;
	private CommentList	comments;

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

	public ToList getTo()
	{
		return to;
	}

	public void setTo(ToList to)
	{
		this.to = to;
	}

	public String getMessage()
	{
		return message;
	}

	public void setMessage(String message)
	{
		this.message = message;
	}

	public String getPicture()
	{
		return picture;
	}

	public void setPicture(String picture)
	{
		this.picture = picture;
	}

	public String getLink()
	{
		return link;
	}

	public void setLink(String link)
	{
		this.link = link;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getCaption()
	{
		return caption;
	}

	public void setCaption(String caption)
	{
		this.caption = caption;
	}

	public String getDescription()
	{
		return description;
	}

	public void setDescription(String description)
	{
		this.description = description;
	}

	public String getIcon()
	{
		return icon;
	}

	public void setIcon(String icon)
	{
		this.icon = icon;
	}

	public List<Link> getActions()
	{
		return actions;
	}

	public void setActions(List<Link> actions)
	{
		this.actions = actions;
	}

	public Privacy getPrivacy()
	{
		return privacy;
	}

	public void setPrivacy(Privacy privacy)
	{
		this.privacy = privacy;
	}

	public String getType()
	{
		return type;
	}

	public void setType(String type)
	{
		this.type = type;
	}

	public String getCreated_time()
	{
		return created_time;
	}

	public void setCreated_time(String created_time)
	{
		this.created_time = created_time;
	}

	public String getUpdated_time()
	{
		return updated_time;
	}

	public void setUpdated_time(String updated_time)
	{
		this.updated_time = updated_time;
	}

	public LikeList getLikes()
	{
		return likes;
	}

	public void setLikes(LikeList likes)
	{
		this.likes = likes;
	}

	public CommentList getComments()
	{
		return comments;
	}

	public void setComments(CommentList comments)
	{
		this.comments = comments;
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
