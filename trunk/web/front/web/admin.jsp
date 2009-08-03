<%-- 
    Document   : admin
    Created on : Oct 7, 2008, 4:33:56 PM
    Author     : jkaplan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="/WEB-INF/tlds/c.tld" prefix="c" %>

<head>
    <title>Project Wonderland Server Administration</title>
    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
    <meta name="keywords" content="Project Wonderland, Virtual World, Open Source" />
    <meta name="description" content="Project Wonderland Server Administration" />
    <link href="ce.css" rel="stylesheet" type="text/css" media="screen" />
    <link href="admin.css" rel="stylesheet" type="text/css" media="screen" />

    <script type="text/javascript">
            function resizeIframe() {
                var height = document.documentElement.clientHeight;
                height -= document.getElementById('contentFrame').offsetTop;
                height -= 10; // bottom margin
    
                document.getElementById('contentFrame').style.height = height + "px"; 
            };
    
            window.onresize = resizeIframe;
    </script>
</head>

<body onload="resizeIframe()">
    <div id="container">
        
        <div id="header">
            <img style="float:left" src="images/banner-left.jpg" />
            <img src="images/banner-right-wonderland.jpg" />
            <div id="projectName">
                <span id="labs">Project Wonderland</span><br />
                Server Administration
            </div>
            <div id="serverInfo">
                Wonderland Server:  <%= request.getLocalName()%><br/>
                Wonderland Version: ${requestScope['version'].version}
                                    (rev. ${requestScope['version'].revision})<br/>
            </div>
        </div>
        
        <div id="moduleMenu">
            <c:forEach var="adminPage" items="${requestScope['adminPages']}">
                <c:choose>
                    <c:when test="${adminPage.absolute}">
                        <a href="${adminPage.url}">${adminPage.displayName}</a><br/>
                    </c:when>
                    <c:otherwise>
                        <a href="admin?pageURL=${adminPage.url}">${adminPage.displayName}</a><br/>
                    </c:otherwise>
                </c:choose>
            </c:forEach>
        </div>
        
        <div id="content">
            <iframe id="contentFrame" frameborder="0" width="100%" height="100%"
                    src="${requestScope['pageURL']}" name="content"/>

           
        </div>
    </div>
</body>
</html>
