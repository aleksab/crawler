package no.hioa.crawler.filmweb.parser;

import no.hioa.crawler.filmweb.ExternalContentParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class AdressaParser implements ExternalContentParser
{	
	public boolean canParseDomain(String domain)
	{
		return "adressa.no".equalsIgnoreCase(domain);
	}
	
	public boolean shouldIgnore()
	{
		return false;
	}

	public String getContent(String content)
	{
		Document doc = Jsoup.parse(content);
		Elements elements = doc.select("div[class*=polarisStoryContent]");

		StringBuffer buffer = new StringBuffer();
		for (Element element : elements)
		{
			buffer.append(element.text() + " ");
		}

		return buffer.toString();
	}
}
