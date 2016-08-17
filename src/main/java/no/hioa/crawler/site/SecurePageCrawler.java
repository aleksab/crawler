package no.hioa.crawler.site;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.PropertyConfigurator;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.Parameter;

import no.hioa.crawler.model.Link;
import no.hioa.crawler.service.DefaultCrawler;

public class SecurePageCrawler extends DefaultCrawler {
	private static final Logger logger = LoggerFactory.getLogger("fileLogger");
	private static final Logger consoleLogger = LoggerFactory.getLogger("stdoutLogger");

	private Link page = null;
	private HashSet<Link> externalLinks = new HashSet<>();

	@Parameter(names = "-page", description = "Secure page to crawel", required = true)
	private String url = null;

	@Parameter(names = "-save", description = "Should we save page? Default is false", required = false)
	private boolean shouldSavePage = false;

	@Parameter(names = "-output", description = "Where to store page", required = false)
	private String folder = "target/";

	private String outputFolder = null;
	private boolean shouldAbort = false;
	private List<String> ignoreList = null;

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		SecurePageCrawler crawler = new SecurePageCrawler(args);
		crawler.crawlPage();
	}

	public SecurePageCrawler(String[] args) throws IOException {
		new JCommander(this, args);

		this.page = new Link(url);
		this.setDomain(page);

		if (shouldSavePage) {
			outputFolder = folder + "/";
			FileUtils.forceMkdir(new File(outputFolder));
		}
	}

	/**
	 * Crawl the page and save result to a file. The crawler will not exit
	 * before all found links have been crawled.
	 */
	public void crawlPage() {
		logger.info("Starting to crawl " + page.getLink());
		consoleLogger.info("Starting to crawl " + page.getLink());

		Document document = fetchContent(page);
		if (document == null)
			return;

		if (shouldSavePage) {
			savePage(document, page.getLink());
		}

		logger.info("Done crawling " + page.getLink());
	}

	private static final String USER_AGENT = "Mozilla/5.0 (Linux 3.0.0-13-virtual x86_64) Crawler (ab@prognett.no)";
	private static final int PAGE_TIMEOUT = 1000 * 15;

	String username = "linnmf@gmail.com";
	String password = "444bheik";

	public Map<String,String> login() {
		Map<String,String> cookies = new HashMap<>();
		
		return cookies;
	}

			
	public Document fetchContent(Link link) {
		try {
			Map<String,String> cookies = new HashMap<>();
			cookies.put("SPID_NO", "dbk8082glah50s9651vvv3tg9egksb0a3tvl3g3b7a3bl0v8jj71; path=/; domain=.payment.schibsted.no; Secure; HttpOnly");
			cookies.put("SP_ALLOWED_PIDS", "dHVwbGVzPSgoMTAwMDIzLDE0NzEzODY1MjMpKSxzaWc9MHhmMWI4NjYyNTY0ZDAzNTdkYjQ4MmRlOWQ5MjUwNzVmYzRlMDNmZjE0ZWNmMTE5MDM4ZWQ2ZjIyZDE0ZDRlNmQ1; path=/; domain=pluss.vg.no");
			cookies.put("SP_ID", "eyJjbGllbnRfaWQiOiI0ZWYxY2ZiMGU5NjJkZDJlMGQ4ZDAwMDAiLCJhdXRoIjoidVk4dzBQWWFFdWttXzZkR1pjZ3hCVEo5U0VaRkhIbnJZeXA2VVZ5bkFURnhVdkJSd2VHQjhnVEZtdnN6RWtXZjZHUnQzUTZnQVROWXNTQUtRdnpYVHZjUUZGNkZ3bGxrRmYxRFBPT0tvU28ifQ; expires=Tue, 30 Aug 2016 22:23:44 GMT; path=/; domain=.pluss.vg.no");
			
			String properLink = link.getLink();
			if (!StringUtils.startsWithIgnoreCase(properLink, "http://")
					&& !StringUtils.startsWithIgnoreCase(properLink, "https://"))
				properLink = "http://" + properLink;
			return Jsoup.connect(properLink).timeout(PAGE_TIMEOUT).userAgent(USER_AGENT).cookies(cookies).followRedirects(true).get();
		} catch (Exception ex) {
			logger.warn("Could not fetch content for link " + link.getLink(), ex);
			return null;
		}
	}

	protected void crawlDocument(Document document, Link url) {

	}

	protected boolean shouldIgnoreLink(String link) {
		return false;
	}

	protected boolean shouldFollowDynamicLinks() {
		return true;
	}

	protected boolean shouldAbort() {
		return shouldAbort;
	}

	void savePage(Document document, String url) {
		try {
			StringBuffer buffer = new StringBuffer();
			buffer.append("URL: ").append(url).append("\n");
			buffer.append(document.html());

			String file = outputFolder + "/" + System.currentTimeMillis() + ".html";
			FileUtils.writeStringToFile(new File(file), buffer.toString(), "UTF-8");
		} catch (IOException ex) {
			logger.error("Could not save page", ex);
		}
	}

	private String removeNoneAlphaNumeric(String input) {
		return input.replaceAll("[^A-Za-z0-9]", "").toLowerCase();
	}
}
