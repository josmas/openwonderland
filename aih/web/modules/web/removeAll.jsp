<%-- 
    Document   : removeAll
    Created on : Sep 23, 2008, 1:32:21 PM
    Author     : jordanslott
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN" "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link href="/wonderland-web-front/css/base.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="/wonderland-web-front/css/module.css" rel="stylesheet" type="text/css" media="screen" />
        <title>JSP Page</title>
    </head>
    <body>
        <%@ page import="java.util.Arrays" %>
        <%@ page import="java.util.List" %>
        <%@ page import="java.util.Collection" %>
        <%@ page import="org.jdesktop.wonderland.modules.service.ModuleManager" %>
        <%
         ModuleManager manager = ModuleManager.getModuleManager();
         String[] removed = request.getParameterValues("remove");
         List<String> moduleNames = Arrays.asList(removed);
         Collection<String> ret = manager.addToUninstall(moduleNames);
         manager.uninstallAll();
        %>
        <%= ret%>
        Click back and refresh the page.
    </body>
</html>
