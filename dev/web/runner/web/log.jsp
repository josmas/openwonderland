<%-- 
    Document   : index
    Created on : Aug 7, 2008, 4:31:15 PM
    Author     : jkaplan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<%@ page import="org.jdesktop.wonderland.runner.Runner" %>

<%@ taglib uri="/WEB-INF/tlds/c.tld" prefix="c" %>

<link href="runner.css" rel="stylesheet" type="text/css" media="screen" />
<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
        <title>Project Wonderland Server Management</title>
    </head>
    <body>
        <div id="header">
            <img style="float:left" src="images/banner-left.jpg" />
            <img src="images/banner-right-wonderland.jpg" />
            <h1>
                <span id="labs">Project Wonderland</span><br/>
                Log Viewer
            </h1>
        </div>
        <div id="contents">
            <h3>Log</h3>
            <c:forEach var="line" items="${log}">
                ${line}<br>
            </c:forEach> 
        </div>
    </body>
</html>