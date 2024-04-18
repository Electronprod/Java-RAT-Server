package electron.actions;

import electron.networking.SocketHandler;
import electron.networking.packets.OutputPacket;

public class Misc {

	public static void toggle_overlay(SocketHandler handler, boolean isWeb) {
		OutputPacket.sendOutPacket("/overlay", handler, isWeb);
	}

	public static void toggle_blockmouse(SocketHandler handler, boolean isWeb) {
		OutputPacket.sendOutPacket("/blockmouse", handler, isWeb);
	}

	public static void presskeys(SocketHandler handler, String keys, boolean isWeb) {
		if (keys.isEmpty()) {
			return;
		}
		if (keys.contains(" ")) {
			OutputPacket.sendOutPacket("/presskeys " + keys, handler, isWeb);
			return;
		}
		OutputPacket.sendOutPacket("/presskey " + keys, handler, isWeb);
	}

	public static void toggle_screen(SocketHandler handler, boolean isWeb) {
		OutputPacket.sendOutPacket("/screen", handler, isWeb);
	}

	public static void toggle_screenV2(SocketHandler handler, boolean isWeb) {
		OutputPacket.sendOutPacket("/screenv2", handler, isWeb);
	}
}
