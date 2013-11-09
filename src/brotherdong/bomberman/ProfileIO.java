package brotherdong.bomberman;

import java.io.*;

import java.security.*;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author Lawrence
 */
public final class ProfileIO {

	//TODO: select better extension
	public static final String EXT = ".dat";

	private static final byte[] key;
	private static final Key keySpec;

	static {
		//TODO: generate better key
		//must be exactly 16 bytes (for AES at least)
		//do not change during runtime
		key = new byte[] {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16};
		keySpec = new SecretKeySpec(key, "AES");
	}

	public static void writeProfile(Profile profile)
		throws IOException, GeneralSecurityException {

		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.ENCRYPT_MODE, keySpec);

		ObjectOutputStream objOut = null;
		try {
			//Wrap file output in cipher and object outputs
			File file = new File(profile.getName() + EXT);
			objOut = new ObjectOutputStream(
				new CipherOutputStream(new FileOutputStream(file), cipher));

			objOut.writeObject(profile);
			objOut.close();
		} finally {
			if (objOut != null) objOut.close();
		}
	}

	public static Profile readProfile(File file)
		throws ClassNotFoundException, IOException, GeneralSecurityException {

		Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
		cipher.init(Cipher.DECRYPT_MODE, keySpec);

		ObjectInputStream objIn = null;
		try {
			//Wrap file input in cipher and object inputs
			objIn = new ObjectInputStream(
				new CipherInputStream(new FileInputStream(file), cipher));

			return (Profile) objIn.readObject();
		} finally {
			if (objIn != null) objIn.close();
		}
	}

//	private static KeyPair generateKeys() throws NoSuchAlgorithmException {
//		KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("AES");
//		KeyPair keyPair = keyPairGenerator.generateKeyPair();
//		return keyPair;
//	}

//	private static byte[] encryptString(String input) throws NoSuchAlgorithmException,
//				NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
//		Cipher rsa = Cipher.getInstance("AES/ECB/PKCS5Padding");
//		rsa.init(rsa.ENCRYPT_MODE, publicKey);
//		byte[] ciphertext = rsa.doFinal(input.getBytes());
//		return ciphertext;
//	}
//
//	private static String decryptString(String input, PublicKey publicKey, PrivateKey privateKey) throws NoSuchAlgorithmException,
//                NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException,
//                BadPaddingException {
//		Cipher rsa = Cipher.getInstance("AES/ECB/PKCS5Padding");
//		rsa.init(rsa.DECRYPT_MODE, privateKey);
//		return new String(rsa.doFinal(input.getBytes()));
//	}

//	private static byte[] getPublicKey(PublicKey publicKey) throws NoSuchAlgorithmException {
//		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//		byte[] key = publicKey.getEncoded();
//		return key;
//	}
//
//	private static byte[] getPrivateKey(PrivateKey privateKey) throws NoSuchAlgorithmException {
//		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//		byte[] key = privateKey.getEncoded();
//		return key;
//	}
//
//	private static PublicKey byteToPublicKey(byte[] key) throws NoSuchAlgorithmException, InvalidKeySpecException {
//		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//		KeySpec keySpec = new X509EncodedKeySpec(key);
//		PublicKey publicKey = keyFactory.generatePublic(keySpec);
//		return publicKey;
//	}
//
//	private static PrivateKey byteToPrivateKey(byte[] key) throws NoSuchAlgorithmException, InvalidKeySpecException {
//		KeyFactory keyFactory = KeyFactory.getInstance("RSA");
//		KeySpec keySpec = new X509EncodedKeySpec(key);
//		PrivateKey privateKey = keyFactory.generatePrivate(keySpec);
//		return privateKey;
//	}
}