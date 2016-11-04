package pt.upa.transporter.ws.it;

import org.junit.*;

import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobView;
import static org.junit.Assert.*;

public class RequestJobIT extends AbstractIT{

	@Test(expected = BadLocationFault_Exception.class)
	public void invalidLocationsTest() throws BadLocationFault_Exception,
			BadPriceFault_Exception {
		CLIENTSOUTH.requestJob(invalidOrigin, validDestinationSouth, validPrice);
		CLIENTNORTH.requestJob(invalidOrigin, validDestinationSouth, validPrice);
	}

	@Test(expected = BadPriceFault_Exception.class)
	public void invalidPriceRangeTest() throws BadLocationFault_Exception,
			BadPriceFault_Exception {
		CLIENTSOUTH.requestJob(validOrigin, validDestinationSouth, invalidPrice);
		CLIENTNORTH.requestJob(validOrigin, validDestinationSouth, invalidPrice);
	}

	@Test
	public void unservableLocationsTest() throws BadLocationFault_Exception,
			BadPriceFault_Exception {
		JobView result = CLIENTSOUTH.requestJob(northLocation, southLocation,
				validPrice);

		assertNull(result);
		result = CLIENTNORTH.requestJob(northLocation, southLocation, validPrice);
		assertNull(result);
	}

	@Test(expected = BadLocationFault_Exception.class)
	public void unknownLocationTest() throws BadLocationFault_Exception,
			BadPriceFault_Exception {
		CLIENTSOUTH.requestJob(unknownLocation, centralLocation, validPrice);
		CLIENTNORTH.requestJob(unknownLocation, centralLocation, validPrice);
	}

	@Test
	public void success() throws BadLocationFault_Exception,
			BadPriceFault_Exception {
		JobView job = CLIENTSOUTH.requestJob(validOrigin, validDestinationSouth,
				validPrice);
		assertEquals(validOrigin, job.getJobOrigin());
		assertEquals(validDestinationSouth, job.getJobDestination());
		assertTrue(job.getJobPrice() >= 0);

		job = CLIENTNORTH.requestJob(validOrigin, validDestinationSouth, validPrice);
		assertNull(job);
	}
}
