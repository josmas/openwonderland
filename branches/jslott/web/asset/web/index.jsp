<%-- 
    Document   : index
    Created on : Aug 7, 2008, 4:31:15 PM
    Author     : jkaplan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
<body>
    <%@ page import="org.jdesktop.wonderland.web.asset.deployer.ArtDeployer" %>
    <%@ page import="java.util.Map" %>
    <%@ page import="java.util.Iterator" %>
    <%@ page import="java.io.File" %>
    
    <h3>Installed Artwork</h3>
    <%
        Map<String, File> map = AssetDeployer.getFileMap("art");
        Iterator<Map.Entry<String, File>> it = map.entrySet().iterator();
        while (it.hasNext() == true) {
            Map.Entry<String, File> entry = it.next();
            String moduleName = entry.getKey();
            File file = entry.getValue();
    %>
        <%= moduleName%> <%=file.getAbsolutePath()%><br>
        <%
        }
        %>
</body>
</html>
