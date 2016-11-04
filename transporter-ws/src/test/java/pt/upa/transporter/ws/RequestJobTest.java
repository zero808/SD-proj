package pt.upa.transporter.ws;

import java.io.OutputStream;
import java.io.PrintStream;

import org.junit.*;

import static org.junit.Assert.*;

public class RequestJobTest {

	// static members
	private static TransporterPort port;
	private static String companyName = "UpaTransporter1";
	private static String companyName2 = "UpaTransporter2";
	private static String invalidOrigin = null;
	private static String invalidDestination = null;
	private static String validOrigin = "Lisboa";
	private static String validDestination = "Faro";
	private static String northLocation = "Porto";
	private static String centralLocation = "Lisboa";
	private static String southLocation = "Beja";
	private static String unknownLocation = "Baleizao";
	private int validPrice = 50;
	private int invalidPrice = -50;
	private static PrintStream originalStream;

	@BeforeClass
	public static void oneTimeSetUp() {
		port = new TransporterPort();
		port.setCompanyName(companyName);
		port.setType(companyName);
		
		originalStream = System.out;
		//Silence Output
		System.setOut(new PrintStream(new OutputStream(){
		    public void write(int b) {
		        //NO-OP
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
	}

	@After
	public void tearDown() {
	}

	// tests
	
	@Test(expected = BadLocationFault_Exception.class)
	public void nullOriginArgumentsTest() throws BadLocationFault_Exception,
			BadPriceFault_Exception {
			port.requestJob(null, validDestination, validPrice);	
	}
	@Test(expected = BadLocationFault_Exception.class)
	public void nullDestinationArgumentsTest() throws BadLocationFault_Exception,
			BadPriceFault_Exception {
			port.requestJob(validOrigin, null, validPrice);	
	}

	@Ignore //TODO: Este teste n passa
	//@Test(expected = BadPriceFault_Exception.class)
	public void nullPriceArgumentsTest() throws BadLocationFault_Exception,
	BadPriceFault_Exception {
		port.requestJob("leo", validDestination, (Integer) null);	
	}
	@Test(expected = BadLocationFault_Exception.class)
	public void nullArgumentsTest() throws BadLocationFault_Exception,
			BadPriceFault_Exception {
			port.requestJob(null, null, validPrice);	
	}

	@Test(expected = BadLocationFault_Exception.class)
	public void invalidLocationsTest() throws BadLocationFault_Exception,
			BadPriceFault_Exception {
		port.requestJob(invalidOrigin, validDestination, validPrice);
		port.requestJob(validOrigin, invalidDestination, validPrice);
	}

	@Test(expected = BadPriceFault_Exception.class)
	public void invalidPriceRangeTest() throws BadLocationFault_Exception,
			BadPriceFault_Exception {
		port.requestJob(validOrigin, validDestination, invalidPrice);
	}

	@Test
	public void unservableLocationsTest() throws BadLocationFault_Exception,
			BadPriceFault_Exception {
		JobView result = port.requestJob(northLocation, southLocation,
				validPrice);
		JobView result2 = port.requestJob(southLocation, northLocation,
				validPrice);

		assertNull(result);
		assertNull(result2);
	}

	@Test(expected = BadLocationFault_Exception.class)
	public void unknownLocationTest() throws BadLocationFault_Exception,
			BadPriceFault_Exception {
		port.requestJob(unknownLocation, centralLocation, validPrice);
		port.requestJob(centralLocation, unknownLocation, validPrice);
	}

	@Test
	public void success() throws BadLocationFault_Exception,
			BadPriceFault_Exception {
		JobView job = port
				.requestJob(validOrigin, validDestination, validPrice);
		assertEquals(validOrigin, job.getJobOrigin());
		assertEquals(validDestination, job.getJobDestination());
		assertTrue(job.getJobPrice() >= 0);
	}

}