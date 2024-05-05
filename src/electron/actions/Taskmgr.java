package electron.actions;

import electron.networking.SocketHandler;
import electron.networking.packets.OutputPacket;
import electron.networking.packets.ProcessPacket;
import electron.utils.Utils;

public class Taskmgr {

	public static void killProcess_PID(ProcessPacket packet, SocketHandler handler) {
		if (!packet.getSession().getValue().equalsIgnoreCase("Linux")) {
			// Windows
			OutputPacket.sendOutPacket("taskkill /pid " + packet.getPid().getValue() + " /t /f", handler);
		} else {
			Utils.showErrorMessage("Error", "Unsupported OS", "This function unsupported for this OS");
		}
	}

	public static void killProcess_NAME(ProcessPacket packet, SocketHandler handler) {
		if (!packet.getSession().getValue().equalsIgnoreCase("Linux")) {
			// Windows
			OutputPacket.sendOutPacket("taskkill /im " + packet.getName().get() + " /f", handler);
		} else {
			Utils.showErrorMessage("Error", "Unsupported OS", "This function unsupported for this OS");
		}
	}

	// For Web
	public static void killProcess_NAME(String name, SocketHandler handler) {
		OutputPacket.sendOutPacket("taskkill /im " + name + " /f", handler);
	}

	// For Web
	public static void killProcess_PID(String pid, SocketHandler handler) {
		OutputPacket.sendOutPacket("taskkill /pid " + pid + " /t /f", handler);
	}

	public static void requestData(SocketHandler handler, boolean fastmode) {
		if (fastmode) {
			OutputPacket.sendOutPacket("/tasklistfast", handler);
		} else {
			OutputPacket.sendOutPacket("/tasklist", handler);
		}
	}
}
