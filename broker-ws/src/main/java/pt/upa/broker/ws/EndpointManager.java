package pt.upa.broker.ws;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;
import javax.xml.ws.Endpoint;
import javax.xml.ws.handler.Handler;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.transporter.ws.TransporterPortType;
import pt.upa.transporter.ws.TransporterService;
import pt.upa.transporter.ws.cli.TransporterClient;
import pt.upa.ws.handler.HeaderHandler;

public class EndpointManager {

	private String uddiURL;
	private String name;
	private String url;
	
	private Endpoint endpoint = null;
	private UDDINaming uddiNaming = null;
	private BrokerPort port = null; // save for fetchCompanies()

	public EndpointManager(String uddiURL, String name, String url) {
		this.uddiURL = uddiURL;
		this.name = name;
		this.url = url;
		
		port = new BrokerPort();
	}

	public void start() throws JAXRException {

		port.setSs(new SynchronizeState(uddiURL));
		endpoint = Endpoint.create(port);
	
		// publish endpoint
		System.out.printf("Starting %s%n", url);
		endpoint.publish(url);
		
		// publish to UDDI
		System.out.printf("Publishing '%s' to UDDI at %s%n", name, uddiURL);
		uddiNaming = new UDDINaming(uddiURL);
		uddiNaming.rebind(name, url);
	}

	@SuppressWarnings("rawtypes")
	private void configHandlers(List<Handler> handlers) {
		String path;
		path = "src/main/resources/UpaBroker.jks";

		for (Handler handler : handlers) {
			if (handler instanceof HeaderHandler) {
				((HeaderHandler) handler).init("UpaBroker", path, url, uddiURL);
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

	public void fetchCompanies() throws Exception {
		BindingProvider bindingProvider;
		ConcurrentHashMap<String, String> companies = new ConcurrentHashMap<String, String>();

		String ret = "";

		for (int i = 1; ret != null; ++i) {
			String companyName = "UpaTransporter" + i;
			ret = uddiNaming.lookup(companyName);
			
			if (ret != null) {
				System.out.println(companyName + " url is: " + uddiNaming.lookup(companyName));
				companies.put(companyName, ret);

				TransporterClient client = new TransporterClient(ret);
				bindingProvider = (BindingProvider) client.getPort();
				configHandlers(bindingProvider.getBinding().getHandlerChain());
				
				port.addTransporterCompanies(companyName, client);
				System.out.println("obtained " + companyName);
			} else {
				System.out.printf("Cant find %s\n", companyName);
				return;
			}
		}
	}

	public void checkMainServer(String uddiURL, String name) {
		
		String mainServerURL;
		try {
			mainServerURL = uddiNaming.lookup(name);
		} catch (JAXRException e) {
			System.out.println("Problem finding main server. Taking over.");
			return;
		}
		
		ServerStatusChecker ssc = new ServerStatusChecker(10000, mainServerURL);
		ssc.run();
		
		while(ssc.isServerUp()){
			// do nothing
		}
		
	}
	
	public BrokerPort getPort() {
		return port;
	}
	
	public void setPort(BrokerPort port) {
		this.port = port;
	}
	
}
