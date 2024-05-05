package electron.web.pages;

import java.io.IOException;

import com.sun.net.httpserver.HttpHandler;

import electron.web.Loader;
import electron.web.WebUtils;

public class FullScreenPageHandler implements HttpHandler {
	@Override
	public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
		if (!Loader.checkAuth(exchange)) {
			return;
		}
		String response = WebUtils.getResource("fullscreen.html");
		WebUtils.sendResponse(exchange, response, 200);
	}
}
