package no.hioa.crawler.filmweb.parser;

import no.hioa.crawler.filmweb.ExternalContentParser;

public class IgnoreParser implements ExternalContentParser
{
	public boolean canParseDomain(String domain)
	{
		return "p3.no".equalsIgnoreCase(domain) || "nrkp3.no".equalsIgnoreCase(domain) || "filmguiden.no".equalsIgnoreCase(domain)
				|| "dt.no".equalsIgnoreCase(domain) || "dagsavisen.no".equalsIgnoreCase(domain) || "aftenbladet.no".equalsIgnoreCase(domain)
				|| "web3.aftenbladet.no".equalsIgnoreCase(domain) || "nd.no".equalsIgnoreCase(domain) || "oslopuls.no".equalsIgnoreCase(domain)
				|| "oslopuls.aftenposten.no".equalsIgnoreCase(domain) || "fvn.no".equalsIgnoreCase(domain) || "aftenposten.no".equalsIgnoreCase(domain)
				|| "go.api.no".equalsIgnoreCase(domain);
	}

	public boolean shouldIgnore()
	{
		return true;
	}

	public String getContent(String content)
	{
		return null;
	}
}
