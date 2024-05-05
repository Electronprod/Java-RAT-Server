package electron.web.pages;

import java.io.IOException;

import com.sun.net.httpserver.HttpHandler;

import electron.web.Loader;
import electron.web.WebUtils;

public class ConsolePageHandler implements HttpHandler {
	@Override
	public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
		if (!Loader.checkAuth(exchange)) {
			return;
		}
		String response = WebUtils.getResource("console.html");
		WebUtils.sendResponse(exchange, response, 200);
	}

}
