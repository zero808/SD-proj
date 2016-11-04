package pt.upa.broker.ws;

import static org.junit.Assert.*;

import mockit.Expectations;
import mockit.Mocked;

import org.junit.*;

import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;
import pt.upa.transporter.ws.TransporterPortType;
import pt.upa.transporter.ws.cli.TransporterClient;

/**
 * Unit Test example
 * 
 * Invoked by Maven in the "test" life-cycle phase If necessary, should invoke
 * "mock" remote servers
 */
public class RequestTransportTest {

	@Mocked
	private TransporterClient tCN;

	@Mocked
	private TransporterClient tCS;

	private BrokerPort bP;
	private JobView responseJobSouthCompany;
	private JobView responseJobNorthCompany;
	private static int price = 50;
	private static int negativePrice = -15;
	private static String southCompanyName = "UpaTransporter1";
	private static String northCompanyName = "UpaTransporter2";
	private static String southLocation = "Beja";
	private static String centralLocation = "Lisboa";
	private static String northLocation = "Porto";
	private static String unknownLocation = "Baleizão";

	@Before
	public void setUp() {
		bP = new BrokerPort();
		bP.addTransporterCompanies(northCompanyName, tCN);
		bP.addTransporterCompanies(southCompanyName, tCS);
		responseJobSouthCompany = new JobView();
		responseJobSouthCompany.setCompanyName(southCompanyName);
		responseJobSouthCompany.setJobState(JobStateView.PROPOSED);
		responseJobSouthCompany.setJobPrice(price);
		responseJobSouthCompany.setJobIdentifier("0");

		responseJobNorthCompany = new JobView();
		responseJobNorthCompany.setCompanyName(northCompanyName);
		responseJobNorthCompany.setJobState(JobStateView.PROPOSED);
		responseJobNorthCompany.setJobPrice(price);
		responseJobNorthCompany.setJobIdentifier("1");
	}

	@After
	public void tearDown() {
		bP = null;
		responseJobSouthCompany = null;
		responseJobNorthCompany = null;
	}

	// tests

	@Test(expected = InvalidPriceFault_Exception.class)
	public void invalidPriceTest() throws BadLocationFault_Exception, BadPriceFault_Exception,
			InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception,
			UnknownLocationFault_Exception {
		bP.requestTransport(southLocation, centralLocation, negativePrice);
	}

	@Test(expected = UnavailableTransportFault_Exception.class)
	public void unavailableTransportTest() throws BadLocationFault_Exception, BadPriceFault_Exception,
			InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception,
			UnknownLocationFault_Exception {
		new Expectations() {
			{
				tCS.requestJob(southLocation, centralLocation, price);
				result = null;
			}
		};
		new Expectations() {
			{
				tCN.requestJob(southLocation, centralLocation, price);
				result = null;
			}
		};
		assertTrue(bP.requestTransport(southLocation, centralLocation, price) == null);
	}

	@Test(expected = UnavailableTransportPriceFault_Exception.class)
	public void unavailableTransportPriceTest() throws BadLocationFault_Exception, BadPriceFault_Exception,
			InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception,
			UnknownLocationFault_Exception {
		new Expectations() {
			{
				tCS.requestJob(southLocation, centralLocation, price);
				responseJobSouthCompany.setJobOrigin(southLocation);
				responseJobSouthCompany.setJobDestination(centralLocation);
				responseJobSouthCompany.setJobPrice(price + 10);
				result = responseJobSouthCompany;
			}
		};
		new Expectations() {
			{
				tCN.requestJob(southLocation, centralLocation, price);
				result = null;
			}
		};
		assertTrue(bP.requestTransport(southLocation, centralLocation, price) == null);
	}

	@Test(expected = UnknownLocationFault_Exception.class)
	public void unknownLocationTest() throws BadLocationFault_Exception, BadPriceFault_Exception,
			InvalidPriceFault_Exception, UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception,
			UnknownLocationFault_Exception {
		bP.requestTransport(unknownLocation, centralLocation, price);
	}

	@Test
	public void successTest() throws BadLocationFault_Exception, BadPriceFault_Exception, InvalidPriceFault_Exception,
			UnavailableTransportFault_Exception, UnavailableTransportPriceFault_Exception,
			UnknownLocationFault_Exception {
		
		new Expectations() {
			{
				tCS.requestJob(southLocation, centralLocation, price+1);
				responseJobSouthCompany.setJobOrigin(southLocation);
				responseJobSouthCompany.setJobDestination(centralLocation);
				responseJobSouthCompany.setCompanyName(southCompanyName);
				responseJobSouthCompany.setJobIdentifier("1");
				responseJobSouthCompany.setJobPrice(price-1);
				result = responseJobSouthCompany;
			}
		};
		new Expectations() {
			{
				tCN.requestJob(southLocation, centralLocation, price+1);
				result = null;
			}
		};
		
		String output = bP.requestTransport(southLocation, centralLocation, price+1);
		assertTrue(output.equals("0"));
	}

}