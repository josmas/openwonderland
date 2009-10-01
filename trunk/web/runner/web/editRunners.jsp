<%-- 
    Document   : index
    Created on : Aug 7, 2008, 4:31:15 PM
    Author     : jkaplan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="/WEB-INF/tlds/c.tld" prefix="c" %>

<html>
    <head>
        <link href="/wonderland-web-front/css/base.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="/wonderland-web-front/css/module.css" rel="stylesheet" type="text/css" media="screen" />
    </head>
    <body>
        <h3>Edit Server Components</h3>
        <form action="run">
            <input type="hidden" name="action" value="editRunnersForm"/>
            <table class="installed" id="runnersTable">
                <tr class="header">
                    <td class="installed"><b>Name</b></td>
                    <td class="installed"><b>Class</b></td>
                    <td class="installed"><b>Location</b></td>
                    <td class="installed"><b>Actions</b></td>
                </tr>
                <c:forEach var="entry" items="${requestScope['entries']}">
                    <tr>
                        <td>${entry.runnerName}</td>
                        <td>${entry.runnerClass}</td>
                        <td>${entry.location}</td>
                        <td><a href="/wonderland-web-front/admin?pageURL=/wonderland-web-runner/run%3faction=removeRunner%26name=${entry.runnerName}" target="_top">remove</a></td>
                    </tr>
                </c:forEach>
            </table>
            <a href="/wonderland-web-front/admin?pageURL=/wonderland-web-runner/run%3faction=addRunner" target="_top">Add component</a>
            <div style="float: right;">
                <input type="submit" name="button" value="Save"/>
                <input type="submit" name="button" value="Restore Defaults"/>
                <input type="submit" name="button" value="Cancel"/>
            </div>
        </form>
    </body>
</html>