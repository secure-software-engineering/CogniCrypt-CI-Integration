package de.fraunhofer.iem.cognicrypt.testapp;

import java.nio.charset.StandardCharsets;
import java.security.KeyPair;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.util.Arrays;

import javax.crypto.Cipher;

public class TC_C0101RSA_32e9dc7e  {

	public void main() throws Exception {
		//final int rsaKeySize = expect().globalParameter("RsaKeySize").isInt().intValue();
		final int rsaKeySize = 1024;
		System.out.println("<RsaKeySize> is " + rsaKeySize + ".");

		TF_GenerateR_611f6867 kpg = new TF_GenerateR_611f6867(rsaKeySize);
		final KeyPair keyPair = kpg.getKeyPair();
		final RSAPrivateKey privateRsaKey = (RSAPrivateKey) keyPair.getPrivate();
		final RSAPublicKey publicRsaKey = (RSAPublicKey) keyPair.getPublic();
		System.out.println("Modulus is " + privateRsaKey.getModulus() + ".");
		System.out.println("Exponent of $PrivateRsaKey is " + privateRsaKey.getPrivateExponent() + ".");
		System.out.println("Exponent of $PublicRsaKey is " + publicRsaKey.getPublicExponent() + ".");

		final String testData = "my test data string";
		System.out.println("<TestData> is \"" + testData + "\".");
		final byte[] plaintext = testData.getBytes(StandardCharsets.UTF_8);
		System.out.println("$Plaintext is " + plaintext);

		final Cipher cipher = Cipher.getInstance("RSA");
		cipher.init(Cipher.ENCRYPT_MODE, publicRsaKey);
		final byte[] buffer = new byte[rsaKeySize / 8];
		final int encryptedLength = cipher.doFinal(plaintext, 0, plaintext.length, buffer, 0);
		final byte[] ciphertext = Arrays.copyOf(buffer, encryptedLength);
		System.out.println("$Ciphertext is " + ciphertext );

		cipher.init(Cipher.DECRYPT_MODE, privateRsaKey);
		final int decryptedLength = cipher.doFinal(ciphertext, 0, ciphertext.length, buffer, 0);
		final byte[] newPlaintext = Arrays.copyOf(buffer, decryptedLength);
		System.out.println("$NewPlaintext is " + newPlaintext);
	}
}
