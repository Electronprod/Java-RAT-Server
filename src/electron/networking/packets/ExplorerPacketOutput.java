package electron.networking.packets;

import org.json.simple.JSONObject;

public class ExplorerPacketOutput {
	private JSONObject main = new JSONObject();

	@SuppressWarnings("unchecked")
	public ExplorerPacketOutput(String path, String command) {
		main.put("command", command);
		main.put("path", path);
		main.put("packettype", "1");
	}

	public String get() {
		return main.toJSONString();
	}

}
