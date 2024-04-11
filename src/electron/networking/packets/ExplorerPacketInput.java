package electron.networking.packets;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

public class ExplorerPacketInput {
	private JSONObject main = new JSONObject();

	public ExplorerPacketInput(JSONObject main) {
		this.main=main;
//		main.put("files", files);
//		main.put("path", path);
//		main.put("packettype", "2");
	}

	public String getPath() {
		return String.valueOf(main.get("path"));
	}
	public JSONArray getFiles() {
		return (JSONArray) main.get("files");
	}

}
