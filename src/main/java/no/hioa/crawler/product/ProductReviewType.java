package no.hioa.crawler.product;


public enum ProductReviewType
{
	KOMPLETT("Komplett", "komplett.no"), MPX("Mpx", "mpx.no");

	private String	name;
	private String	url;

	ProductReviewType(String name, String url)
	{
		this.name = name;
		this.url = url;
	}

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
	}

	public String getUrl()
	{
		return url;
	}

	public void setUrl(String url)
	{
		this.url = url;
	}

	@Override
	public String toString()
	{
		return this.getName();
	}

	public static ProductReviewType getEnum(String name)
	{
		for (ProductReviewType re : ProductReviewType.values())
		{
			if (re.name.compareTo(name) == 0 || re.url.compareTo(name) == 0)
			{
				return re;
			}
		}
		throw new IllegalArgumentException("Invalid product review value: " + name);
	}
}
