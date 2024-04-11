package electron.library.electron.updatelib;

import java.util.EventListener;

public interface ActionListener extends EventListener {
	void receivedUpdates();

	void updateFailed();
}
