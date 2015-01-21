package no.hioa.crawler.site;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import no.hioa.crawler.model.Link;
import no.hioa.crawler.model.facebook.GroupFeed;
import no.hioa.crawler.model.facebook.JsonStringAdapter;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class FacebookCrawler
{
	private static final Logger	logger			= LoggerFactory.getLogger("fileLogger");
	private static final Logger	consoleLogger	= LoggerFactory.getLogger("stdoutLogger");

	private Link				group			= null;

	@Parameter(names = "-group", description = "Facebook group to crawel", required = true)
	private String				url				= null;

	@Parameter(names = "-save", description = "Should we save pages? Default is false", required = false)
	private boolean				shouldSavePages	= false;

	@Parameter(names = "-output", description = "Where to store pages", required = false)
	private String				folder			= "target/";

	@Parameter(names = "-maxSize", description = "Max size of files", required = false)
	private double				maxSizeMb		= 10 * 1024 * 2014;

	private String				outputFolder	= null;
	private boolean				shouldAbort		= false;

	public static void main(String[] args) throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		FacebookCrawler crawler = new FacebookCrawler(args);
		// crawler.crawlGroup("438852196217474",
		// "CAACEdEose0cBAFHedpQdCuLXqZBp9oXpszjd93ZAnB7f0V8kFh0W2KnbkyR23NZBOgHrTmxZAhWn8wZCcL3sgUYa9xIhZB6xtSErZA3Wh3aSoGaPSpcaWZCyjsGF0owIarPoTSyQgD6ysZBXiZAn46eU6ikWO8EKrYqsCLcRVgOf0J7EYhSKble64AFPonFgcZAkhNiJ4C76EyP8b1U9G1KrHj9");
		crawler.parseJson("C:/Development/workspace 2/Hioa - Crawler/target/facebookcombritainfirstgb/test.json");
	}

	public FacebookCrawler(String[] args) throws IOException
	{
		new JCommander(this, args);

		this.group = new Link(url);

		if (shouldSavePages)
		{
			outputFolder = folder + "/" + removeNoneAlphaNumeric(group.getLink());
			FileUtils.forceMkdir(new File(outputFolder));
		}
	}

	/**
	 * Crawl the group and save stats to a file. The crawler will not exit before all found links have been crawled.
	 */
	public void crawlGroup(String groupId, String accessToken)
	{
		logger.info("Starting to crawl group " + group.getLink());
		consoleLogger.info("Starting to crawl group " + group.getLink());

		String text = openRestUrl("https://graph.facebook.com/" + groupId + "/feed?access_token=" + accessToken);
		if (text != null)
			saveJson(text);

		logger.info("Done crawling group " + group.getLink());
	}

	public void parseJson(String file)
	{
		String json = readJson(file);
		GroupFeed feed = getFeed(json);
		consoleLogger.info("Feed: " + feed);
	}

	private GroupFeed getFeed(String json)
	{
		GsonBuilder gsonBuilder = new GsonBuilder();
		gsonBuilder.registerTypeAdapter(String.class, new JsonStringAdapter());
		Gson gson = gsonBuilder.create();

		return gson.fromJson(json, GroupFeed.class);
	}

	String openRestUrl(String restUrl)
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

	String readJson(String file)
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

	void saveJson(String text)
	{
		try
		{
			String file = outputFolder + "/" + System.currentTimeMillis() + ".json";
			FileUtils.writeStringToFile(new File(file), text, "UTF-8");
		}
		catch (IOException ex)
		{
			logger.error("Could not save text", ex);
		}
	}

	private String removeNoneAlphaNumeric(String input)
	{
		return input.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
	}
}
