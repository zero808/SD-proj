package pt.upa.transporter.ws.it;

import org.junit.*;

import pt.upa.transporter.ws.BadJobFault_Exception;
import pt.upa.transporter.ws.BadLocationFault_Exception;
import pt.upa.transporter.ws.BadPriceFault_Exception;
import pt.upa.transporter.ws.JobStateView;
import pt.upa.transporter.ws.JobView;
import static org.junit.Assert.*;

/**
 * Unit Test example
 * 
 * Invoked by Maven in the "test" life-cycle phase If necessary, should invoke
 * "mock" remote servers
 */
public class DecideJobIT extends AbstractIT{

	// tests

	@Test(expected = BadJobFault_Exception.class)
	public void invalidJob() throws BadJobFault_Exception {
		// it doesn't matter which company we test
		CLIENTNORTH.decideJob(invalidID, true);
	}

	@Test
	public void cancelJobSouth() throws BadJobFault_Exception, BadLocationFault_Exception, BadPriceFault_Exception {
		j = CLIENTSOUTH.requestJob(validOrigin, validDestinationSouth, validPriceSouth);
		assertNotNull(j);
		JobView jv = CLIENTSOUTH.decideJob(j.getJobIdentifier(), false);
		assertNotNull(jv);
		assertTrue("Job wasn't successfully canceled", jv.getJobState() == JobStateView.REJECTED);
	}

	@Test
	public void cancelJobNorth() throws BadJobFault_Exception, BadLocationFault_Exception, BadPriceFault_Exception {
		j = CLIENTNORTH.requestJob(validOrigin, validDestinationNorth, validPriceNorth);
		assertNotNull(j);
		JobView jv = CLIENTNORTH.decideJob(j.getJobIdentifier(), false);
		assertNotNull(jv);
		assertTrue("Job wasn't successfully canceled", jv.getJobState() == JobStateView.REJECTED);
	}
	
	@Test
	public void acceptJob() throws BadJobFault_Exception, BadLocationFault_Exception, BadPriceFault_Exception {
		j = CLIENTNORTH.requestJob(validOrigin, validDestinationNorth, validPriceNorth);
		assertNotNull(j);
		JobView jv = CLIENTNORTH.decideJob(j.getJobIdentifier(), true);
		assertNotNull(jv);
		assertTrue("Job wasn't successfully accepted", jv.getJobState() == JobStateView.ACCEPTED);
		
		j = CLIENTSOUTH.requestJob(validOrigin, validDestinationSouth, validPriceSouth);
		assertNotNull(j);
		jv = CLIENTSOUTH.decideJob(j.getJobIdentifier(), true);
		assertNotNull(jv);
		assertTrue("Job wasn't successfully accepted", jv.getJobState() == JobStateView.ACCEPTED);
	}

}
