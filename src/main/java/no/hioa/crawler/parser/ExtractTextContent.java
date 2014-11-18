package no.hioa.crawler.parser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;
import java.util.StringTokenizer;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtractTextContent
{
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	private List<String>		stopWords		= null;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		ExtractTextContent extractor = new ExtractTextContent();

		extractor.extractFolders(new File("C:/data/crawl"), new File("C:/data/text"));
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
				consoleLogger.info("Extracting text from folder {}", folder.getName());

				try
				{
					File outputFolder = new File(outputPath + "/" + folder.getName());
					FileUtils.forceMkdir(outputFolder);
					extractFolderContent(folder, outputFolder);
				}
				catch (IOException ex)
				{
					consoleLogger.error("Could not save content for folder", ex);
				}
			}
		}
	}

	public boolean extractFolderContent(File folder, File outputFolder)
	{
		for (File file : folder.listFiles())
		{
			if (file.isFile())
			{
				String content = extractTextContent(file);
				if (content != null && !StringUtils.isEmpty(content))
				{
					saveResult(new File(outputFolder + "/" + System.currentTimeMillis() + ".txt"), content);
				}
			}
		}

		return true;
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
			consoleLogger.error("Unknown error", ex);
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
			consoleLogger.error("Could not save content to file " + file, ex);
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
			consoleLogger.error("Could not read content for file " + file.getAbsolutePath(), ex);
		}

		return words;
	}
}