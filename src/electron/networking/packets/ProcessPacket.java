package electron.networking.packets;

import org.json.simple.JSONObject;

import javafx.beans.property.SimpleStringProperty;

public class ProcessPacket {
	private javafx.beans.property.SimpleStringProperty pid = new SimpleStringProperty();
	private javafx.beans.property.SimpleStringProperty name = new SimpleStringProperty();
	private javafx.beans.property.SimpleStringProperty user = new SimpleStringProperty();
	private javafx.beans.property.SimpleStringProperty state = new SimpleStringProperty();
	private javafx.beans.property.SimpleStringProperty memory = new SimpleStringProperty();
	private javafx.beans.property.SimpleStringProperty cpu_time = new SimpleStringProperty();
	private javafx.beans.property.SimpleStringProperty session = new SimpleStringProperty();
	private javafx.beans.property.SimpleStringProperty title = new SimpleStringProperty();

	/**
	 * @param pid
	 * @param name
	 * @param user
	 * @param state
	 * @param memory
	 * @param cpu_time
	 * @param session
	 * @param title
	 */
	public ProcessPacket(String pid, String name, String user, String state, String memory, String cpu_time,
			String session, String title) {
		this.pid = new SimpleStringProperty(pid.replaceAll("\"", ""));
		this.name = new SimpleStringProperty(name.replaceAll("\"", ""));
		this.user = new SimpleStringProperty(user.replaceAll("\"", ""));
		this.state = new SimpleStringProperty(state.replaceAll("\"", ""));
		this.memory = new SimpleStringProperty(memory.replaceAll("\"", ""));
		this.cpu_time = new SimpleStringProperty(cpu_time.replaceAll("\"", ""));
		this.session = new SimpleStringProperty(session.replaceAll("\"", ""));
		this.title = new SimpleStringProperty(title.replaceAll("\"", ""));
	}

	/**
	 * @return the pid
	 */
	public javafx.beans.property.SimpleStringProperty getPid() {
		return pid;
	}

	/**
	 * @return the name
	 */
	public javafx.beans.property.SimpleStringProperty getName() {
		return name;
	}

	/**
	 * @return the user
	 */
	public javafx.beans.property.SimpleStringProperty getUser() {
		return user;
	}

	/**
	 * @return the state
	 */
	public javafx.beans.property.SimpleStringProperty getState() {
		return state;
	}

	/**
	 * @return the memory
	 */
	public javafx.beans.property.SimpleStringProperty getMemory() {
		return memory;
	}

	/**
	 * @return the cpu_time
	 */
	public javafx.beans.property.SimpleStringProperty getCpu_time() {
		return cpu_time;
	}

	/**
	 * @return the session
	 */
	public javafx.beans.property.SimpleStringProperty getSession() {
		return session;
	}

	/**
	 * @return the title
	 */
	public javafx.beans.property.SimpleStringProperty getTitle() {
		return title;
	}

	/**
	 * Converts JSON to TaskListPacket
	 * 
	 * @param input - JSON
	 * @return TaskListPacket
	 */
	public static ProcessPacket parse(JSONObject input) {
		String pid = String.valueOf(input.get("pid"));
		String name = String.valueOf(input.get("name"));
		String user = String.valueOf(input.get("user"));
		String state = String.valueOf(input.get("state"));
		String memory = String.valueOf(input.get("memory"));
		String cpu_time = String.valueOf(input.get("cpu_time"));
		String session = String.valueOf(input.get("session"));
		String title = String.valueOf(input.get("title"));
		return new ProcessPacket(pid, name, user, state, memory, cpu_time, session, title);
	}
}
