package server;

import java.util.Arrays;
import java.util.Dictionary;
import java.util.Hashtable;

import javax.xml.bind.DatatypeConverter;

public final class UserData {
	/*
	 * private String name; private byte[] passwordHash;
	 */

	private static Dictionary<String, byte[]> set;

	/*
	 * public UserData(String name, byte[] passwordHash) { this.name = name;
	 * this.passwordHash = passwordHash; set.put(name, passwordHash); }
	 * 
	 * public boolean verify(UserData b) { return name == b.name && passwordHash
	 * == b.passwordHash; }
	 */

	public static boolean login(String name, byte[] passwordHash) {
		byte[] cb;
		if ((cb = set.get(name)) != null)
			return Arrays.equals(cb, passwordHash);
		return false;
	}

	public static void initialise() {
		set = new Hashtable<>();
		set.put("user",
				DatatypeConverter.parseHexBinary("a665a45920422f9d417e4867efdc4fb8a04a1f3fff1fa07e998e86f7f7a27ae3"));
	}
}
