package electron.library.electron.updatelib;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import org.json.simple.JSONArray;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

class ParserThread extends Thread {
	private UpdateLib lib;
	private ActionListener listener;

	public ParserThread(UpdateLib l) {
		lib = l;
	}

	public void run() {
		int i = 0;
		while (i < 5) {
			if (updateData()) {
				break;
			}
			i++;
		}
		if (i != 5) {
			// If update parsed successfully
			if (listener != null) {
				listener.receivedUpdates();
			}
		} else {
			// If update parsing failed
			if (listener != null) {
				listener.updateFailed();
			}
		}
	}

	public void setActionListener(ActionListener listener) {
		this.listener = listener;
	}

	/**
	 * Parse information from GitHub
	 * 
	 * @return fail/success (false/true)
	 */
	public boolean updateData() {
		try {
			HttpURLConnection con = (HttpURLConnection) lib.url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Accept", "application/json");

			int status = con.getResponseCode();
			if (status == 200) {
				BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
				String inputLine;
				StringBuilder content = new StringBuilder();
				while ((inputLine = in.readLine()) != null) {
					content.append(inputLine);
				}
				in.close();
				con.disconnect();
				lib.versionsarr = (JSONArray) ParseJs(content.toString());
				return true;
			}
			return false;
		} catch (IOException e) {
			System.out.println(e.getMessage());
			return false;
		}
	}

	/**
	 * JSON format checker & loader
	 * 
	 * @param d - string in JSON format
	 * @return Object JSON
	 */
	public static Object ParseJs(String d) {
		if (d == null) {
			System.err.println("[UpdateLib] Error parsing JSON. \nError message: input string = null");
			return null;
		}
		try {
			Object obj = (new JSONParser()).parse(d);
			return obj;
		} catch (ParseException e) {
			System.err.println("[UpdateLib] Error parsing JSON. \nError message: " + e.getMessage());
			return null;
		}
	}
}