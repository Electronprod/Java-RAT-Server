package electron.networking.packets;

import org.json.simple.JSONObject;

public class InputMessage {
	private String message;
	public InputMessage(String message) {
		this.message = message;
	}

	public String getMessage() {
		return message;
	}

	public static InputMessage parse(JSONObject input) {
		String message = String.valueOf(input.get("message"));
		return new InputMessage(message);
	}
}
