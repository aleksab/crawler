package no.hioa.crawler.filmweb;

public class ReviewContent extends Review
{
	private String	content;
	private String	domain;

	public ReviewContent()
	{

	}

	public ReviewContent(String link, int rating, String name, String content)
	{
		super(link, rating, name);
		this.content = content;
	}

	public String getContent()
	{
		return content;
	}

	public void setContent(String content)
	{
		this.content = content;
	}

	public String getDomain()
	{
		return domain;
	}

	public void setDomain(String domain)
	{
		this.domain = domain;
	}
}