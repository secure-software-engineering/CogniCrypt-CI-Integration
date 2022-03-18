package de.fraunhofer.iem.cognicrypt.testapp;

import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

public class Main {
	public static void main(String... args) throws GeneralSecurityException{
		Cipher instance = Cipher.getInstance("AES");
		instance.doFinal();
	}
}