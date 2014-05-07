package no.hioa.crawler.filmweb.parser;

import no.hioa.crawler.filmweb.ReviewContentParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class VgParser implements ReviewContentParser
{		
	public boolean canParseDomain(String domain)
	{
		return "vg.no".equalsIgnoreCase(domain) || "www1.vg.no".equalsIgnoreCase(domain);
	}

	public boolean shouldIgnore()
	{
		return false;
	}
	
	public String getContent(String content)
	{
		Document doc = Jsoup.parse(content);
		Elements elements = doc.select("div#article-body-text > p");
		
		StringBuffer buffer = new StringBuffer();
		for (int i = 0; i < (elements.size() - 1); i++)
		{
			Element element = elements.get(i);
			buffer.append(element.text());
		}
		
		return buffer.toString();
	}
}
