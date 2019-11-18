<!doctype html>
<html>
<head>
<title>Funkrufsender DB0XYZ</title>
<meta name="author" content="Ralf Wilke DH3WR">
<meta name="description" content="Funkrufsender DB0XYZ">
<meta name="keywords" content="Funkruf, POCSAG, UniPager, C9000, Ericsson">
</head>
<body>
<header>
<h1 align="center">Funkrufsender DB0XYZ</h1>
</header>
<p><a href="/frontansicht.jpg" target="_blank"><img src="/frontansicht_klein.jpg" alt="Frontansicht"></a></p>
<h3>
<?php
	$ip = $_SERVER['SERVER_ADDR'];
	print "<a href=\"http://".$ip.":8073\" target=\"_blank\">Admin-Interface von Unipager</a>"; ?>
</h3>
<h3>Temperaturen im Sender</h3>
<p><a href="/munin/db0xyz.ampr.org/c9000.db0xyz.ampr.org/index.html">
 <img src="/munin-cgi/munin-cgi-graph/db0xyz.ampr.org/c9000.db0xyz.ampr.org/1wire-week.png" alt="Temperaturen"></a></p>
<p>Funkrufe absetzen auf <a href="https://hampager.de" target="_blank">hampager.de</a> oder
<a href="http://dapnet.db0sda.ampr.org" target="_blank">dapnet.db0sda.ampr.org</a>.</p>
<footer>
  <p>Umgebaut durch Amateurfunkgruppe an der RWTH Aachen 2019
<a href="https://www.afu.rwth-aachen.de" target="_blank">www.afu.rwth-aachen.de (Internet)</a>
<a href="http://db0sda.ampr.org" target="_blank">db0sda.ampr.org (HAMNET)</a> </p>
</footer>
</body>
</html>

