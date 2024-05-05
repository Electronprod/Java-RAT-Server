package electron.networking.packets;

import org.json.simple.JSONObject;
import javafx.beans.property.SimpleStringProperty;

public class ClientInfo {
	private javafx.beans.property.SimpleStringProperty os = new SimpleStringProperty("os");
	private javafx.beans.property.SimpleStringProperty address = new SimpleStringProperty("address");
	private javafx.beans.property.SimpleStringProperty username = new SimpleStringProperty("username");
	private javafx.beans.property.SimpleStringProperty country = new SimpleStringProperty("country");
	private javafx.beans.property.SimpleStringProperty nativeimage = new SimpleStringProperty("native-image");
	private javafx.beans.property.SimpleStringProperty headless = new SimpleStringProperty("headless");

	public ClientInfo(String os, String address, String username, String country, String nativeimage, String headless) {
		this.os = new SimpleStringProperty(os);
		this.address = new SimpleStringProperty(address);
		this.username = new SimpleStringProperty(username);
		this.country = new SimpleStringProperty(country);
		this.nativeimage = new SimpleStringProperty(nativeimage);
		this.headless = new SimpleStringProperty(headless);
	}

	public javafx.beans.property.SimpleStringProperty HeadlessProperty() {
		return headless;
	}

	public javafx.beans.property.SimpleStringProperty OsProperty() {
		return os;
	}

	public javafx.beans.property.SimpleStringProperty AddressProperty() {
		return address;
	}

	public javafx.beans.property.SimpleStringProperty UsernameProperty() {
		return username;
	}

	public javafx.beans.property.SimpleStringProperty CountryProperty() {
		return country;
	}

	public javafx.beans.property.SimpleStringProperty NativeImageProperty() {
		return nativeimage;
	}

	public String getOs() {
		return os.get();
	}

	public String getAddress() {
		return address.get();
	}

	public String getUsername() {
		return username.get();
	}

	public String getCountry() {
		return country.get();
	}

	public String getNativeimage() {
		return nativeimage.get();
	}

	public String getHeadless() {
		return headless.get();
	}

	public static ClientInfo parse(JSONObject input) {
		String os = String.valueOf(input.get("os"));
		String net_address = String.valueOf(input.get("net_address"));
		String username = String.valueOf(input.get("username"));
		String country = String.valueOf(input.get("country"));
		String nativeimage = String.valueOf(input.get("native-image"));
		String headless = String.valueOf(input.get("headless"));
		return new ClientInfo(os, net_address, username, country, nativeimage, headless);
	}
}
