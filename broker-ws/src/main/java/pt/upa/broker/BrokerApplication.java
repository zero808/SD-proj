package pt.upa.broker;

import javax.xml.registry.JAXRException;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.broker.ws.BrokerPort;
import pt.upa.broker.ws.EndpointManager;

public class BrokerApplication {

	public static void main(String[] args) throws Exception {

		// Check arguments
		if (args.length < 5) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s uddiURL wsName wsURL replicaMode replicaURL%n",
					BrokerApplication.class.getName());
			return;
		}

		String uddiURL = args[0];
		String name = args[1];
		String url = args[2];
		boolean replica = Boolean.parseBoolean(args[3]);
		String replicaURL = args[4];

		EndpointManager eM = new EndpointManager(uddiURL, String.format("%sReplica", name), replicaURL);
		BrokerPort port = null;
		if (replica) {
			startReplicaServer(eM, uddiURL, name);
			port = eM.getPort();
		}

		eM = new EndpointManager(uddiURL, name, url);
		if (port != null)
			eM.setPort(port);

		startMainServer(eM);
	}

	private static void startReplicaServer(EndpointManager eM, String uddiURL, String name) {

		// Check if main server is actually alive. If not, takeover
		try {
			UDDINaming uddiNaming = new UDDINaming(uddiURL);
			if (uddiNaming.lookup(name) == null)
				return;
		} catch (JAXRException e1) {
			e1.printStackTrace();
		}

		try {
			eM.start();
			eM.fetchCompanies();
			eM.checkMainServer(uddiURL, name);
		} catch (Exception e) {
			System.out.printf("Caught exception: %s%n", e);
			e.printStackTrace();
		} finally {
			eM.stop();
		}
	}

	public static void startMainServer(EndpointManager eM) {

		try {

			eM.start();
			eM.fetchCompanies();
			eM.awaitConnections();

		} catch (Exception e) {
			System.out.printf("Caught exception: %s%n", e);
			e.printStackTrace();

		} finally {
			eM.stop();
		}
	}
}
