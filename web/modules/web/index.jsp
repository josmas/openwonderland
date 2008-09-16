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
            <table class="installed">
                <tr class="header">
                    <td width="15%" class="installed"><b>Module Name</b></td>
                    <td width="15%" class="installed"><b>Module Version</b></td>
                    <td width="35%" class="installed"><b>Plugins</b></td>
                    <td width="35%" class="installed"><b>WFSs</b></td>
                </tr>
                <%@ page import="org.jdesktop.wonderland.modules.ModuleUtils" %>
                <%@ page import="org.jdesktop.wonderland.modules.service.ModuleManager" %>
                <%@ page import="org.jdesktop.wonderland.modules.service.InstalledModule" %>
                <%@ page import="org.jdesktop.wonderland.modules.service.ModuleManager.State" %>
                <% ModuleManager mm = ModuleManager.getModuleManager(); %>
                <% String modules[] = mm.getModules(State.INSTALLED).toArray(new String[] {});%>
                <% for (String moduleName : modules) {%>
                <% InstalledModule im = (InstalledModule)mm.getModule(moduleName, State.INSTALLED);%>
                <tr class="installed_a">
                    <td width="15%" class="installed"><%= moduleName%></td>
                    <td width="15%" class="installed"><%= im.getModuleInfo().toString()%></td>
                    <td width="35%" class="installed"><%= ModuleUtils.getPluginNames(im)%></td>
                    <td width="35%" class="installed"><%= ModuleUtils.getWFSNames(im)%></td>
                </tr>
                <% }%>
            </table>
            <br>
            <h3>Pending Modules (will be installed during next restart)</h3>
            <table class="installed">
                <tr class="header">
                    <td width="15%" class="installed"><b>Module Name</b></td>
                    <td width="15%" class="installed"><b>Module Version</b></td>
                    <td width="35%" class="installed"><b>Plugins</b></td>
                    <td width="35%" class="installed"><b>WFSs</b></td>
                </tr>
                <%@ page import="org.jdesktop.wonderland.modules.service.PendingModule" %>
                <% for (String pendingModuleName : mm.getModules(State.PENDING).toArray(new String[] {})) {%>
                <% PendingModule pm = (PendingModule)mm.getModule(pendingModuleName, State.PENDING);%>
                <tr class="installed_a">
                    <td width="15%" class="installed"><%= pendingModuleName%></td>
                    <td width="15%" class="installed"><%= pm.getModuleInfo().toString()%></td>
                    <td width="35%" class="installed"><%= ModuleUtils.getPluginNames(pm)%></td>
                    <td width="35%" class="installed"><%= ModuleUtils.getWFSNames(pm)%></td>
                </tr>
                <% }%>
            </table>
            <br>
            <h3>Removed Modules (will be removed during next restart)</h3>
            <table class="installed">
                <tr class="header">
                    <td width="15%" class="installed"><b>Module Name</b></td>
                    <td width="15%" class="installed"><b>Module Version</b></td>
                    <td width="35%" class="installed"><b>Plugins</b></td>
                    <td width="35%" class="installed"><b>WFSs</b></td>
                </tr>
                <% for (String pendingModuleName : mm.getModules(State.REMOVE)) {%>
                <tr class="installed_a">
                    <td width="15%" class="installed"><%= pendingModuleName%></td>
                    <td width="15%" class="installed">TBD</td>
                    <td width="35%" class="installed">N/A</td>
                    <td width="35%" class="installed">N/A</td>
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
        </div>
    </body>
</html>
