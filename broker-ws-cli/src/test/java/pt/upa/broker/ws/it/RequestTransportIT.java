package pt.upa.broker.ws.it;

import org.junit.*;

import pt.upa.broker.ws.InvalidPriceFault_Exception;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;
import pt.upa.broker.ws.UnavailableTransportPriceFault_Exception;
import pt.upa.broker.ws.UnknownLocationFault_Exception;
import pt.upa.broker.ws.cli.BrokerClient;
import static org.junit.Assert.*;

/**
 * Integration Test example
 * 
 * Invoked by Maven in the "verify" life-cycle phase Should invoke "live" remote
 * servers
 */
public class RequestTransportIT extends AbstractIT{

	// static members
	private static String validOrigin = "Faro";
	private static String validDestination = "Lisboa";
	private static String northLocation = "Porto";
	private static String centralLocation = "Lisboa";
	private static String southLocation = "Beja";
	private static String unknownLocation = "Baleizao";
	private int evenPrice = 50;
	private int validPrice = 51;
	private int invalidPrice = -50;

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
		CLIENT.getFrontEnd().clearTransports();
	}

	// tests

	@Test(expected = InvalidPriceFault_Exception.class)
	public void invalidPriceTest() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
			UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
		CLIENT.getFrontEnd().requestTransport(southLocation, centralLocation, invalidPrice);
	}

	@Test(expected = UnavailableTransportFault_Exception.class)
	public void unavailableTransportTest() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
			UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
		CLIENT.getFrontEnd().requestTransport(southLocation, northLocation, validPrice);
	}
	
	@Test(expected = UnavailableTransportPriceFault_Exception.class)
	public void unavailableTransportPriceTest() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
			UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
		CLIENT.getFrontEnd().requestTransport(southLocation, centralLocation, evenPrice);
	}

	@Test(expected = UnknownLocationFault_Exception.class)
	public void unknownLocationTest() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
			UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
		CLIENT.getFrontEnd().requestTransport(unknownLocation, validDestination, validPrice);
	}

	@Test
	public void success() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
			UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
		String result = CLIENT.getFrontEnd().requestTransport(validOrigin, validDestination, validPrice);
		assertNotNull(result);
	}

}