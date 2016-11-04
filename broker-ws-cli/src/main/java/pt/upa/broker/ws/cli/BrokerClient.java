package pt.upa.broker.ws.cli;

import java.util.Scanner;

import javax.xml.registry.JAXRException;

import com.sun.xml.ws.client.ClientTransportException;

import pt.upa.broker.ws.InvalidPriceFault_Exception;
import pt.upa.broker.ws.TransportView;
import pt.upa.broker.ws.UnavailableTransportFault_Exception;
import pt.upa.broker.ws.UnavailableTransportPriceFault_Exception;
import pt.upa.broker.ws.UnknownLocationFault_Exception;
import pt.upa.broker.ws.UnknownTransportFault_Exception;

public class BrokerClient {

	private FrontEnd frontEnd;

	public BrokerClient(String uddiURL, String wsName) {
		this.frontEnd = new FrontEnd(uddiURL, wsName);
	}
	
	public FrontEnd getFrontEnd() {
		return frontEnd;
	}

	public void handleRequests()
			throws NumberFormatException, InvalidPriceFault_Exception, UnavailableTransportFault_Exception,
			UnavailableTransportPriceFault_Exception, UnknownLocationFault_Exception, JAXRException {

		boolean end = false;
		String input, parts[], response;
		Scanner scan = new Scanner(System.in);
		TransportView tV;

		System.out.println("");
		System.out.println("  /@@   /@@                    /@@@@@@@                     /@@");
		System.out.println("  | @@  | @@                   | @@__  @@                   | @@");
		System.out
				.println("  | @@  | @@  /@@@@@@  /@@@@@@ | @@  \\ @@  /@@@@@@  /@@@@@@ | @@   /@@  /@@@@@@   /@@@@@@");
		System.out
				.println("  | @@  | @@ /@@__  @@|____  @@| @@@@@@@  /@@__  @@/@@__  @@| @@  /@@/ /@@__  @@ /@@__  @@");
		System.out.println(
				"  | @@  | @@| @@  \\ @@ /@@@@@@@| @@__  @@| @@  \\__/ @@  \\ @@| @@@@@@/ | @@@@@@@@| @@  \\__/");
		System.out.println("  | @@  | @@| @@  | @@/@@__  @@| @@  \\ @@| @@     | @@  | @@| @@_  @@ | @@_____/| @@");
		System.out.println("  |  @@@@@@/| @@@@@@@/  @@@@@@@| @@@@@@@/| @@     |  @@@@@@/| @@ \\  @@|  @@@@@@@| @@");
		System.out.println("   \\______/ | @@____/ \\_______/|_______/ |__/      \\______/ |__/  \\__/ \\_______/|__/");
		System.out.println("            | @@");
		System.out.println("            | @@");
		System.out.println("            |__/");

		System.out.println("Input something. ex 'operation arg1 arg2'");
		System.out.println("Type \"help\" for a list of available commands. Press enter to exit.");
		while (!end) {
			System.out.print("UpaBroker % ");
			input = scan.nextLine();
			parts = input.split(" ");
			response = null;
			tV = null;

			try {

				switch (parts[0]) {

				case "request":
					if ((parts.length - 1) != 3) {
						System.out.println("Invalid request. Correct input is: 'request origin destination price'");
						break;
					}
					try {
						response = frontEnd.requestTransport(parts[1], parts[2], Integer.parseInt(parts[3]));
					} catch (UnknownLocationFault_Exception e) {
						System.out.println("'" + e.getFaultInfo().getLocation() + "' is an unknown location.");
						break;
					} catch (ClientTransportException e){
						throw e;
					}
					catch (Exception e) {
						System.out.println(e.getMessage());
						break;
					}

					if (response == null) {
						System.out.println("No one accepted your request.");
						break;
					}

					System.out.println("Request registered. Your request's id is '" + response + "'");

					break;
				case "view":
					if ((parts.length - 1) != 1) {
						System.out.println("Invalid request. Correct input is: 'view transportId'");
						break;
					}
					try {
						tV = frontEnd.viewTransport(parts[1]);
					} catch (UnknownTransportFault_Exception e) {
						System.out.println(e.getMessage());
						break;
					}

					System.out.println("\n>>>  Transport with id " + tV.getId() + "  <<<");
					System.out.println("Served by      : " + tV.getTransporterCompany());
					System.out.println("Current status : " + tV.getState().toString() + "\n");

					break;
				case "list":
					if ((parts.length - 1) != 0) {
						System.out.println("Invalid request. Correct input is: 'list'");
						break;
					}
					for (TransportView transport : frontEnd.listTransports()) {
						System.out.println("\n>>>  Transport with id " + transport.getId() + "  <<<");
						System.out.println("Served by      : " + transport.getTransporterCompany());
						System.out.println("Current status : " + transport.getState().toString() + "\n");
					}
					break;
				case "clear":
					if ((parts.length - 1) != 0) {
						System.out.println("Invalid request. Correct input is: 'clear'");
						break;
					}

					frontEnd.clearTransports();
					break;
				case "ping":
					if ((parts.length - 1) != 1) {
						System.out.println("Invalid request. Correct input is: 'ping text'");
						break;
					}

					System.out.println(frontEnd.ping(parts[1]));
					break;
				case "":
				case "exit":
				case "quit":
					System.out.println("Goodbye.");
					end = true;
					break;
				case "kill":
					frontEnd.kill();
					break;
				case "help":
					System.out.println("To request a transport: 'request <origin> <destination> <price>'");
					System.out.println("To view a transport: 'view <Transport Number>'");
					System.out.println("To clear the list of transports: 'clear'");
					System.out.println("To list all the transports registered: 'list'");
					System.out.println("To exit the program: 'quit'");
				default:
					System.out.println("Unknown command.");
					break;
				}
			} catch (ClientTransportException e) {
				System.out.println("Request timed out!");
				try {
					System.out.println("Waiting for 10seconds.");
					try {
						Thread.sleep(10000);
					} catch (InterruptedException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
					frontEnd.lookup();
				} catch (JAXRException e2) {
					System.out.println("Could't find ws. Exiting.");
					break;
				}
			}
		}
		scan.close();
	}
	
}
