package common;

import java.io.InvalidObjectException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;

public abstract class NetworkInitialise {
	protected final int opmode;
	protected final Key pk;

	protected final Cipher RSAci;

	protected NetworkInitialise(int opmode) throws Exception {

		if (opmode < 1 || opmode > 2)
			throw new InvalidObjectException("InvalidOpmode");
		else
			this.opmode = opmode;

		Key lk = null;
		Cipher lc = null;
		if (opmode == Cipher.DECRYPT_MODE) {
			PKCS8EncodedKeySpec pkI = new PKCS8EncodedKeySpec(Files.readAllBytes(Paths.get("private.der")));
			KeyFactory kf = KeyFactory.getInstance("RSA");
			lk = kf.generatePrivate(pkI);
		} else {
			X509EncodedKeySpec pkI = new X509EncodedKeySpec(Files.readAllBytes(Paths.get("public.der")));
			KeyFactory kf = KeyFactory.getInstance("RSA");
			lk = kf.generatePublic(pkI);
		}
		lc = Cipher.getInstance("RSA/ECB/OAEPWithSHA-512AndMGF1Padding");
		lc.init(opmode, lk);

		if (lk == null)
			throw new InvalidObjectException("Key is null");
		pk = lk;
		RSAci = lc;
	}

	protected NetworkInitialise(int opmode, Key pk) throws Exception {
		this.opmode = opmode;
		this.pk = pk;
		Cipher lc = null;
		lc = Cipher.getInstance("RSA/ECB/OAEPWithSHA-512AndMGF1Padding");
		lc.init(opmode, pk);
		RSAci = lc;
	}
}
