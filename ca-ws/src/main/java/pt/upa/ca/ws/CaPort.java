package pt.upa.ca.ws;

import static javax.xml.bind.DatatypeConverter.parseBase64Binary;
import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.cert.Certificate;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import javax.jws.WebService;

@WebService(endpointInterface = "pt.upa.ca.ws.CaPortType", name = "CaWebService", portName = "CaPort", targetNamespace = "http://ws.ca.upa.pt/", serviceName = "CaService")
public class CaPort implements CaPortType {

	private KeyStore keyStore = null;
	private PrivateKey privKey = null;
	private String[] companies = { "UpaBroker", "UpaTransporter1", "UpaTransporter2" };
	private String KsPassword = "ins3cur3";

	public byte[] getCertificate(String name) throws BadNameFault_Exception {

		System.out.println("Received a request for " + name + "'s certificate");
		Certificate cert = null;

		if (name == null) {
			BadNameFault faultInfo = new BadNameFault();
			faultInfo.setName(name);
			throw new BadNameFault_Exception("Name is invalid", faultInfo);
		}

		try {
			cert = keyStore.getCertificate(name);
		} catch (KeyStoreException e) {
			return null;
		}

		if (cert == null) {
			BadNameFault faultInfo = new BadNameFault();
			faultInfo.setName(name);
			throw new BadNameFault_Exception("Name is invalid", faultInfo);
		}

		try {
			return cert.getEncoded();
		} catch (CertificateEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	public void importKeys() throws Exception {

		KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
		ks.load(null, KsPassword.toCharArray());

		// Import Ca's private key
		// importPrivateKey();

		// Import companies' certificates
		for (String company : companies) {

			Certificate cert = importCertificate(company);

			// Sign the certificate
			signCertificate(cert);
			ks.setCertificateEntry(company, cert);
		}

		keyStore = ks;

	}

	private void importPrivateKey() {
		// chave privada em src/main/resources/ca-key.pem.txt
		String keyString = null;
		try {
			keyString = new String(Files.readAllBytes(Paths.get("src/main/resources/ca-key.pem.txt")));
		} catch (IOException e) {
			System.out.println("Private key not found");
			e.printStackTrace();
		}

		// Remove the first and last lines
		keyString = keyString.replace("-----BEGIN ENCRYPTED PRIVATE KEY-----", "");
		keyString = keyString.replace("-----END ENCRYPTED PRIVATE KEY-----", "");
		keyString = keyString.replaceAll("\\r\\n|\\r|\\n", "");
		System.out.println("key here:\n" + keyString);

		// Base64 decode the data
		byte[] encoded = parseBase64Binary(keyString);
		System.out.println("encoded size:" + encoded.length);

		// PKCS8 decode the encoded RSA private key
		try {

			PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(encoded);
			KeyFactory kf = KeyFactory.getInstance("RSA");
			privKey = kf.generatePrivate(spec);

		} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// Display the results
		System.out.println("private key is:\n" + privKey.toString());

	}

	private Certificate importCertificate(String company) throws Exception {
		String file = String.format("src/main/resources/%s.cer", company);
		FileInputStream fis = new FileInputStream(file);
		BufferedInputStream bis = new BufferedInputStream(fis);

		CertificateFactory cf = CertificateFactory.getInstance("X.509");
		Certificate cert = null;

		while (bis.available() > 0) {
			cert = cf.generateCertificate(bis);
			System.out.println(cert.toString());
		}
		return cert;
	}

	public void signCertificate(Certificate cert) {
		System.out.println("Signing this certificate");
		// TODO
	}
}