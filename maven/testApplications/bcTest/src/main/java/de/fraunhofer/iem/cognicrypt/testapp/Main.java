package de.fraunhofer.iem.cognicrypt.testapp;

import org.bouncycastle.crypto.engines.RSAEngine;

public class Main {
	public static void main(String... args) {
		RSAEngine rsaEngine = new RSAEngine();
		//rsaEngine.init(false, null);
		rsaEngine.processBlock(new byte[8], 0,0);
		System.err.println(2);
	}
}