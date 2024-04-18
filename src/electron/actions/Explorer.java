package electron.actions;

import java.io.File;

import org.json.simple.JSONObject;

import electron.networking.FileReceiver;
import electron.networking.FileSender;
import electron.networking.Listener;
import electron.networking.SocketHandler;
import electron.networking.packets.ExplorerPacketOutput;
import electron.networking.packets.OutputPacket;
import electron.utils.Utils;

public class Explorer {

	public static void runFile(SocketHandler handler, String path, String f, boolean isWeb) {
		if (handler.isWindows()) {
			// Windows
			OutputPacket.sendOutPacket("cd " + path + "&start " + f, handler, isWeb);
		} else {
			// Linux
			OutputPacket.sendOutPacket("cd " + path + "; ./" + f, handler, isWeb);
		}
		Utils.showMessage("runAction", "Success", "Started file: " + f, isWeb);
	}

	public static void runListener(SocketHandler handler, String path, String f, boolean isWeb) {
		if (!handler.isWindows()) {
			Utils.showErrorMessage("Error", "Unsupported OS", "Unsupported action for this OS", isWeb);
			return;
		}
		OutputPacket.sendOutPacket("cd " + path + "&" + "\"" + f + "\"", handler, isWeb);
		Utils.showMessage("runListenerAction", "Success", "Started file: " + f, isWeb);
	}

	public static String openPath(String path, String file) {
		if (path.endsWith("/") || path.endsWith("\\")) {
			path = path + file;
		} else {
			path = path + "/" + file;
		}
		return path;
	}

	public static String getParent(SocketHandler handler, String path, boolean isWeb) {
		if (!path.contains("/") && !path.contains("\\") || handler == null) {
			Utils.showErrorMessage("Incorrect action", "Incorrect path", "Incorrect path.", isWeb);
			return path;
		}
		if (handler.isWindows()) {
			// Windows root
			if (path.equalsIgnoreCase("C:\\") || path.equalsIgnoreCase("C:/")) {
				Utils.showErrorMessage("Incorrect action", "Impossible action.", "Impossible action.", isWeb);
				return path;
			}
		} else {
			// Linux root
			if (path.equals("/")) {
				Utils.showErrorMessage("Incorrect action", "Impossible action.", "Impossible action.", isWeb);
				return path;
			}
		}
		if (path.endsWith("/") || path.endsWith("//")) {
			path = Utils.removeLastChar(path);
			return path;
		}
		if (path.endsWith("//") || path.endsWith("\\\\")) {
			path = Utils.removeLastChar(path);
			path = Utils.removeLastChar(path);
			return path;
		}
		while (!path.endsWith("/")) {
			path = Utils.removeLastChar(path);
			if (handler.isWindows() && path.endsWith(":\\")) {
				return path;
			}
			if (path.endsWith("\\")) {
				break;
			}
		}
		path = Utils.removeLastChar(path);
		return path;
	}

	public static String getParentNative(String path) {
		File f = new File(path);
		return f.getParent();
	}

	public static boolean createFile(SocketHandler handler, String path, String file) {
		String path1 = path;
		if (path.endsWith("/") || path.endsWith("\\")) {
			path = path + file;
		} else {
			path = path + "/" + file;
		}
		ExplorerPacketOutput packet = new ExplorerPacketOutput(path1, "create " + path);
		return handler.send(packet.get());
	}

	public static boolean deleteFile(SocketHandler handler, String path, String file) {
		String path1 = path;
		if (path.endsWith("/") || path.endsWith("\\")) {
			path = path + file;
		} else {
			path = path + "/" + file;
		}
		ExplorerPacketOutput packet = new ExplorerPacketOutput(path1, "del " + path);
		return handler.send(packet.get());
	}

	public static void play(SocketHandler handler, String path, String file, boolean isWeb) {
		if (path.endsWith("/") || path.endsWith("\\")) {
			path = path + file;
		} else {
			path = path + "/" + file;
		}
		OutputPacket.sendOutPacket("/player " + path, handler, isWeb);
		if (!isWeb) {
			OutputPacket.sendOutPacket("/player soundpacket", handler, false);
		}
	}

	public static boolean lauch_uploader(SocketHandler handler, String path, File file) {
		// Starting server
		new FileSender(file).start();
		// Starting receiver
		JSONObject packet = new JSONObject();
		packet.put("packettype", "2");
		packet.put("type", "1");
		packet.put("path", path + "/" + file.getName());
		return handler.send(packet.toJSONString());
	}

	public static boolean lauch_downloader(SocketHandler handler, String path, String file) {
		if (path.endsWith("/") || path.endsWith("\\")) {
			path = path + file;
		} else {
			path = path + "/" + file;
		}
		JSONObject packet = new JSONObject();
		packet.put("packettype", "2");
		packet.put("type", "0");
		packet.put("path", path);
		if (System.getProperty("os.name").toLowerCase().contains("windows")) {
			new FileReceiver(Listener.getPort() + 1, System.getProperty("user.home") + "/Downloads/" + file).start();
		} else {
			new FileReceiver(Listener.getPort() + 1, System.getProperty("user.home") + "/" + file).start();
		}
		return handler.send(packet.toJSONString());
	}

	public static boolean updateExplorer(SocketHandler handler, String path) {
		ExplorerPacketOutput packet = new ExplorerPacketOutput(path, "");
		return handler.send(packet.get());
	}
}
