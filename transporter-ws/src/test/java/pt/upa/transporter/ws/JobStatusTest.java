package pt.upa.transporter.ws;

import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.*;

import static org.junit.Assert.*;

public class JobStatusTest {

	// static members
	private static TransporterPort port;
	private static String companyName = "UpaTransporter1";
	private static String validOrigin = "Lisboa";
	private static String validDestination = "Faro";
	private static String invalidID = "cenas";
	private static int validPrice = 50;
	private static JobView j;

	// one-time initialization and clean-up

	@BeforeClass
	public static void oneTimeSetUp() {
		// Silence Output
		System.setOut(new PrintStream(new OutputStream() {
			public void write(int b) {
				// NO-OP
			}
		}));
	}

	@AfterClass
	public static void oneTimeTearDown() {
		port = null;
	}

	// initialization and clean-up for each test

	@Before
	public void setUp() {
		port = new TransporterPort();
		port.setCompanyName(companyName);
		port.setType(companyName);

		try {
			j = port.requestJob(validOrigin, validDestination, validPrice);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@After
	public void tearDown() {
		j = null;
		port = null;
	}

	// tests

	@Test
	public void nullArgumentsTest() {
		assertNull("Response was not null", port.jobStatus(null));
	}

	@Test
	public void invalidJob() {
		assertNull("This id shouldn't exist", port.jobStatus(invalidID));
	}

	@Test
	public void proposedJob() throws BadLocationFault_Exception,
			BadPriceFault_Exception {
		j = port.requestJob(validOrigin, validDestination, validPrice);
		assertNotNull(j);
		assertTrue("Job was in the wrong map",
				j.getJobState() == JobStateView.PROPOSED);
	}

	@Test
	public void acceptedJob() throws BadLocationFault_Exception,
			BadPriceFault_Exception, BadJobFault_Exception {
		j = port.requestJob(validOrigin, validDestination, validPrice);
		assertNotNull(j);
		port.decideJob(j.getJobIdentifier(), true);
		assertTrue("Job was in the wrong map",
				j.getJobState() == JobStateView.ACCEPTED);
	}

}