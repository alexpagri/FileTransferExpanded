package common;

import java.io.Serializable;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;

public class DataBlock implements Serializable {
	//RCE at readObject
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -6395846350595044557L;

	public final byte[] content;//DDOS, OVF

	public final byte[] SHA256;//DDOS, OVF

	public DataBlock(byte[] content) {
		if (content != null) {
			MessageDigest t = null;
			try {
				t = MessageDigest.getInstance("SHA-256");
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
			}
			SHA256 = t.digest(content);
		} else
			SHA256 = null;
		this.content = content;

	}

	public boolean verify() {
		try {
			return Arrays.equals(MessageDigest.getInstance("SHA-256").digest(content), SHA256);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		return false;
	}
}
