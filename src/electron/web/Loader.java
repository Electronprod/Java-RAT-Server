package electron.web;

import java.io.IOException;
import java.net.InetSocketAddress;
import electron.utils.logger;
import electron.web.pages.API;
import electron.web.pages.AboutPageHandler;
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

	public void setPort(int port1) {
		port = port1;
	}

	public static void launch() {
		try {
			server = HttpServer.create(new InetSocketAddress(port), 0);
			server.createContext("/", new electron.web.pages.MainPageHandler());
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
}
