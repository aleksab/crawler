package no.hioa.crawler.filmweb.parser;

public class OsloByParser extends BtParser
{
	public boolean canParseDomain(String domain)
	{
		return "osloby.no".equalsIgnoreCase(domain);
	}	
}
