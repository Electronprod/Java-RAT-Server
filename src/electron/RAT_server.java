package electron;

import java.awt.GraphicsEnvironment;
import java.io.File;
import java.net.URL;

import electron.console.Console;
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
	 * 0 - enable all modes <br>
	 * 1 - console + WebSite <br>
	 * 2 - only <b>GUI mode</b> <br>
	 * 3 - only <b>WebSite</b> mode<br>
	 * 4 - only <b>Console</b>
	 */
	private static int mode = 0;

	/**
	 * @author Electron_prod (Alexander Spiridonov)
	 */
	public static void main(String[] args) {
		int inputmode = 0;
		try {
			if (GraphicsEnvironment.isHeadless()) {
				inputmode = 5;
			}
			if (args.length > 0) {
				for (String arg : args) {
					String[] parts = arg.split(":");
					if (parts.length == 2) {
						String key = parts[0];
						String value = parts[1];
						if (key.toLowerCase().equals("mode")) {
							inputmode = Integer.valueOf(value);
						}
					}
				}
			}
		} catch (Exception e) {
			logger.error("Error parsing args: " + e.getMessage());
		}
		logger.log("Starting application in mode: " + inputmode);
		// Starting network listener
		new Listener(50000).start();
		new ScreenV2Receiver().start();
		Utils.loadDir(new File("scripts"));
		switch (inputmode) {
		case 0:
			Loader.launch();
			Console.launch();
			launch(args);
		case 1:
			Loader.launch();
			Console.launch();
		case 2:
			launch(args);
		case 3:
			Loader.launch();
		case 4:
			Console.launch();
		default:
			logger.error("Error starting app. Unknown start mode: " + inputmode);
		}
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
	 * Get current program mode
	 * 
	 * @return int mode
	 * @apiNote 0 - enable all modes <br>
	 *          1 - console + WebSite <br>
	 *          2 - only <b>GUI mode</b> <br>
	 *          3 - only <b>WebSite</b> mode<br>
	 *          4 - only <b>Console</b>
	 */
	public static int getMode() {
		return mode;
	}

	/**
	 * Get initial JavaFX stage
	 * 
	 * @return Stage initialStage
	 */
	public static Stage getInitialStage() {
		return initialStage;
	}

	/**
	 * Get initial JavaFX scene
	 * 
	 * @return Scene initialScene
	 */
	public static Scene getInitialScene() {
		return initialScene;
	}
}
