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
public class ViewTransportTest {

	@Mocked
	private TransporterClient tC;

	private BrokerPort bP;
	private JobView responseJob;
	private TransportView tV;
	private static int jobPrice = 25;
	private static int transportPrice = 50;
	private static int negativePrice = -15;
	private static String transportId = "0";
	private static String unknownTransportId = "3";
	private static String jobId = "1";
	private static String companyName = "UpaTransporter1";
	private static String southLocation = "Beja";
	private static String centralLocation = "Lisboa";
	private static String northLocation = "Porto";
	private static String unknownLocation = "Baleizão";

	@Before
	public void setUp() {
		responseJob = new JobView();
		responseJob.setCompanyName(companyName);
		responseJob.setJobState(JobStateView.ACCEPTED);
		responseJob.setJobPrice(jobPrice);
		responseJob.setJobIdentifier(jobId);

		tV = new TransportView();
		tV.setOrigin(southLocation);
		tV.setDestination(centralLocation);
		tV.setId(transportId);
		tV.setPrice(transportPrice);
		tV.setState(TransportStateView.BOOKED);
		tV.setTransporterCompany(companyName);

		bP = new BrokerPort();
		bP.addTransporterCompanies(companyName, tC);
		bP.getRequestedJobs().put(tV, responseJob);
	}

	@After
	public void tearDown() {
		bP = null;
		tV = null;
		responseJob = null;
	}

	// tests

	@Test(expected=UnknownTransportFault_Exception.class)
	public void unknownTransportTest() throws UnknownTransportFault_Exception{
		bP.viewTransport(unknownTransportId);
	}
	
	@Test
	public void successTest() throws UnknownTransportFault_Exception {

		new Expectations() {
			{
				tC.jobStatus(jobId);
				result = responseJob;
			}
		};
		assertTrue(bP.viewTransport(transportId).getState().toString()
				.equals("BOOKED"));
	}

}