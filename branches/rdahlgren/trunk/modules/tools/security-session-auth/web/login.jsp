<%-- 
    Document   : login
    Created on : Jun 23, 2009, 2:10:46 PM
    Author     : jkaplan
--%>

<%@page contentType="text/html" pageEncoding="MacRoman"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="/WEB-INF/tlds/c.tld" prefix="c" %>

<html>
<head>
    <link href="/wonderland-web-front/admin.css" rel="stylesheet" type="text/css" media="screen" />
    <title>Wonderland Login</title>
</head>
<body>
    <h1>Log in</h1>

    <form action="login" method="POST">
        <input type="hidden" name="forwardPage" value="${param['forwardPage']}"/>
        <input type="hidden" name="action" value="login"/>
        
        <table class="installed">
            <tr>
                <td colspan="2">
                    <font color="red">${requestScope['error']}</font>
                </td>
            </tr>
            <tr>
                <td>Username:</td>
                <td><input type="text" name="username" size="20"/></td>
            </tr>
            <tr>
                <td>Password:</td>
                <td><input type="password" name="password" size="20"/></td>
            </tr>
            <tr>
                <td></td>
                <td><input type="submit" name="Log In" value="Log In"/></td>
            </tr>
        </table>
    </form>
</body>