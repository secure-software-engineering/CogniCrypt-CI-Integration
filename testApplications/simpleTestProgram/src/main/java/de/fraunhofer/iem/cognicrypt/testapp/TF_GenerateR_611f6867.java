package de.fraunhofer.iem.cognicrypt.testapp;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.SecureRandom;
import java.security.spec.RSAKeyGenParameterSpec;

public class TF_GenerateR_611f6867 {
	private KeyPair keyPair;
	
	public TF_GenerateR_611f6867(Integer keySizeBits) throws Exception {
		final KeyPairGenerator generator = KeyPairGenerator.getInstance("RSA");
		final RSAKeyGenParameterSpec parameters = new RSAKeyGenParameterSpec(keySizeBits, RSAKeyGenParameterSpec.F4);
		generator.initialize(parameters, new SecureRandom());
		keyPair = generator.generateKeyPair();
	}
	
	public KeyPair getKeyPair() {
		return keyPair;
	}
	
}