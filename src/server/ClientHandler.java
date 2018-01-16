package server;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Key;
import java.util.ArrayList;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

import common.CommandL;
import common.DataBlock;
import common.FileManager;
import common.NetworkFunctionality;
import common.SynchronizedObject;
import common.CommandL.Command;
import main.Main;

public class ClientHandler extends NetworkFunctionality {
	private SynchronizedObject<ArrayList<ClientHandler>> threadList;

	protected SynchronizedObject<Boolean> orderStop;

	protected final Thread thisThread;

	private String uname;

	public ClientHandler(Socket socket, int opmode, Key key, SynchronizedObject<ArrayList<ClientHandler>> threadList)
			throws Exception {
		super(socket, opmode, key);
		this.threadList = threadList;
		orderStop = new SynchronizedObject<Boolean>(false);
		thisThread = Thread.currentThread();
	}

	public void issueClose() {
		orderStop.synchronizedSet(true);
	}

	private void initChannel() throws Exception {
		DataBlock eb = EreadRSA();
		SecretKey sk = new SecretKeySpec(eb.content, "AES");
		eb = EreadRSA();
		IvParameterSpec iv = new IvParameterSpec(eb.content);
		initAES(sk, iv);
	}

	private void logon() throws Exception {
		CommandL ok = new CommandL(Command.OK, null, null);
		CommandL nok = new CommandL(Command.TotalFailureReset, "Login failed".getBytes(), null);
		CommandL user = getCommandL();
		if (user.command == Command.LoginData)
			write(ok);
		else {
			write(nok);
			throw new Exception("Expected user logon data");
		}
		uname = new String(user.content);
		CommandL pw = getCommandL();
		if (pw.command != Command.LoginData) {
			write(nok);
			throw new Exception("Expected user logon data");
		}
		if (!UserData.login(new String(user.content), pw.content)) {
			write(nok);
			throw new Exception("Login incorrect");
		}
		write(ok);
	}

	public void run() {
		threadList.synchronizedGetL().add(this);
		threadList.release();
		System.out.println("Client " + s.getRemoteSocketAddress() + " connected");
		try {
			initChannel();
			logon();
		} catch (Exception e) {
			System.out.println("Encryption / Logon process failed on client " + s.getRemoteSocketAddress()
					+ ", no way to continue like this!");
			e.printStackTrace();
			orderStop.synchronizedSet(true);
		}
		Path userPath = Paths.get(uname);
		userPath.toFile().mkdir();
		FileManager t = new FileManager(this);
		while (!orderStop.synchronizedGetL()) {
			orderStop.release();
			try {
				Thread.sleep(Main.timeout / 10);
				CommandL c = getCommandL();
				if (c.command == Command.KeepAlive) {
					t.sendOK();
					continue;
				} else if (c.command == Command.RequestFile) {
					if (t.getFileExists(Paths.get("./" + uname + "/" + new String(c.content))))
						t.sendOK();
					else {
						t.sendNOK("File does not exist");
						continue;
					}
					t.sendFile(Paths.get("./" + uname + "/" + new String(c.content)),
							Paths.get("./" + uname + "/" + new String(c.content)));
				} else if (c.command == Command.FullList) {
					t.sendOK();
					t.sendFileList(Paths.get("./" + uname + "/"));
				} else if (c.command == Command.SendFile) {
					t.sendOK();
					if (c.command == Command.TotalFailureReset)
						continue;
					t.getFile(uname, false);
				} else if (c.command == Command.DeleteFile) {
					if (t.getFileExists(Paths.get("./" + uname + "/" + new String(c.content))))
						t.sendOK();
					else {
						t.sendNOK("File does not exist");
						continue;
					}
					t.deleteFile(Paths.get("./" + uname + "/" + new String(c.content)));
				}
				// orderStop.synchronizedSet(true);
			} catch (SocketTimeoutException e) {
				orderStop.synchronizedSet(true);
				System.out.println("Client " + s.getRemoteSocketAddress() + " timed out");
			} catch (Exception e) {
				// e.printStackTrace();
				orderStop.synchronizedSet(true);
			}
		}
		orderStop.release();
		threadList.synchronizedGetL().remove(this);
		threadList.release();
		System.out.println("Client " + s.getRemoteSocketAddress() + " disconnected");
		try {
			s.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
