package no.hioa.crawler.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegexUtil
{
	public static String matchRegex(String regex, String input)
	{
		try
		{
			Pattern p = Pattern.compile(regex, Pattern.DOTALL | Pattern.CASE_INSENSITIVE);
			Matcher m = p.matcher(input);

			if (m.matches())
				return m.group(1);
			else
				return null;
		}
		catch (Exception ex)
		{
			return null;
		}

	}
}
