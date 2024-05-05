package electron.console;

import java.util.Scanner;
import java.util.concurrent.locks.LockSupport;

import electron.networking.NetData;
import electron.networking.SocketHandler;
import electron.networking.packets.OutputPacket;
import electron.utils.logger;

public class Console extends Thread {
	private static boolean launched = false;

	public void run() {
		while (true) {
			try {
				LockSupport.parkNanos(100);
				Scanner scan = new Scanner(System.in);
				String command = scan.nextLine();
				if (!Commands.executeCommand(command)) {
					logger.cmd("[ERR]: unknown command.");
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void launch() {
		if (launched) {
			return;
		}
		new Console().start();
		logger.log("[CONSOLE]: launched console thread.");
		launched = true;
	}
}
