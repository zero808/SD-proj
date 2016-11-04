
package pt.upa.ca.ws;

public class BadNameFault_Exception extends Exception {

	private BadNameFault faultInfo;

	public BadNameFault_Exception(String message, BadNameFault faultInfo) {
		super(message);
		this.faultInfo = faultInfo;
	}

	public BadNameFault_Exception(String message, BadNameFault faultInfo, Throwable cause) {
		super(message, cause);
		this.faultInfo = faultInfo;
	}

	public BadNameFault getFaultInfo() {
		return faultInfo;
	}

}
