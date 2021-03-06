package no.hioa.crawler.filmweb.parser;

import no.hioa.crawler.filmweb.ExternalContentParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class FilmMagasinetParser implements ExternalContentParser
{	
	public boolean canParseDomain(String domain)
	{
		return "filmmagasinet.no".equalsIgnoreCase(domain);
	}

	public boolean shouldIgnore()
	{
		return false;
	}
	
	public String getContent(String content)
	{
		Document doc = Jsoup.parse(content);
		Element element = doc.select("div#primaryContentBox").first();
		if (element != null)
			return element.text();
		else
			return null;
	}
}
