<%-- 
    Document   : index
    Created on : Aug 7, 2008, 4:31:15 PM
    Author     : jkaplan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<link href="modules.css" rel="stylesheet" type="text/css" media="screen" />
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Project Wonderland Module Management</title>
    </head>
    <body>
        <div id="header">
            <img style="float:left" src="images/banner-left.jpg" />
            <img src="images/banner-right-wonderland.jpg" />
            <h1>
                <span id="labs">Project Wonderland</span><br/>
                Module Management
            </h1>
        </div>
        <div id="contents">
            <h3>Installed Modules</h3>
            <form action="removeAll.jsp">
                <table class="installed">
                    <tr class="header">
                        <td width="5%" class="installed"></td>
                        <td width="15%" class="installed"><b>Module Name</b></td>
                        <td width="15%" class="installed"><b>Module Version</b></td>
                        <td width="65%" class="installed"><b>Description</b></td>
                    </tr>
                    <%@ page import="org.jdesktop.wonderland.modules.ModuleUtils" %>
                    <%@ page import="org.jdesktop.wonderland.modules.service.ModuleManager" %>
                    <%@ page import="org.jdesktop.wonderland.modules.service.InstalledModule" %>
                    <%@ page import="org.jdesktop.wonderland.modules.service.ModuleManager.State" %>
                    <% ModuleManager mm = ModuleManager.getModuleManager();%>
                    <% String modules[] = mm.getModules(State.INSTALLED).toArray(new String[]{});%>
                    <% for (String moduleName : modules) {%>
                    <% InstalledModule im = (InstalledModule) mm.getModule(moduleName, State.INSTALLED);%>
                    <tr class="installed_a">
                        <td width="5%" class="installed"><input type="checkbox" name="remove" value="<%= moduleName%>"/></td>
                        <td width="15%" class="installed"><%= moduleName%></td>
                        <td width="15%" class="installed">v<%= im.getModuleInfo().getMajor()%>.<%= im.getModuleInfo().getMinor()%></td>
                        <td width="65%" class="installed"><%= im.getModuleInfo().getDescription()%></td>
                    </tr>
                    <% }%>
                </table>
                <input type="submit" value="Remove Selected Modules">
            </form>
            <h3>Pending Modules (will be installed during next restart)</h3>
            <table class="installed">
                <tr class="header">
                    <td width="15%" class="installed"><b>Module Name</b></td>
                    <td width="15%" class="installed"><b>Module Version</b></td>
                    <td width="70%" class="installed"><b>Description</b></td>
                </tr>
                <%@ page import="org.jdesktop.wonderland.modules.service.PendingModule" %>
                <% for (String pendingModuleName : mm.getModules(State.PENDING).toArray(new String[] {})) {%>
                <% PendingModule pm = (PendingModule)mm.getModule(pendingModuleName, State.PENDING);%>
                <tr class="installed_a">
                    <td width="15%" class="installed"><%= pendingModuleName%></td>
                    <td width="15%" class="installed">v<%= pm.getModuleInfo().getMajor()%>.<%= pm.getModuleInfo().getMinor()%></td>
                    <td width="70%" class="installed"><%= pm.getModuleInfo().getDescription()%></td>
                </tr>
                <% }%>
            </table>
            <br>
            <h3>Removed Modules (will be removed during next restart)</h3>
            <table class="installed">
                <tr class="header">
                    <td width="15%" class="installed"><b>Module Name</b></td>
                    <td width="15%" class="installed"><b>Module Version</b></td>
                    <td width="70%" class="installed"><b>Descrption</b></td>
                </tr>
                <%@ page import="org.jdesktop.wonderland.modules.ModuleInfo" %>
                <% for (ModuleInfo info : mm.getUninstalledModuleInfos()) {%>
                <tr class="installed_a">
                    <td width="15%" class="installed"><%= info.getName()%></td>
                    <td width="15%" class="installed">v<%= info.getMajor()%>.<%= info.getMinor()%></td>
                    <td width="70%" class="installed"><%= info.getDescription()%></td>
                </tr>
                <% }%>
            </table>
            <br>
            <h3>Install a New Module</h3>
            <p>
                Select a new module JAR to install and click INSTALL. If successfull
                the module will be installed during the next restart.
            </p>
            <form method="post" enctype="multipart/form-data" action="ModuleUploadServlet">
                Module JAR file: <input type="file" name="moduleJAR">
                <input type="submit" value="INSTALL">
            </form>
            <br>
            <h3>Test Buttons, just for fun</h3>
            <form action="installAll.jsp">
                <input type="submit" value="INSTALL ALL">
            </form>
            <form action="uninstallAll.jsp">
                <input type="submit" value="UNINSTALL ALL">
            </form>
        </div>
    </body>
</html>
