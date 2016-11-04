package pt.upa.transporter.ws.it;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Properties;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.JobView;
import pt.upa.transporter.ws.cli.TransporterClient;

/**
 * Integration Test suite abstract class
 */
public class AbstractIT {

	private static final String TEST_PROP_FILE = "/test.properties";

	private static Properties PROPS;
	protected static TransporterClient CLIENTSOUTH;
	protected static TransporterClient CLIENTNORTH;

	// static members
	protected static String invalidOrigin = null;
	protected static String invalidDestination = null;
	protected static String validOrigin = "Lisboa";
	protected static String validDestinationSouth = "Faro";
	protected static String validDestinationNorth = "Porto";
	protected static String unknownLocation = "Baleizao";
	protected static String invalidID = "cenas";
	protected static String northLocation = "Porto";
	protected static String centralLocation = "Lisboa";
	protected static String southLocation = "Beja";	
	protected static int validPriceNorth = 50;
	protected static int validPriceSouth = 51;
	protected int validPrice = 50;
	protected int invalidPrice = -50;
	protected static JobView j;

	private static PrintStream originalStream;
	
	
	@BeforeClass
	public static void oneTimeSetup() throws Exception {
		silenceSysout();
		PROPS = new Properties();
		try {
			PROPS.load(AbstractIT.class.getResourceAsStream(TEST_PROP_FILE));
		} catch (IOException e) {
			final String msg = String.format("Could not load properties file {}", TEST_PROP_FILE);
			System.out.println(msg);
			throw e;
		}

		String uddiURL = PROPS.getProperty("uddi.url");
		String wsName = PROPS.getProperty("ws.name");

		UDDINaming naming = new UDDINaming(uddiURL);
		System.out.println(uddiURL);
		System.out.println(String.format("%s1", wsName));
		System.out.println(String.format("%s2", wsName));
String name = String.format("%s1", wsName);
		String endpointAddress = naming.lookup(name);
System.out.println(endpointAddress);
		CLIENTSOUTH = new TransporterClient(endpointAddress);

		endpointAddress = naming.lookup(String.format("%s2", wsName));
		System.out.println(endpointAddress);
		CLIENTNORTH = new TransporterClient(endpointAddress);

	}

	@AfterClass
	public static void cleanup() {
		restoreSysout();
		CLIENTSOUTH = null;
		CLIENTNORTH = null;
	}

	@Before
	public void setUp() {
	}

	@After
	public void tearDown() {
		j = null;
		CLIENTNORTH.clearJobs();
		CLIENTSOUTH.clearJobs();
	}
	
	private static void silenceSysout() {
		originalStream = System.out;
		// Silence Output
		System.setOut(new PrintStream(new OutputStream() {
			public void write(int b) {
				// NO-OP
			}
		}));
	}
	
	private static void restoreSysout(){
		System.setOut(originalStream);
	}

}
