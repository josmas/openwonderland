_This material is distributed under the_<a href='http://www.gnu.org/licenses/gpl-2.0.html'>GNU General Public License Version 2</a>_._To obtain a copy of the original source code, make a request on the <a href='http://groups.google.com/group/openwonderland'>Wonderland Forum</a>_._

## Open Wonderland v0.5: Launching Clients using Java Web Start ##
by Jordan Slott (jslott@dev.java.net) and Jonathan Kaplan (jonathankap@gmail.com)


### Introduction ###

Support for [Java Web Start](http://java.sun.com/javase/technologies/desktop/javawebstart/index.jsp) is built into Open Wonderland v0.5. The Wonderland server automatically supports launching clients from Java Web Start when you compile Open Wonderland from the source code (or from binary releases in the future). With Java Web Start, clients only need to open a URL in a web browser, and as long as Java SE 6 is installed on their system, the Open Wonderland client will be automatically downloaded and launched.

### The Open Wonderland Server URL ###

When you start the Open Wonderland server, a web server is started which enables users to launch the client. This web page defaults to port 8080. For example, if the host name of your server is www.company.com, then the URL of the Open Wonderland client page will be http://www.company.com:8080. If you are unsure what the host name of your machine is, you can use the IP address of your server instead.

When you start the server, it displays a message with the URL of the web server. For example:

```
     [java] -----------------------------------------------------------
     [java] Wonderland web server started successfully.
     [java] Log files are in /Users/me/.wonderland-server/0.5-dev/log
     [java] Web server running on http://129.148.173.164:8080/
     [java] -----------------------------------------------------------
```


### Launching a Client ###

If you open the URL in a web browser, you should see the page below. Click the "Launch Wonderland 0.5" button to launch the client. It only needs to download the client software once; future attempts will use a cached version of the software. (click the image for a full-sized version).

![![](http://openwonderland.googlecode.com/svn/www/wiki/webstart/client-webpage-thumbnail.png)](http://openwonderland.googlecode.com/svn/www/wiki/webstart/client-webpage.png)

### Displaying the Error Log ###

When debugging a problem with Wonderland, it is often helpful to open the error log. This displays error messages that may explain problems you are experiencing in Wonderland.  To open the error log, select the "Error Log" item from the "Help" menu:

![http://openwonderland.googlecode.com/svn/www/wiki/webstart/error-log-menu.png](http://openwonderland.googlecode.com/svn/www/wiki/webstart/error-log-menu.png)

This will bring up the error log window, which displays messages. You may be asked to configure the error log, for example to show more detail about a certain problem. If so, you can use the "configure" button on the error log window:

![http://openwonderland.googlecode.com/svn/www/wiki/webstart/log-viewer.png](http://openwonderland.googlecode.com/svn/www/wiki/webstart/log-viewer.png)

If you are submitting a bug report to the Wonderland forum or the issue tracker, you will want to generate an error report. This includes lots of extra data to help track down the problem. To generate an error report, click on the "error report" button on the error log window shown above. Then copy and paste the resulting report into the bug report or forum post:

![http://openwonderland.googlecode.com/svn/www/wiki/webstart/error-report.png](http://openwonderland.googlecode.com/svn/www/wiki/webstart/error-report.png)


### Displaying the Java Console ###

In some situations, the error needed to debug Wonderland may not be included in the error log. In this case, you may need to turn on the Java console, which displays all log messages for the client. To turn on the Java console:

**Showing the Java Console on Windows**

  1. Make sure the Open Wonderland client is not running
  1. From the Start menu, select Control Panel.
  1. In the Control Panel, click on Java.
  1. In the Java Control Panel, click on the Advanced tab.
  1. Click on the + before Debugging and select Enable logging.
  1. Click on the + before Java console and click on Show console.
  1. Click OK

**Showing the Java Console on Mac OS**

  1. Make sure the Open Wonderland client is not running
  1. Navigate to Applications --> Utilities --> Java
  1. Open Java Preferences
  1. Click the Advanced tab
  1. Under the Debugging heading, check Enable logging
  1. Under the Java console heading, click Show console
  1. Click Save and close the Java Preference dialog

**Showing the Java Console on Linux/Solaris**

  1. Make sure the Open Wonderland client is not running
  1. From a terminal, enter **javaws -viewer**. Two windows should appear, one titled "Java Control Panel" and another titled "Java Cache Viewer".
  1. On the window titled "Java Cache Viewer", click the Close button.
  1. In the Java Control Panel, click on the Advanced tab.
  1. Click on the + before Debugging and select Enable logging.
  1. Click on the + before Java console and click on Show console.
  1. Click OK