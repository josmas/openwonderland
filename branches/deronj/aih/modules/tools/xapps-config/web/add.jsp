<%-- 
    Document   : setname
    Created on : Mar 17, 2009, 9:01:58 AM
    Author     : jkaplan
--%>

<%@page contentType="text/html" pageEncoding="MacRoman"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="/WEB-INF/tlds/c.tld" prefix="c" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=MacRoman">
        <script src="/wonderland-web-front/javascript/prototype-1.6.0.3.js" type="text/javascript"></script>
        <link href="/wonderland-web-front/css/base.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="/wonderland-web-front/css/module.css" rel="stylesheet" type="text/css" media="screen" />
        <title>Add new X11 App</title>
    </head>
    <body>
        <h1>Add new X11 App</h1>

        <form id="nameForm" action="/xapps-config/wonderland-xapps-config/browse">
            <input type="hidden" name="action" value="add"/>

            <table>
                <tr>
                    <td>App Name:</td>
                    <td><input type="text" name="appName"/></td>
                </tr>
                <tr>
                    <td>Command:</td>
                    <td><input type="text" name="command"/></td>
                </tr>
            </table>
            <br><br>
            <a href="javascript:void(0)" onclick="$('nameForm').submit()">Ok</a>
            <a href="/xapps-config/wonderland-xapps-config/browse?action=view">Cancel</a>
        </form>
    </body>
</html>
