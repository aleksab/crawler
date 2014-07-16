package no.hioa.crawler.product;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "reviews")
public class ReviewHeader
{
	private List<ProductReview> reviews;

	public ReviewHeader()
	{

	}

	public ReviewHeader(List<ProductReview> reviews)
	{
		super();
		this.reviews = reviews;
	}

	@XmlElement(name = "review")
	public List<ProductReview> getProductReview()
	{
		return reviews;
	}

	public void setProductReview(List<ProductReview> reviews)
	{
		this.reviews = reviews;
	}

}
