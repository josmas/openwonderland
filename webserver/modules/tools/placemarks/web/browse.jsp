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
        <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
        <link href="/wonderland-web-front/css/base.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="/wonderland-web-front/css/module.css" rel="stylesheet" type="text/css" media="screen" />
        <title>Project Wonderland Placemarks Configuration</title>
    </head>
    <body>
        <table class="installed" id="runnerTable">
            <tr>
                <td colspan="3"><h3>System-wide Placemarks</h3></td>
                <td class="refresh" id="periods"></td>
            </tr>
            <tr class="header">
                <td class="installed"><b>Name</b></td>
                <td class="installed"><b>Server URL</b></td>
                <td class="installed"><b>Transport Location</b></td>
                <td class="installed"><b>Look Direction</b></td>
                <td class="installed"><b>Actions</b></td>
            </tr>
            <c:forEach var="entry" items="${requestScope['entries']}">
                <tr>
                    <td class="installed">${entry.name}</td>
                    <td class="installed">${entry.url}</td>
                    <td class="installed">(${entry.x}, ${entry.y}, ${entry.z})</td>
                    <td class="installed">${entry.angle} degrees</td>
                    <td class="installed">
                        <c:forEach var="action" items="${entry.actions}">
                            <a href="${pageContext.servletContext.contextPath}/browse?action=${action.url}">${action.name}</a>
                        </c:forEach>
                    </td>
                </tr>
            </c:forEach>
        </table>
        <br>
        <a href="/placemarks/wonderland-placemarks/add.jsp">Add Placemark</a>
    </body>
</html>
