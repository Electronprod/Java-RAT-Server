package electron.web;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

import electron.networking.NetData;
import electron.utils.FileOptions;
import electron.utils.logger;
import electron.web.pages.API;
import electron.web.pages.AboutPageHandler;
import electron.web.pages.AuthAPIHandler;
import electron.web.pages.ConsolePageHandler;
import electron.web.pages.ExplorerPageHandler;
import electron.web.pages.FullScreenPageHandler;
import electron.web.pages.ScreenPageHandler;
import electron.web.pages.TaskmgrPageHandler;

import com.sun.net.httpserver.HttpServer;

@SuppressWarnings("restriction")
public class Loader {
	private static int port = 80;
	private static HttpServer server;
	private static String password;
	private static String login;

	public void setPort(int port1) {
		port = port1;
	}

	public static String getLogin() {
		return login;
	}

	public static String getPassword() {
		return password;
	}

	public static void launch() {
		launchAuth();
		try {
			server = HttpServer.create(new InetSocketAddress(port), 0);
			server.createContext("/", new electron.web.pages.MainPageHandler());
			server.createContext("/auth", new AuthAPIHandler());
			server.createContext("/taskmgr", new TaskmgrPageHandler());
			server.createContext("/explorer", new ExplorerPageHandler());
			server.createContext("/license", new AboutPageHandler());
			server.createContext("/screen", new ScreenPageHandler());
			server.createContext("/fullscreen", new FullScreenPageHandler());
			server.createContext("/console", new ConsolePageHandler());
			server.createContext("/api/connections", API.getConnections());
			server.createContext("/api/", API.GetSocketAPI());
			server.setExecutor(null);
			server.start();
			logger.log("[electron.web.loader]: started Web server on " + server.getAddress().toString());
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("[electron.web.loader]: error launching Web server. Message: " + e.getMessage());
		}
	}

	@SuppressWarnings("unchecked")
	private static void launchAuth() {
		File authfile = new File("auth.json");
		FileOptions.loadFile(authfile);
		if (FileOptions.getFileLines(authfile.getPath()).isEmpty()) {
			logger.log("[WebAuth]: creating JSON map for storage...");
			JSONObject main = new JSONObject();
			main.put("login", "electron"); // Default login is "electron"
			main.put("password", "prod"); // Default password is "prod"
			FileOptions.writeFile(main.toJSONString(), authfile);
			logger.log("[WebAuth]: map created.");
		}
		try {
			logger.log("[WebAuth]: loading storage...");
			JSONObject main = (JSONObject) FileOptions.ParseJsThrought(FileOptions.getFileLine(authfile));
			login = String.valueOf(main.get("login"));
			password = String.valueOf(main.get("password"));
			main.put("ips", new JSONArray());
			FileOptions.writeFile(main.toJSONString(), authfile);
			logger.log("[WebAuth]: loaded storage file.");
		} catch (ParseException e) {
			logger.error("[WebAuth]: error loading storage file. Reason: " + e.getMessage());
			System.exit(1);
		}
	}

	public static boolean addAuthed(com.sun.net.httpserver.HttpExchange exchange) {
		String url = WebUtils.getUrl(exchange);
		if (!url.contains("?login=")) {
			return false;
		}
		String[] spl = url.split("\\?login=");
		String login = spl[1];
		if (login.contains("&")) {
			login = login.split("&")[0];
		} else {
			return false;
		}
		if (!login.equals(getLogin())) {
			return false;
		}
		if (!url.contains("&pass=")) {
			return false;
		}
		String pass = url.split("&pass=")[1];
		if (!pass.equals(getPassword())) {
			return false;
		}
		try {
			JSONObject main = (JSONObject) FileOptions.ParseJsThrought(FileOptions.getFileLine(new File("auth.json")));
			JSONArray ips = (JSONArray) main.get("ips");
			if (ips == null) {
				ips = new JSONArray();
			}
			if (ips.contains(exchange.getRemoteAddress().getHostName())) {
				return true;
			}
			ips.add(exchange.getRemoteAddress().getHostName());
			main.put("ips", ips);
			FileOptions.writeFile(main.toJSONString(), new File("auth.json"));
		} catch (ParseException e) {
			logger.error("[WebAuth]: error loading storage file. Sending rejection. Reason: " + e.getMessage());
			return false;
		}
		return true;
	}

	public static boolean isAuthed(com.sun.net.httpserver.HttpExchange exchange) {
		try {
			JSONObject main = (JSONObject) FileOptions.ParseJsThrought(FileOptions.getFileLine(new File("auth.json")));
			JSONArray ips = (JSONArray) main.get("ips");
			if (ips == null) {
				return false;
			}
			String userIP = exchange.getRemoteAddress().getHostName();
			if (ips.contains(userIP)) {
				return true;
			}
			return false;
		} catch (ParseException e) {
			logger.error("[WebAuth]: error loading storage file. Sending rejection. Reason: " + e.getMessage());
			return false;
		} catch (Exception ex) {
			logger.error("[WebAuth]: error processing request. Reason: " + ex.getMessage());
			ex.printStackTrace();
			return false;
		}
	}

	public static boolean checkAuth(com.sun.net.httpserver.HttpExchange exchange) {
		if (isAuthed(exchange)) {
			return true;
		}
		WebUtils.sendResponse(exchange, WebUtils.getResource("auth.html"), 401);
		return false;
	}
}
