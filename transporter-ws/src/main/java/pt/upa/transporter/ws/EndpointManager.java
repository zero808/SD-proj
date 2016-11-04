package pt.upa.transporter.ws;

import java.io.IOException;
import java.util.List;

import javax.xml.registry.JAXRException;
import javax.xml.ws.Endpoint;
import javax.xml.ws.handler.Handler;
import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.ws.handler.HeaderHandler;

public class EndpointManager {

	private String uddiURL;
	private String name;
	private String url;

	Endpoint endpoint = null;
	UDDINaming uddiNaming = null;

	public EndpointManager(String uddiURL, String name, String url) {
		this.uddiURL = uddiURL;
		this.name = name;
		this.url = url;
	}

	public void start() throws JAXRException {

		TransporterPort port = new TransporterPort();
		port.setCompanyName(name);
		port.setType(name);
		endpoint = Endpoint.create(port);

		// publish endpoint
		System.out.printf("Starting %s%n", url);
		endpoint.publish(url);

		configHandlers(endpoint.getBinding().getHandlerChain());
		
		// publish to UDDI
		System.out.printf("Publishing '%s' to UDDI at %s%n", name, uddiURL);
		uddiNaming = new UDDINaming(uddiURL);
		uddiNaming.rebind(name, url);
	}

	@SuppressWarnings("rawtypes")
	private void configHandlers(List<Handler> handlers) {
		String path;
		int i = Integer.parseInt(name.replaceAll("\\D+", ""));
		path = String.format("src/main/resources/UpaTransporter%d.jks", i);

		for (Handler handler : handlers) {
			if (handler instanceof HeaderHandler) {
				((HeaderHandler) handler).init(name, path, url, uddiURL);
			}
		}
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
