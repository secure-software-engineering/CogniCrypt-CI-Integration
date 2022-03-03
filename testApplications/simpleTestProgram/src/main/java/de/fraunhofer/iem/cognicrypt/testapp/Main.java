package de.fraunhofer.iem.cognicrypt.testapp;

import java.math.BigInteger;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.util.encoders.Hex;

public class Main {

	public static void main(String... args) throws GeneralSecurityException, InvalidCipherTextException {
		Cipher instance = Cipher.getInstance("AES");
		instance.doFinal();
		doBouncyCastle();
	}

	public static void doBouncyCastle() throws InvalidCipherTextException {
		String edgeInput = "ff6f77206973207468652074696d6520666f7220616c6c20676f6f64206d656e";
		byte[] data = Hex.decode(edgeInput);
		AsymmetricBlockCipher eng = new RSAEngine();
		// missing init()
		byte[] cipherText = eng.processBlock(data, 0, data.length);
		System.out.println(cipherText);
	}

}