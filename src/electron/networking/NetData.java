package electron.networking;

import java.util.ArrayList;
import java.util.List;

/**
 * Stores connection information
 */
public class NetData {
	private static List<SocketHandler> clients = new ArrayList<SocketHandler>();

	/**
	 * Get clients connected
	 * 
	 * @return List<SocketHandler>
	 */
	public static List<SocketHandler> getClients() {
		return clients;
	}

	/**
	 * Add client to list of connected clients
	 * 
	 * @param client - SocketHandler to add
	 */
	public static void addClient(SocketHandler client) {
		clients.add(client);
	}

	/**
	 * Remove client from list of connected clients
	 * 
	 * @param client - SocketHandler to remove
	 */
	public static void removeClient(SocketHandler client) {
		clients.remove(client);
	}

}
