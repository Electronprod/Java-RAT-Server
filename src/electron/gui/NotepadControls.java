package electron.gui;

import java.io.IOException;
import java.net.URL;

import electron.RAT_server;
import electron.networking.packets.EditPacket;
import electron.utils.logger;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class NotepadControls {
	private static String text;
	private String text1;
	private static String path;
	private String path1;

	@FXML
	private TextArea notepad;

	@FXML
	private void saveAction() {
		if (MainWindowControls.handler == null) {
			return;
		}
		if (!MainWindowControls.handler.send(new EditPacket(path1, notepad.getText()).get().toJSONString())) {
			logger.error("[electron.user.NotepadControls.saveAction]: error sending packet.");
			return;
		}
	}

	@FXML
	private void cancelAction() {
		Stage toClose = (Stage) notepad.getScene().getWindow();
		toClose.close();
	}

	@FXML
	private void initialize() {
		text1 = text;
		path1 = path;
		notepad.setText(text1);
	}

	public static void edit(String content, String path2) {
		try {
			text = content;
			path = path2;
			Stage notepadstage = new Stage();
			URL url = RAT_server.class.getResource("/resources/notepad.fxml");
			logger.log("[Graphics]: loading .fxml config: " + url);
			FXMLLoader loader = new FXMLLoader(url);
			Scene scene = new Scene(loader.load());
			notepadstage.setTitle("Edit file: " + path);
			notepadstage.setScene(scene);
			Image img = new Image(RAT_server.class.getResourceAsStream("/resources/appicon.png"));
			notepadstage.getIcons().add(img);
			notepadstage.show();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
