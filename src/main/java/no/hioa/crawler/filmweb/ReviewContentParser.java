package no.hioa.crawler.filmweb;

/**
 * Parser for movie content.
 */
public interface ReviewContentParser
{
	/**
	 * Can we parse this domain.
	 * 
	 * @param domain
	 * @return
	 */
	public boolean canParseDomain(String domain);

	/**
	 * Should we ignore this domain.
	 * 
	 * @return
	 */
	public boolean shouldIgnore();

	/**
	 * Extract review content.
	 * 
	 * @param content
	 * @return
	 */
	public String getContent(String content);
}
