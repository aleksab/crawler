package no.hioa.crawler.parser;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.List;

import no.hioa.crawler.model.facebook.Comment;
import no.hioa.crawler.model.facebook.DateStringAdapter;
import no.hioa.crawler.model.facebook.GroupFeed;
import no.hioa.crawler.model.facebook.JsonStringAdapter;
import no.hioa.crawler.model.facebook.Post;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class ExtractFacebookContent
{
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		ExtractFacebookContent extractor = new ExtractFacebookContent();

		extractor.extractFolders(new File("C:/data/terror/crawl/facebook"), new File("C:/data/terror/text/facebook"));
	}

	public ExtractFacebookContent()
	{

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
				List<String> allContent = extractTextContent(file);
				for (String content : allContent)
				{
					saveResult(new File(outputFolder + "/" + System.currentTimeMillis() + ".txt"), content);
				}
			}
		}

		return true;
	}

	public List<String> extractTextContent(File htmlFile)
	{
		List<String> content = new LinkedList<>();

		try
		{
			String json = FileUtils.readFileToString(htmlFile, "UTF-8");
			GroupFeed feed = translateGroupFeed(json);

			if (feed.getData() != null)
			{
				for (Post post : feed.getData())
				{
					if (shouldAdd(post.getMessage()))
						content.add(post.getMessage());

					if (post.getComments() != null)
					{
						for (Comment comment : post.getComments().getData())
						{
							if (shouldAdd(comment.getMessage()))
								content.add(comment.getMessage());
						}
					}
				}
			}
		}
		catch (Exception ex)
		{
			consoleLogger.error("Unknown error", ex);
		}

		return content;
	}

	private boolean shouldAdd(String input)
	{
		if (input != null && input.length() > 5)
			return true;
		else
			return false;
	}

	private GroupFeed translateGroupFeed(String json)
	{
		try
		{
			GsonBuilder gsonBuilder = new GsonBuilder();
			gsonBuilder.registerTypeAdapter(String.class, new JsonStringAdapter());
			gsonBuilder.registerTypeAdapter(DateTime.class, new DateStringAdapter());
			Gson gson = gsonBuilder.create();

			return gson.fromJson(json, GroupFeed.class);
		}
		catch (Exception ex)
		{
			consoleLogger.error("Could not translate feed from " + json, ex);
			return null;
		}
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
}
