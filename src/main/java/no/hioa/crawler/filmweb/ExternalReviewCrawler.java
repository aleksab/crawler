package no.hioa.crawler.filmweb;

import java.util.List;

import no.hioa.crawler.model.Review;
import no.hioa.crawler.util.LinkUtil;

import org.apache.commons.lang.StringUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExternalReviewCrawler extends Thread
{
	private static final Logger			logger				= LoggerFactory.getLogger("fileLogger");

	private static final String			USER_AGENT			= "Mozilla/5.0 (Linux 3.0.0-13-virtual x86_64) Crawler (ab@prognett.no)";
	private static final int			PAGE_TIMEOUT		= 1000 * 10;

	private Review						review				= null;
	private String						content				= null;
	private List<ExternalContentParser>	contentParsers		= null;
	private boolean						hasParsedContent	= false;

	public ExternalReviewCrawler(Review review, List<ExternalContentParser> contentParsers)
	{
		super();
		this.review = review;
		this.contentParsers = contentParsers;
	}

	@Override
	public void run()
	{
		Document document = fetchContent(review.getLink());
		if (document != null)
		{
			logger.info("Got content for {}", review.getLink());
			content = document.html();

			String domain = LinkUtil.normalizeDomain(review.getLink());

			// see if we can extract the content with one of our parsers
			for (ExternalContentParser siteParser : contentParsers)
			{
				if (siteParser.canParseDomain(domain))
				{
					String extractedContent = siteParser.getContent(content);

					// treat reviews less than 10 letters as empty
					if (extractedContent == null || extractedContent.trim().length() < 10)
						extractedContent = null;

					if (!StringUtils.isEmpty(extractedContent))
					{
						review.setContent(extractedContent);
						hasParsedContent = true;
						break;
					}
				}
			}
		}
		else
			logger.warn("Could not get content for {}", review.getLink());
	}

	public Review getReview()
	{
		return review;
	}

	public String getContent()
	{
		return content;
	}

	public boolean hasParsedContent()
	{
		return hasParsedContent;
	}

	private Document fetchContent(String link)
	{
		try
		{
			if (!StringUtils.startsWith(link, "http://"))
				link = "http://" + link;

			return Jsoup.connect(link).timeout(PAGE_TIMEOUT).userAgent(USER_AGENT).followRedirects(true).get();
		}
		catch (Exception ex)
		{
			logger.warn("Could not fetch content for link " + link, ex);
			return null;
		}
	}
}
