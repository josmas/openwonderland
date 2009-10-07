<%-- 
    Document   : index
    Created on : Aug 7, 2008, 4:31:15 PM
    Author     : jkaplan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
        <link href="/wonderland-web-front/css/base.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="/wonderland-web-front/css/module.css" rel="stylesheet" type="text/css" media="screen" />
</head>
<body>
    <%@ page import="java.util.Collections" %>
    <%@ page import="java.util.Map" %>
    <%@ page import="java.util.List" %>
    <%@ page import="java.util.LinkedList" %>
    <%@ page import="java.util.Iterator" %>
    <%@ page import="org.jdesktop.wonderland.modules.Module" %>
    <%@ page import="org.jdesktop.wonderland.common.modules.ModuleInfo" %>
    <%@ page import="org.jdesktop.wonderland.modules.service.ModuleManager" %>
    <h3>Installed Modules</h3>
    <form action="removeAll.jsp">
        <table class="installed">
            <tr class="header">
                <td width="5%" class="installed"></td>
                <td width="15%" class="installed"><b>Module Name</b></td>
                <td width="15%" class="installed"><b>Module Version</b></td>
                <td width="65%" class="installed"><b>Description</b></td>
            </tr>
            <%
            ModuleManager manager = ModuleManager.getModuleManager();
            Map<String, Module> installed = manager.getInstalledModules();
            List<String> nameList = new LinkedList(installed.keySet());
            Collections.sort(nameList);
            for (String moduleName : nameList) {
                ModuleInfo moduleInfo = installed.get(moduleName).getInfo();
                String description = moduleInfo.getDescription();
            %>
            <tr class="installed_a">
                <td width="5%" class="installed"><input type="checkbox" name="remove" value="<%= moduleName%>"/></td>
                <td width="15%" class="installed"><%= moduleName%></td>
                <td width="15%" class="installed">v<%= moduleInfo.getMajor()%>.<%= moduleInfo.getMinor()%></td>
                <td width="65%" class="installed"><%= (description != null) ? description : "[None]" %></td>
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
        <%
            Map<String, Module> pending = manager.getPendingModules();
            Iterator<Map.Entry<String, Module>> it2 = pending.entrySet().iterator();
            while (it2.hasNext() == true) {
                Map.Entry<String, Module> entry = it2.next();
                String moduleName = entry.getKey();
                ModuleInfo moduleInfo = entry.getValue().getInfo();
        %>
        <tr class="installed_a">
            <td width="15%" class="installed"><%= moduleName%></td>
            <td width="15%" class="installed">v<%= moduleInfo.getMajor()%>.<%= moduleInfo.getMinor()%></td>
            <td width="70%" class="installed"><%= moduleInfo.getDescription()%></td>
        </tr>
        <% }%>
    </table>
    <br>
    <h3>Removed Modules (will be removed during next restart)</h3>
    <table class="installed">
        <tr class="header">
            <td width="15%" class="installed"><b>Module Name</b></td>
            <td width="15%" class="installed"><b>Module Version</b></td>
            <td width="70%" class="installed"><b>Description</b></td>
        </tr>
        <%
        Map<String, ModuleInfo> uninstall = manager.getUninstallModuleInfos();
            Iterator<Map.Entry<String, ModuleInfo>> it3 = uninstall.entrySet().iterator();
            while (it3.hasNext() == true) {
                Map.Entry<String, ModuleInfo> entry = it3.next();
                ModuleInfo moduleInfo = entry.getValue();
        %>
        <tr class="installed_a">
            <td width="15%" class="installed"><%= moduleInfo.getName()%></td>
            <td width="15%" class="installed">v<%= moduleInfo.getMajor()%>.<%= moduleInfo.getMinor()%></td>
            <td width="70%" class="installed"><%= moduleInfo.getDescription()%></td>
        </tr>
        <% }%>
    </table>
    <br>
    <h3>Install a New Module</h3>
    <p>
        Select a new module JAR to install and click INSTALL. If successful
        the module will be installed during the next restart.
    </p>
    <form method="post" enctype="multipart/form-data" action="ModuleUploadServlet">
        Module JAR file: <input type="file" name="moduleJAR">
        <input type="submit" value="INSTALL">
    </form>
    <br>
</body>
</html>