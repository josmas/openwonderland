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
        <title>Project Wonderland Content Repository Browser</title>
    </head>
    <body>
        <c:set var="current" value="${requestScope['current']}"/>

        <table class="installed" id="runnerTable">
            <tr>
                <td colspan="3"><h3>Directory listing for ${current.path}</h3></td>
                <td class="refresh" id="periods"></td>
            </tr>
            <tr class="header">
                <td class="installed"><b>Name</b></td>
                <td class="installed"><b>Actions</b></td>
            </tr>
            <c:forEach var="entry" items="${requestScope['entries']}">
                <tr>
                    <td class="installed"><a href="${pageContext.servletContext.contextPath}/browse${entry.path}">${entry.name}</a></td>
                    <td class="installed">
                        <c:forEach var="action" items="${entry.actions}">
                            <a href="${pageContext.servletContext.contextPath}/browse${entry.path}?action=${action.url}">${action.name}</a>
                        </c:forEach>
                    </td>
                </tr>
            </c:forEach>
        </table>
        <br>
        <h3>Add new files</h3>
        <p>
            Create new files and directories.
        </p>
        <form method="post" action="${pageContext.servletContext.contextPath}/browse${current.path}">
            <input type="hidden" name="action" value="mkdir"/>
            <input type="text" name="dirname"/>
            <input type="submit" value="Create Directory"/>
        </form>
        <br>
        <form method="post" enctype="multipart/form-data" action="${pageContext.servletContext.contextPath}/browse${current.path}">
            <input type="hidden" name="action" value="upload"/>
            Upload file: <input type="file" name="file">
            <input type="submit" value="Upload">
        </form>
        <br>
    </body>
</html>
