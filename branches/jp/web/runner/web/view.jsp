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
                Server Management
            </h1>
        </div>
        <div id="contents">
            <h3>Server Components</h3>
            <table class="installed">
                <tr class="header">
                    <td class="installed"><b>Name</b></td>
                    <td class="installed"><b>Location</b></td>
                    <td class="installed"><b>Status</b></td>
                    <td class="installed"><b>Actions</b></td>
                </tr>
                
                <c:forEach var="runner" items="${requestScope['runnerList']}">
                    <tr>
                        <td class="installed">${runner.name}</td>
                        <td class="installed">localhost</td>
                     
                        <c:choose>
                                <c:when test="${runner.status == 'NOT_RUNNING'}" >
                                    <td class="installed">Not Running</td>
                                    <td class="installed">
                                        <a href="run?action=start&name=${runner.name}">start</a>
                                </c:when>
                                <c:when test="${runner.status == 'STARTING_UP'}" >
                                    <td class="installed">Starting up</td>
                                    <td class="installed">
                                </c:when>
                                <c:when test="${runner.status == 'RUNNING'}" >
                                    <td class="installed">Running</td>
                                    <td class="installed">
                                        <a href="run?action=stop&name=${runner.name}">stop</a>
                                </c:when>
                                <c:when test="${runner.status == 'SHUTTING_DOWN'}" >
                                    <td class="installed">Shutting Down</td>
                                    <td class="installed">
                                </c:when>
                                <c:when test="${runner.status == 'ERROR'}" >
                                    <td class="installed">Error</td>
                                    <td class="installed">
                                </c:when>
                                <c:otherwise>
                                    Unknown
                                </c:otherwise>
                            </c:choose>
                            
                            <c:if test="${!empty runner.logFile}">
                                <a href="run?action=log&name=${runner.name}">log</a>
                            </c:if>
                        </td>
                    </tr>
                </c:forEach>
            </table>
            <br>            
        </div>
    </body>
</html>