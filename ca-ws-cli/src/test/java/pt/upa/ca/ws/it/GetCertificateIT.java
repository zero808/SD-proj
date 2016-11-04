package pt.upa.ca.ws.it;

import static org.junit.Assert.assertNotNull;

import org.junit.Test;

import pt.upa.ca.ws.BadNameFaultException_Exception;

/**
 * Test suite
 */
public class GetCertificateIT extends AbstractIT {

	private String[] companies = { "UpaBroker", "UpaTransporter1", "UpaTransporter2" };
	private String nonExistingCompany = "UpaUpa";

	// tests

	@Test(expected = BadNameFaultException_Exception.class)
	public void nonExistingNameTest() throws BadNameFaultException_Exception {
		CLIENT.getCertificate(nonExistingCompany);
	}

	@Test
	public void successTest() throws BadNameFaultException_Exception {
		for (String string : companies) {
			assertNotNull(CLIENT.getCertificate(string));
		}
	}

}
