package client;

import java.awt.Dimension;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.nio.file.Paths;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;

import client.ClientCommand.LCommand;

public class ClientG {
	JFrame jf;

	JButton dl;

	JButton ul;

	JButton del;

	private static final boolean shortNames = true;

	public int w;
	public int h;

	public int margin;

	public int btH;

	private Client cli;

	private JList<String> jl;

	private void Listhandler(ListSelectionEvent e) {
		if (jl.getSelectedIndex() == -1) {
			dl.setEnabled(false);
			del.setEnabled(false);
		} else {
			dl.setEnabled(true);
			del.setEnabled(true);
		}
	}

	private void setPrefs(JFileChooser t) {
		t.setPreferredSize(new Dimension(w, h));
		Action details = t.getActionMap().get("viewTypeDetails");
		details.actionPerformed(null);
	}

	private void Delete(ActionEvent e) {
		String path = jl.getSelectedValue().split("=")[0];
		cli.clc.synchronizedGetL().filename = Paths.get(path).getFileName().toString();
		cli.clc.synchronizedGetL().command = LCommand.Delete;
		cli.clc.release();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		boolean b = cli.clc.synchronizedGetL().status;
		cli.clc.release();
		if (b) {
			JOptionPane.showMessageDialog(null, "Successfuly deleted!");
		} else
			JOptionPane.showMessageDialog(null, "Deletion failed!");
		Refresh(null);
	}

	private void Upload(ActionEvent e) {
		JFileChooser t = new JFileChooser();
		setPrefs(t);
		if (t.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			cli.clc.synchronizedGetL().filename = t.getSelectedFile().getName();
			cli.clc.synchronizedGetL().path = t.getSelectedFile().toPath();
			cli.clc.synchronizedGetL().command = LCommand.Send;
			cli.clc.release();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			boolean b = cli.clc.synchronizedGetL().status;
			cli.clc.release();
			if (b) {
				JOptionPane.showMessageDialog(null, "Successfuly uploaded!");
			} else
				JOptionPane.showMessageDialog(null, "Upload failed!");
			Refresh(null);
		}
	}

	private void Download(ActionEvent e) {
		JFileChooser t = new JFileChooser();
		String path = jl.getSelectedValue().split("=")[0];
		setPrefs(t);
		t.setSelectedFile(Paths.get(path).toFile());
		if (t.showSaveDialog(null) == JFileChooser.APPROVE_OPTION) {
			cli.clc.synchronizedGetL().filename = Paths.get(path).getFileName().toString();
			cli.clc.synchronizedGetL().path = t.getSelectedFile().toPath();
			cli.clc.synchronizedGetL().command = LCommand.Request;
			cli.clc.release();
			try {
				Thread.sleep(100);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			boolean b = cli.clc.synchronizedGetL().status;
			cli.clc.release();
			if (b) {
				JOptionPane.showMessageDialog(null, "Successfuly downloaded!");
			} else
				JOptionPane.showMessageDialog(null, "Download failed!");
		}
	}

	private void Refresh(ActionEvent e) {
		cli.clc.synchronizedGetL().command = LCommand.List;
		cli.clc.release();
		try {
			Thread.sleep(100);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		boolean b = cli.clc.synchronizedGetL().status;
		cli.clc.release();
		if (b) {
			String a = cli.h.synchronizedGetL().toString();
			a = a.replaceAll("(\\{?\\}?)", "");
			String[] vect = a.split(", ");
			if (!vect[0].equals("")) {
				if (shortNames) {
					for (int i = 0; i < vect.length; i++) {
						vect[i] = vect[i].substring(vect[i].lastIndexOf("\\") + 1, vect[i].indexOf("="));
					}
				}
				jl.setListData(vect);
			} else
				jl.setListData(new String[0]);
			cli.h.release();
		} else
			JOptionPane.showMessageDialog(null, "Listing files failed!");
	}

	public ClientG(Client cli) {
		if (cli == null) {
			JOptionPane.showMessageDialog(null, "Connection failed!");
			System.exit(-1);
		}
		if (cli.doExit.synchronizedGetL()) {
			JOptionPane.showMessageDialog(null, "Client initialization failed!");
			System.out.println("Client initialization failed");
			System.exit(-1);
		}
		cli.doExit.release();
		this.cli = cli;
		w = h = 400;
		btH = 20;
		margin = 10;
		jf = new JFrame("File Transfer Expanded");
		jf.setLayout(null);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setResizable(false);
		jf.setVisible(true);
		Insets is = jf.getInsets();
		jf.setVisible(false);
		jf.setSize(w + is.left + is.right, h + is.top + is.bottom);
		jf.setLocationRelativeTo(null);
		jl = new JList<>();
		jl.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		jl.addListSelectionListener(this::Listhandler);
		JButton jb = new JButton("Refresh");
		jb.addActionListener(this::Refresh);
		dl = new JButton("Download");
		ul = new JButton("Upload");
		del = new JButton("Delete");
		dl.setBounds(w / 2, 2 * margin + btH, w / 2 - margin, btH);
		ul.setBounds(w / 2, 3 * margin + 2 * btH, w / 2 - margin, btH);
		del.setBounds(w / 2, 4 * margin + 3 * btH, w / 2 - margin, btH);
		dl.setEnabled(false);
		del.setEnabled(false);
		dl.addActionListener(this::Download);
		ul.addActionListener(this::Upload);
		del.addActionListener(this::Delete);
		jl.setBounds(margin, margin, w / 2 - 2 * margin, h - 2 * margin);
		jl.repaint();
		jb.setBounds(w / 2, margin, w / 2 - 1 * margin, btH);
		jf.add(jl);
		jf.add(jb);
		jf.add(dl);
		jf.add(ul);
		jf.add(del);
		jf.setVisible(true);
		Refresh(null);
	}
}
