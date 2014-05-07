package no.hioa.crawler.filmweb;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AggregateContent extends ParallellReviewCrawler
{
	private static final Logger			logger			= LoggerFactory.getLogger("fileLogger");
	private static final Logger			consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	private List<ReviewContentParser>	contentParsers;

	public static void main(String[] args)
	{
		PropertyConfigurator.configure("log4j.properties");

		new AggregateContent().generateXml(new File("C:/Development/workspace juno/Hioa - Crawler/target/output/"), new File(
				"C:/Development/workspace juno/Hioa - Crawler/target/output_reviews/"), new File("result.xml"));
	}

	public AggregateContent()
	{
		super(null, null);
		contentParsers = new LinkedList<>();
		contentParsers.add(new DagbladetParser());
		contentParsers.add(new Side3Parser());
		contentParsers.add(new VgParser());
		contentParsers.add(new CineParser());
		contentParsers.add(new FilmMagasinetParser());
		contentParsers.add(new BtParser());
		contentParsers.add(new IgnoreParser());
		contentParsers.add(new OsloByParser());
		contentParsers.add(new Tv2Parser());
		contentParsers.add(new AdressaParser());
		contentParsers.add(new NordlysParser());
		contentParsers.add(new NrkParser());
	}

	/**
	 * Generates a xml file with external reviews for each movie (from filmweb).
	 * 
	 * @param filmwebDir
	 * @param reviewDir
	 */
	public void generateXml(File filmwebDir, File reviewDir, File xmlFile)
	{
		List<Movie> movies = new LinkedList<>();

		// get all reviews with external links
		for (File file : filmwebDir.listFiles())
		{
			FileContent content = getFileContent(file);
			Set<Review> externalReviews = getExternalReviews(content);
			List<ReviewContent> reviews = new LinkedList<>();

			// merge with fetch external results
			for (Review review : externalReviews)
			{
				ReviewContent reviewContent = findReviewContent(reviewDir, file.getName(), review, externalReviews.size());
				String domain = LinkUtil.normalizeDomain(review.getLink());

				if (reviewContent != null)
				{
					// see if we can extract the content with one of our parsers
					for (ReviewContentParser siteParser : contentParsers)
					{
						if (siteParser.canParseDomain(domain))
						{
							String extractedContent = siteParser.getContent(reviewContent.getContent());

							// treat reviews less than 10 letters as empty
							if (extractedContent == null || extractedContent.trim().length() < 10)
								extractedContent = null;

							if (extractedContent != null)
							{
								reviewContent.setDomain(domain);
								reviewContent.setContent(extractedContent);
								reviews.add(reviewContent);
								break;
							}
						}
					}
				}
			}

			// we only want to keep movies with at least one external reviews
			if (!reviews.isEmpty())
			{
				String title = getTitle(file);
				String originalTitle = getOriginalTitle(file);
				movies.add(new Movie(content.link, title, originalTitle, reviews));
			}
		}

		logger.info("Extracted content for {} movies", movies.size());
		consoleLogger.info("Extracted content for {} movies", movies.size());
		MovieHeader movieHeader = new MovieHeader(movies);

		try
		{
			JAXBContext context = JAXBContext.newInstance(MovieHeader.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(movieHeader, new FileOutputStream(xmlFile));
		}
		catch (Exception ex)
		{
			logger.error("Unknown error", ex);
			consoleLogger.error("Unknown error", ex);
		}
	}

	/**
	 * Print stats about which external content we are able to parse content from. The output will be grouped by external domain.
	 * 
	 * @param filmwebDir
	 * @param reviewDir
	 */
	public void printParseStats(File filmwebDir, File reviewDir)
	{
		Map<String, Integer> hasExternalContent = new HashMap<>();
		Map<String, Integer> noExternalContent = new HashMap<>();

		// TODO: This can be improved by also merging with the movies from filmweb in order to figure out which links we were not able to download.

		// get all reviews with external links
		for (File file : filmwebDir.listFiles())
		{
			FileContent content = getFileContent(file);
			Set<Review> externalReviews = getExternalReviews(content);

			// merge with fetch external results
			for (Review review : externalReviews)
			{
				ReviewContent reviewContent = findReviewContent(reviewDir, file.getName(), review, externalReviews.size());
				String domain = LinkUtil.normalizeDomain(review.getLink());

				if (reviewContent == null)
				{
					if (noExternalContent.containsKey(domain))
						noExternalContent.put(domain, noExternalContent.get(domain) + 1);
					else
						noExternalContent.put(domain, 1);
				}
				else
				{
					if (hasExternalContent.containsKey(domain))
						hasExternalContent.put(domain, hasExternalContent.get(domain) + 1);
					else
						hasExternalContent.put(domain, 1);
				}
			}
		}

		for (String name : hasExternalContent.keySet())
		{
			consoleLogger.info("Link {} has fetched {} external contents", name, hasExternalContent.get(name));
		}

		consoleLogger.info("-------------------------------------");

		for (String name : noExternalContent.keySet())
		{
			consoleLogger.info("Link {} has NOT fetched {} external contents", name, noExternalContent.get(name));
		}
	}

	/**
	 * Prints stats about external reviews fetched, like number of reviews, total letters etc.
	 * 
	 * @param inputDir
	 */
	public void printReviewStats(File reviewDir)
	{
		int counter = 0;
		long letters = 0;
		long words = 0;
		HashMap<String, String> names = new HashMap<>();

		for (File file : reviewDir.listFiles())
		{
			ReviewContent content = extractReviewContent(file);
			String domain = LinkUtil.normalizeDomain(content.getLink());

			boolean found = false;
			for (ReviewContentParser siteParser : contentParsers)
			{
				if (siteParser.canParseDomain(domain))
				{
					String extractedContent = siteParser.getContent(content.getContent());

					if (extractedContent == null || extractedContent.trim().length() < 10)
						extractedContent = null;
					else
					{
						counter++;
						letters += extractedContent.length();
						words += extractedContent.split(" ").length;

						if (extractedContent.length() > 10000)
						{
							consoleLogger.info("Length {} - {}", extractedContent.length(), content.getLink());
							consoleLogger.info(extractedContent);
						}

					}

					content.setContent(extractedContent);
					found = true;
					break;
				}
			}

			if (!found)
			{
				consoleLogger.warn("Could not find parser for domain {} ({}) on file {}", domain, content.getLink(), file.getAbsolutePath());
				return;
			}

			String link = LinkUtil.normalizeDomain(content.getLink());

			if (!names.containsKey(link))
				names.put(link, content.getLink());
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

	/**
	 * Find the review content based on a filmweb movie. Since reviews are not stored in an orderly fashion we need to check all potential reviews for
	 * a movie.
	 * 
	 * @param reviewDir
	 * @param name
	 * @param review
	 * @param maxReviews
	 * @return
	 */
	ReviewContent findReviewContent(File reviewDir, String name, Review review, int maxReviews)
	{
		String prefix = reviewDir.getAbsolutePath() + File.separator + StringUtils.substringBefore(name, ".page") + "_";

		for (int i = 1; i <= maxReviews; i++)
		{
			String reviewName = prefix + i + ".page";
			File reviewFile = new File(reviewName);

			if (reviewFile.exists())
			{
				ReviewContent content = extractReviewContent(reviewFile);
				if (content.getLink().equalsIgnoreCase(review.getLink()))
					return content;
			}
		}

		return null;
	}

	/**
	 * Extract data from a stored review page (from the crawler).
	 * 
	 * @param file
	 * @return
	 */
	ReviewContent extractReviewContent(File file)
	{
		String link = null;
		int rating = 0;
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
				rating = Integer.valueOf(StringUtils.substringAfter(scanner.nextLine(), "RATING: ").trim());
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

		return new ReviewContent(link, rating, name, buffer.toString());
	}

	/**
	 * Extract title from a filmweb movie page.
	 * 
	 * @param file
	 * @return
	 */
	String getTitle(File file)
	{
		try
		{
			Document doc = Jsoup.parse(file, "UTF-8");
			Element element = doc.select("div.info > h1").first();
			if (element != null)
				return element.text();
		}
		catch (Exception ex)
		{
			logger.error("Unknown error", ex);
		}

		return "Unknown";
	}

	/**
	 * Extract original title from a filmweb movie page.
	 * 
	 * @param file
	 * @return
	 */
	String getOriginalTitle(File file)
	{
		try
		{
			Document doc = Jsoup.parse(file, "UTF-8");
			Element element = doc.select("div.orginalTitle").first();
			if (element != null)
			{
				return StringUtils.substringAfter(element.text(), "Originaltittel: ");
			}
		}
		catch (Exception ex)
		{
			logger.error("Unknown error", ex);
		}

		return "Unknown";
	}
}
