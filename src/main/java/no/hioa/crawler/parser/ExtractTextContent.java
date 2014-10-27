package no.hioa.crawler.parser;

import java.io.File;
import java.util.Iterator;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtractTextContent
{
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		ExtractTextContent extractor = new ExtractTextContent();

		String content = extractor.extractTextContent(new File(
				"C:/Users/Alex/Desktop/blogs/blogs~/target/0sloasthattumblrcom/0sloasthat_1412332351502"));
		consoleLogger.info(content);
	}

	public String extractTextContent(File htmlFile)
	{
		try
		{
			String html = FileUtils.readFileToString(htmlFile, "UTF-8");
			Document doc = Jsoup.parse(html);
			Elements elements = doc.select("p");

			StringBuffer buffer = new StringBuffer();
			Iterator<Element> it = elements.listIterator();
			while (it.hasNext())
			{
				Element element = it.next();
				String content = element.text();

				StringTokenizer words = new StringTokenizer(content);
				if (words.countTokens() < 5)
				{

				}
				else if (words.countTokens() > 5 && words.countTokens() < 15)
				{
					boolean found = false;
					while (words.hasMoreTokens())
					{
						if (StandardAnalyzer.STOP_WORDS_SET.contains(words.nextToken()))
						{
							found = true;
							break;
						}
					}

					if (found)
						buffer.append("\n" + content);
				}
				else
				{
					buffer.append("\n" + content);
				}
			}

			return buffer.toString();
		}
		catch (Exception ex)
		{
			consoleLogger.error("Unknown error", ex);
			return null;
		}
	}
}
