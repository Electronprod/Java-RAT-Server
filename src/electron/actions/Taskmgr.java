package electron.actions;

import electron.networking.SocketHandler;
import electron.networking.packets.OutputPacket;
import electron.networking.packets.ProcessPacket;

public class Taskmgr {

	public static void killProcess_PID(ProcessPacket packet, SocketHandler handler, boolean isWeb) {
		if (!packet.getSession().getValue().equalsIgnoreCase("Linux")) {
			// Windows
			OutputPacket.sendOutPacket("taskkill /pid " + packet.getPid().getValue() + " /t /f", handler, isWeb);
		} else {
			// Linux
			OutputPacket.sendOutPacket("kill -9 " + packet.getPid().getValue(), handler, isWeb);
		}
	}

	public static void killProcess_NAME(ProcessPacket packet, SocketHandler handler, boolean isWeb) {
		if (!packet.getSession().getValue().equalsIgnoreCase("Linux")) {
			// Windows
			OutputPacket.sendOutPacket("taskkill /im " + packet.getName().get() + " /f", handler, isWeb);
		} else {
			// Linux unsupported
		}
	}

	public static void requestData(SocketHandler handler, boolean fastmode, boolean isWeb) {
		if (fastmode) {
			OutputPacket.sendOutPacket("/tasklistfast", handler, isWeb);
		} else {
			OutputPacket.sendOutPacket("/tasklist", handler, isWeb);
		}
	}
}
