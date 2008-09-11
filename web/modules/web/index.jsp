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
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>JSP Page</title>
    </head>
    <body>
        <h3>Module System</h3>
        <table>
            <%@ page import="org.jdesktop.wonderland.modules.service.ModuleManager" %>
            <% String modules[] = ModuleManager.getModuleManager().getInstalledModules(); %>
            <% for (String module : modules) { %>
            <tr>
                <td><%= module%></td>
            </tr>
            <% } %>
        </table>
    </body>
</html>
