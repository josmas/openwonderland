<%-- 
    Document   : installAll
    Created on : Sep 23, 2008, 10:51:08 AM
    Author     : jordanslott
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <link href="/wonderland-web-front/css/base.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="/wonderland-web-front/css/module.css" rel="stylesheet" type="text/css" media="screen" />
        <title>JSP Page</title>
    </head>
    <body>
        <%@ page import="org.jdesktop.wonderland.modules.service.ModuleManager" %>
        <% ModuleManager mm = ModuleManager.getModuleManager();%>
        <% mm.installAll(); %>
        Click back and refresh the page.
    </body>
</html>
