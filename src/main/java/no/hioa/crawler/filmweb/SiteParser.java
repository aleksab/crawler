package no.hioa.crawler.filmweb;

public interface SiteParser
{
	public boolean canParseDomain(String domain);
	
	public boolean shouldIgnore();
	
	public String getContent(String content);
}
