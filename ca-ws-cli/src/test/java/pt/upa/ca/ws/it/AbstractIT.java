package pt.upa.ca.ws.it;

import java.io.IOException;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.ca.ws.cli.CaClient;

/**
 * Integration Test suite abstract class
 */
public class AbstractIT {

	private static final String TEST_PROP_FILE = "/test.properties";

	private static Properties PROPS;
	protected static CaClient CLIENT;
	
	@BeforeClass
	public static void oneTimeSetup() throws Exception {
		PROPS = new Properties();
		try {
			PROPS.load(AbstractIT.class.getResourceAsStream(TEST_PROP_FILE));
		} catch (IOException e) {
			final String msg = String.format("Could not load properties file {}", TEST_PROP_FILE);
			System.out.println(msg);
			throw e;
		}
		String uddiEnabled = PROPS.getProperty("uddi.enabled");
		String uddiURL = PROPS.getProperty("uddi.url");
		String wsName = PROPS.getProperty("ws.name");

		if ("true".equalsIgnoreCase(uddiEnabled)) {
			System.out.println(wsName);
			System.out.println(uddiURL);
			UDDINaming naming = new UDDINaming(uddiURL);
			String endpointAddress = naming.lookup(wsName);
			CLIENT = new CaClient(endpointAddress);
		}
	}

	@AfterClass
	public static void cleanup() {
		CLIENT = null;
	}

}
