package electron.web.pages;

import java.io.IOException;

import org.json.simple.JSONObject;

import com.sun.net.httpserver.HttpHandler;

import electron.utils.logger;
import electron.web.Loader;
import electron.web.WebUtils;

public class AuthAPIHandler implements HttpHandler {
	@Override
	public void handle(com.sun.net.httpserver.HttpExchange exchange) throws IOException {
		JSONObject answer = new JSONObject();
		if (Loader.addAuthed(exchange)) {
			logger.warn("[WebAuth]: authed new user from " + exchange.getRemoteAddress().toString());
			answer.put("result", "success");
		} else {
			answer.put("result", "fail");
		}
		WebUtils.sendResponse(exchange, answer.toJSONString(), 200);
	}
}
