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
        <title>JSP Page</title>
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
                <% ModuleManager mm = ModuleManager.getModuleManager(); %>
                <% String modules[] = mm.getInstalledModules();%>
                <% for (String moduleName : modules) {%>
                <% InstalledModule im = mm.getInstalledModule(moduleName);%>
                <tr class="installed_a">
                    <td width="15%" class="installed"><%= moduleName%></td>
                    <td width="15%" class="installed"><%= im.getModuleInfo().toString()%></td>
                    <td width="35%" class="installed"><%= ModuleUtils.getPluginNames(im)%></td>
                    <td width="35%" class="installed"><%= ModuleUtils.getWFSNames(im)%></td>
                </tr>
                <% }%>
            </table>
            <h3>Pending Modules (will be installed during next restart)</h3>
            <table class="installed">
                <tr class="header">
                    <td width="15%" class="installed"><b>Module Name</b></td>
                    <td width="15%" class="installed"><b>Module Version</b></td>
                    <td width="35%" class="installed"><b>Plugins</b></td>
                    <td width="35%" class="installed"><b>WFSs</b></td>
                </tr>
                <%@ page import="org.jdesktop.wonderland.modules.service.PendingModule" %>
                <% for (String pendingModuleName : mm.getPendingModules()) {%>
                <% PendingModule pm = mm.getPendingModule(pendingModuleName);%>
                <tr class="installed_a">
                    <td width="15%" class="installed"><%= pendingModuleName%></td>
                    <td width="15%" class="installed"><%= pm.getModuleInfo().toString()%></td>
                    <td width="35%" class="installed"><%= ModuleUtils.getPluginNames(pm)%></td>
                    <td width="35%" class="installed"><%= ModuleUtils.getWFSNames(pm)%></td>
                </tr>
                <% }%>
            </table>
            <h3>Removed Modules (will be removed during next restart)</h3>
            <table class="installed">
                <tr class="header">
                    <td width="15%" class="installed"><b>Module Name</b></td>
                    <td width="15%" class="installed"><b>Module Version</b></td>
                    <td width="35%" class="installed"><b>Plugins</b></td>
                    <td width="35%" class="installed"><b>WFSs</b></td>
                </tr>
                <%@ page import="org.jdesktop.wonderland.modules.service.PendingModule" %>
                <% for (String pendingModuleName : mm.getPendingModules()) {%>
                <% PendingModule pm = mm.getPendingModule(pendingModuleName);%>
                <tr class="installed_a">
                    <td width="15%" class="installed"><%= pendingModuleName%></td>
                    <td width="15%" class="installed"><%= pm.getModuleInfo().toString()%></td>
                    <td width="35%" class="installed"><%= ModuleUtils.getPluginNames(pm)%></td>
                    <td width="35%" class="installed"><%= ModuleUtils.getWFSNames(pm)%></td>
                </tr>
                <% }%>
            </table>
        </div>
    </body>
</html>
