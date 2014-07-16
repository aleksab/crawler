package no.hioa.crawler.product;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AggregateContent
{
	private static final Logger logger = LoggerFactory.getLogger("fileLogger");
	private static final Logger consoleLogger = LoggerFactory.getLogger("stdoutLogger");

	public static void main(String[] args)
	{
		PropertyConfigurator.configure("log4j.properties");

		//new AggregateContent().generateXml(new File("C:/Users/Aleksander/Downloads/mpx/mpx"), new File("target/mpx-reviews.xml"));
		new AggregateContent().generateXml(new File("C:/Users/Aleksander/Downloads/komplett/komplett"), new File("target/komplett-reviews.xml"));
	}

	/**
	 * Generates a xml file with reviews for all product reviews.
	 * 
	 * @param reviewDir
	 * @param xmlFile
	 */
	public void generateXml(File reviewDir, File xmlFile)
	{
		List<ProductReview> reviews = new LinkedList<>();

		// get all reviews
		for (File file : reviewDir.listFiles())
		{
			ProductReview review = getProductReview(file);

			if (review != null)
				reviews.add(review);
		}

		logger.info("Extracted {} reviews", reviews.size());
		consoleLogger.info("Extracted {} reviews", reviews.size());
		ReviewHeader movieHeader = new ReviewHeader(reviews);

		try
		{
			JAXBContext context = JAXBContext.newInstance(ReviewHeader.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_ENCODING, "Unicode");
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(movieHeader, new FileOutputStream(xmlFile));
		} catch (Exception ex)
		{
			logger.error("Unknown error", ex);
			consoleLogger.error("Unknown error", ex);
		}
	}

	/**
	 * Extract data from a stored review page (from the crawler).
	 * 
	 * @param file
	 * @return
	 */
	ProductReview getProductReview(File file)
	{
		String link = null;
		String author = null;
		String date = null;
		int rating = 0;
		String title = null;
		StringBuffer buffer = new StringBuffer();

		try (Scanner scanner = new Scanner(new FileInputStream(file), "ISO-8859-1"))
		{
			if (scanner.hasNextLine())
			{
				link = StringUtils.substringAfter(scanner.nextLine(), "URL: ").trim();
			}

			if (scanner.hasNextLine())
			{
				author = StringUtils.substringAfter(scanner.nextLine(), "AUTHOR: ").trim();
			}

			if (scanner.hasNextLine())
			{
				date = StringUtils.substringAfter(scanner.nextLine(), "DATE: ").trim();
			}

			if (scanner.hasNextLine())
			{
				rating = Integer.valueOf(StringUtils.substringAfter(scanner.nextLine(), "RATING: ").trim());
			}

			if (scanner.hasNextLine())
			{
				title = StringUtils.substringAfter(scanner.nextLine(), "TITLE: ").trim();
			}

			while (scanner.hasNextLine())
			{
				String input = scanner.nextLine();				
				input = Jsoup.parse(input).text();
				//input = HtmlEscape.unescapeHtml(input);				
				buffer.append(input);
			}
		} catch (Exception ex)
		{
			consoleLogger.error("Could not read content for file " + file.getAbsolutePath(), ex);
		}

		return new ProductReview(link, rating, title, buffer.toString(), author, date);
	}
}
