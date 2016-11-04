package pt.upa.broker.ws.it;

import java.io.IOException;
import java.util.Properties;

import org.junit.AfterClass;
import org.junit.BeforeClass;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.ca.ws.cli.CaClient;
import pt.upa.transporter.ws.cli.TransporterClient;

/**
 * Integration Test suite abstract class
 */
public class AbstractIT {

	private static final String TEST_PROP_FILE = "/test.properties";

	private static Properties PROPS;
	protected static TransporterClient CLIENT1;
	protected static TransporterClient CLIENT2;
	
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

		System.out.println(uddiEnabled);
		
		if ("true".equalsIgnoreCase(uddiEnabled)) {
			UDDINaming naming = new UDDINaming(uddiURL);
			String endpointAddress = naming.lookup("UpaTransporter1");
			CLIENT1 = new TransporterClient(endpointAddress);
			
			endpointAddress = naming.lookup("UpaTransporter2");
			CLIENT2 = new TransporterClient(endpointAddress);
		}
	}

	@AfterClass
	public static void cleanup() {
		CLIENT1 = null;
		CLIENT2 = null;
	}

}
