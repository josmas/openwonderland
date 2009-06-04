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
        <link href="xapps-config.css" rel="stylesheet" type="text/css" media="screen" />
        <title>Add new X11 App</title>
    </head>
    <body>
        <h1>Add new X11 App</h1>

        <form id="nameForm" action="/xapps-config/wonderland-xapps-config/browse">
            <input type="hidden" name="action" value="add"/>

            App Name: <input type="text" name="appName"/><br>
            Command: <input text="text" name="command"/>
            <br><br>
            <a href="javascript:void(0)" onclick="$('nameForm').submit()">Ok</a>
            <a href="/xapps-config/wonderland-xapps-config/browse?action=view">Cancel</a>
        </form>
    </body>
</html>
