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

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
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

		// System.out.println(extractor.extractLinks(new
		// Link("frie-ytringer.com"), new
		// File("E:/Data/blogs2/crawl/frieytringercom/1428526543682.html")));

		// extractor.extractFolderLinks(new Link("marchforengland.weebly.com"),
		// new File("E:/Data/blogs2/crawl/marchforenglandweeblycom/"), new File(
		// "E:/Data/blogs2/links/"));

		// extractor.extractLinksFolders();
		extractor.filterLinksFolder(new File("D:/Data/blogs2/links"), new File("D:/Data/blogs2/alllinks.csv"));
	}

	public ExtractTextContent()
	{
		stopWords = getStopWords();
	}

	public void filterLinksFolder(File folderPath, File outputFile) throws Exception
	{
		StringBuffer buffer = new StringBuffer();

		long links = 0;
		long unknown = 0;
		long notFound = 0;
		long invalid = 0;
		
		for (File file : folderPath.listFiles())
		{
			List<LinkDate> dates = new LinkedList<>();
			List<String> lines = FileUtils.readLines(file, "UTF-8");

			for (String line : lines)
			{
				try
				{
					String domain = file.getName().replace("-links.txt", "");
					String date = StringUtils.substringBefore(line, ":");
					String foundDate = StringUtils.substringAfter(line, ":");
					foundDate = StringUtils.substringBefore(foundDate, ":");

					String url = StringUtils.substringAfter(line, ":");
					url = StringUtils.substringAfter(url, ":");
					url = LinkUtil.normalizeDomain(url);

					buffer.append(domain).append(";");
					buffer.append(date).append(";");
					buffer.append(foundDate).append(";");
					buffer.append(url).append("\n");

					if (date.equalsIgnoreCase("2000-01-01"))
						unknown++;
					if (foundDate.equalsIgnoreCase("false") && !date.equalsIgnoreCase("2000-01-01"))
						notFound++;

					links++;
				}
				catch (Exception ex)
				{
					invalid++;
				}
			}
		}

		FileUtils.writeStringToFile(outputFile, buffer.toString());

		logger.info("Links found: {}", links);
		logger.info("Unknown dates: {}", unknown);
		logger.info("Not found dates: {}", notFound);
		logger.info("Invalid: {}", invalid);
	}

	public void extractLinksFolders()
	{
		this.extractFolderLinks(new Link("4freedoms.com"), new File("E:/Data/blogs2/crawl/4freedomsningcom/"), new File("E:/Data/blogs2/links/"));
		this.extractFolderLinks(new Link("britainfirst.org"), new File("E:/Data/blogs2/crawl/britainfirstorg/"), new File("E:/Data/blogs2/links/"));
		this.extractFolderLinks(new Link("demokratene.no"), new File("E:/Data/blogs2/crawl/demokrateneno/"), new File("E:/Data/blogs2/links/"));
		this.extractFolderLinks(new Link("document.no"), new File("E:/Data/blogs2/crawl/documentno/"), new File("E:/Data/blogs2/links/"));
		this.extractFolderLinks(new Link("frie-ytringer.com"), new File("E:/Data/blogs2/crawl/frieytringercom/"), new File("E:/Data/blogs2/links/"));
		this.extractFolderLinks(new Link("frihetspartiet.net"), new File("E:/Data/blogs2/crawl/frihetspartietnet/"),
				new File("E:/Data/blogs2/links/"));
		this.extractFolderLinks(new Link("honestthinking.org"), new File("E:/Data/blogs2/crawl/honestthinkingorgno/"), new File(
				"E:/Data/blogs2/links/"));
		this.extractFolderLinks(new Link("idag.no"), new File("E:/Data/blogs2/crawl/idagno/"), new File("E:/Data/blogs2/links/"));
		this.extractFolderLinks(new Link("kristentsamlingsparti.no"), new File("E:/Data/blogs2/crawl/kristentsamlingspartino/"), new File(
				"E:/Data/blogs2/links/"));
		this.extractFolderLinks(new Link("lionheartuk.blogspot.no"), new File("E:/Data/blogs2/crawl/lionheartukblogspotno/"), new File(
				"E:/Data/blogs2/links/"));
		this.extractFolderLinks(new Link("marchforengland.weebly.com"), new File("E:/Data/blogs2/crawl/marchforenglandweeblycom/"), new File(
				"E:/Data/blogs2/links/"));
		this.extractFolderLinks(new Link("norgesavisen.no"), new File("E:/Data/blogs2/crawl/norgesavisenno/"), new File("E:/Data/blogs2/links/"));
		this.extractFolderLinks(new Link("norwegiandefenceleague.com"), new File("E:/Data/blogs2/crawl/norwegiandefenceleaguecom/"), new File(
				"E:/Data/blogs2/links/"));
		this.extractFolderLinks(new Link("rights.no"), new File("E:/Data/blogs2/crawl/rightsno/"), new File("E:/Data/blogs2/links/"));
		this.extractFolderLinks(new Link("ronnyalte.net"), new File("E:/Data/blogs2/crawl/ronnyaltenet/"), new File("E:/Data/blogs2/links/"));
		this.extractFolderLinks(new Link("sian.no"), new File("E:/Data/blogs2/crawl/sianno/"), new File("E:/Data/blogs2/links/"));
		this.extractFolderLinks(new Link("sisteskanse.net"), new File("E:/Data/blogs2/crawl/sisteskansenet/"), new File("E:/Data/blogs2/links/"));
		this.extractFolderLinks(new Link("theenglishdefenceleagueextra.blogspot.no"), new File(
				"E:/Data/blogs2/crawl/theenglishdefenceleagueextrablogspotno/"), new File("E:/Data/blogs2/links/"));
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

	public boolean extractFolderLinks(Link domain, File folder, File outputFolder)
	{
		List<LinkDate> dates = new LinkedList<>();

		for (File file : folder.listFiles())
		{
			if (file.isFile())
			{
				dates.addAll(extractLinks(domain, file));
			}
		}

		try
		{
			int found = 0;
			int notfound = 0;
			String buffer = "";
			for (LinkDate date : dates)
			{
				buffer += date.date + ":" + date.foundDate + ":" + date.url + "\n";

				if (date.foundDate)
					found++;
				else
					notfound++;
			}

			buffer = StringUtils.substringBeforeLast(buffer, ",");

			FileUtils.writeStringToFile(new File(outputFolder + "/" + domain.getLink() + "-links.txt"), buffer.toString());

			logger.info("Found dates: {}", found);
			logger.info("Did not find dates: {}", notfound);
		}
		catch (Exception ex)
		{
			logger.error("Unknown error", ex);
		}

		return true;
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

	public List<LinkDate> extractLinks(Link domain, File htmlFile)
	{
		try
		{
			List<LinkDate> links = new LinkedList<>();
			List<String> unknown = new LinkedList<>();

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

					// find tag in source (can be multiple)
					// extract 500 chars before and after, regex for dates
					int index = StringUtils.indexOf(html, e.html());
					if (index != -1)
					{
						int start = index - 1000;
						if (start < 0)
							start = 0;

						int end = index + 4000;
						if (end >= html.length())
							end = html.length() - 1;

						String source = StringUtils.substring(html, start, end);
						source = StringUtils.replace(source, url, "");

						if (url.contains("http://www.vg.no/nyheter/innenriks/lyst-til"))
						{
							// logger.info(source);
						}

						LocalDate date = getDate(source, url);

						if (date != null)
						{
							// logger.info("Found date ({}) for {}", date, url);
							links.add(new LinkDate(url, date, true));
						}
						else
						{
							// logger.info("Could not find date ({}) for {} in {}",
							// date, url, htmlFile);
							unknown.add(url);
						}

					}
				}

				el = element.select("iframe[src]");
				for (Element e : el)
				{
					String url = e.attr("src");

					if (!shouldUseLink(domain, url))
						continue;

					// find tag in source (can be multiple)
					// extract 500 chars before and after, regex for dates
					int index = StringUtils.indexOf(html, e.toString());
					if (index != -1)
					{
						int start = index - 1000;
						if (start < 0)
							start = 0;

						String source = StringUtils.substring(html, start, index);
						source = StringUtils.replace(source, url, "");

						if (url.contains("nzRliBASdCc"))
						{
							// logger.info(source);
						}

						LocalDate date = getDate(source, url);

						if (date != null)
						{
							// logger.info("Found date ({}) for {}", date, url);
							links.add(new LinkDate(url, date, true));
						}
						else
						{
							// logger.info("Could not find date ({}) for {} in {}",
							// date, url, htmlFile);
							unknown.add(url);
						}

					}
				}
			}

			if (!unknown.isEmpty() && !links.isEmpty())
			{
				LocalDate firstDate = links.get(0).date;
				for (String url : unknown)
					links.add(new LinkDate(url, firstDate, false));
			}
			else if (!unknown.isEmpty() && links.isEmpty())
			{
				LocalDate fileDate = extractDate(htmlFile);
				if (fileDate != null)
				{
					for (String url : unknown)
						links.add(new LinkDate(url, fileDate, false));
				}
				else
				{
					for (String url : unknown)
						links.add(new LinkDate(url, LocalDate.parse("2000-01-01", DateTimeFormat.forPattern("yyyy-MM-dd")), false));
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

	private LocalDate getDate(String input, String url)
	{
		try
		{
			input = input.replaceAll("\n", " ");
			input = input.replaceAll("\"", "");

			LocalDate date = findDate(input);
			if (date != null)
				return date;

			date = findDateFromUrl(url);

			return date;
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}

		return null;
	}

	private LocalDate findDate(String input)
	{
		try
		{
			String regex = ".*(january|february|march|april|may|june|july|august|september|october|november|december)(\\s)(\\d+?)(,\\s)(20\\d\\d).*";
			Pattern p = Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(input);

			if (m.matches())
			{
				String year = m.group(5);
				String month = getMonthFromText(m.group(1));
				String day = m.group(3);

				String date = year + "-" + month + "-" + day;
				return LocalDate.parse(date, DateTimeFormat.forPattern("yyyy-MM-dd"));
			}

			regex = ".*(\\s)(\\d+?)(\\s)(january|february|march|april|may|june|july|august|september|october|november|december)(\\s)(20\\d\\d).*";
			p = Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			m = p.matcher(input);

			if (m.matches())
			{
				String year = m.group(6);
				String month = getMonthFromText(m.group(4));
				String day = m.group(2);

				String date = year + "-" + month + "-" + day;
				return LocalDate.parse(date, DateTimeFormat.forPattern("yyyy-MM-dd"));
			}

			regex = ".*(\\d\\d)(/)(\\d\\d)(/)(20\\d\\d).*";
			p = Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			m = p.matcher(input);

			if (m.matches())
			{
				String year = m.group(5);
				String month = m.group(3);
				String day = m.group(1);

				String date = year + "-" + month + "-" + day;
				return LocalDate.parse(date, DateTimeFormat.forPattern("yyyy-MM-dd"));
			}

			return null;
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	private LocalDate findDateFromUrl(String url)
	{
		try
		{
			String regex = ".*(/)(20\\d\\d)(/)(\\d\\d)(/)(\\d\\d)(/).*";
			Pattern p = Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(url);

			if (m.matches())
			{
				String year = m.group(2);
				String month = getMonthFromText(m.group(4));
				String day = m.group(6);

				String date = year + "-" + month + "-" + day;
				return LocalDate.parse(date, DateTimeFormat.forPattern("yyyy-MM-dd"));
			}

			regex = ".*(/)(20\\d\\d)(/)(\\d\\d)(/).*";
			p = Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			m = p.matcher(url);

			if (m.matches())
			{
				String year = m.group(2);
				String month = getMonthFromText(m.group(4));
				String day = "01";

				String date = year + "-" + month + "-" + day;
				return LocalDate.parse(date, DateTimeFormat.forPattern("yyyy-MM-dd"));
			}

			regex = ".*(/)(20\\d\\d)(/)(\\d\\d)(/).*";
			p = Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			m = p.matcher(url);

			if (m.matches())
			{
				String year = m.group(2);
				String month = getMonthFromText(m.group(4));
				String day = "01";

				String date = year + "-" + month + "-" + day;
				return LocalDate.parse(date, DateTimeFormat.forPattern("yyyy-MM-dd"));
			}

			regex = ".*(/)(20\\d\\d)(/)(jan|feb|mar|apr|may|jun|jul|aug|sep|oct|nov|dec)(/)(\\d\\d)(/).*";
			p = Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			m = p.matcher(url);

			if (m.matches())
			{
				String year = m.group(2);
				String month = getMonthFromText(m.group(4));
				String day = m.group(6);

				String date = year + "-" + month + "-" + day;
				return LocalDate.parse(date, DateTimeFormat.forPattern("yyyy-MM-dd"));
			}

			return null;
		}
		catch (Exception ex)
		{
			return null;
		}
	}

	public boolean shouldUseLink(Link domain, String url)
	{
		try
		{
			if (StringUtils.isEmpty(url))
				return false;

			if (!url.startsWith("http"))
				return false;

			if (url.contains(domain.getLink()))
				return false;

			url = url.replace("https://", "http://");

			if (domain.getLink().equalsIgnoreCase(LinkUtil.normalizeDomain(url)) || "/".equalsIgnoreCase(LinkUtil.normalizeDomain(url)))
			{
				logger.debug("Ignoring link since internal: " + url);
				return false;
			}

			for (String ignore : getIgnoreLinks())
			{
				if (url.contains(ignore))
					return false;
			}

			return true;
		}
		catch (Exception ex)
		{
			return false;
		}
	}

	private String getMonthFromText(String input)
	{
		switch (input.toLowerCase())
		{
		case "january":
			return "01";
		case "february":
			return "02";
		case "march":
			return "03";
		case "april":
			return "04";
		case "may":
			return "05";
		case "june":
			return "06";
		case "july":
			return "07";
		case "august":
			return "08";
		case "september":
			return "09";
		case "october":
			return "10";
		case "november":
			return "11";
		case "december":
			return "12";
		}

		return "01";
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

	private List<String> getIgnoreLinks()
	{
		List<String> ignore = new LinkedList<>();

		ignore.add("facebook");
		ignore.add("ning");
		ignore.add("twitter");
		ignore.add("dropbox");
		ignore.add("mailto");
		ignore.add("google.com");
		ignore.add("linkedin.com");
		ignore.add("paypal");
		ignore.add("presscustomizr");
		ignore.add("ideaboxthemes");
		ignore.add("youtube.com/DocumentNo");
		ignore.add("presse.no/Etisk-regelverk/");

		return ignore;
	}

	public class LinkDate
	{
		public String		url;
		public LocalDate	date;
		public boolean		foundDate;

		public LinkDate(String url, LocalDate date, boolean foundDate)
		{
			super();
			this.url = url;
			this.date = date;
			this.foundDate = foundDate;
		}

		@Override
		public String toString()
		{
			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
		}
	}
}
