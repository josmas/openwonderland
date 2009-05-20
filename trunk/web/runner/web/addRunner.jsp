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
        <link href="runner.css" rel="stylesheet" type="text/css" media="screen" />
    </head>
    <body>
        <h3>Add Component</h3>
        <form action="run">
            <input type="hidden" name="action" value="addRunnerForm"/>

            <c:if test="${not empty requestScope['error']}">
                <font color="red">${requestScope['error']}"</font>
            </c:if>

            <table>
                <tr>
                    <td>Component Name:</td>
                    <td><input type="text" name="name" value="${requestScope['entry'].runnerName}"/></td>
                </tr>
                <tr>
                    <td>Component Class:</td>
                    <td><input type="text" name="class" vakye="${requestScope['entry'].runnerClass}"/></td>
                </tr>
                <tr>
                    <td><input type="submit" name="button" value="OK"/>
                        <input type="submit" name="button" value="Cancel"/>
                    </td>
                </tr>
            </table>
        </form>
    </body>
</html>