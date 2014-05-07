package no.hioa.crawler.filmweb.parser;

import no.hioa.crawler.filmweb.ReviewContentParser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class NrkParser implements ReviewContentParser
{
	public boolean canParseDomain(String domain)
	{
		return "nrk.no".equalsIgnoreCase(domain);
	}

	public boolean shouldIgnore()
	{
		return false;
	}

	public String getContent(String content)
	{
		Document doc = Jsoup.parse(content);
		Elements elements = doc.select("div.brt");

		StringBuffer buffer = new StringBuffer();
		for (Element element : elements)
		{
			buffer.append(element.text());
		}

		return buffer.toString();
	}
}
