package no.hioa.crawler.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import no.hioa.crawler.model.Link;
import no.hioa.crawler.util.LinkUtil;
import no.hioa.crawler.util.RegexUtil;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtractTextContent
{
	private static final Logger	logger		= LoggerFactory.getLogger(ExtractTextContent.class);

	private List<String>		stopWords	= null;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		ExtractTextContent extractor = new ExtractTextContent();

		// extractor.extractFolders(new File("E:/Data/blogs2/crawl/"), new
		// File("E:/Data/blogs2/text/"));

		// extractor.extractFolderContent(new
		// File("E:/Data/blogs2/crawl/honestthinkingorgno/"), new
		// File("E:/Data/blogs2/text/honestthinkingorgno/"));

		// System.out.println(extractor.extractDate(new
		// File("E:/Data/blogs2/crawl/4freedomsningcom/1428482190019.html")));

		System.out.println(extractor.extractLinks(new Link("4freedoms.com"), new File("E:/Data/blogs2/crawl/4freedomsningcom/1428482196826.html")));

	}

	public ExtractTextContent()
	{
		stopWords = getStopWords();
	}

	public void extractFolders(File folderPath, File outputPath)
	{
		for (File folder : folderPath.listFiles())
		{
			if (folder.isDirectory())
			{
				logger.info("Extracting text from folder {}", folder.getName());

				try
				{
					File outputFolder = new File(outputPath + "/" + folder.getName());
					FileUtils.forceMkdir(outputFolder);
					extractFolderContent(folder, outputFolder);
				}
				catch (IOException ex)
				{
					logger.error("Could not save content for folder", ex);
				}
			}
		}
	}

	public boolean extractFolderContent(File folder, File outputFolder)
	{
		HashMap<String, LocalDate> dates = new HashMap<>();

		for (File file : folder.listFiles())
		{
			if (file.isFile())
			{
				String content = extractTextContent(file);
				if (content != null && !StringUtils.isEmpty(content))
				{
					saveResult(new File(outputFolder + "/" + file.getName() + ".txt"), content);
					dates.put(file.getName(), extractDate(file));
				}
			}
		}

		try
		{
			String buffer = "";
			for (String key : dates.keySet())
			{
				if (dates.get(key) == null)
					buffer += key + ":unknown,";
				else
					buffer += key + ":" + dates.get(key) + ",";
			}

			buffer = StringUtils.substringBeforeLast(buffer, ",");

			FileUtils.writeStringToFile(new File(outputFolder + "/dates.txt"), buffer.toString());
		}
		catch (Exception ex)
		{
			logger.error("Unknown error", ex);
		}

		return true;
	}

	public boolean shouldUseLink(Link domain, String url)
	{
		try
		{
			if (StringUtils.isEmpty(url))
				return false;

			url = LinkUtil.normalizeDomain(url);
			if (domain.getLink().equalsIgnoreCase(url) || "/".equalsIgnoreCase(url))
			{
				logger.debug("Ignoring link since internal: " + url);
				return false;
			}

			return true;
		}
		catch (Exception ex)
		{
			return false;
		}
	}

	public HashMap<LocalDate, String> extractLinks(Link domain, File htmlFile)
	{
		try
		{
			HashMap<LocalDate, String> links = new HashMap<>();

			String html = FileUtils.readFileToString(htmlFile, "UTF-8");
			Document doc = Jsoup.parse(html);
			Elements elements = doc.select("p");

			Iterator<Element> it = elements.listIterator();
			while (it.hasNext())
			{
				Element element = it.next();

				Elements el = element.select("a[href]");
				for (Element e : el)
				{
					String url = e.attr("href");

					if (!shouldUseLink(domain, url))
						continue;

					// logger.info("Trying to get date for link {}", url);

					// consoleLogger.info(e.html());
					// find tag in source (can be multiple)
					// extract 500 chars before and after, regex for dates
					int index = StringUtils.indexOf(html, e.html());
					if (index != -1)
					{
						int start = index - 1000;
						if (start < 0)
							start = 0;

						String source = StringUtils.substring(html, start, index);
						source = StringUtils.replace(source, url, "");						
						
						if (url.contains("shariaunveiled"))
						{
							logger.info(source);
							LocalDate date = getDate(source);

							if (date != null)
								logger.info("Found date: " + date);
							else
								logger.info("Found no date");
						}
					}
				}
			}

			return links;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
			return null;
		}
	}
	
	private LocalDate getDate(String input)
	{
		try
		{
			input = input.replaceAll("\n", " ");
			input = input.replaceAll("\"", "");

			String year = findYear(input);
			String month = findMonth(input);

			if (year != null && month != null)
			{
				String date = year + "-" + month;
				return LocalDate.parse(date, DateTimeFormat.forPattern("yyyy-MM"));
			}
			else if (year != null)
				return LocalDate.parse(year, DateTimeFormat.forPattern("yyyy"));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		return null;
	}
	
	private String findYear(String input)
	{
		String match = RegexUtil.matchRegex(".*(20\\d\\d).*", input);

		if (match != null)
			return match;

		return null;
	}
	
	private String findMonth(String input)
	{
		String match = RegexUtil.matchRegex(".*(\\d\\d).*", input);

		if (match != null)
			return match;

		return null;
	}

	@SuppressWarnings("unchecked")
	public LocalDate extractDate(File htmlFile)
	{
		try
		{
			List<String> lines = FileUtils.readLines(htmlFile);

			String urlLine = lines.get(0);

			if (!StringUtils.contains(urlLine, "URL:"))
			{
				logger.error("Could not find URL in file: {}", htmlFile);
				return null;
			}
			else
			{
				String url = StringUtils.substringAfter(urlLine, "URL: ");
				String year = getYear(url);
				String month = getMonth(url);

				if (year != null && month != null)
				{
					String date = year + "-" + month;
					return LocalDate.parse(date, DateTimeFormat.forPattern("yyyy-MM"));
				}
				else if (year != null)
					return LocalDate.parse(year, DateTimeFormat.forPattern("yyyy"));
				else
				{
					for (String line : lines)
					{
						if (StringUtils.contains(line, "article:published_time"))
						{
							String date = StringUtils.substringAfter(line, "content");
							date = StringUtils.substringBetween(date, "\"", "\"");

							return LocalDate.parse(year, DateTimeFormat.forPattern("yyyy-MM-dd'T'HH:mm:ssZ"));
						}
					}
				}
			}

			return null;
		}
		catch (Exception ex)
		{
			return null;
		}
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
					// ignore
				}
				else if (words.countTokens() > 5 && words.countTokens() < 15)
				{
					boolean found = false;
					while (words.hasMoreTokens())
					{
						if (stopWords.contains(words.nextToken()))
						// if
						// (StandardAnalyzer.STOP_WORDS_SET.contains(words.nextToken()))
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
			logger.error("Unknown error", ex);
			return null;
		}
	}

	private List<String> getStopWords()
	{
		return getFileContent(new File("src/main/resources/no/hioa/crawler/parser/stop-words-english.txt"));
	}

	private void saveResult(File file, String buffer)
	{
		try (PrintWriter writer = new PrintWriter(file, "ISO-8859-1"))
		{
			writer.write(buffer.toString());
		}
		catch (IOException ex)
		{
			logger.error("Could not save content to file " + file, ex);
		}
	}

	private List<String> getFileContent(File file)
	{
		List<String> words = new LinkedList<>();

		try (Scanner scanner = new Scanner(new FileInputStream(file), "ISO-8859-1"))
		{
			while (scanner.hasNextLine())
			{
				String input = scanner.nextLine().toLowerCase();
				words.add(input);
			}
		}
		catch (Exception ex)
		{
			logger.error("Could not read content for file " + file.getAbsolutePath(), ex);
		}

		return words;
	}

	private String getMonth(String input)
	{
		try
		{
			Pattern p = Pattern.compile(".*([/\\\\]\\d\\d[/\\\\]).*");
			Matcher m = p.matcher(input.replaceAll(" ,", ","));

			if (m.matches())
				return StringUtils.substringBetween(m.group(1), "/", "/");
			else
			{
				return null;
			}
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	private String getYear(String input)
	{
		try
		{
			Pattern p = Pattern.compile(".*([/\\\\]20\\d\\d[/\\\\]).*");
			Matcher m = p.matcher(input);

			if (m.matches())
				return StringUtils.substringBetween(m.group(1), "/", "/");
			else
			{
				return null;
			}
		}
		catch (Exception ex)
		{
			return null;
		}
	}
}
