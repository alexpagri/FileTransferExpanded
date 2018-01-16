package main;

import java.util.Scanner;

import client.ClientLogin;
import server.Server;

public final class Main {
	public static final int port = 688;
	public static final int timeout = 10000;
	public static final int maximumConnections = 4096;
	public static final int buf = 65536 * 512;
	public static final int maxRetries = 5;
	public static final int maxListFiles = 10000;
	public static final Scanner sc = new Scanner(System.in);

	public static void main(String[] args) {
		try {
			// Path th = Paths.get("./uname");
			// Path child = Paths.get("./uname/user/../../../uname");
			if (args.length == 0) {
				new ClientLogin();
			} else {
				Server.run(Integer.parseInt(args[0]));
			}
			/*
			int val = Integer.parseInt(sc.nextLine());
			switch (val) {
			case 1: {
				new ClientLogin();
				break;
			}
			case 2: {
				Server.run(sc.nextInt());
				break;
			}
			}*/
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
		sc.close();
	}

}
