package no.hioa.crawler.service;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import no.hioa.crawler.model.Page;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple content manager that saves pages to disk using timestamp as part of the filename.
 */
public class FileContentManager implements ContentManager
{
	private static final Logger	logger	= LoggerFactory.getLogger("fileLogger");

	private String				folder	= null;

	public FileContentManager(String folder) throws IOException
	{
		super();
		this.folder = folder;

		FileUtils.forceMkdir(new File(folder));
	}

	@Override
	public void savePages(List<Page> pages)
	{
		for (Page page : pages)
		{
			Path newFile = Paths.get(folder, System.currentTimeMillis() + ".page");
			logger.info("Saving page {} to file {}", page.getUrl(), newFile);
			try (BufferedWriter writer = Files.newBufferedWriter(newFile, Charset.defaultCharset()))
			{
				writer.append("URL: " + page.getUrl().getLink() + "\n");
				writer.append(page.getContent());
			}
			catch (IOException ex)
			{
				logger.error("Could not save page " + page + " to file " + newFile, ex);
			}
		}
	}
}
