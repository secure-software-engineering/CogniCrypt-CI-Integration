package de.fraunhofer.iem.cognicrypt.testapp;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

import org.bouncycastle.crypto.engines.RSAEngine;

public class Main {
	public static void main(String... args) throws GeneralSecurityException{
		Cipher instance = Cipher.getInstance("AES");
		instance.doFinal();
		doBouncyCastle();
	}

	private static void doBouncyCastle() {
		RSAEngine rsaEngine = new RSAEngine();
		rsaEngine.init(false, null);
		rsaEngine.processBlock(new byte[8], 0,0);
		System.err.println(2);
	}

}