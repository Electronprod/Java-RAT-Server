package electron.networking;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

import javax.imageio.ImageIO;

public class ScreenV2Receiver extends Thread {
	private static BufferedImage img;

	public static BufferedImage getImage() {
		return img;
	}

	public void run() {
		while (true) {
			try {
				ServerSocket serverSocket = new ServerSocket(Listener.getPort() + 2);
				Socket socket;
				socket = serverSocket.accept();
				InputStream is = socket.getInputStream();
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				byte[] buffer = new byte[1024];
				int bytesRead;
				while ((bytesRead = is.read(buffer)) != -1) {
					baos.write(buffer, 0, bytesRead);
				}
				byte[] imageBytes = baos.toByteArray();
				ByteArrayInputStream bais = new ByteArrayInputStream(imageBytes);
				img = ImageIO.read(bais);
				is.close();
				socket.close();
				serverSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
