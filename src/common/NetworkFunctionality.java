package common;

import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.security.GeneralSecurityException;
import java.security.Key;

import javax.crypto.Cipher;
import javax.crypto.SealedObject;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import common.CommandL.Command;
import main.Main;

public class NetworkFunctionality extends NetworkInitialise {
	protected final Socket s;
	protected final ObjectOutputStream os;
	protected final ObjectInputStream is;

	protected Cipher enc;
	protected Cipher dec;

	protected CommandL read() throws Exception {
		if (dec == null)
			return null;
		SealedObject so = (SealedObject) is.readObject();//RCE, DDOS, OVF
		CommandL db = (CommandL) so.getObject(dec);
		return db;
		/*
		 * ArrayList<byte[]> byteArr = new ArrayList<byte[]>(); byte[] cArray =
		 * new byte[Main.buf]; int len = 0; while ((len = is.read(cArray, 0,
		 * 512)) != -1) { byteArr.add(cArray); cArray = new byte[Main.buf]; } if
		 * (byteArr.size() == 0) return null; cArray = new byte[len];
		 * System.arraycopy(byteArr.remove(byteArr.size() - 1), 0, cArray, 0,
		 * len); byteArr.add(cArray); byte[] total = new byte[(byteArr.size() -
		 * 1) * Main.buf + len]; int pos = 0; for (byte[] b : byteArr) {
		 * System.arraycopy(b, 0, total, pos, b.length); pos += Main.buf; }
		 * return total;
		 */
	}

	public CommandL getCommandL() throws Exception {
		CommandL resp = read();
		if (resp.command != Command.FileData)
			return resp;
		if (resp.verify())
			return resp;
		CommandL re = new CommandL(Command.RequestResend, null, null);
		write(re);
		for (int i = 0; i < Main.maxRetries; i++) {
			if (resp.command == Command.FileData) {
				resp = read();
				if (resp.verify())
					return resp;
				write(re);
			}
		}
		throw new Exception("Failed after too many retries / Server aborted" + resp.command);
	}

	protected void write(CommandL db) throws Exception {
		if (enc != null) {
			os.reset();
			SealedObject so = new SealedObject(db, enc);
			os.writeObject(so);
		}
		/*
		 * for (int i = 0; i < s.length; i += Main.buf) os.write(s, i,
		 * Math.min(Main.buf, s.length - i));
		 */
	}

	public boolean sendCommandL(CommandL c) throws Exception {
		write(c);
		CommandL resp = read();
		for (int i = 0; i < Main.maxRetries; i++) {
			if (resp.command == Command.RequestResend) {
				write(c);
				resp = read();
			} else
				break;
		}
		if (resp.command == Command.OK)
			return true;
		if (resp.command == Command.TotalFailureReset) {
			System.out.println("Abort reason: " + new String(resp.content));
			return false;
		}
		throw new Exception("Too many retries or Illegal command " + resp.command);
	}

	protected DataBlock EreadRSA() throws Exception {
		if (opmode == Cipher.DECRYPT_MODE) {
			SealedObject so = (SealedObject) is.readObject();//RCE, DDOS, OVF
			DataBlock db = (DataBlock) so.getObject(RSAci);
			if (db.verify())
				return db;
			else
				throw new InvalidObjectException("DataBlock Checksum mismatch");
		}
		return null;
	}

	protected void EwriteRSA(DataBlock db) throws Exception {
		if (opmode == Cipher.ENCRYPT_MODE) {
			SealedObject so = new SealedObject(db, RSAci);
			os.writeObject(so);
		}
	}

	protected void initAES(SecretKey aesK, IvParameterSpec iv) throws GeneralSecurityException {
		enc = Cipher.getInstance("AES/CBC/PKCS5Padding");
		enc.init(Cipher.ENCRYPT_MODE, aesK, iv);
		dec = Cipher.getInstance("AES/CBC/PKCS5Padding");
		dec.init(Cipher.DECRYPT_MODE, aesK, iv);
	}

	protected void postInit() throws SocketException {
		s.setSoTimeout(Main.timeout);
	}

	protected NetworkFunctionality(Socket socket, int opmode) throws Exception {
		super(opmode);
		os = new ObjectOutputStream(socket.getOutputStream());
		is = new ObjectInputStream(socket.getInputStream());
		s = socket;
		postInit();
	}

	protected NetworkFunctionality(Socket socket, int opmode, Key key) throws Exception {
		super(opmode, key);
		os = new ObjectOutputStream(socket.getOutputStream());
		is = new ObjectInputStream(socket.getInputStream());
		s = socket;
		postInit();
	}
}
