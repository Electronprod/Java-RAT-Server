package electron.utils;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.List;

import javax.imageio.ImageIO;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

public class Utils {
	/**
	 * Parse String to JSON
	 * 
	 * @param str - String to parse
	 * @return Object
	 * @throws ParseException
	 */
	public static Object ParseJsThrought(String str) throws ParseException {
		Object obj = (new JSONParser()).parse(str);
		return obj;
	}

	/**
	 * Checks if it's the right packet
	 * 
	 * @param type  - type we need
	 * @param input - packet to check
	 * @return boolean
	 */
	public static boolean isPacketType(int type, JSONObject input) {
		if (input == null) {
			return false;
		}
		if (!input.containsKey("packettype")) {
			return false;
		}
		if (Integer.parseInt(String.valueOf(input.get("packettype"))) != type) {
			return false;
		}
		return true;
	}

	/**
	 * Decode image from Base64
	 * 
	 * @param imageString - image in Base64
	 * @return BufferedImage
	 */
	public static BufferedImage base64StringToImage(String imageString) {
		BufferedImage image = null;
		byte[] imageByte;
		try {
			imageByte = Base64.getDecoder().decode(imageString);
			ByteArrayInputStream bis = new ByteArrayInputStream(imageByte);
			image = ImageIO.read(bis);
			bis.close();
		} catch (Exception e) {
			logger.error("[electron.utils.base64StringToImage][" + Thread.currentThread().getName() + "]: "
					+ e.getMessage());
		}
		return image;
	}

	public static List<String> getFileLines(String path) {
		try {
			List<String> lines = Files.readAllLines(Paths.get(path), StandardCharsets.UTF_8);
			return lines;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static String getFileLineWithSeparator(List<String> path, String splitter) {
		List<String> lines = path;
		if (lines == null) {
			return "Error loading file: null";
		}
		String result = "";
		for (int i = 0; i < lines.size(); i++) {
			result = result + lines.get(i) + splitter;
		}
		return result;
	}

	public static void loadDir(File file) {
		if (!file.exists()) {
			if (!file.mkdir()) {
				logger.error("[electron.utils.loadDir]: error creating directory.");
				return;
			}
			logger.log("[electron.utils.loadDir]: directory created.");
		}
	}

	public static void showErrorMessage(String title, String header, String message, boolean isWeb) {
		Platform.runLater(() -> {
			Alert al = new Alert(AlertType.ERROR);
			al.setTitle(title);
			al.setHeaderText(header);
			al.setContentText(message);
			al.show();
		});
	}

	public static void showMessage(String title, String header, String message, boolean isWeb) {
		Platform.runLater(() -> {
			Alert al = new Alert(AlertType.INFORMATION);
			al.setTitle(title);
			al.setHeaderText(header);
			al.setContentText(message);
			al.show();
		});
	}

	public static String removeLastChar(String str) {
		return str.substring(0, str.length() - 1);
	}
}
