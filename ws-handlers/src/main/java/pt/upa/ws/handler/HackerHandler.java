package pt.upa.ws.handler;

import java.security.InvalidKeyException;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;

/**
 * This SOAPHandler shows how to set/get values from headers in inbound/outbound
 * SOAP messages.
 *
 * A header is created in an outbound message and is read on an inbound message.
 *
 * The value that is read from the header is placed in a SOAP message context
 * property that can be accessed by other handlers or by the application.
 */
public class HackerHandler implements SOAPHandler<SOAPMessageContext> {

	private int counter = 0;
	private SOAPMessageContext oldMessage = null;

	public static final String CONTEXT_PROPERTY = "my.property";

	//
	// Handler interface methods
	//
	public Set<QName> getHeaders() {
		return null;
	}

	public boolean handleMessage(SOAPMessageContext smc) {
		Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		switch (counter) {
		case 0: // changed body
			oldMessage = smc;
			break;
		case 1: // repeated message
			smc.setMessage(oldMessage.getMessage());
			return true;
		}
		
		counter++;
		
		try {
			if (outboundElement.booleanValue())
				handleOutboundMessage(smc);
			else
				handleInboundMessage(smc);
			return true;

		} catch (Exception e) {
			System.out.print("Caught exception in handleMessage: ");
			System.out.println(e);
			System.out.println("Continue normal processing...");
		}

		return true;
	}

	private void handleOutboundMessage(SOAPMessageContext smc) throws SOAPException, InvalidKeyException {
	}

	private void handleInboundMessage(SOAPMessageContext smc) throws SOAPException, InvalidKeyException {
		System.out.println("Hacking CIA");

		// get SOAP envelope
		SOAPMessage msg = smc.getMessage();
		SOAPPart sp = msg.getSOAPPart();
		SOAPEnvelope se = sp.getEnvelope();

		// add header
		SOAPHeader sh = se.getHeader();
		if (sh == null)
			sh = se.addHeader();

		Name name = se.createName("hacker", "signature", "http://localhost:1337");
		se.getBody().addAttribute(name, "hacker").addTextNode("THIS WAS HACKED");

	}

	public boolean handleFault(SOAPMessageContext smc) {
		System.out.println("Ignoring fault message...");
		return true;
	}

	public void close(MessageContext messageContext) {
	}

}