package no.hioa.crawler.filmweb;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

public class Movie
{
	private String				link;
	private String				title;
	private String				originalTitle;
	private List<ReviewContent>	reviews;

	public Movie()
	{

	}

	public Movie(String link, String title, String originalTitle, List<ReviewContent> reviews)
	{
		super();
		this.link = link;
		this.title = title;
		this.originalTitle = originalTitle;
		this.reviews = reviews;
	}

	public String getLink()
	{
		return link;
	}

	public void setLink(String link)
	{
		this.link = link;
	}

	public String getTitle()
	{
		return title;
	}

	public void setTitle(String title)
	{
		this.title = title;
	}

	public String getOriginalTitle()
	{
		return originalTitle;
	}

	public void setOriginalTitle(String originalTitle)
	{
		this.originalTitle = originalTitle;
	}

	@XmlElement(name = "review")
	@XmlElementWrapper(name = "reviews")
	public List<ReviewContent> getReviews()
	{
		return reviews;
	}

	public void setReviews(List<ReviewContent> reviews)
	{
		this.reviews = reviews;
	}
}
