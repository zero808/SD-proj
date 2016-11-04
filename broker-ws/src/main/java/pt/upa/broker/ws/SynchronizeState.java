/**
 * 
 */
package pt.upa.broker.ws;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.Map;

import javax.xml.registry.JAXRException;
import javax.xml.ws.BindingProvider;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;

public class SynchronizeState implements Runnable {

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Runnable#run()
	 */

	private TransportView tv;
	private String jv_id;
	private String companyName;
	private String uddiURL;
	BrokerPortType port;
	private String jobState;
	private int jobPrice;
	private String jobOrigin;
	private String jobDestination;

	public SynchronizeState(String uddiURL) {
		this.uddiURL = uddiURL;
		init();
	}

	public void set(TransportView tv, String companyName, String jv_id, String jobState, int jobPrice, String jobOrigin,
			String jobDestination) {
		this.tv = tv;
		this.companyName = companyName;
		this.jv_id = jv_id;
		this.jobState = jobState;
		this.jobPrice = jobPrice;
		this.jobOrigin = jobOrigin;
		this.jobDestination = jobDestination;
	}

	private void init() {
		try {
			System.out.println(uddiURL);
			UDDINaming uddi = new UDDINaming(uddiURL);
			String replicaURL = uddi.lookup("UpaBrokerReplica");

			if (replicaURL == null) {
				System.out.println("No replica found.");
				port = null;
				return;
			}

			BrokerService service = new BrokerService();
			port = service.getBrokerPort();

			BindingProvider bindingProvider = (BindingProvider) port;
			Map<String, Object> requestContext = bindingProvider.getRequestContext();

			requestContext.put(ENDPOINT_ADDRESS_PROPERTY, replicaURL);
		} catch (JAXRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void run() {

		if (port == null){
			init();
		}

		if (port == null) {
			System.out.println("Replica isnt running anywhere.");
			return;
		}

		try {
			port.update(tv, companyName, jv_id, jobState, jobPrice, jobOrigin, jobDestination);
		} catch (Exception e) {
			System.out.println("Replica appears to be unresponsive");
			port = null;
		}
	}

	public void syncClear() {
		if (port == null){
			init();
		}

		if (port == null) {
			System.out.println("Replica isnt running anywhere.");
			return;
		}

		try {
			port.syncClear();
		} catch (Exception e) {
			System.out.println("Replica appears to be unresponsive");
			port = null;
		}
	}

}
