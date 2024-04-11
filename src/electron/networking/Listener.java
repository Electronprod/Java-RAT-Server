package electron.networking;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.locks.LockSupport;

import electron.utils.logger;

public class Listener extends Thread {
	private static int port;
	private static Listener listenerobj;

	/**
	 * Get port are listening by server
	 * 
	 * @return port
	 */
	public static int getPort() {
		return port;
	}

	/**
	 * Start port listener for input connections
	 * 
	 * @param port
	 */
	public Listener(int port) {
		Listener.port = port;
	}

	/**
	 * Stops listener.
	 */
	public static void stopListener() {
		logger.warn("[electron.networking.Listener]: listener stopped by request.");
		listenerobj.interrupt();
	}

	public void run() {
		listenerobj = this;
		while (true) {
			// Listening for connections
			ServerSocket servSocket;
			try {
				servSocket = new ServerSocket(port);
				logger.log("[electron.networking.Listener]: started server on " + servSocket);
				while (true) {
					LockSupport.parkNanos(100);
					// Accepting connections
					SocketHandler socketHander = new SocketHandler(servSocket.accept());
					socketHander.start();
				}
			} catch (IOException e) {
				logger.error("[electron.networking.Listener]: I/O cautght: " + e.getMessage());
			}
		}
	}

}
