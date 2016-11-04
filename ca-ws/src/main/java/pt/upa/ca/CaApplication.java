package pt.upa.ca;

import pt.upa.ca.ws.EndpointManager;

public class CaApplication {

	public static void main(String[] args) throws Exception {

		// Check arguments
		if (args.length < 3) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s uddiURL wsName wsURL%n",
					CaApplication.class.getName());
			return;
		}

		String uddiURL = args[0];
		String name = args[1]; 
		String url = args[2];

		EndpointManager eM = new EndpointManager(uddiURL, name, url);
		
		try {
		eM.importKeys();
		} catch (Exception e) {
			System.out.println("Problem importing cerificates. Exiting.");
			e.printStackTrace();
			return;
		}

		try {

			eM.start();
			eM.awaitConnections();

		} catch (Exception e) {
			System.out.printf("Caught exception: %s%n", e);
			e.printStackTrace();

		} finally {
			eM.stop();
		}

	}

}
