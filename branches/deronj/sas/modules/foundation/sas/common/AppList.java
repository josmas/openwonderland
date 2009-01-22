
/**
 * A simple list of apps. Can contain apps of the same name if they have different launch infos.
 */

public AppList extends AppCollection {

    private LinkedList<LaunchableAppInfo> apps = new LinkedList<LaunchableAppInfo>;

    public void add (String appName, String launchInfo) {
	LaunchableAppInfo appInfo = findApp(appName, launchInfo);
	if (appInfo != null) {
	    // Already added
	    return;
	}

	appInfo = new LaunchableAppInfo(appName, launchInfo);
	apps.add(appInfo);
    }

    public void remove (String appName, String launchInfo) {
	LaunchableAppInfo appInfo = findApp(appName, launchInfo);
	if (appInfo != null) {
	    // Not in list
	    return;
	}

	apps.remove(appInfo);
    }

    public AppCollection union (AppCollection apps) {
	if (apps instanceof AppList) {
	    union((AppList)apps);
	    return this;
	} else if (apps instanceof AppTree) {
	    return union((AppTree)apps);
	}
    }

    protected void union (AppList apps) {
	Iterator<LaunchableAppInfo> it = apps.iterator();
	while (it.hasNext()) {
	    LaunchableAppInfo srcApp = it.next();
	    LaunchableAppInfo app = findApp(srcApp.name, srcApp.launchInfo);
	    if (app != null) {
		apps.add(srcApp);
	    }
	}	
    }

    /*
    protected AppCollection union (AppTree apps) {
	// TODO
    }
    */    

    protected LaunchableAppInfo findApp (String appName, String launchInfo) {
	for (LaunchableAppInfo app : apps) {
	    if (app.name.equals(appName) &&
		app.launchInfo.equals(launchInfo)) {
		return app;
	    }
	}

	return null;
    }
}