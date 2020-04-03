package ez.forum.util.AES;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import javax.faces.context.FacesContext;

/**
 * This class can encrypt and decrypt data using AES 256.
 * @author matafokka
 *
 */
public class AES {
	private static String pathToMasterKey = "/WEB-INF/classes/ez/forum/util/AES/AESMasterKey.txt";
	/*
	 * If you want to to hardcode the master key:
	 *	1. Type your master key in a field below.
	 *	2. Comment-out or remove static block after field below.
	 */
	public static String masterKey = "Type your master key here";
	
	static {
		try {
			masterKey = readMasterKey();
		} catch (FileNotFoundException e) {
			System.err.println(
					"ERROR: File \"" + pathToMasterKey + "\" not found.\n"
					+ "Download this file again and put it in a root directory of the project.\n"
					+ "Or create this file manually and put your master key into it."
					);
			System.exit(-1);
		} catch (IOException e) {
			System.err.println("ERROR: Can't read file \"" + pathToMasterKey + "\" because I/O error has occured. Please, deal with that file.\n"
					+ "Following error has occured: " + e.getMessage());
			System.exit(-1);
		}
	}
	
	/**
	 * Reads master key from the password.txt.
	 * @throws FileNotFoundException If passwords.txt does not exist.
	 * @throws IOException If an I/O error occurs.
	 */
	private static String readMasterKey() throws IOException, FileNotFoundException {
		BufferedReader reader;
		reader = new BufferedReader(
				new FileReader(
							FacesContext.
							getCurrentInstance().
							getExternalContext().
							getRealPath(pathToMasterKey)
						)
				);
		String toReturn = reader.readLine();
		reader.close();
		return toReturn;
	}
	
	/**
	 * Does all the stuff depending on a mode.
	 * @param text - text to process.
	 * @param password - password for encryption or decryption.
	 * @param mode - Cipher operation mode.
	 * @return
	 */
	private static byte[] doThing(byte[] text, String password, int mode) {
		Boolean doEncrypt = false;
		if (mode == Cipher.ENCRYPT_MODE) { doEncrypt = true; }
		else if (mode != Cipher.DECRYPT_MODE) {
			throw new IllegalArgumentException("Mode should be either Cipher.ENCRYPT_MODE or Cipher.DECRYPT_MODE");
		}
		
		Cipher c;
		SecureRandom secureRandom;
		try {
			c = Cipher.getInstance("AES/GCM/PKCS5Padding");
			secureRandom = SecureRandom.getInstanceStrong();
		} catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
			// Cifer should have these stuff. SecureRandom should have at least one strong algorithm.
			// If it doesn't, throw your JRE implementation away.
			return null;
		}
		
		// Generate IV and get params for AES
		byte[] iv;
		int ivSize = c.getBlockSize();
		// If we encrypt, generate IV
		if (doEncrypt) {
			iv = new byte[ivSize];
			secureRandom.nextBytes(iv);
		}
		// If we decrypt, cut IV from text
		else {
			iv = Arrays.copyOf(text, ivSize);
			text = Arrays.copyOfRange(text, ivSize, text.length);
		}
		IvParameterSpec ivParams = new IvParameterSpec(iv);
		GCMParameterSpec gcmParams = new GCMParameterSpec(128, ivParams.getIV());
		
		// Create key from given password
		MessageDigest sha;
		try {
			sha = MessageDigest.getInstance("SHA-256");
		} catch (NoSuchAlgorithmException e1) { return null; }
		byte[] passwordByte = stringToByte(password);
		passwordByte = sha.digest(passwordByte);

		// For AES 256, obviously, key length is 256 bits = 32 bytes.
		//int len = 32;
		// If password is too long or too short, truncate or pad it.
		//if (passwordByte.length != len) { passwordByte = Arrays.copyOf(passwordByte, len); }
		
		// Initialize cipher
		try {
			SecretKey key = new SecretKeySpec(passwordByte, "AES");
			c.init(mode, key, gcmParams);
		} catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
			e.printStackTrace();
			return null;
		}
		
		// Return final stuff
		try {
			byte[] finalText = c.doFinal(text);
			// If we ecrypt, append iv to the beginning of the encoded text.
			if (doEncrypt) {
				byte[] toReturn = new byte[iv.length + finalText.length];
				System.arraycopy(iv, 0, toReturn, 0, iv.length);
				System.arraycopy(finalText, 0, toReturn, iv.length, finalText.length);
				return toReturn;
			}
			return finalText;
		} catch (IllegalBlockSizeException | BadPaddingException e) {
			// I'm sick of these exceptions T_T
			return null;
		}
	}
	
	/**
	 * Encrypts given text with given password
	 * @param text - text to encrypt
	 * @param password - encryption password
	 * 
	 * @return
	 * Encrypted text
	 */
	public static byte[] encrypt(String text, String password) {
		return doThing(stringToByte(text), password, Cipher.ENCRYPT_MODE);
	}
	
	/**
	 * Decrypts given text using given password
	 * @param text - text to decrypt
	 * @param password - password
	 * 
	 * @return
	 * Decrypted text or empty string if unable to decrypt text.
	 */
	public static String decrypt(byte[] text, String password) {
		if (text == null || password == null) { return ""; }
		try { return new String(doThing(text, password, Cipher.DECRYPT_MODE)); }
		catch (NullPointerException e) { return ""; }
	}
	
	/**
	 * Converts String text to byte array
	 * @param text - text to convert.
	 * @return
	 * String converted to byte array
	 */
	private static byte[] stringToByte(String text) {
		byte[] textByte;
		try {
			textByte = text.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			// Fallback to system encoding if JRE somehow doesn't support UTF.
			textByte = text.getBytes();
		}
		return textByte;
	}
}
