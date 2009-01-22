
/*
treeNode.add(AppTreeNode treeNode)
treeNode.add(AppList list ) - appends list of apps to the null group
treeNode.add(String, String) - appends app to the null group

in the gui, the apps in the null group is listed after the groups

boolean treeNode.remove(groupName)
    removes group name
*/

/**
 * A tree of apps.
 */

public AppTreeNode extends AppCollection {

    String groupName;

    private class GroupAppInfo extends AppInfo {
	protected String groupName;
	protected AppInfo (String groupName, String appName) {
	    super(appName);
	    this.groupName = groupName;
	}
    }

    private LinkedList<GroupAppInfo> apps = new LinkedList<GroupAppInfo>;

    public void add (String GroupName, Serializable launchInfo) {
	AppInfo appInfo = findApp(appName, launchInfo);
	if (appInfo != null) {
	    // Already added
	    return;
	}

	appInfo = new AppInfo(appName, launchInfo);
	apps.add(appInfo);
    }

    public void remove (String appName, Serializable launchInfo) {
	AppInfo appInfo = findApp(appName, launchInfo);
	if (appInfo != null) {
	    // Not in list
	    return;
	}

	apps.remove(appInfo);
    }


    public void add (String appName, Serializable launchInfo) {
	AppInfo appInfo = findApp(appName, launchInfo);
	if (appInfo != null) {
	    // Already added
	    return;
	}

	appInfo = new AppInfo(appName, launchInfo);
	apps.add(appInfo);
    }

    public void remove (String appName, Serializable launchInfo) {
	AppInfo appInfo = findApp(appName, launchInfo);
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
	Iterator<AppInfo> it = apps.iterator();
	while (it.hasNext()) {
	    AppInfo srcApp = it.next();
	    AppInfo app = findApp(srcApp.appName, srcApp.launchInfo);
	    if (app != null) {
		apps.add(srcApp);
	    }
	}	
    }

    protected AppCollection union (AppTree apps) {
	// TODO
    }
    

    protected AppInfo findApp (String appName, Serializable launchInfo) {
	for (AppInfo app : apps) {
	    if (app.appName.equals(appName) &&
		app.launchInfo.equals(launchInfo)) {
		return app;
	    }
	}

	return null;
    }
}