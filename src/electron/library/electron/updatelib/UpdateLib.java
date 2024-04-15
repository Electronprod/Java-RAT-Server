package electron.library.electron.updatelib;

import java.net.MalformedURLException;
import java.net.URL;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class UpdateLib {
	/**
	 * DO NOT TOUCH!!!
	 */
	public URL url;
	/**
	 * DO NOT TOUCH!!!
	 */
	public JSONArray versionsarr;
	private ParserThread parseclass;

	/**
	 * Initialize library method
	 * 
	 * @param url - GitHub API program link
	 * @throws MalformedURLException
	 */
	public UpdateLib(String urlStr) throws MalformedURLException {
		url = new URL(urlStr);
		parseclass = new ParserThread(this);
		Thread parsethread = parseclass;
		parsethread.start();
	}

	public void setActionListener(ActionListener l) {
		parseclass.setActionListener(l);
	}

	/**
	 * Get last release
	 * 
	 * @return String jsonobject
	 */
	public String getLastVersionJSON() {
		JSONObject lastversion = (JSONObject) versionsarr.get(0);
		return lastversion.toJSONString();
	}

	/**
	 * Get all releases data
	 * 
	 * @return String jsonarray
	 */
	public String getJSON() {
		return versionsarr.toJSONString();
	}

	public static boolean isDraft(String version) {
		JSONObject versionJSON = (JSONObject) ParserThread.ParseJs(version);
		if (Boolean.parseBoolean(String.valueOf(versionJSON.get("draft")))) {
			return true;
		} else {
			return false;
		}
	}

	public static boolean isPreRelease(String version) {
		JSONObject versionJSON = (JSONObject) ParserThread.ParseJs(version);
		if (Boolean.parseBoolean(String.valueOf(versionJSON.get("prerelease")))) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * Get release's url
	 * 
	 * @param String version
	 * @return String url
	 */
	public static String getReleaseUrl(String version) {
		JSONObject versionJSON = (JSONObject) ParserThread.ParseJs(version);
		return String.valueOf(versionJSON.get("html_url"));
	}

	/**
	 * Get release's publish date
	 * 
	 * @param String version
	 * @return String date
	 */
	public static String getPublishDate(String version) {
		JSONObject versionJSON = (JSONObject) ParserThread.ParseJs(version);
		return String.valueOf(versionJSON.get("published_at"));
	}

	/**
	 * Get release's create date
	 * 
	 * @param String version
	 * @return String date
	 */
	public static String getCreateDate(String version) {
		JSONObject versionJSON = (JSONObject) ParserThread.ParseJs(version);
		return String.valueOf(versionJSON.get("created_at"));
	}

	/**
	 * Get release's name
	 * 
	 * @param String version
	 * @return String name
	 */
	public static String getName(String version) {
		JSONObject versionJSON = (JSONObject) ParserThread.ParseJs(version);
		return String.valueOf(versionJSON.get("name"));
	}

	/**
	 * Get release's tagname
	 * 
	 * @param String version
	 * @return String tagname
	 */
	public static String getTagName(String version) {
		JSONObject versionJSON = (JSONObject) ParserThread.ParseJs(version);
		return String.valueOf(versionJSON.get("tag_name"));
	}

	/**
	 * Get release's body
	 * 
	 * @param String version
	 * @return String body
	 */
	public static String getBody(String version) {
		JSONObject versionJSON = (JSONObject) ParserThread.ParseJs(version);
		return String.valueOf(versionJSON.get("body"));
	}

	/**
	 * Get all info about author
	 * 
	 * @param String version
	 * @return String jsonobject
	 */
	public static String getAuthor(String version) {
		JSONObject versionJSON = (JSONObject) ParserThread.ParseJs(version);
		return String.valueOf(versionJSON.get("author"));
	}

	/**
	 * Get assets for version
	 * 
	 * @param String version
	 * @return String jsonarray
	 */
	public static String getAssets(String version) {
		JSONObject versionJSON = (JSONObject) ParserThread.ParseJs(version);
		return String.valueOf(versionJSON.get("assets"));
	}
}