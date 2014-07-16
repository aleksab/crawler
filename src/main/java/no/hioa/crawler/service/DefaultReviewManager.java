package no.hioa.crawler.service;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;


import no.hioa.crawler.model.Review;
import no.hioa.crawler.model.ReviewHeaderXML;
import no.hioa.crawler.model.ReviewType;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultReviewManager
{
	private static final Logger	logger			= LoggerFactory.getLogger("fileLogger");

	private String				reviewFolder	= null;

	public DefaultReviewManager(String reviewFolder)
	{
		this.reviewFolder = reviewFolder;

		try
		{			
			FileUtils.forceMkdir(new File(reviewFolder));
		}
		catch (IOException ex)
		{
			logger.error("Could not create folder " + reviewFolder, ex);
		}
	}

	/**
	 * Generate a xml file of all the reviews in the review folder.
	 * 
	 * @param xmlOutputFile
	 */
	public void generateXml(String xmlOutputFile)
	{
		List<Review> reviews = new LinkedList<>();

		// get all reviews
		for (File file : new File(reviewFolder).listFiles())
		{
			Review review = getReview(file);

			if (review != null)
				reviews.add(review);
		}

		logger.info("Extracted {} reviews", reviews.size());
		ReviewHeaderXML movieHeader = new ReviewHeaderXML(reviews);

		try
		{
			JAXBContext context = JAXBContext.newInstance(Review.class, ReviewHeaderXML.class);
			Marshaller m = context.createMarshaller();
			m.setProperty(Marshaller.JAXB_ENCODING, "Unicode");
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			m.marshal(movieHeader, new FileOutputStream(xmlOutputFile));
		}
		catch (Exception ex)
		{
			logger.error("Unknown error", ex);
		}

		logger.info("Results saved to {}", xmlOutputFile);
	}

	/**
	 * Save a list of reviews.
	 * 
	 * @param reviews
	 */
	public void saveReviews(List<Review> reviews)
	{
		for (Review review : reviews)
		{
			saveReview(review);
		}
	}

	/**
	 * Save a review to a unique file.
	 * 
	 * @param review
	 */
	public void saveReview(Review review)
	{
		File newFile = new File(reviewFolder + "/" + System.currentTimeMillis() + ".review");
		logger.info("Saving review to file {}", newFile);
		try (PrintWriter writer = new PrintWriter(newFile, "ISO-8859-1"))
		{
			writer.write("URL: " + review.getLink() + "\n");
			writer.append("AUTHOR: " + review.getAuthor() + "\n");
			writer.append("DATE: " + review.getDate() + "\n");
			writer.append("RATING: " + review.getRating() + "\n");
			writer.append("TITLE: " + review.getTitle() + "\n");
			writer.append("TYPE: " + review.getType() + "\n");
			writer.append(review.getContent());
		}
		catch (IOException ex)
		{
			logger.error("Could not save review " + review + " to file " + newFile, ex);
		}
	}

	/**
	 * Extract data from a stored review file.
	 * 
	 * @param file
	 * @return
	 */
	public Review getReview(File file)
	{
		String link = null;
		String author = null;
		String date = null;
		int rating = 0;
		String title = null;
		ReviewType type = null;
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
			
			if (scanner.hasNextLine())
			{
				type = ReviewType.getEnum(StringUtils.substringAfter(scanner.nextLine(), "TYPE: ").trim());
			}

			while (scanner.hasNextLine())
			{
				String input = scanner.nextLine();
				input = Jsoup.parse(input).text();
				// input = HtmlEscape.unescapeHtml(input);
				buffer.append(input);
			}
		}
		catch (Exception ex)
		{
			logger.error("Could not read content for file " + file.getAbsolutePath(), ex);
		}

		return new Review(link, rating, title, buffer.toString(), author, date, type);
	}
}
