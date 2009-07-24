<%-- 
    Document   : index
    Created on : Oct 7, 2008, 2:17:04 PM
    Author     : jkaplan
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN"
   "http://www.w3.org/TR/html4/loose.dtd">

<head>
<title>Project Wonderland 0.5 Launch Page</title>

<meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1" />
<meta name="keywords" content="Project Wonderland, Virtual World, Open Source" />
<meta name="description" content="Project Wonderland Start Page" />
<link href="ce.css" rel="stylesheet" type="text/css" media="screen" />
</head>

<body>

<div id="container">

  <div id="header">
    <img style="float:left" src="images/banner-left.jpg" />
    <img src="images/banner-right-wonderland.jpg" />
    <div id="projectName">
      <span id="labs">Project</span><br />
      Wonderland
    </div>
    <div id="serverInfo">
        Wonderland Server:  <%= request.getLocalName() %><br/>
        Wonderland Version: @VERSION@<br/>    
    </div>
  </div>
    
<div id="frontMatter">
<div id="contents">
<h3>On this page:</h3>
<ul>
    <li><a href="#resource">Wonderland Resources</a> </li>
    <li><a href="#admin">Server Administration</a></li>
    <li><a href="#troubleshooting">Troubleshooting</a> </li>
</ul>
</div>

<! **************** INTRODUCTION ********************** >

<div id="intro">
<p>Welcome to Project Wonderland.  Use the button below to launch the <strong>Project Wonderland Client</strong> using Java Web Start.* Java Web Start will automatically download the latest version of the software. To get started, all you need is a version of <a href="http://java.com">Java</a> installed on your system.  </p>

<p>
<form action="app/Wonderland.jnlp">
  <span class="button"><input type="submit" value="Launch Wonderland 0.5"></span>
</form>
</p>
	
<p></p>
<p>For more information on how to use Wonderland, including navigation and audio features, please see the <a href="https://wonderland.dev.java.net/0.5/user-guide-toc.html">Wonderland User's Guide</a>.</p>

</div>

</div> <! end frontMatter >

<! **************** WONDERLAND RESOURCES ********************** >

<div id="sections">

<a name="current">
<h3>Wonderland Resources</h3></a>

<div class="project">
    <img src="images/team-room.png" width="250" border="1" />
    <a name="wonderland"></a>
    <span class="projectName"><a href="https://wonderland.dev.java.net/">Project Wonderland Web Site</a></span>
    <p>Wonderland is an open source toolkit for building virtual worlds. It provides live shared applications, immersive stereo audio, and a completely extensible platform for creating new 2D and 3D content. Wonderland is built on top of the <a href="http://projectdarkstar.com">Project Darkstar</a> game server infrastructure which provides an enterprise-grade platform for scalability, security, and authentication as well as the ability to connect with external data sources.
</div>

<div class="project">
    <a name="wiki"></a>
    <span class="projectName"><a href="http://wiki.java.net/bin/view/Javadesktop/ProjectWonderland">Project Wonderland Documentation Wiki</a></span>
    <p>The Wonderland Wiki contains the most up-to-date documentation on how to use and extend Wonderland. It contains information on project plans, and sections for end users, systems administrators, artists, and developers. Each section contains documentation and tutorials of interest to these different user populations. For example, the wiki contains articles on how to extend the Wonderland code with modules and how to import new 3D models. Community members can also contribute documentation to the wiki to help others use and understand Wonderland.
</div>

<div class="project">
    <a name="forum"></a>
    <span class="projectName"><a href="http://forums.java.net/jive/forum.jspa?forumID=112">Project Wonderland Forum</a></span>
    <p>If you have a question about Wonderland, the forum is a great place to post questions or search for answers.
</div>
	  
<! **************** SERVER ADMINISTRATION *******************>
<a name="admin">
<h3>Server Administration</h3></a>
          
Click <a href="admin">here</a> to access server administration.          
          
<! **************** TROUBLESHOOTING ********************** >

<a name="troubleshooting">
<h3>Troubleshooting</h3></a>

<div class="project">
    <span class="projectName"><a href="http://wiki.java.net/bin/view/Javadesktop/TroubleshootingGuide">Wonderland Troubleshooting</a></span>
    <p>Having trouble running Wonderland?  The <a href="http://wiki.java.net/bin/view/Javadesktop/TroubleshootingGuide">Troubleshooting Guide</a> can help with most common problems. Still stuck?  Try searching or posting to the <a href="#forum">discussion forum</a>.</p>
</div>

<div id="footer">
<hr>
<strong>For more information</strong> visit the
<a href="http://wonderland.dev.java.net">Wonderland Homepage</a>
</div>

</div>
</div>
</body>
</html>
