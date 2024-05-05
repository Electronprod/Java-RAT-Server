package electron.console;

import electron.networking.NetData;
import electron.networking.SocketHandler;
import electron.utils.logger;

public class Commands {
	/**
	 * Prints active connections
	 */
	public static void drawActiveConnections() {
		System.out.println();
		logger.cmd("ACTIVE CONNECTIONS:");
		int i = 0;
		for (SocketHandler client : NetData.getClients()) {
			// Printing data
			logger.cmd(i + " | " + client.getInfo().getAddress() + " | " + client.getInfo().getUsername() + " | "
					+ client.getInfo().getOs() + " | " + client.getInfo().getCountry());
			i++;
		}
		System.out.println();
		logger.cmd("Total clients connected: " + NetData.getClients().size());
	}

}
