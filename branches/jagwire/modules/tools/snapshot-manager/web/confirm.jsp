<%-- 
    Document   : snapshots
    Created on : Jan 4, 2009, 11:33:13 AM
    Author     : jkaplan
--%>

<%@page contentType="text/html"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
"http://www.w3.org/TR/html4/loose.dtd">

<%@ taglib uri="/WEB-INF/tlds/c.tld" prefix="c" %>
<%@ taglib uri="/WEB-INF/tlds/fmt.tld" prefix="fmt" %>

<html>
    <head>
        <meta http-equiv="Content-Type" content="text/html; charset=utf-8">
        <link href="/wonderland-web-front/css/base.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="/wonderland-web-front/css/module.css" rel="stylesheet" type="text/css" media="screen" />
        <link href="/wonderland-web-front/css/wonderland-theme/jquery-ui.css" rel="stylesheet" type="text/css" media="screen" />


        <title>Confirm</title>
        
        <script src="/wonderland-web-front/javascript/jquery.min.js" type="text/javascript"></script>
        <script src="/wonderland-web-front/javascript/jquery-ui.min.js" type="text/javascript"></script>      
        
        <script>
            $(function() {
                $("#dialog").dialog({
                    autoOpen: false,
                    resizable: false,
                    height: 280,
                    modal: true
//                    buttons: [{
//                         id: "doneButton",
//                        text: "Done",
//                        click: function() {                            
//                            $(this).dialog('close');
//                            window.location.href="./SnapshotManager";
//                        }
//                    },
//                    {   
//                        id: "cancel-DialogButton",
//                        text: "Cancel",
//                        click: function() {
//                            $(this).dialog('close');
//                        }
//                    }]
                    
                });
                
                $("#progressbar").progressbar();
                
                $("#OK").button();                
                $("#OK").click(function() {
                    $("#dialog").dialog('open');
                    $("#doneButton").button("disable");
                    $("#progressbar").progressbar('value',20);
                    $("#status").text("Stopping server!");
                    stopServer();
                 });
                 
                 $("#Cancel").button();
                 $("#Cancel").click(function() { 
                     window.location.href="./SnapshotManager";
                 });
                 
                 
              function stopServer() {
                  $.ajax({
                      type: 'GET',
                      url: '../../wonderland-web-runner/services/runner/all/stop',
                      success: function(data) {
                          //update progress bar: taking snapshot
                          if($("#actionID").val() == "snapshot") {
                            $("#progressbar").progressbar('value', 40);
                            $("#status").text("Stopped. Creating snapshot...");
                            takeSnapshot();
                          } else if ($("#actionID").val() == "current") {
                              //make current was pressed
                              $("#progressbar").progressbar('value', 50);
                              $("#status").text("Stopped. Making snapshot current.");
                              makeSnapshotCurrent();
                              
                          
                          } else if($("#actionID").val() == "restore") {
                              //restore was pressed
                              $("#progressbar").progressbar('value', 50);
                              $("#status").text("Stopped. Restoring snapshot.");
                              restoreSnapshot();
                              
                          } else {// if($("#actionID").val() == "current") {
                              $("#progressbar").progressbar('value', 75);
                              $("#status").text("Stopped. Restarting Server!");
                              startServer();
                              
                              
                          }
                      }
                      
                  });
              }
              
              function restoreSnapshot() {
                  
                  var str = $("#rootID").val();
                  
                  $.ajax({
                      type: 'GET',
                      url: 'resources/snapshot/restore/'+str.replace("/", "&"),
                      success: function(data) {
                          $("#progressbar").progressbar('value', 75);
                            $("#status").text("Snapshot restored. Restarting server!");
                            startServer();            
                      }
                  });
              }
              
              function makeSnapshotCurrent() {
                  var str = $("#rootID").val();
                    $.ajax({
                      type: 'GET',
                      url: 'resources/snapshot/make/current/'+str.replace("/", "&"),
                      success: function(data) {
                          $("#progressbar").progressbar('value', 80);
                            $("#status").text("Snapshot changed. Restarting server!");
                            startServer();            
                      }
                  });
              }
              
              function makeSnapshotCurrentWithData(data) {
                  var str = data
                    $.ajax({
                      type: 'GET',
                      url: 'resources/snapshot/make/current/'+str.replace("/", "&"),
                      success: function(data) {
                          $("#progressbar").progressbar('value', 80);
                            $("#status").text("Snapshot changed. Restarting server!");
                            startServer();            
                      }
                  });
              }              
              
              
              function takeSnapshot() {
                  $.ajax({
                      type: 'GET',
                      url: 'resources/snapshot/take/snapshot',
                      dataType: "text",
                      success: function(data) {
                          //update progress bar: starting server
                          if($("#actionID").val() == "snapshot") {
                            $("#progressbar").progressbar('value', 60);
                            $("#status").text("Snapshot taken. Making snapshot current!");
                            //startServer();                     
                            makeSnapshotCurrentWithData(data);                            
                          }
                      }   
                  });
              }
              
              function startServer() {
                  $.ajax({
                      type: 'GET',
                      url: '../../wonderland-web-runner/services/runner/all/start',
                      success: function(data) {
                          //update progressbar to 100%: server started.
                         
                          $("#progressbar").progressbar('value', 100);
                          if($("#actionID").val() == "snapshot") {
                            $("#status").text("Server started. Snapshot successful!");  
                          } else {
                            $("#status").text("Server started!");
                          }
                          
                          window.setTimeout(function() { 
                            window.location.href="./SnapshotManager";
                            },
                            1500);
               
                      }
                  });
              }
              if($("#actionID").val() == "restore") {
                $("#messageID").text('Are you sure you want to refresh the snapshot? Darkstar will need to be restarted in order to complete the process.');
              } else if($("#actionID").val() == "current") {
                $("#messageID").text('Are you sure you want to switch the snapshot? Darkstar will need to be restarted in order to complete the process.');
              }
            });
            
        </script>

    </head>
    <body>
        <c:set var="action" value="${param['action']}"/>
        <c:set var="root" value="${param['root']}"/>
               
        <h2>Confirm</h2>
        <br>
        <h4><p id="messageID">Are you sure you want to take a snapshot? Darkstar will need to be restarted in order to complete the process.</p></h4>

        <br><br><br>
        <button id="OK">OK</button>
        <button id="Cancel">Cancel</button>
        
        <div id="dialog" title="Server Status">
            <div><h5 id="status">status</h5></div>
            <div id="progressbar"></div>
        </div>
        <input type="hidden" name="action" id="actionID" value="${action}"/>
        <input type="hidden" name="root" id="rootID" value="${root}"/>
    </body>
</html>
