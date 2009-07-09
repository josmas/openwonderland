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
    <link href="/wonderland-web-front/admin.css" rel="stylesheet" type="text/css" media="screen" />
    <script src="/wonderland-web-front/javascript/prototype-1.6.0.3.js" type="text/javascript"></script>
</head>
<body>
    <h1>View Users</h1>

    <c:if test="not empty ${requestScope['error']}">
        <font color="red">${requestScope['error']}</font>
    </c:if>

    <table class="installed" id="userTable">
        <tr>
            <td colspan="3"><h3>Users</h3></td>
        </tr>
        <tr class="header">
            <td class="installed"><b>Name</b></td>
            <td class="installed"><b>Id</b></td>
            <td class="installed"><b>Email</b></td>
            <td class="installed"><b>Actions</b></td>
        </tr>

        <c:forEach var="user" items="${requestScope['users']}">
            <tr>
                <td class="installed">${user.fullname}</td>
                <td class="installed">${user.id}</td>
                <td class="installed">${user.email}</td>
                <td class="installed">
                    <a href="/wonderland-web-front/admin?pageURL=/security-session-auth/security-session-auth/users%3faction=edit%26id=${user.id}"
                       target="_top"/>edit</a>
                    <a href="/wonderland-web-front/admin?pageURL=/security-session-auth/security-session-auth/users%3faction=remove%26id=${user.id}"
                       target="_top"/>remove</a>
                </td>
            </tr>
        </c:forEach>
    </table>

    <a href="/wonderland-web-front/admin?pageURL=/security-session-auth/security-session-auth/users%3faction=create" target="_top">Add user</a>
</body>