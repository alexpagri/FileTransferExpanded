package client;

import java.awt.Insets;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

public class ClientLogin {
	JFrame jf;

	public int w;
	public int h;

	public int margin;

	public int btH;

	private String adr;

	private int port;

	public ClientLogin() {
		w = 200;
		h = 100;
		btH = 20;
		margin = 10;
		jf = new JFrame("Login");
		jf.setLayout(null);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setResizable(false);
		jf.setVisible(true);
		Insets is = jf.getInsets();
		jf.setVisible(false);
		jf.setSize(w + is.left + is.right, h + is.top + is.bottom);
		jf.setLocationRelativeTo(null);
		JLabel unameL = new JLabel("Username:");
		unameL.setBounds(margin, margin, 3 * w / 8, btH);
		JTextField uname = new JTextField();
		uname.setBounds(3 * w / 8 + margin, margin + 2, w / 2, btH);
		JLabel pwL = new JLabel("Password:");
		pwL.setBounds(margin, 2 * margin + btH, 3 * w / 8, btH);
		JPasswordField pw = new JPasswordField();
		pw.setBounds(3 * w / 8 + margin, 2 * margin + btH + 2, w / 2, btH);
		JButton jb = new JButton("Login");
		jb.setBounds(w / 4, h - margin - btH, w / 2, btH);
		jb.addActionListener((a) -> {
			String unameS = uname.getText();
			byte[] pwS = new String(pw.getPassword()).getBytes();
			if (unameS.length() > 0 && pw.getPassword().length > 0) {
				try {
					new ClientG(Client.run(adr, port, unameS, MessageDigest.getInstance("SHA-256").digest(pwS)));
				} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
				}
				jf.dispose();
			}
		});
		jf.add(jb);
		jf.add(uname);
		jf.add(unameL);
		jf.add(pw);
		jf.add(pwL);
		File con = new File("connection.txt");
		if (!con.exists()) {
			try {
				OutputStream os = Files.newOutputStream(con.toPath(), StandardOpenOption.CREATE,
						StandardOpenOption.WRITE);
				os.write("127.0.0.1".getBytes());
				os.write(System.lineSeparator().getBytes());
				os.write("688".getBytes());
				adr = "127.0.0.1";
				port = 688;
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		} else {
			try {
				BufferedReader br = Files.newBufferedReader(con.toPath());
				adr = br.readLine();
				port = Integer.parseInt(br.readLine());
			} catch (IOException e) {
				e.printStackTrace();
				System.exit(-1);
			}
		}
		jf.setVisible(true);
	}
}
