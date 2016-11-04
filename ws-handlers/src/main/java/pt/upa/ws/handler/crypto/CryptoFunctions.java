package pt.upa.ws.handler.crypto;

import java.security.InvalidKeyException;
import java.security.Key;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

public class CryptoFunctions {

	private String digestAlgorithm = "SHA-1";
	private String cipherAlgorithm = "RSA";

	public byte[] digest(byte[] plainBytes) {
		MessageDigest messageDigest = null;
		try {
			messageDigest = MessageDigest.getInstance(digestAlgorithm);
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		// System.out.println(messageDigest.getProvider().getInfo());
		messageDigest.update(plainBytes);
		byte[] digest = messageDigest.digest();

		return digest;
	}

	public byte[] cipherContent(byte[] input, Key key) {

		Cipher cipher = null;
		byte[] cipherBytes = null;

		try {
			
			cipher = Cipher.getInstance(cipherAlgorithm);
			cipher.init(Cipher.ENCRYPT_MODE, key);
			cipherBytes = cipher.doFinal(input);
			
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException
				| BadPaddingException | InvalidKeyException e) {
			e.printStackTrace();
		}

		return cipherBytes;
	}

	public byte[] decipherContent(byte[] input, Key key) {

		Cipher cipher = null;
		byte[] messageBytes = null;

		try {

			cipher = Cipher.getInstance(cipherAlgorithm);
			cipher.init(Cipher.DECRYPT_MODE, key);
			messageBytes = cipher.doFinal(input);

		} catch (IllegalBlockSizeException | BadPaddingException | InvalidKeyException | NoSuchAlgorithmException
				| NoSuchPaddingException e) {
			e.printStackTrace();
		}

		return messageBytes;
	}

}
