package electron.networking.packets;

import org.json.simple.JSONObject;

import electron.gui.MainWindowControls;
import electron.networking.SocketHandler;
import electron.utils.Utils;
import electron.utils.logger;

public class OutputPacket {
	private JSONObject main = new JSONObject();

	@SuppressWarnings("unchecked")
	public OutputPacket(String command) {
		main.put("command", command);
		main.put("packettype", "0");
	}

	public String get() {
		return main.toJSONString();
	}

	/**
	 * Sends OutputPacket command to remote client
	 * 
	 * @param message - command to send
	 * @return boolean
	 */
	public static boolean sendOutPacket(String message, SocketHandler handler, boolean isWeb) {
		OutputPacket packet = new OutputPacket(message);
		// Sending it to selected client
		if (!handler.send(packet.get())) {
			logger.error("[electron.networking.packets.sendOutPacket]: error sending packet. Data: " + packet.get());
			Utils.showErrorMessage("Error sending packet", "Network error", "Error sending data to remote client.",
					isWeb);
			return false;
		}
		return true;
	}
}
