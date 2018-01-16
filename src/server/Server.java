package server;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Scanner;

import javax.crypto.Cipher;
import javax.net.ServerSocketFactory;

import common.NetworkInitialise;
import common.SynchronizedObject;
import main.Main;

public class Server extends NetworkInitialise {

	private ServerSocket s;
	private SynchronizedObject<ArrayList<ClientHandler>> threadList;

	private SynchronizedObject<Boolean> doExit;

	private ClientHandler accept() throws Exception {
		return new ClientHandler(s.accept(), opmode, pk, threadList);
	}

	private Server(int port) throws Exception {
		super(Cipher.DECRYPT_MODE);
		s = ServerSocketFactory.getDefault().createServerSocket();
		s.bind(new InetSocketAddress(port));
		s.setSoTimeout(Main.timeout);
		threadList = new SynchronizedObject<>(new ArrayList<ClientHandler>());
		doExit = new SynchronizedObject<Boolean>(false);
	}

	private void loop() {
		while (!doExit.synchronizedGetL()) {
			doExit.release();
			try {
				if (threadList.synchronizedGetL().size() < Main.maximumConnections) {
					threadList.release();
					ClientHandler cliHandler = accept();
					new Thread(cliHandler::run, "Client Handler Thread").start();
				} else {
					System.out.println("Maximum client number reached!");
					Thread.sleep(Main.timeout);
				}
			} catch (SocketTimeoutException e) {
				continue;
			} catch (Exception e) {
				System.out.println("Critical: server must not crash here!");
				e.printStackTrace();
			}
		}
		doExit.release();
		System.out.println("Main listen thread is out!");
		threadList.synchronizedGetL().forEach((a) -> a.orderStop.synchronizedSet(true));
		while (!threadList.synchronizedGetL().isEmpty()) {
			Thread ct = threadList.synchronizedGetL().get(0).thisThread;
			threadList.release();
			try {
				ct.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

	private void executeCommand(int command) {
		switch (command) {
		case 0: {
			doExit.synchronizedSet(true);
			System.out.println("Server shutting down, please wait ......");
			break;
		}
		}
	}

	public static void run(int port) {
		try {
			Server sv = new Server(port);
			try {
				UserData.initialise();
				System.out.println("Server listening on port " + port);
				Thread main = new Thread(sv::loop, "Main listen thread");
				main.start();
				System.out.println("Main listening thread started");
				Scanner sc = new Scanner(System.in);
				while (!sv.doExit.synchronizedGetL()) {
					sv.doExit.release();
					System.out.println("Commands: help[prints this], exit[shuts down entire server]");
					String issuedCommand = sc.nextLine();
					if (issuedCommand.equals("help"))
						continue;
					else if (issuedCommand.equals("exit"))
						sv.executeCommand(0);
					else
						System.out.println("Invalid Command");
				}
				sv.doExit.release();
				sc.close();
				main.join();
				System.out.println("Server closed ok.");
			} catch (Exception e) {
				System.out.println("Sorry, server command line crashed");
				e.printStackTrace();
				System.exit(-1);
			}
		} catch (Exception e) {
			System.out.println("Sorry, server just crashed before booting up!");
			e.printStackTrace();
			System.exit(-1);
		}
	}
}
