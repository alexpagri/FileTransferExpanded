package client;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.SecureRandom;
import java.util.Hashtable;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.net.SocketFactory;

import client.ClientCommand.LCommand;
import common.CommandL;
import common.CommandL.Command;
import common.DataBlock;
import common.FileManager;
import common.NetworkFunctionality;
import common.SynchronizedObject;

public class Client extends NetworkFunctionality {

	public SynchronizedObject<Boolean> doExit;

	private String uname;
	private byte[] pwH;
	public SynchronizedObject<ClientCommand> clc;
	public SynchronizedObject<Hashtable<Path, Long>> h;

	private Client(String address, int port, String uname, byte[] pwH) throws Exception {
		super(SocketFactory.getDefault().createSocket(address, port), Cipher.ENCRYPT_MODE);
		doExit = new SynchronizedObject<Boolean>(true);
		this.uname = uname;
		this.pwH = pwH;
		clc = new SynchronizedObject<ClientCommand>(new ClientCommand(null, null));
		h = new SynchronizedObject<Hashtable<Path, Long>>(new Hashtable<Path, Long>());
	}

	private void initChannel() throws Exception {
		KeyGenerator kf = KeyGenerator.getInstance("AES");
		kf.init(128);
		SecretKey sk = kf.generateKey();
		byte[] ivv = new byte[16];
		new SecureRandom().nextBytes(ivv);
		IvParameterSpec iv = new IvParameterSpec(ivv);
		EwriteRSA(new DataBlock(sk.getEncoded()));
		EwriteRSA(new DataBlock(ivv));
		initAES(sk, iv);
	}

	private void logon() throws Exception {
		if (!sendCommandL(new CommandL(Command.LoginData, uname.getBytes(), null)))
			throw new Exception("Logon failed");
		if (!sendCommandL(new CommandL(Command.LoginData, pwH, null)))
			throw new Exception("Logon failed");
	}

	private void loop() {
		doExit.forceLock();
		try {
			initChannel();
			logon();
		} catch (Exception e) {
			System.out.println("Encryption / Logon process failed, no way to continue like this!");
			e.printStackTrace();
			doExit.release();
			return;
		}
		CommandL ka = new CommandL(Command.KeepAlive, null, null);
		CommandL req;
		CommandL reqList = new CommandL(Command.FullList, null, null);
		CommandL sendF = new CommandL(Command.SendFile, null, null);
		CommandL deleteF;
		doExit.synchronizedSet(false);
		while (!doExit.synchronizedGetL()) {
			doExit.release();
			try {
				switch (clc.synchronizedGetL().command) {
				case KeepAlive: {
					sendCommandL(ka);
					break;
				}
				case Request: {
					clc.synchronizedGetL().status = false;
					req = new CommandL(Command.RequestFile, clc.synchronizedGetL().filename.getBytes(), null);
					if (sendCommandL(req)) {
						clc.synchronizedGetL().status = new FileManager(this)
								.getFile(clc.synchronizedGetL().path.toString(), true);
					}
					clc.synchronizedGetL().command = LCommand.KeepAlive;
					break;
				}
				case List: {
					clc.synchronizedGetL().status = false;
					if (sendCommandL(reqList)) {
						h.synchronizedSet(new FileManager(this).getFileList());
						// System.out.println(h.toString());
						if (h.synchronizedGetL() != null)
							clc.synchronizedGetL().status = true;
						h.release();
					}
					clc.synchronizedGetL().command = LCommand.KeepAlive;
					break;
				}
				case Send: {
					clc.synchronizedGetL().status = false;
					if (sendCommandL(sendF)) {
						clc.synchronizedGetL().status = new FileManager(this).sendFile(clc.synchronizedGetL().path,
								Paths.get(clc.synchronizedGetL().filename));
					}
					clc.synchronizedGetL().command = LCommand.KeepAlive;
					break;
				}
				case Delete: {
					clc.synchronizedGetL().status = false;
					deleteF = new CommandL(Command.DeleteFile, clc.synchronizedGetL().filename.getBytes(), null);
					clc.synchronizedGetL().status = sendCommandL(deleteF);
					clc.synchronizedGetL().command = LCommand.KeepAlive;
					break;
				}
				}
				clc.release();
			} catch (SocketTimeoutException e) {
				doExit.synchronizedSet(true);
				System.out.println("Server " + s.getRemoteSocketAddress() + " timed out");
			} catch (Exception e) {
				// e.printStackTrace();
				doExit.synchronizedSet(true);
			}
		}
		doExit.release();
		System.out.println("Disconnected from " + s.getRemoteSocketAddress() + " server");
		try {
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Client run(String address, int port, String uname, byte[] pwH) {

		try {
			Client cli = new Client(address, port, uname, pwH);

			Thread th = new Thread(cli::loop, "Client Command Thread");
			// cli.loop();
			th.start();

			Thread.sleep(100);

			return cli;

			// cli.s.close();
			// System.out.println(new String(cli.read().content));
			/*
			 * Files.write(Paths.get("file2.txt"), cli.read(),
			 * StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING,
			 * StandardOpenOption.CREATE);
			 */
		} catch (Exception e) {
			System.out.println("Connection Failed!");
			e.printStackTrace();
		}
		return null;
	}
}
