package pt.upa.ca.ws;

import javax.jws.WebService;

@WebService
public interface CaPortType {

	public byte[] getCertificate(String name) throws BadNameFault_Exception;

}
