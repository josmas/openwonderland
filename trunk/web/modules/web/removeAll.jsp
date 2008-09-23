<%-- 
    Document   : removeAll
    Created on : Sep 23, 2008, 1:32:21 PM
    Author     : jordanslott
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <%@ page import="org.jdesktop.wonderland.modules.*" %>
        <%@ page import="org.jdesktop.wonderland.modules.service.*" %>
        <%@ page import="org.jdesktop.wonderland.modules.service.ModuleManager.State" %>
        <%@ page import="java.util.*" %>
        <% ModuleManager manager = ModuleManager.getModuleManager(); %>
        <% String[] removed = request.getParameterValues("remove"); %>
        <% Collection<ModuleInfo> removedInfos = new LinkedList<ModuleInfo>(); %>
        <% if (removed != null) { %>
        <%   for (int i = 0; i < removed.length; i++) { %>
        <%     Module module = manager.getModule(removed[i], State.INSTALLED); %>
        <%     if (module != null) { %>
        <%= removed[i] %>
        <%       removedInfos.add(module.getModuleInfo()); %>
        <%     } %>
        <%    } %>
        <% } %>
        <% Collection<String> ret = manager.removeAll(removedInfos); %>
        <%= ret %>
        Click back and refresh the page.
    </body>
</html>
