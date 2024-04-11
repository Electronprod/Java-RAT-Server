package electron;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import electron.library.electron.updatelib.ActionListener;
import electron.library.electron.updatelib.UpdateLib;
import electron.networking.Listener;
import electron.networking.ScreenV2Receiver;
import electron.utils.Utils;
import electron.utils.logger;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class RAT_server extends Application {
	private static Stage initialStage;
	static final Double version = 1.1;

	/**
	 * @author Electron_prod (Alexander Spiridonov)
	 */
	public static void main(String[] args) {
		// Checking updates
		checkUpdates();
		// Starting network listener
		new Listener(50000).start();
		new ScreenV2Receiver().start();
		Utils.loadDir(new File("scripts"));
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

	/*
	 * Update checker using UpdateLib library (package
	 * electron.library.electron.updatelib)
	 */
	private static void checkUpdates() {
		UpdateLib updater;
		try {
			updater = new UpdateLib("https://api.github.com/repos/Electronprod/Java-RAT-Server/releases");
			updater.setActionListener(new ActionListener() {
				@Override
				public void receivedUpdates() {
					String versionobj = updater.getLastVersionJSON();
					String lastversion = updater.getTagName(versionobj);
					double newversion = Double.parseDouble(lastversion.replace("v", ""));
					if (newversion > version) {
						logger.error("-----------------[Update]-----------------");
						logger.error("New version available: " + lastversion);
						logger.error("Release notes:");
						logger.error(updater.getBody(versionobj));
						logger.error("");
						logger.error("Publish date: " + updater.getPublishDate(versionobj));
						logger.error("");
						logger.error("Download it:");
						logger.error(updater.getReleaseUrl(versionobj));
						logger.error("-----------------[Update]-----------------");
					} else {
						logger.log("[Updater] it's latest version.");
					}
				}

				@Override
				public void updateFailed() {
					System.err.println("[Updater]: error checking updates.");
				}
			});
		} catch (MalformedURLException e) {
			logger.error("[Updater]: Incorrect url. Can't check updates. Message: " + e.getMessage());
		}

	}
}
