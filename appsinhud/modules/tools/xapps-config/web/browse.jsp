<%-- 
    Document   : browse.jsp
    Created on : Dec 14, 2008, 12:11:54 PM
    Author     : jkaplan
--%>

<%@page contentType="text/html" pageEncoding="MacRoman"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="/WEB-INF/tlds/c.tld" prefix="c" %>

<html>
    <head>
        <title>Project Wonderland X11 Apps Configuration</title>
        <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
        <link href="${pageContext.servletContext.contextPath}/xapps-config.css" rel="stylesheet" type="text/css" media="screen" />
    </head>
    <body>
        <table class="installed" id="runnerTable">
            <tr>
                <td colspan="3"><h3>System-wide X11 Applications</h3></td>
                <td class="refresh" id="periods"></td>
            </tr>
            <tr class="header">
                <td class="installed"><b>App Name</b></td>
                <td class="installed"><b>Command</b></td>
                <td class="installed"><b>Actions</b></td>
            </tr>
            <c:forEach var="entry" items="${requestScope['entries']}">
                <tr>
                    <td class="installed">${entry.appName}</td>
                    <td class="installed">${entry.command}</td>
                    <td class="installed">
                        <c:forEach var="action" items="${entry.actions}">
                            <a href="${pageContext.servletContext.contextPath}/browse${entry.path}?action=${action.url}">${action.name}</a>
                        </c:forEach>
                    </td>
                </tr>
            </c:forEach>
        </table>
        <br>
        <a href="/xapps-config/wonderland-xapps-config/add.jsp">Add X11 App</a>
    </body>
</html>
