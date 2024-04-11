package electron.networking;

import java.awt.Frame;
import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JOptionPane;

import electron.utils.logger;

public class FileReceiver extends Thread {
	private int port;
	private String path;

	public FileReceiver(int port, String path) {
		this.port = port;
		this.path = path;
	}

	public void run() {
		try {
			ServerSocket serverSocket = new ServerSocket(port);
			serverSocket.setSoTimeout(10000);
			Socket socket = serverSocket.accept();

			InputStream is = socket.getInputStream();
			FileOutputStream fos = new FileOutputStream(path);
			BufferedOutputStream bos = new BufferedOutputStream(fos);
			byte[] byteArray = new byte[1024];
			int bytesRead;
			while ((bytesRead = is.read(byteArray)) != -1) {
				bos.write(byteArray, 0, bytesRead);
			}
			bos.close();
			is.close();
			serverSocket.close();
			socket.close();
			JOptionPane.showMessageDialog(new Frame(), "File downloaded: " + path, "File receiver",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(new Frame(),
					"[electron.networking.FileReceiver]: error receiving file: " + e.getMessage(), "File receiver",
					JOptionPane.ERROR_MESSAGE);
			logger.error("[electron.networking.FileReceiver]: error: " + e.getMessage());
		}
	}
}
