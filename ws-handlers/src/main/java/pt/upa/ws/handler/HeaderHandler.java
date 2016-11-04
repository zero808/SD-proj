package pt.upa.ws.handler;

import java.io.FileInputStream;
import java.io.StringWriter;
import java.security.InvalidKeyException;
import java.security.Key;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.SignatureException;
import java.security.UnrecoverableKeyException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.Base64;
import java.util.Iterator;
import java.util.Set;

import javax.xml.namespace.QName;
import javax.xml.registry.JAXRException;
import javax.xml.soap.Name;
import javax.xml.soap.SOAPBody;
import javax.xml.soap.SOAPElement;
import javax.xml.soap.SOAPEnvelope;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPHeader;
import javax.xml.soap.SOAPHeaderElement;
import javax.xml.soap.SOAPMessage;
import javax.xml.soap.SOAPPart;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.ws.handler.MessageContext;
import javax.xml.ws.handler.soap.SOAPHandler;
import javax.xml.ws.handler.soap.SOAPMessageContext;
import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;

import pt.ulisboa.tecnico.sdis.ws.uddi.UDDINaming;
import pt.upa.ca.ws.cli.CaClient;
import pt.upa.ws.handler.crypto.CryptoFunctions;

/**
 * This SOAPHandler shows how to set/get values from headers in inbound/outbound
 * SOAP messages.
 *
 * A header is created in an outbound message and is read on an inbound message.
 *
 * The value that is read from the header is placed in a SOAP message context
 * property that can be accessed by other handlers or by the application.
 */
public class HeaderHandler implements SOAPHandler<SOAPMessageContext> {

	public static final String CONTEXT_PROPERTY = "my.property";
	private String keyStorePath;
	private KeyStore ks = null;
	private String companyName = null;
	private String nameSpace;
	private String KsPassword = "ins3cur3";
	private String keyPassword = "1nsecure";
	private CaClient certAuth = null;
	private String uddiURL = null;
	private String caName = "UpaCa";
	private CryptoFunctions crypto;
	private boolean allowUnverifiedMessage = true;
	private int tolerance = 2; // in seconds

	public void init(String name, String path, String namespace, String uddiURL) {
		this.companyName = name;
		this.keyStorePath = path;
		this.nameSpace = namespace;
		this.uddiURL = uddiURL;
		this.crypto = new CryptoFunctions();

		try {
			importKeyStore();
		} catch (Exception e) {
			System.out.println("Problem importing private key.");
			e.printStackTrace();
		}

		certAuth = createCaClient();

		if (certAuth == null) {
			System.out.println("WARNING: CA webservice appears to be offline. Allowing unverified messages!");
			allowUnverifiedMessage = true;
		}

	}

	public CaClient createCaClient() {
		String endpointAddress = null;
		CaClient newClient = null;

		if (uddiURL == null) {
			System.out.println("I dont know where uddi is located");
			return null;
		}

		System.out.printf("Contacting UDDI at %s%n", uddiURL);
		UDDINaming uddiNaming;
		try {
			uddiNaming = new UDDINaming(uddiURL);

			System.out.printf("Looking for '%s'%n", caName);
			endpointAddress = uddiNaming.lookup(caName);

		} catch (JAXRException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		if (endpointAddress == null) {
			System.out.println("Not found!");
			return null;
		} else {
			System.out.printf("Found %s at %s%n", caName, endpointAddress);
		}

		try {
			newClient = new CaClient(endpointAddress);
		} catch (Exception pfe) {
			System.out.println("Caught: " + pfe);
		}

		return newClient;
	}

	private void importKeyStore() throws Exception {

		// ler o keystore do ficheiro privateKeyPath
		ks = KeyStore.getInstance("jks");
		ks.load(new FileInputStream(keyStorePath), KsPassword.toCharArray());

		if (ks != null) {
			System.out.println("Keystore imported correctly.");
		}
	}

	private void importCertificate(String name) {

		try {
			Certificate cert = ks.getCertificate(name);
			if (cert == null) {
				cert = certAuth.retrieveCertificate(name);

				if (cert == null) {
					System.out.printf("Cannot get %s's certificate from CA.", name);
					return;
				}

				try {
					System.out.println("Verifying certificate's signature.");
					cert.verify(ks.getCertificate("ca").getPublicKey());
				} catch (InvalidKeyException | CertificateException | NoSuchAlgorithmException | NoSuchProviderException
						| SignatureException e) {
					System.out.println("Cannot verify signature on received certificate.");
					return;
				}
			}

			ks.setCertificateEntry(name, cert);
		} catch (KeyStoreException e) {
			System.out.println("Problem storing certificate.");
			e.printStackTrace();
		}
	}

	private byte[] generateHeader(SOAPBody soapBody) throws InvalidKeyException, TransformerConfigurationException,
			TransformerException, TransformerFactoryConfigurationError {

		byte[] encryptedDigest = null;
		byte[] plainBytes = getBodyBytes(soapBody);
		byte[] digest = crypto.digest(plainBytes);

		if (plainBytes == null) {
			System.out.println("Couldn't find message body.");
			return null;
		}

		try {

			encryptedDigest = crypto.cipherContent(digest, ks.getKey(companyName, keyPassword.toCharArray()));

		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e) {
			System.out.println("Problem found while deciphering.");
			e.printStackTrace();
		}

		return encryptedDigest;
	}

	private byte[] getBodyBytes(SOAPBody soapBody) {
		try {

			DOMSource source = new DOMSource(soapBody);
			StringWriter stringResult = new StringWriter();
			TransformerFactory.newInstance().newTransformer().transform(source, new StreamResult(stringResult));
			String message = stringResult.toString();
			final byte[] plainBytes = message.getBytes();
			return plainBytes;

		} catch (TransformerException | TransformerFactoryConfigurationError e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	//
	// Handler interface methods
	//
	public Set<QName> getHeaders() {
		return null;
	}

	public boolean handleMessage(SOAPMessageContext smc) {
		Boolean outboundElement = (Boolean) smc.get(MessageContext.MESSAGE_OUTBOUND_PROPERTY);

		// If no keystore accept or reject depending on the setting
		if (ks == null) {
			System.out.println("No keystore found. Allowing messages through unverified.");
			return allowUnverifiedMessage;
		}

		try {
			if (outboundElement.booleanValue())
				handleOutboundMessage(smc);
			else
				return handleInboundMessage(smc);

		} catch (ReturnedMessageException e) {
			System.out.println("This is a returned message. Request was denied since message is not authentic or fresh.");
			try {
				smc.getMessage().getSOAPBody().addFault();
			} catch (SOAPException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return false;
			// TODO Ignore this rejected message
		} catch (Exception e) {
			System.out.print("Caught exception in handleMessage: ");
			System.out.println(e);
			System.out.println("Continue normal processing...");
		}

		return true;
	}

	private void handleOutboundMessage(SOAPMessageContext smc) throws SOAPException, InvalidKeyException {
		// System.out.println("Writing header in outbound SOAP message...");

		// get SOAP envelope
		SOAPMessage msg = smc.getMessage();
		SOAPPart sp = msg.getSOAPPart();
		SOAPEnvelope se = sp.getEnvelope();

		// add header
		SOAPHeader sh = se.getHeader();
		if (sh == null)
			sh = se.addHeader();

		// add emiting entity name
		Name name = se.createName(companyName, "from", nameSpace);
		SOAPHeaderElement element = sh.addHeaderElement(name);
		element.addTextNode(companyName);

		// Add the timestamp
		name = se.createName(companyName, "timestamp", nameSpace);
		element = sh.addHeaderElement(name);

		// Get current time for timestamp
		Date dt = new Date();
		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HHmmss");
		String timeString = DATE_FORMAT.format(dt) + "kk";
		byte[] timeBinary = Base64.getDecoder().decode(timeString);
		
		byte[] encodedTime = null;
		try {
			encodedTime = crypto.cipherContent(timeBinary, ks.getKey(companyName, keyPassword.toCharArray()));
		} catch (UnrecoverableKeyException | KeyStoreException | NoSuchAlgorithmException e1) {
			System.out.println("Problem generating timestamp");
		}
		element.addTextNode(printBase64Binary(encodedTime));

		// add header element (name, namespace prefix, namespace)
		name = se.createName(companyName, "signature", nameSpace);
		element = sh.addHeaderElement(name);

		// add signature to header
		try {

			byte[] encryptedDigest = generateHeader(msg.getSOAPBody());
			String header = printBase64Binary(encryptedDigest);
			element.addTextNode(header);

		} catch (TransformerException | TransformerFactoryConfigurationError e) {
			System.out.println("Problem generating signature");
		}

	}

	private boolean handleInboundMessage(SOAPMessageContext smc) throws SOAPException, ReturnedMessageException {
		// System.out.println("Reading header in inbound SOAP message...");

		// get SOAP envelope header
		SOAPMessage msg = smc.getMessage();
		SOAPPart sp = msg.getSOAPPart();
		SOAPEnvelope se = sp.getEnvelope();
		SOAPHeader sh = se.getHeader();

		// check header
		if (sh == null) {
			return allowUnverifiedMessage;
		}

		// get first header element
		Iterator it = sh.getChildElements();

		// check header element
		if (!it.hasNext()) {
			System.out.println("Header element not found.");
			return true;
		}
		SOAPElement element = (SOAPElement) it.next();

		// Get author's name, signature and serial
		String fromName = element.getValue();
		element = (SOAPElement) it.next();
		String timestamp = element.getValue();
		element = (SOAPElement) it.next();
		String encryptedDigest = element.getValue();

		// If this case happens it means the message has been returned
		if (fromName.equals(companyName)) {
			throw new ReturnedMessageException();
		}

		// Verificar se fromName existe na keystore, importar caso nao exista
		importCertificate(fromName);

		// Desencriptar encryptedDigest com chave publica do emissor
		Key key = null;
		try {
			key = ks.getCertificate(fromName).getPublicKey();
		} catch (KeyStoreException e) {
			System.out.printf("I don't have %s's certificate.\n", fromName);
			return false;
		}

		// Get current time for timestamp
		Date dt = new Date();
		SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("HHmmss");
		String timeString = DATE_FORMAT.format(dt);
		String timestampDecoded = printBase64Binary(crypto.decipherContent(parseBase64Binary(timestamp), key));
		int timeint = Integer.parseInt(timeString);
		int timeintDecoded = Integer.parseInt(timestampDecoded.replaceAll("k", ""));
		
		if (timeint > timeintDecoded + tolerance) {
			System.out.println("This message is not fresh.");
			return false;
		} else {
			System.out.println("Message is fresh");
		}

		// Calcular digest do body
		byte[] plainBytes = getBodyBytes(msg.getSOAPBody());
		byte[] digest = crypto.digest(plainBytes);

		if (plainBytes == null) {
			System.out.println("Couldn't find message body.");
			return false;
		}

		byte[] decryptedHeaderBytes = crypto.decipherContent(parseBase64Binary(encryptedDigest), key);
		if (Arrays.equals(digest, decryptedHeaderBytes)) {
			System.out.println("Message is authentic");
			return true;
		}

		System.out.println("Message is not authentic. Rejecting.");
		return false;
	}

	public boolean handleFault(SOAPMessageContext smc) {
		System.out.println("Ignoring fault message...");
		return true;
	}

	public void close(MessageContext messageContext) {
	}

}