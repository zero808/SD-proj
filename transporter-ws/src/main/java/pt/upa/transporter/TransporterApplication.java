package pt.upa.transporter;

import pt.upa.transporter.ws.EndpointManager;

public class TransporterApplication {

	public static void main(String[] args) throws Exception {

		// Check arguments
		if (args.length < 3) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s uddiURL wsName wsURL%n",
					TransporterApplication.class.getName());
			return;
		}

		String uddiURL = args[0];
		String name = args[1];
		String url = args[2];

		EndpointManager eM = new EndpointManager(uddiURL, name, url);

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
