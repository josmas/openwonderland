<%-- 
    Document   : index
    Created on : Aug 7, 2008, 4:31:15 PM
    Author     : jkaplan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<html>
<head>
    <script src="/wonderland-web-front/javascript/jquery.min.js" type="text/javascript"></script>
    <script src="/wonderland-web-front/javascript/jquery-ui.min.js" type="text/javascript"></script>
    <script src="scripts/jquery.fileupload.js" type="text/javascript"></script>
    <script src="scripts/jquery.iframe-transport.js" type="text/javascript"></script>

    <link href="/wonderland-web-front/css/wonderland-theme/jquery-ui.css" rel="stylesheet" type="text/css" media="screen" />
    <link href="/wonderland-web-front/css/base.css" rel="stylesheet" type="text/css" media="screen" />
    <link href="/wonderland-web-front/css/module.css" rel="stylesheet" type="text/css" media="screen" />
    
    <style type="text/css">
        .install-module .ui-widget {
            font-size: .8em;
        }
        
        #install-instructions {
            margin: 5px 0 10px 0;
            display: block;
        }
        
        #install-filename {
            width: 400px;
        }
        
        #hidden-div {
            position: absolute;
            visibility: hidden;
        }
        
        #accordion-root .modules-accordion {
            background: transparent;
            margin: 0px;
            padding: 0px;
        }
        
        #accordion-root .modules-accordion .action-links {
            padding: 5px 10px 10px 0;
        }
        
        #accordion-root .modules-accordion .action-links a {
            padding-left: 10px;
            font-size: .9em;
        }
    </style>
    <script type="text/javascript">
        var toUpload;
        
        $(document).ready(function() { 
            $('#fileupload').fileupload({
                dataType: 'json',
                url: 'modules/install/module',

                add: function(e, data) {
                  var file = data.files[0].name;
                  $("#install-filename").val(file);
                  
                  toUpload = data;
                  $("#install-button").button("enable");
                },

                done: function(e, data) {
                    //nothing
                    location.reload();
                }
            });
            			 
            $("#accordion-root").accordion({
                active: 2,
                autoHeight: false,
                collapsible: true
                
            });
            
            $("#install-button").button({
                disabled: true
            });
            $("#install-button").click(function() { 
                toUpload.submit();
            });
            
            $("#browse-button").button();
            $("#browse-button").click(function() { 
                $("#fileupload").click();
            });
        });
        
    </script>
</head>
<body>
    <%@ page import="java.util.Collections" %>
    <%@ page import="java.util.Map" %>
    <%@ page import="java.util.List" %>
    <%@ page import="java.util.LinkedList" %>
    <%@ page import="java.util.Iterator" %>
    <%@ page import="org.jdesktop.wonderland.modules.Module" %>
    <%@ page import="org.jdesktop.wonderland.common.modules.ModuleInfo" %>
    <%@ page import="org.jdesktop.wonderland.modules.service.ModuleManager" %>
    <%  ModuleManager manager = ModuleManager.getModuleManager(); %>
    
    <h2>Manage Modules</h2> <br />
    <div id="hidden-div">
        <input id="fileupload" type="file" name="files[]"/>
    </div>
    
    <table class="installed">
        <caption>
            <span class="heading">Install a New Module</span>
        </caption>
        <tbody>
            <tr class="installed">
                <td class="installed install-module">
                    <span id="install-instructions">Drag and drop module jar file onto this page or select "Choose File" to find a module on your computer.</span>
                    <span id="install-current-file">Selected module:</span>
                    <input type="text" id="install-filename" class="ui-widget ui-corner-all" value="none" readonly/>
                    <button id="browse-button">Choose File</button>
                    <button id="install-button">Install</button>
                </td>
            </tr>
        </tbody>
    </table>
    
    <br />
    <br />
    <div id="accordion-root">


        <% int pendingSize = manager.getPendingModules().size();
           int uninstallSize = manager.getUninstallModuleInfos().size();
           int installedSize = manager.getInstalledModules().size();
        %>
        <h5><a href="#">Pending Modules (will be installed during next restart) (<%= pendingSize %>)</a></h5>
        <div class="modules-accordion">
            <table class="installed">
                <tr class="header">
                    <td width="15%" class="installed">Module Name</td>
                    <td width="15%" class="installed">Module Version</td>
                    <td width="70%" class="installed">Description</td>
                </tr>
                <%
                    Map<String, Module> pending = manager.getPendingModules();
                    Iterator<Map.Entry<String, Module>> it2 = pending.entrySet().iterator();
                    while (it2.hasNext() == true) {
                        Map.Entry<String, Module> entry = it2.next();
                        String moduleName = entry.getKey();
                        ModuleInfo moduleInfo = entry.getValue().getInfo();
                %>
                <tr class="installed">
                    <td width="15%" class="installed"><%= moduleName%></td>
                        <%if (moduleInfo.getMini() == 0) {%>
                        <td width="15%" class="installed">v<%= moduleInfo.getMajor()%>.<%= moduleInfo.getMinor()%></td>
                        <%} else {%>
                        <td width="15%" class="installed">v<%= moduleInfo.getMajor()%>.<%= moduleInfo.getMinor()%>.<%= moduleInfo.getMini()%></td>
                        <%}%>
                        <td width="70%" class="installed"><%= moduleInfo.getDescription()%></td>
                </tr>
                <% }%>
            </table>
        </div>
        <h5><a href="#">Removed Modules (will be removed during next restart) (<%= uninstallSize %>)</a></h5>
        <div class="modules-accordion">
            <table class="installed">
                <tr class="header">
                    <td width="15%" class="installed">Module Name</td>
                    <td width="15%" class="installed">Module Version</td>
                    <td width="70%" class="installed">Description</td>
                </tr>
                <%
                Map<String, ModuleInfo> uninstall = manager.getUninstallModuleInfos();
                    Iterator<Map.Entry<String, ModuleInfo>> it3 = uninstall.entrySet().iterator();
                    while (it3.hasNext() == true) {
                        Map.Entry<String, ModuleInfo> entry = it3.next();
                        ModuleInfo moduleInfo = entry.getValue();
                %>
                <tr class="installed">
                    <td width="15%" class="installed"><%= moduleInfo.getName()%></td>
                        <%if (moduleInfo.getMini() == 0) {%>
                        <td width="15%" class="installed">v<%= moduleInfo.getMajor()%>.<%= moduleInfo.getMinor()%></td>
                        <%} else {%>
                        <td width="15%" class="installed">v<%= moduleInfo.getMajor()%>.<%= moduleInfo.getMinor()%>.<%= moduleInfo.getMini()%></td>
                        <%}%>
                        <td width="70%" class="installed"><%= moduleInfo.getDescription()%></td>
                </tr>
                <% }%>
            </table>
        </div>
        <h5><a href="#">Installed Modules (<%= installedSize%>)</a></h5>
        <div class="modules-accordion">
            <form id="removeForm" action="/wonderland-web-modules/editor">
                <input type="hidden" name="action" value="remove"/>
                <input type="hidden" name="confirm" value="true"/>
                <table class="installed" border="0">
                    <tr class="header">
                        <td width="5%" class="installed"></td>
                        <td width="15%" class="installed">Module Name</td>
                        <td width="15%" class="installed">Module Version</td>
                        <td width="65%" class="installed">Description</td>
                    </tr>
                    <%

                    Map<String, Module> installed = manager.getInstalledModules();
                    List<String> nameList = new LinkedList(installed.keySet());
                    Collections.sort(nameList);
                    for (String moduleName : nameList) {
                        ModuleInfo moduleInfo = installed.get(moduleName).getInfo();
                        String description = moduleInfo.getDescription();
                    %>
                    <tr class="installed">
                        <td width="5%" class="installed"><input type="checkbox" name="remove" value="<%= moduleName%>"/></td>
                        <td width="15%" class="installed"><%= moduleName%></td>
                        <%if (moduleInfo.getMini() == 0) {%>
                        <td width="15%" class="installed">v<%= moduleInfo.getMajor()%>.<%= moduleInfo.getMinor()%></td>
                        <%} else {%>
                        <td width="15%" class="installed">v<%= moduleInfo.getMajor()%>.<%= moduleInfo.getMinor()%>.<%= moduleInfo.getMini()%></td>
                        <%}%>
                        <td width="65%" class="installed"><%= (description != null) ? description : "[None]" %></td>
                    </tr>
                    <% }%>
                </table>
                <div id="action-links">
                    <a href="javascript:void(0)" onclick="$('#removeForm').submit()">Remove selected modules</a>
                </div>
            </form>
        </div>
    </div>
</body>
</html>