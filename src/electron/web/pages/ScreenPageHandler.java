package electron.web.pages;

import java.io.IOException;

import com.sun.net.httpserver.HttpHandler;

import electron.web.WebUtils;

public class ScreenPageHandler implements HttpHandler {
	@Override
	public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
		String response = WebUtils.getResource("screen.html");
		WebUtils.sendResponse(exchange, response, 200);
	}

}
