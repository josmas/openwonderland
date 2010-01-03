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
        <link href="/wonderland-web-front/css/base.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="/wonderland-web-front/css/module.css" rel="stylesheet" type="text/css" media="screen" />
        <title>Manage Snapshots</title>
    </head>
    <body>
        <h3>Initial Worlds</h3>

        <c:set var="currentRoot" value="${requestScope['currentroot']}"/>

        <table class="installed">
            <tr class="header">
                <td class="installed"><b>World Name</b></td>
                <td class="installed"><b>Path</b></td>
                <td class="installed"><b>Actions</b></td>
            </tr>
            <c:forEach var="root" items="${requestScope['roots']}">
                <tr class="installed_a">
                    <td class="installed">${root.name}</td>
                    <td class="installed">${root.rootPath}</td>
                    <td class="installed">
                        <c:choose>
                            <c:when test="${currentroot == root}">
                                Current <a href="?action=restore&root=worlds/${root.name}">restore</a>
                            </c:when>
                            <c:otherwise>
                                <a href="?action=current&root=worlds/${root.name}">make current</a>
                            </c:otherwise>
                        </c:choose>
                    </td>
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
                    <td class="installed">
                        <c:choose>
                            <c:when test="${currentroot == snapshot}">
                                Current <a href="?action=restore&root=snapshots/${snapshot.name}">restore</a>
                            </c:when>
                            <c:otherwise>
                                <a href="?action=current&root=snapshots/${snapshot.name}">make current</a>
                            </c:otherwise>
                        </c:choose>
                        <a href="?action=edit&root=snapshots/${snapshot.name}">edit</a>
                        <a href="?action=remove&root=snapshots/${snapshot.name}">remove</a>
                    </td>
                </tr>
            </c:forEach>
            <tr>
                <td colspan="4" align="right"><a href="?action=snapshot">Create snapshot</a></td>
            </tr>
        </table>
    </body>
</html>
