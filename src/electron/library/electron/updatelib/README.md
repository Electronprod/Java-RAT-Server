# Electron's UpdateLib
### What is it?
This is a library for checking updates on GitHub.
## 📙 Requirement
* Make sure you have Java 8 or higher version installed.
## ⚙️ Adding it to your project
* Download *[latest release](https://github.com/Electronprod/UpdateLib/releases)*
* Add UpdateLib.jar file to your project's build path.
## Code example from *[TimeTableSite project](https://github.com/Electronprod/TimeTableSite)*
```
static final Double version = 1.1;

  private static void checkUpdates() throws MalformedURLException {
		UpdateLib updater = new UpdateLib("https://api.github.com/repos/Electronprod/TimeTableSite/releases");
		updater.setActionListener(new ActionListener() {
            @Override
            public void reveivedUpdates() {
            	String versionobj = updater.getLastVersionJSON();
            	String lastversion = updater.getTagName(versionobj);
            	double newversion = Double.parseDouble(lastversion.replace("v", ""));
            	if(newversion>version) {
            		logger.error("-----------------[Update]-----------------");
            		logger.error("New version available: "+lastversion);
            		logger.error("Release notes:");
            		logger.error(updater.getBody(versionobj));
            		logger.error("");
            		logger.error("Publish date: "+updater.getPublishDate(versionobj));
            		logger.error("");
            		logger.error("Download it:");
            		logger.error(updater.getReleaseUrl(versionobj));
            		logger.error("-----------------[Update]-----------------");
            	}else {
            		logger.log("[Updater] it's latest version.");
            	}
            }
  
			@Override
			public void updateFailed() {
				System.err.println("[Updater] error checking updates.");
			}
        });
	}
 ```
