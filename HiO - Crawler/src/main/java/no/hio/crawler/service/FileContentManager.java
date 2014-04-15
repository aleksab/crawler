package no.hio.crawler.service;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import no.hio.crawler.model.Page;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A simple content manager that saves pages to disk using timestamp as part of the filename.
 */
public class FileContentManager implements ContentManager
{
	private static final Logger	logger	= LoggerFactory.getLogger(FileContentManager.class);

	private String				folder	= null;

	public FileContentManager(String folder)
	{
		super();
		this.folder = folder;
	}

	@Override
	public boolean savePages(List<Page> pages)
	{
		for (Page page : pages)
		{
			Path newFile = Paths.get(folder, System.currentTimeMillis() + ".page");
			try (BufferedWriter writer = Files.newBufferedWriter(newFile, Charset.defaultCharset()))
			{
				writer.append(page.getContent());
			}
			catch (IOException ex)
			{
				logger.error("Could not save page " + page + " to file " + newFile, ex);
				return false;
			}
		}

		return true;
	}
}
