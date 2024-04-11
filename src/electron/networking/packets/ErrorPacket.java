package electron.networking.packets;

import org.json.simple.JSONObject;

public class ErrorPacket {
	private String message;

	public ErrorPacket(String message) {
		this.message = message;
	}

	public String get() {
		return message;
	}

	public static ErrorPacket parseErrorPacket(JSONObject input) {
		String message = String.valueOf(input.get("message"));
		return new ErrorPacket(message);
	}
}
