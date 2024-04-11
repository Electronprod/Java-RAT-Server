package electron.networking;

import java.awt.Frame;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.swing.JOptionPane;

import electron.utils.logger;

public class FileSender extends Thread{
	private File file;

	public FileSender(File file) {
		this.file = file;
	}

	public void run() {
		ServerSocket serverSocket;
		try {
			serverSocket = new ServerSocket(Listener.getPort() + 1);
			serverSocket.setSoTimeout(10000);// Waiting 10 seconds
			Socket socket = serverSocket.accept();
			byte[] byteArray = new byte[(int) file.length()];
			BufferedInputStream bis = new BufferedInputStream(new FileInputStream(file));
			bis.read(byteArray, 0, byteArray.length);
			OutputStream os = socket.getOutputStream();
			os.write(byteArray, 0, byteArray.length);
			os.flush();
			socket.close();
			serverSocket.close();
			bis.close();
			os.close();
			logger.log("[electron.networking.FileSender]: sent file.");
			JOptionPane.showMessageDialog(new Frame(), "File sent: "+file.getName(), "File sender", JOptionPane.INFORMATION_MESSAGE);
		} catch (IOException e) {
			logger.error("[electron.networking.FileSender]: error: " + e.getMessage());
			JOptionPane.showMessageDialog(new Frame(), "[electron.networking.FileSender]: error sending file"+file.getName()+": " + e.getMessage(), "File sender", JOptionPane.ERROR_MESSAGE);
		}
	}
}
