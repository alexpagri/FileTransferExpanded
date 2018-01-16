package client;

import java.nio.file.Path;

public class ClientCommand {
	public Path path;
	public String filename;
	public LCommand command;
	public boolean status;

	public enum LCommand {
		KeepAlive, Request, List, Send, Delete
	}

	public ClientCommand(Path p, String s) {
		path = p;
		filename = s;
		command = LCommand.KeepAlive;
		status = true;
	}
}
