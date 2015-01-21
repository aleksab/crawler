package no.hioa.crawler.site;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import no.hioa.crawler.model.facebook.DateStringAdapter;
import no.hioa.crawler.model.facebook.GroupFeed;
import no.hioa.crawler.model.facebook.JsonStringAdapter;
import no.hioa.crawler.model.facebook.Post;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

/**
 * Will gather all posts and comments from a facebook group. In order for this to work you need the group id (http://lookup-id.com/) and a valid
 * access token (https://developers.facebook.com/tools/explorer)
 */
public class FacebookCrawler
{
	private static final Logger	logger			= LoggerFactory.getLogger("fileLogger");
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	@Parameter(names = "-groupId", description = "Facebook group id to crawel", required = true)
	private String				groupId			= null;

	@Parameter(names = "-token", description = "Access token for facebook graph api", required = true)
	private String				token			= null;

	@Parameter(names = "-save", description = "Should we save posts? Default is false", required = false)
	private boolean				shouldSavePages	= false;

	@Parameter(names = "-output", description = "Where to store posts", required = false)
	private String				folder			= "target/";

	@Parameter(names = "-maxSize", description = "Max size of files", required = false)
	private double				maxSizeMb		= 10 * 1024 * 2014;

	@Parameter(names = "-minDate", description = "Min date of posts (dd-mm-yyyy)", required = false)
	private String				minDate			= null;

	private DateTime			dateThreshold	= null;
	private String				outputFolder	= null;
	private boolean				shouldAbort		= false;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");

		FacebookCrawler crawler = new FacebookCrawler(args);
		crawler.crawlGroup();
	}

	public FacebookCrawler(String[] args) throws IOException
	{
		new JCommander(this, args);

		if (shouldSavePages)
		{
			outputFolder = folder + "/" + groupId;
			FileUtils.forceMkdir(new File(outputFolder));
		}

		if (!StringUtils.isEmpty(minDate))
		{
			dateThreshold = DateTime.parse(minDate, DateTimeFormat.forPattern("dd-MM-yyyy"));
			logger.info("Minimum date of posts " + dateThreshold);
		}
	}

	public void crawlGroup()
	{
		logger.info("Starting to crawl group " + groupId);
		consoleLogger.info("Starting to crawl group " + groupId);

		startCrawling();

		logger.info("Done crawling group " + groupId);
	}

	private void startCrawling()
	{
		String nextPageUrl = "https://graph.facebook.com/" + groupId + "/feed?access_token=" + token;
		int pageCounter = 1;

		while (!shouldAbort())
		{
			long startTime = System.currentTimeMillis();

			String json = openRestUrl(nextPageUrl);
			if (json == null)
				shouldAbort = true;
			else
			{
				GroupFeed feed = translateGroupFeed(json);
				if (feed == null || feed.getPaging() == null || feed.getPaging().getNext() == null)
				{
					logger.info("End of group, aborting");
					shouldAbort = true;
				}
				else
				{
					saveJson(json, pageCounter);

					Post lastPost = feed.getData().get(feed.getData().size() - 1);
					consoleLogger.info("Crawled {} pages with last post {}", pageCounter, lastPost.getCreated_time());

					if (lastPost.getCreated_time().isBefore(dateThreshold) && lastPost.getUpdated_time().isBefore(dateThreshold))
					{
						logger.info("Last post is after threshold, aborting!");
						shouldAbort = true;
					}

					pageCounter++;
					nextPageUrl = feed.getPaging().getNext();
				}
			}

			beNice(startTime);
		}
	}

	GroupFeed parseJson(String file)
	{
		String json = readJson(file);
		return translateGroupFeed(json);
	}

	private boolean shouldAbort()
	{
		return shouldAbort || hasReachMaxSize();
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
			logger.error("Could not translate feed from " + json, ex);
			return null;
		}
	}

	private String openRestUrl(String restUrl)
	{
		try
		{
			URL url = new URL(restUrl);

			// make connection
			URLConnection urlc = url.openConnection();

			// get result
			BufferedReader br = new BufferedReader(new InputStreamReader(urlc.getInputStream(), "UTF-8"));
			String l = null;
			StringBuffer buffer = new StringBuffer();
			while ((l = br.readLine()) != null)
			{
				buffer = buffer.append(l);
			}
			br.close();

			return buffer.toString();
		}
		catch (Exception ex)
		{
			logger.error("Unknown error", ex);
			return null;
		}
	}

	private String readJson(String file)
	{
		try
		{
			return FileUtils.readFileToString(new File(file), "UTF-8");
		}
		catch (IOException ex)
		{
			logger.error("Could not read file", ex);
			return null;
		}
	}

	private void saveJson(String json, int pageCounter)
	{
		try
		{
			String file = outputFolder + "/" + pageCounter + ".json";
			FileUtils.writeStringToFile(new File(file), json, "UTF-8");
		}
		catch (IOException ex)
		{
			logger.error("Could not save json", ex);
		}
	}

	private boolean hasReachMaxSize()
	{
		double sizeFolder = (double) FileUtils.sizeOfDirectory(new File(outputFolder)) / (1024d * 1024d);
		if (sizeFolder >= maxSizeMb)
		{
			logger.warn("Size of folder with pages has reached limit: {}", sizeFolder);
			return true;
		}
		else
		{
			return false;
		}
	}

	private void beNice(long startTime)
	{
		try
		{
			long elapsedTime = System.currentTimeMillis() - startTime;

			// sleep a random time between 1 and 4 seconds
			long time = 1000 + (long) (Math.random() * 3000) - elapsedTime;

			if (time > 0)
			{
				logger.info("Waiting for {} ms", time);
				Thread.sleep(time);
			}
			else
				logger.info("No need to wait since elapsedTime was over limit");
		}
		catch (InterruptedException ex)
		{
			logger.error("Could not sleep", ex);
		}
	}
}
