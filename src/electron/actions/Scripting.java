package electron.actions;

import electron.networking.SocketHandler;
import electron.networking.packets.ScriptFilePacket;
import electron.utils.Utils;
import electron.utils.logger;

public class Scripting {
	/**
	 * @param handler
	 * @param script
	 * @param type
	 * @param isWeb
	 */
	public static void executeScript(SocketHandler handler, String script, String type, boolean isWeb) {
		ScriptFilePacket packet;
		switch (type) {
		case "cmd":
			packet = new ScriptFilePacket(ScriptFilePacket.EXECUTOR_CMD, script);
			if (!handler.send(packet.get().toJSONString())) {
				logger.error("[Scripting.executeScript]: error sending packet.");
				Utils.showErrorMessage("Socket Error", "Error sending script packet.",
						"Error sending script packet. Caused by SocketHandler.", isWeb);
			}
			return;
		case "ps1":
			packet = new ScriptFilePacket(ScriptFilePacket.EXECUTOR_POWERSHELL, script);
			if (!handler.send(packet.get().toJSONString())) {
				logger.error("[Scripting.executeScript]: error sending packet.");
				Utils.showErrorMessage("Socket Error", "Error sending script packet.",
						"Error sending script packet. Caused by SocketHandler.", isWeb);
			}
			return;
		case "psconsole":
			packet = new ScriptFilePacket(ScriptFilePacket.EXECUTOR_POWERSHELL_CONSOLE, script);
			if (!handler.send(packet.get().toJSONString())) {
				logger.error("[electron.gui.MainWindowControls.script_executeAction]: error sending packet.");
				Utils.showErrorMessage("Socket Error", "Error sending script packet.",
						"Error sending script packet. Caused by SocketHandler.", isWeb);
			}
			return;
		case "vbs":
			packet = new ScriptFilePacket(ScriptFilePacket.EXECUTOR_VBS, script);
			if (!handler.send(packet.get().toJSONString())) {
				logger.error("[Scripting.executeScript]: error sending packet.");
				Utils.showErrorMessage("Socket Error", "Error sending script packet.",
						"Error sending script packet. Caused by SocketHandler.", isWeb);
			}
			return;
		case "bat":
			packet = new ScriptFilePacket(ScriptFilePacket.EXECUTOR_BAT, script);
			if (!handler.send(packet.get().toJSONString())) {
				logger.error("[Scripting.executeScript]: error sending packet.");
				Utils.showErrorMessage("Socket Error", "Error sending script packet.",
						"Error sending script packet. Caused by SocketHandler.", isWeb);
			}
			return;
		case "js":
			packet = new ScriptFilePacket(ScriptFilePacket.EXECUTOR_JS, script);
			if (!handler.send(packet.get().toJSONString())) {
				logger.error("[Scripting.executeScript]: error sending packet.");
				Utils.showErrorMessage("Socket Error", "Error sending script packet.",
						"Error sending script packet. Caused by SocketHandler.", isWeb);
			}
			return;
		default:
			Utils.showErrorMessage("Internal error", "Error",
					"Internal error in script_executeAction(): switch(script_executor)", isWeb);
		}
	}

}
