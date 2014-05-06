package no.hioa.crawler.filmweb.parser;

import no.hioa.crawler.filmweb.SiteParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class CineParser implements SiteParser
{		
	public boolean canParseDomain(String domain)
	{
		return "cine.no".equalsIgnoreCase(domain);
	}
	
	public boolean shouldIgnore()
	{
		return false;
	}

	public String getContent(String content)
	{
		Document doc = Jsoup.parse(content);
		Element element = doc.select("div.articleText").first();
		if (element != null)
			return element.text();
		else
			return null;
	}
}
