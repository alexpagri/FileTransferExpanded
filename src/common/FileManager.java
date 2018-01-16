package common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Hashtable;
import java.util.concurrent.TimeoutException;

import common.CommandL.Command;
import main.Main;

public class FileManager {
	private NetworkFunctionality s;

	public boolean getFile(String unameOrPath, boolean isPath) {
		RandomAccessFile fileR = null;
		try {
			CommandL co = s.getCommandL();
			if (co.command != Command.SendNewFileHeader)
				throw new Exception("Did not get a file");
			long size = (long) co.object;
			String activeDir = null;
			if (isPath) {
				int splitloc;
				if (unameOrPath.lastIndexOf("/") == -1)
					splitloc = unameOrPath.lastIndexOf("\\");
				else
					splitloc = unameOrPath.lastIndexOf("/");
				activeDir = unameOrPath.substring(0, splitloc);
			}
			/*
			 * if (unameOrPath.matches(".*(/?.*)/")) activeDir =
			 * unameOrPath.split(".*(/?.*)/")[0]; else activeDir = "";
			 */
			File c;
			if (!isPath)
				c = Paths.get("./" + unameOrPath + "/").toFile();
			else
				c = Paths.get(activeDir).toFile();
			long cmaxsz = c.getUsableSpace();
			if (size >= cmaxsz)
				throw new Exception("Out of space / File too large");
			sendOK();
			CommandL fn = s.getCommandL();
			if (fn.command != Command.FileData)
				throw new Exception("Incorrect command received");
			File file = null;
			if (!isPath) {
				file = new File(unameOrPath + "/" + new String(fn.content));
				if (!file.toPath().normalize().startsWith(unameOrPath))
					throw new Exception("Path out of the limited area");
			} else
				file = new File(unameOrPath);
			fileR = new RandomAccessFile(file, "rwd");
			fileR.setLength(size);
			sendOK();
			int i;
			for (i = 0; i < size / Main.buf; i++) {
				System.gc();
				fn = s.getCommandL();
				fileR.write(fn.content, 0, Main.buf);
				sendOK();
			}
			int len = (int) (size - i * Main.buf);
			if (len > 0) {
				System.gc();
				fn = s.getCommandL();
				fileR.write(fn.content, 0, len);
				sendOK();
			}
		} catch (Exception e) {
			try {
				e.printStackTrace();
				sendNOK("Out of space");
				if (fileR != null)
					fileR.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			return false;
		}
		if (fileR != null)
			try {
				fileR.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		return true;
	}

	public void deleteFile(Path p) {
		p.toFile().delete();
	}

	public void sendNOK(String reason) {
		try {
			s.write(new CommandL(Command.TotalFailureReset, reason.getBytes(), null));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void sendOK() {
		try {
			s.write(new CommandL(Command.OK, null, null));
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public boolean getFileExists(Path p) {
		return Files.exists(p);
	}

	public boolean sendFile(Path p, Path remote) {
		BufferedInputStream b = null;
		try {
			long fSz = p.toFile().length();
			if (!s.sendCommandL(new CommandL(Command.SendNewFileHeader, null, fSz)))
				return false;// Abort cause can be ?
			if (!s.sendCommandL(new CommandL(Command.FileData, remote.toString().getBytes(), null)))
				return false;// Abort cause can be file too large
			b = new BufferedInputStream(Files.newInputStream(p), Main.buf);
			int i;
			for (i = 0; i < fSz / Main.buf; i++) {
				System.gc();
				byte[] readChunk = new byte[Main.buf];
				b.read(readChunk, 0, Main.buf);
				if (!s.sendCommandL(new CommandL(Command.FileData, readChunk, null)))
					return false;// Abort requested
			}
			int len = (int) (fSz - i * Main.buf);
			if (len > 0) {
				System.gc();
				byte[] readChunk = new byte[len];
				b.read(readChunk, 0, len);
				if (!s.sendCommandL(new CommandL(Command.FileData, readChunk, null)))
					return false;// Abort requested
			}
			return true;
		} catch (TimeoutException e) {
			System.out.println("Timed out! Aborting.");
			return false;
		} catch (Exception e) {
			System.out.println("Error / file does not exist / denied");
			e.printStackTrace();
			return false;
		}
	}

	public void sendFileList(Path p) {
		try {
			Object[] files = Files.walk(p).filter(Files::isRegularFile).toArray();
			if (!s.sendCommandL(new CommandL(Command.FullList, null, Math.min(files.length, Main.maxListFiles))))
				return;
			for (int i = 0; i < Math.min(files.length, Main.maxListFiles); i++) {
				if (!s.sendCommandL(new CommandL(Command.FullList, files[i].toString().getBytes(),
						((Path) files[i]).toFile().length())))
					return;
			}
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}

	public Hashtable<Path, Long> getFileList() {
		try {
			CommandL c = s.getCommandL();
			if (c.command != Command.FullList)
				throw new Exception("Did not get a file list");
			int size = (int) c.object;
			if (size > Main.maxListFiles)
				throw new Exception("Too many files received");
			sendOK();
			Hashtable<Path, Long> h = new Hashtable<>();
			for (int i = 0; i < size; i++) {
				c = s.getCommandL();
				if (c.command != Command.FullList)
					throw new Exception("Incorrect command received");
				if (c.content == null || c.object == null)
					throw new Exception("Incorrect command received");
				sendOK();
				h.put(Paths.get(new String(c.content)), (long) c.object);
			}
			return h;
		} catch (Exception e) {
			try {
				sendNOK("Error occured");
			} catch (Exception e1) {
				e1.printStackTrace();
			}
			e.printStackTrace();
			return null;
		}
	}

	public FileManager(NetworkFunctionality s) {
		this.s = s;
	}

}
