package pt.upa.ca.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Map;

import javax.xml.ws.BindingProvider;

import pt.upa.ca.ws.BadNameFaultException_Exception;
import pt.upa.ca.ws.CaPortType;
import pt.upa.ca.ws.CaService;

public class CaClient implements CaPortType {

	private CaPortType port;

	public CaClient(String url) {
		
		CaService service = new CaService();
		CaPortType port = service.getCaPort();

		System.out.println("Setting endpoint address ...");
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider
				.getRequestContext();
		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, url);
		
		
		this.port = port;
	}

	@Override
	public byte[] getCertificate(String arg0) throws BadNameFaultException_Exception {
		return port.getCertificate(arg0);
	}

	public Certificate retrieveCertificate(String name) {
		byte[] bytes;
		X509Certificate cert = null;
		CertificateFactory certFactory;
		
		try {
			bytes = port.getCertificate(name);
			certFactory = CertificateFactory.getInstance("X.509");
			InputStream in = new ByteArrayInputStream(bytes);
			cert = (X509Certificate) certFactory.generateCertificate(in);
		} catch (Exception e) {
			System.out.println("Problem retrieving Certificate");
			return null;
		}

		return cert;
	}

}
