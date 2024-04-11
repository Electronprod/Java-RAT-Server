package electron.networking.packets;

import org.json.simple.JSONObject;

public class EditPacket {
	private String filepath;
	private String content;

	public EditPacket(String filepath, String content) {
		this.filepath = filepath;
		this.content = content;
	}

	public JSONObject get() {
		JSONObject main = new JSONObject();
		main.put("packettype", "4");
		main.put("content", content);
		main.put("filepath", filepath);
		return main;
	}
}
