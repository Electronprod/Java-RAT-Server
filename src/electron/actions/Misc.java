package electron.actions;

import electron.networking.SocketHandler;
import electron.networking.packets.OutputPacket;

public class Misc {

	public static void toggle_overlay(SocketHandler handler) {
		OutputPacket.sendOutPacket("/overlay", handler);
	}

	public static void toggle_blockmouse(SocketHandler handler) {
		OutputPacket.sendOutPacket("/blockmouse", handler);
	}

	public static void presskeys(SocketHandler handler, String keys) {
		if (keys.isEmpty()) {
			return;
		}
		if (keys.contains(" ")) {
			OutputPacket.sendOutPacket("/presskeys " + keys, handler);
			return;
		}
		OutputPacket.sendOutPacket("/presskey " + keys, handler);
	}

	public static void toggle_screen(SocketHandler handler) {
		OutputPacket.sendOutPacket("/screen", handler);
	}

	public static void toggle_screenV2(SocketHandler handler) {
		OutputPacket.sendOutPacket("/screenv2", handler);
	}
}
