
/**
 * An abstract collection of app information.
 */

public abstract AppCollection implements Serializable {

    protected class AppInfo {
	protected String name;
	protected AppInfo (String appName) {
	    this.appName = appName;
	}
    }

    protected class LaunchableAppInfo extends AppInfo {
	protected String launchInfo;
	protected AppInfo (String name, String launchInfo) {
	    super(name);
	    this.launchInfo = launchInfo;
	}
    }
}

