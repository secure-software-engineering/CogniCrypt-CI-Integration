package de.fraunhofer.iem.cognicrypt.testapp;

import java.math.BigInteger;
import java.security.GeneralSecurityException;

import javax.crypto.Cipher;

import org.bouncycastle.crypto.AsymmetricBlockCipher;
import org.bouncycastle.crypto.InvalidCipherTextException;
import org.bouncycastle.crypto.engines.RSAEngine;
import org.bouncycastle.crypto.params.RSAKeyParameters;
import org.bouncycastle.util.encoders.Hex;

public class Main {

	public static BigInteger mod = new BigInteger("b259d2d6e627a768c94be36164c2d9fc79d97aab9253140e5bf17751197731d6f7540d2509e7b9ffee0a70a6e26d56e92d2edd7f85aba85600b69089f35f6bdbf3c298e05842535d9f064e6b0391cb7d306e0a2d20c4dfb4e7b49a9640bdea26c10ad69c3f05007ce2513cee44cfe01998e62b6c3637d3fc0391079b26ee36d5", 16);
	public static BigInteger pubExp = new BigInteger("11", 16);

	public static void main(String... args) throws GeneralSecurityException, InvalidCipherTextException {
		Cipher instance = Cipher.getInstance("AES");
		instance.doFinal();
		doBouncyCastle();
	}

	public static void doBouncyCastle() throws InvalidCipherTextException {
		String edgeInput = "ff6f77206973207468652074696d6520666f7220616c6c20676f6f64206d656e";
		byte[] data = Hex.decode(edgeInput);
		RSAKeyParameters pubParameters = new RSAKeyParameters(false, mod, pubExp);
		AsymmetricBlockCipher eng = new RSAEngine();
		// missing init()
		byte[] cipherText = eng.processBlock(data, 0, data.length);
	}

}