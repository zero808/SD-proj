package pt.upa.broker.ws.cli;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.List;
import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;

import com.sun.xml.ws.client.ClientTransportException;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.ws.BrokerPortType;
import pt.upa.broker.ws.BrokerService;
import pt.upa.broker.ws.InvalidPriceFault_Exception;
import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;
import pt.upa.broker.ws.UnavailableTransportPriceFault_Exception;
import pt.upa.broker.ws.UnknownLocationFault_Exception;
import pt.upa.broker.ws.UnknownTransportFault_Exception;

public class FrontEnd implements BrokerPortType {

	private String uddiURL;
	private String wsName;
	private BrokerPortType port = null;

	public FrontEnd(String uddiURL, String name) {
		this.uddiURL = uddiURL;
		this.wsName = name;

		try {
			lookup();
		} catch (JAXRException e) {
			System.out.println("Problem finding server.");
			port = null;
		}
	}

	public void lookup() throws JAXRException {
		System.out.printf("Contacting UDDI at %s%n", uddiURL);
		UDDINaming uddiNaming = new UDDINaming(uddiURL);

		System.out.printf("Looking for '%s'%n", wsName);
		String endpointAddress = uddiNaming.lookup(wsName);

		if (endpointAddress == null) {
			System.out.println("Not found!");
			return;
		} else {
			System.out.printf("Found %s%n", endpointAddress);
		}

		System.out.println("Creating stub ...");
		BrokerService service = new BrokerService();
		port = service.getBrokerPort();

		System.out.println("Setting endpoint address ...");
		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, endpointAddress);
	}

	public String ping(String name) {
		try {
			return port.ping(name);
		} catch (ClientTransportException e) {
			try {
				System.out.println("Server appears to be down...");
				lookup();
			} catch (JAXRException e1) {
				System.out.println("Server is down!");
			}
		}
		return null;
	}

	public void kill() {
		port.kill();
	}

	public String requestTransport(String origin, String destination, int price)
			throws InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
			UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception {
		return port.requestTransport(origin, destination, price);
	}

	public TransportView viewTransport(String id) throws UnknownTransportFault_Exception {
		return port.viewTransport(id);
	}

	public List<TransportView> listTransports() {
		return port.listTransports();
	}

	public void clearTransports() {
		port.clearTransports();
	}

	@Override
	public void syncClear() {
		System.out.println("not for client's use!");
	}

	@Override
	public void update(TransportView tv, String companyName, String jvId, String jobState, int jobPrice,
			String jobOrigin, String jobDestination) {
		System.out.println("not for client's use!");
	}

}
