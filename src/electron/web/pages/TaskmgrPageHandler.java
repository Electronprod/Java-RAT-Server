package electron.web.pages;

import java.io.IOException;
import java.util.TreeMap;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.sun.net.httpserver.HttpHandler;

import electron.networking.NetData;
import electron.networking.SocketHandler;
import electron.web.WebUtils;

public class TaskmgrPageHandler implements HttpHandler {
	@Override
	public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
		String page = WebUtils.getResource("taskmgr.html");
		WebUtils.sendResponse(exchange, page, 200);
	}
}
