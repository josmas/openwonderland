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
    <script type="text/javascript" language="javascript">
        var create = "${requestScope['create']}";
        var passwordChanged = false;

        function update() {
            if (!create) {
                // fill in the password field with junk -- this won't be
                // sent unless it is changed
                $("password").value = "12345678";
            }
        }

        function passwordChange() {
            passwordChanged = true;
        }

        function submit() {
            // only send the password if it is changed or a new
            // cell
            if (create || passwordChanged) {
                if ($F("password") != $F("confirmPassword")) {
                    alert("Passwords don't match");
                    return;
                }

                if ($F("password") == "") {
                    alert("empty password");
                    return;
                }
            }

            $("memberForm").submit();
        }
    </script>
</head>
<body>
    <c:set var="user" value="${requestScope['user']}"/>

    <h1>Details for user ${user.id}</h1>

    <c:if test="not empty ${requestScope['error']}">
        <font color="red">${requestScope['error']}</font>
    </c:if>

    <form id="memberForm" action="users" method="POST">
        <input type="hidden" name="action" value="save"/>
        <input type="hidden" name="create" value="${requestScope['create']}"/>

        <table class="installed" id="memberTable">
            <tr>
                <td class="installed">User Id:</td>
                <td class="installed"><input type="text" name="id" id="id" value="${user.id}"
                <c:if test="${empty requestScope['create']}">
                    disabled="true"
                </c:if>
                >
                
                <!-- if the id field is disabled, it doesn't get sent to
                     the server.  Add a hidden field for the id in that
                     case. -->
                <c:if test="${empty requestScope['create']}">
                    <input type="hidden" name="id" value="${user.id}"/>
                </c:if>
                </td>
            </tr>
            <tr>
                <td class="installed">Full Name:</td>
                <td class="installed"><input type="text" name="fullname" id="fullname" value="${user.fullname}"/></td>
            </tr>
            <tr>
                <td class="installed">Email:</td>
                <td class="installed"><input type="text" name="email" id="email" value="${user.email}"/></td>
            </tr>
            <tr>
                <td class="installed">Password:</td>
                <td class="installed"><input type="password" name="password" id="password" onchange="javascript:passwordChange();"/></td>
            </tr>
            <tr>
                <td class="installed">Confirm Password:</td>
                <td class="installed"><input type="password" name="confirmPassword" id="confirmPassword"/></td>
            </tr>
        </table>

        <a href="/wonderland-web-front/admin?pageURL=/security-session-auth/security-session-auth/users" target="_top">Cancel</a>
        <a href="javascript:void(0)" onclick="javascript:submit()">
            <c:choose>
                <c:when test="${requestScope['create']}">
                    Create user
                </c:when>
                <c:otherwise>
                    Update user
                </c:otherwise>
            </c:choose>
        </a>
    </form>

    <script type="text/javascript">
        update();
    </script>
</body>