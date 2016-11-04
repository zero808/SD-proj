package pt.upa.ca.ws;

import java.io.IOException;
import javax.xml.registry.JAXRException;
import javax.xml.ws.Endpoint;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

public class EndpointManager {

	private String uddiURL;
	private String name;
	private String url;

	Endpoint endpoint = null;
	UDDINaming uddiNaming = null;
	CaPort port = null;

	public EndpointManager(String uddiURL, String name, String url) {
		this.uddiURL = uddiURL;
		this.name = name;
		this.url = url;
		port = new CaPort();
	}

	public void start() throws JAXRException {

		endpoint = Endpoint.create(port);

		// publish endpoint
		System.out.printf("Starting %s%n", url);
		endpoint.publish(url);

		// publish to UDDI
		System.out.printf("Publishing '%s' to UDDI at %s%n", name, uddiURL);
		uddiNaming = new UDDINaming(uddiURL);
		uddiNaming.rebind(name, url);
	}

	public void importKeys() throws Exception {
		port.importKeys();
	}

	public void awaitConnections() throws IOException {
		// wait
		System.out.println("Awaiting connections");
		System.out.println("Press enter to shutdown");
		System.in.read();
	}

	public void stop() {
		try {
			if (endpoint != null) {
				// stop endpoint
				endpoint.stop();
				System.out.printf("Stopped %s%n", url);
			}
		} catch (Exception e) {
			System.out.printf("Caught exception when stopping: %s%n", e);
		}
		try {
			if (uddiNaming != null) {
				// delete from UDDI
				uddiNaming.unbind(name);
				System.out.printf("Deleted '%s' from UDDI%n", name);
			}
		} catch (Exception e) {
			System.out.printf("Caught exception when deleting: %s%n", e);
		}
	}

}
