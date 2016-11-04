package pt.upa.broker.ws.it;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import pt.upa.broker.ws.InvalidPriceFault_Exception;
import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;
import pt.upa.broker.ws.UnavailableTransportPriceFault_Exception;
import pt.upa.broker.ws.UnknownLocationFault_Exception;
import pt.upa.broker.ws.UnknownTransportFault_Exception;

public class ViewTransportIT extends AbstractIT{
	private static String validOrigin = "Faro";
	private static String validDestination = "Lisboa";

	private int validPrice = 51;

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
		CLIENT.getFrontEnd().clearTransports();
	}

	// tests

	@Test(expected = UnknownTransportFault_Exception.class)
	public void invalidPriceTest() throws UnknownTransportFault_Exception {
		CLIENT.getFrontEnd().viewTransport("cenas");
	}

	@Test
	public void success() throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
			UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception, UnknownTransportFault_Exception {
		String result = CLIENT.getFrontEnd().requestTransport(validOrigin, validDestination, validPrice);
		assertNotNull(result);
		TransportView vt = CLIENT.getFrontEnd().viewTransport(result);
		assertEquals(result, vt.getId());
		assertEquals(validOrigin, vt.getOrigin());
		assertEquals(validDestination, vt.getDestination());
		assertTrue("Price obtained should be leser than provided price. got " + vt.getPrice() + ", proposed " + validPrice,
				vt.getPrice() < validPrice );
	}
}
