package no.hioa.crawler.service;

import java.io.File;
import java.util.Collections;

import no.hioa.crawler.model.Link;
import no.hioa.crawler.model.Page;
import no.hioa.crawler.service.FileContentManager;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.PropertyConfigurator;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class FileContentManagerTest
{
	private FileContentManager	cm		= null;
	private File				tmpDir	= new File("target/tmpdir");

	@Before
	public void setup() throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		FileUtils.forceMkdir(tmpDir);
		cm = new FileContentManager(tmpDir.getAbsolutePath());
	}

	@After
	public void teardown() throws Exception
	{
		FileUtils.forceDelete(tmpDir);
	}

	@Test
	public void testSavePage() throws Exception
	{
		Link link = new Link("filmweb.no");
		StringBuffer content = new StringBuffer("Dette er en side<br>Filmweb\nSiste linje.");
		Page page = new Page(link, content);

		cm.savePages(Collections.singletonList(page));

		// we assume we have the only file in the folder...
		File checkFile = tmpDir.listFiles()[0];
		String fileContent = FileUtils.readFileToString(checkFile);

		Assert.assertEquals("URL: filmweb.no\n" + content.toString(), fileContent);
	}
}
