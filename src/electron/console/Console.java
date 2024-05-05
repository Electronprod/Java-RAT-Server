package electron.console;

import java.util.Scanner;
import java.util.concurrent.locks.LockSupport;

import electron.networking.NetData;
import electron.networking.SocketHandler;
import electron.networking.packets.OutputPacket;

public class Console {
	private static SocketHandler selectedClient = null;

	public static void launch() {
		return;
//		while (true) {
//			LockSupport.parkNanos(100);
//			Scanner scan = new Scanner(System.in);
//			String command = scan.nextLine();
//			/*
//			 * Server commands executor
//			 */
//			// Client network options
//			if (isMultiCommand("/client", command)) {
//				// Parsing arguments
//				String[] args = getCommandArgs(command);
//				/*
//				 * Select a client to work with
//				 */
//				if (args[1].equalsIgnoreCase("select")) {
//					try {
//						if (isNumber(args[2])) {
//							int selected = Integer.parseInt(args[2]);
//							if (selected == -1) {
//								selectedClient = null;
//								logger.cmd("Set selected client to: unset.");
//								continue;
//							}
//							// If it's incorrect number
//							if (NetData.getClients().size() < selected) {
//								logger.error("Incorrect id.");
//								continue;
//							}
//							selectedClient = NetData.getClients().get(selected);
//							logger.cmd("Set selected client to " + selected + ".");
//						} else {
//							logger.error("Incorrect input.");
//						}
//					} catch (java.lang.ArrayIndexOutOfBoundsException e) {
//						logger.error("Incorrect input. (" + e.getMessage() + ")");
//					} catch (java.lang.IndexOutOfBoundsException e) {
//						logger.error("Incorrect input. (" + e.getMessage() + ")");
//					}
//					/*
//					 * Show the list of clients
//					 */
//				} else if (args[1].equalsIgnoreCase("list")) {
//					drawActiveConnections();
//				} else {
//					logger.cmd("Unknown arguments.");
//				}
//				continue;
//			}
//			/*
//			 * Sending a non-server command
//			 */
//			if (selectedClient == null) {
//				logger.error("Select a client first!");
//				continue;
//			}
//			// Creating packet to send
//			OutputPacket packet = new OutputPacket(command);
//			// Sending it to selected client
//			if (!selectedClient.send(packet.get())) {
//				logger.error("[electron.user.Console.start]: error sending packet.");
//			}
//		}
	}

	public static SocketHandler getSelectedSocket() {
		return selectedClient;
	}

}
