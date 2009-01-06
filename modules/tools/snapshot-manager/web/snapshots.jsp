<%-- 
    Document   : snapshots
    Created on : Jan 4, 2009, 11:33:13 AM
    Author     : jkaplan
--%>

<%@page contentType="text/html" pageEncoding="MacRoman"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="/WEB-INF/tlds/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tlds/fmt.tld" prefix="fmt" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=MacRoman">
        <title>Manage Snapshots</title>
        <link href="snapshot.css" rel="stylesheet" type="text/css" media="screen" />
    </head>
    <body>
        <h3>Initial Worlds</h3>

        <table class="installed">
            <tr class="header">
                <td class="installed"><b>World Name</b></td>
                <td class="installed"><b>Actions</b></td>
            </tr>
            <c:forEach var="root" items="${requestScope['roots']}">
                <tr class="installed_a">
                    <td class="installed">${root}</td>
                    <td class="installed"></td>
                </tr>
            </c:forEach>
        </table>

        <h3>World Snapshots</h3>

        <table class="installed">
            <tr class="header">
                <td class="installed"><b>Name</b></td>
                <td class="installed"><b>Date</b></td>
                <td class="installed"><b>Description</b></td>
                <td class="installed"><b>Actions</b></td>
            </tr>
            <c:forEach var="snapshot" items="${requestScope['snapshots']}">
                <tr class="installed_a">
                    <td class="installed">${snapshot.name}</td>
                    <td class="installed"><fmt:formatDate value="${snapshot.timestamp}" type="both"/></td>
                    <td class="installed">${snapshot.description}</td>
                    <td class="installed"><a href="?action=edit&snapshot=${snapshot.name}">edit</a>
                                          <a href="?action=remove&snapshot=${snapshot.name}">remove</a></td>
                </tr>
            </c:forEach>
        </table>
    </body>
</html>
