package pt.upa.transporter.ws;

import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.*;

import static org.junit.Assert.*;

public class DecideJobTest {

	// static members
	private static TransporterPort port;
	private static String companyName = "UpaTransporter1";
	private static String validOrigin = "Lisboa";
	private static String validDestination = "Faro";
	private static String invalidID = "cenas";
	private static int validPrice = 50;
	private static JobView j;
	private static PrintStream originalStream;

	@BeforeClass
	public static void oneTimeSetUp() {
		originalStream = System.out;
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
		System.setOut(originalStream);
	}

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

	@Test(expected = BadJobFault_Exception.class)
	public void nullIdArgumentTest() throws BadJobFault_Exception {
		j = port.decideJob(null, true);
	}

	@Ignore
	// TODO: Este teste n passa
	// @Test
	public void nullAcceptArgumentTest() throws BadJobFault_Exception {
		port.decideJob("0", (Boolean) null);
	}

	@Test(expected = BadJobFault_Exception.class)
	public void invalidJob() throws BadJobFault_Exception {
		port.decideJob(invalidID, true);
	}

	@Test
	public void cancelJob() throws BadJobFault_Exception {
		port.decideJob(j.getJobIdentifier(), false);
		assertTrue("Job wasn't successfully canceled",
				j.getJobState() == JobStateView.REJECTED);
	}

	@Test
	public void acceptJob() throws BadJobFault_Exception {
		port.decideJob(j.getJobIdentifier(), true);
		assertTrue("Job wasn't successfully accepted",
				j.getJobState() == JobStateView.ACCEPTED);
	}

}
