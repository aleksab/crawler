package no.hio.crawler.filmweb;

import org.apache.log4j.PropertyConfigurator;

public class SingleThreadCrawler implements Runnable
{
	public static void main(String[] args)
	{
		PropertyConfigurator.configure("log4j.properties");

	}

	@Override
	public void run()
	{
		// fetch page
		
		// crawl page (wait if not too long since last time, random amount)		
		
		// send result to qm
		
		// send result to cm
		
	}

}
