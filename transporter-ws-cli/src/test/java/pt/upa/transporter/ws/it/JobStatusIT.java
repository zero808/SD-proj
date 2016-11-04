package pt.upa.transporter.ws.it;

import org.junit.*;

import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobView;
import static org.junit.Assert.*;

public class JobStatusIT extends AbstractIT {

	@Test
	public void invalidJob() {
		JobView jobStatus = CLIENTSOUTH.jobStatus(invalidID);
		assertNull(jobStatus);
	}

	@Test
	public void success() throws BadLocationFault_Exception, BadPriceFault_Exception {

		JobView job;
		JobView jobStatus;
		job = CLIENTNORTH.requestJob(validOrigin, validDestinationNorth, validPriceNorth);
		jobStatus = CLIENTNORTH.jobStatus(job.getJobIdentifier());

		assertNotNull(job);
		assertNotNull(jobStatus);
		assertTrue(jobStatus.getJobPrice() >= 0);
		assertEquals(validOrigin, jobStatus.getJobOrigin());
		assertEquals(validDestinationNorth, jobStatus.getJobDestination());
		assertEquals("PROPOSED", jobStatus.getJobState().toString());

		job = CLIENTSOUTH.requestJob(validOrigin, validDestinationSouth, validPriceSouth);
		jobStatus = CLIENTSOUTH.jobStatus(job.getJobIdentifier());

		assertNotNull(job);
		assertNotNull(jobStatus);
		assertTrue(jobStatus.getJobPrice() >= 0);
		assertEquals(validOrigin, jobStatus.getJobOrigin());
		assertEquals(validDestinationSouth, jobStatus.getJobDestination());
		assertEquals("PROPOSED", jobStatus.getJobState().toString());
	}
}
