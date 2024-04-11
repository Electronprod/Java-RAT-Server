package electron.networking.packets;

import org.json.simple.JSONObject;

public class OutputPacket {
	private JSONObject main = new JSONObject();
	
	public OutputPacket(String command) {
		main.put("command", command);
		main.put("packettype","0");
	}
	public String get() {
		return main.toJSONString();
	}

}
