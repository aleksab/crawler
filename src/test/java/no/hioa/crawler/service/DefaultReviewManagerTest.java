package no.hioa.crawler.service;

import java.io.File;


import no.hioa.crawler.model.Review;
import no.hioa.crawler.model.ReviewType;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class DefaultReviewManagerTest
{
	private DefaultReviewManager	manager	= null;

	@Before
	public void setup() throws Exception
	{
		PropertyConfigurator.configure("log4j.properties");
		manager = new DefaultReviewManager("target/test");
	}

	@Test
	public void testGetReviews()
	{
		Review review = manager.getReview(new File("src/test/resources/no/hioa/crawler/service/1.review"));
		Assert.assertNotNull(review);
		Assert.assertEquals("http://www.adressa.no/kultur/film/article9579773.ece", review.getLink());
		Assert.assertEquals("Adressa", review.getAuthor());
		Assert.assertEquals("", review.getDate());
		Assert.assertEquals("Nebraska", review.getTitle());
		Assert.assertEquals(ReviewType.FILMWEB, review.getType());
		Assert.assertEquals(1321, review.getContent().length());
	}

	@Test
	public void testSaveReviews()
	{
		Review review = new Review(
				"http://www.cine.no/incoming/article1166083.ece",
				6,
				"Nebraska",
				"Nebraska er et årets høydepunkter på kino -gå ikke glipp av den. Det tok åtte år før Alexander Payne fikk laget denne filmen. Resultatet er blitt hans beste film - og det sier ikke lite om en regissør som har filmer som The Descendants, About Schmidt's  og Sideways på samvittigheten. Nebraska er en uvanlig roadmovie med en 77-åring i hovedrollen. Det er fortellingen om Woody Grant (Bruce Dern) og hans sønn David (Will Forte). Woody har fått et brev i posten hvor det står at han har vunnet en million dollar. Han nekter å akseptere at dette er et reklametriks og bestemmer seg får å reise til Nebraska for å hente gevinsten. Sønnen, David, prøver å fortelle at det handler om reklametriks, men det preller av som vann på gåsa.  David ser derfor ingen annen løsning en å legge ut å turen sammen med faren. Sammen begir de seg på en tur gjennom de amerikanske Midtvesten. I tillegg til en siste familiegjenforening når Woody stopper hjembyen Lincoln. Handlingen høres enkel ut, men  det er nettopp det enkle som engasjerer. Alexander Paynes film er en estetisk nytelse.  Det handler om en usedvanlig godt fortalt historie med fremragende skuespillere i hovedrollene.  I scenene fra Lincoln spiller innbyggerne seg selv –noe som gir filmen en ekstra ekthet. Alexander Payne har valgt å skyte filmen i svart/hvit og slik gitt oss  nostalgisk følelse som kler handlingen. Nebraska er et portrett av den amerikanske landsbygda i dag, familieforhold og økonomi  - men først og fremst er det en personlig reise hvor den lettere demente faren og sønnen gis en siste mulighet til sammen å utforske hverandre og det Amerika som en gang var. Det er en fornøyelse å oppleve aldrende skuespillere som Bruce Dern og June Squibb som nå er bedre en noen gang. Nebraska er et årets høydepunkter på kino -gå ikke glipp av den.",
				"Cinema", "", ReviewType.FILMWEB);
		manager.saveReview(review);

		Assert.assertEquals(1, new File("target/test").listFiles().length);
	}
	
	@Test
	public void testGenerateXml()
	{
		Review review = new Review(
				"http://www.cine.no/incoming/article1166083.ece",
				6,
				"Nebraska",
				"Nebraska er et årets høydepunkter på kino -gå ikke glipp av den. Det tok åtte år før Alexander Payne fikk laget denne filmen. Resultatet er blitt hans beste film - og det sier ikke lite om en regissør som har filmer som The Descendants, About Schmidt's  og Sideways på samvittigheten. Nebraska er en uvanlig roadmovie med en 77-åring i hovedrollen. Det er fortellingen om Woody Grant (Bruce Dern) og hans sønn David (Will Forte). Woody har fått et brev i posten hvor det står at han har vunnet en million dollar. Han nekter å akseptere at dette er et reklametriks og bestemmer seg får å reise til Nebraska for å hente gevinsten. Sønnen, David, prøver å fortelle at det handler om reklametriks, men det preller av som vann på gåsa.  David ser derfor ingen annen løsning en å legge ut å turen sammen med faren. Sammen begir de seg på en tur gjennom de amerikanske Midtvesten. I tillegg til en siste familiegjenforening når Woody stopper hjembyen Lincoln. Handlingen høres enkel ut, men  det er nettopp det enkle som engasjerer. Alexander Paynes film er en estetisk nytelse.  Det handler om en usedvanlig godt fortalt historie med fremragende skuespillere i hovedrollene.  I scenene fra Lincoln spiller innbyggerne seg selv –noe som gir filmen en ekstra ekthet. Alexander Payne har valgt å skyte filmen i svart/hvit og slik gitt oss  nostalgisk følelse som kler handlingen. Nebraska er et portrett av den amerikanske landsbygda i dag, familieforhold og økonomi  - men først og fremst er det en personlig reise hvor den lettere demente faren og sønnen gis en siste mulighet til sammen å utforske hverandre og det Amerika som en gang var. Det er en fornøyelse å oppleve aldrende skuespillere som Bruce Dern og June Squibb som nå er bedre en noen gang. Nebraska er et årets høydepunkter på kino -gå ikke glipp av den.",
				"Cinema", "", ReviewType.FILMWEB);
		manager.saveReview(review);
		
		manager.generateXml("target/test.xml");
	}
}
