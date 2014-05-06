package no.hioa.crawler.filmweb;

import java.io.File;
import java.io.FileInputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import no.hioa.crawler.filmweb.ParallellReviewCrawler.Review;
import no.hioa.crawler.filmweb.parser.AdressaParser;
import no.hioa.crawler.filmweb.parser.BtParser;
import no.hioa.crawler.filmweb.parser.CineParser;
import no.hioa.crawler.filmweb.parser.DagbladetParser;
import no.hioa.crawler.filmweb.parser.FilmMagasinetParser;
import no.hioa.crawler.filmweb.parser.IgnoreParser;
import no.hioa.crawler.filmweb.parser.NordlysParser;
import no.hioa.crawler.filmweb.parser.NrkParser;
import no.hioa.crawler.filmweb.parser.OsloByParser;
import no.hioa.crawler.filmweb.parser.Side3Parser;
import no.hioa.crawler.filmweb.parser.Tv2Parser;
import no.hioa.crawler.filmweb.parser.VgParser;
import no.hioa.crawler.util.LinkUtil;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AggregateContent
{
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	public static void main(String[] args)
	{
		PropertyConfigurator.configure("log4j.properties");
		// new AggregateContent().generateXml();

		new AggregateContent().getExternalReviewsContent(new File("C:/Development/workspace juno/Hioa - Crawler/target/output_reviews"));
		//new AggregateContent().getSiteContent(new File("C:/Development/workspace juno/Hioa - Crawler/target/output_reviews/1397642981353_3.page"));
	}

	private List<SiteParser>	siteParsers;

	public AggregateContent()
	{
		siteParsers = new LinkedList<>();
		siteParsers.add(new DagbladetParser());
		siteParsers.add(new Side3Parser());
		siteParsers.add(new VgParser());
		siteParsers.add(new CineParser());
		siteParsers.add(new FilmMagasinetParser());
		siteParsers.add(new BtParser());
		siteParsers.add(new IgnoreParser());
		siteParsers.add(new OsloByParser());
		siteParsers.add(new Tv2Parser());
		siteParsers.add(new AdressaParser());
		siteParsers.add(new NordlysParser());
		siteParsers.add(new NrkParser());
	}

	public void getSiteContent(File file)
	{
		FileContent content = getFileContent(file);
		SiteParser parser = new NrkParser();

		consoleLogger.info(parser.getContent(content.content));
	}

	public void generateXml()
	{
		ParallellReviewCrawler crawler = new ParallellReviewCrawler(null, null);
		Map<File, Set<Review>> externalReviews = crawler.getExternalReviews(new File("C:/Development/workspace juno/Hioa - Crawler/target/output"));

		// check which reviews we do have
	}

	public void getExternalReviewsContent(File inputDir)
	{
		int counter=0;
		long letters=0;
		long words=0;
		HashMap<String, String> names = new HashMap<>();
		for (File file : inputDir.listFiles())
		{
			FileContent content = getFileContent(file);

			String domain = LinkUtil.normalizeDomain(content.link);

			boolean found = false;
			for (SiteParser siteParser : siteParsers)
			{
				if (siteParser.canParseDomain(domain))
				{
					String extractedContent = siteParser.getContent(content.content);
					
					if (extractedContent == null || extractedContent.trim().length() < 10)
						extractedContent = null;
					else
					{
						counter++;
						letters += extractedContent.length();
						words += extractedContent.split(" ").length;
					}
					
					content.content = extractedContent;
					found = true;
					break;
				}
			}

			if (!found)
			{
				consoleLogger.warn("Could not find parser for domain {} ({}) on file {}", domain, content.link, file.getAbsolutePath());
				return;
			}

			String link = LinkUtil.normalizeDomain(content.link);

			if (!names.containsKey(link))
				names.put(link, content.link);								
		}

		consoleLogger.info("Unique: " + names.size());
		for (String name : names.keySet())
		{
			consoleLogger.info("Name: " + name + " - " + names.get(name));
		}
		
		consoleLogger.info("Total reviews: {}", counter);
		consoleLogger.info("Total letters: {}", letters);
		consoleLogger.info("Total words: {}", words);
	}

	public void getExternalReviewsContent2(File inputDir)
	{
		HashMap<String, Integer> names = new HashMap<>();
		for (File file : inputDir.listFiles())
		{
			FileContent content = getFileContent(file);

			String link = LinkUtil.normalizeDomain(content.link);

			if (!names.containsKey(link))
				names.put(link, 1);
			else
				names.put(link, names.get(link) + 1);
		}

		consoleLogger.info("Unique: " + names.size());
		for (String name : names.keySet())
		{
			consoleLogger.info("Url: " + name + " - Occurence: " + names.get(name));
		}
	}

	FileContent getFileContent(File file)
	{
		String link = null;
		String rating = null;
		String name = null;
		StringBuffer buffer = new StringBuffer();

		try (Scanner scanner = new Scanner(new FileInputStream(file), "UTF-8"))
		{
			if (scanner.hasNextLine())
			{
				link = StringUtils.substringAfter(scanner.nextLine(), "URL: ").trim();
			}

			if (scanner.hasNextLine())
			{
				rating = StringUtils.substringAfter(scanner.nextLine(), "RATING: ").trim();
			}

			if (scanner.hasNextLine())
			{
				name = StringUtils.substringAfter(scanner.nextLine(), "NAME: ").trim();
			}

			while (scanner.hasNextLine())
			{
				String input = scanner.nextLine();
				buffer.append(input);
			}
		}
		catch (Exception ex)
		{
			consoleLogger.error("Could not read content for file " + file.getAbsolutePath(), ex);
		}

		return new FileContent(link, rating, name, buffer.toString());
	}

	private class FileContent
	{
		public String	link;
		public String	rating;
		public String	name;
		public String	content;

		public FileContent(String link, String rating, String name, String content)
		{
			super();
			this.link = link;
			this.rating = rating;
			this.name = name;
			this.content = content;
		}
	}
}
