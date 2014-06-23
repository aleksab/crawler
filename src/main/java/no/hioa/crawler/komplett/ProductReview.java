package no.hioa.crawler.komplett;

import no.hioa.crawler.filmweb.Review;

public class ProductReview extends Review
{
	private String	content;
	private String	author;
	private String	date;

	public ProductReview()
	{

	}

	public ProductReview(String link, int rating, String name, String content, String author, String date)
	{
		super(link, rating, name);
		this.content = content;
		this.author = author;
		this.date = date;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public String getAuthor()
	{
		return author;
	}

	public void setAuthor(String author)
	{
		this.author = author;
	}

	public String getDate()
	{
		return date;
	}

	public void setDate(String date)
	{
		this.date = date;
	}
}