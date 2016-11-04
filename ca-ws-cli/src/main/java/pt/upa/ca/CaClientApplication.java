package pt.upa.ca;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.ca.ws.cli.CaClient;

public class CaClientApplication {

	public static void main(String[] args) throws Exception {
		System.out.println(CaClientApplication.class.getSimpleName()
				+ " starting...");

		// Check arguments
		if (args.length < 2) {
			System.err.println("Argument(s) missing!");
			System.err.printf("Usage: java %s uddiURL name%n",
					CaClientApplication.class.getName());
			return;
		}

		String uddiURL = args[0];
		String name = args[1];

		System.out.printf("Contacting UDDI at %s%n", uddiURL);
		UDDINaming uddiNaming = new UDDINaming(uddiURL);

		System.out.printf("Looking for '%s'%n", name);
		String endpointAddress = uddiNaming.lookup(name);

		if (endpointAddress == null) {
			System.out.println("Not found!");
			return;
		} else {
			System.out.printf("Found %s%n", endpointAddress);
		}

		System.out.println("Creating stub ...");
		

		try {
			CaClient cl = new CaClient(endpointAddress);
			
			String[] companies = {"UpaBroker","UpaTransporter1","UpaTransporter2"};
			
			for (String string : companies) {
				System.out.printf("%s's certificate is:\n%s%n", string, cl.retrieveCertificate(string).toString());
			}

		} catch (Exception pfe) {
			System.out.println("Caught: " + pfe);
		}
	}
}
