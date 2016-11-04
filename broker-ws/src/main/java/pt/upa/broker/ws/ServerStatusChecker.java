package pt.upa.broker.ws;

import static javax.xml.ws.BindingProvider.ENDPOINT_ADDRESS_PROPERTY;

import java.util.Map;

import javax.xml.ws.BindingProvider;

public class ServerStatusChecker implements Runnable {

	private int interval;
	private boolean serverUp;
	BrokerPortType port = null;

	public ServerStatusChecker(int i, String mainServerURL) {
		this.interval = i;
		this.serverUp = true;

		BrokerService service = new BrokerService();
		port = service.getBrokerPort();

		BindingProvider bindingProvider = (BindingProvider) port;
		Map<String, Object> requestContext = bindingProvider.getRequestContext();
		requestContext.put(ENDPOINT_ADDRESS_PROPERTY, mainServerURL);

		if (port == null) {
			System.out.println("Something went wrong while getting main server's port.");
			return;
		}

	}

	public void run() {
		while (true) {

			// Check server status
			serverUp = getServerStatus();
			if (!serverUp)
				break;

			try {
				System.out.println("Its up. Going back to sleep...");
				Thread.sleep(interval);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	public boolean isServerUp() {
		return serverUp;
	}

	public void setServerUp(boolean serverUp) {
		this.serverUp = serverUp;
	}

	public boolean getServerStatus() {
		String heartBeat = null;

		try {
			heartBeat = port.ping("Hello");
		} catch (Exception e) {
			return false;
		}

		if (heartBeat == null)
			return false;

		return serverUp;
	}

}