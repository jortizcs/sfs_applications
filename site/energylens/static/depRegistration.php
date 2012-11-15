<html lang="en">
  <head>
    <meta charset="utf-8">
    <title>Energy Lens Application</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="Jorge Ortiz" content="">

    <!-- Le styles -->
    <link href="../bootstrap/docs/assets/css/bootstrap.css" rel="stylesheet">
    <style type="text/css">
      body {
        padding-top: 1px;
        padding-bottom: 1px;
      }
    </style>
    <link href="../bootstrap/docs/assets/css/bootstrap-responsive.css" rel="stylesheet">

    <!-- Le HTML5 shim, for IE6-8 support of HTML5 elements -->
    <!--[if lt IE 9]>
      <script src="//html5shim.googlecode.com/svn/trunk/html5.js"></script>
    <![endif]-->

    <!-- Le fav and touch icons -->
    <link rel="shortcut icon" href="images/favicon.ico">
    <link rel="apple-touch-icon" href="images/apple-touch-icon.png">
    <link rel="apple-touch-icon" sizes="72x72" href="images/apple-touch-icon-72x72.png">
    <link rel="apple-touch-icon" sizes="114x114" href="images/apple-touch-icon-114x114.png">
  </head>

  <body>

    <div class="navbar navbar-fixed-top">
      <div class="navbar-inner">
        <div class="container">
          <a class="btn btn-navbar" data-toggle="collapse" data-target=".nav-collapse">
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
            <span class="icon-bar"></span>
          </a>
	  <a class="brand" href="http://streamfs.cs.berkeley.edu">StreamFS Apps</a>
          <div class="nav-collapse">
            <ul class="nav">
              <li class="active"><a href="index.php">Home</a></li>
              <li><a href="http://www.eecs.berkeley.edu/~jortiz">About</a></li>
              <li><a href="mailto:jortiz@cs.berkeley.edu">Contact</a></li>
		
            </ul>
            <ul class="nav pull-right nav-pills">
      	    <li><a class="btn btn-primary btn-large">CalNet Login</a></li>
      	    </ul>
          </div><!--/.nav-collapse -->
        </div>
      </div>
    </div>
<br><br><br>

<center>
<table>
    <tr>
        <td></td>


        <td>
        <form action="depList.php" method="post"> 
        <label for="name">Deplyoment Name:</label> 
        <input type="text" name="deployment" required placeholder="Name" />

        <label for="website">Host:</label> 
        <input type="url" name="host" required placeholder="http://is4server.com:8080" />

        <label for="website">Root:</label> 
        <input type="search" name="root" required placeholder="/" />

        <label for="website">Home path:</label> 
        <input type="search" name="homepath" required placeholder="/buildings/SDH" />

        <label for="website">Qr-code path:</label> 
        <input type="search" name="qrchome" required placeholder="/buildings/SDH/qrc" />

        <label for="website">Taxonomy path:</label> 
        <input type="search" name="taxhome" required placeholder="/buildings/SDH/tax" />

        <label for="website">Spaces path:</label> 
        <input type="search" name="spaceshome" required placeholder="/buildings/SDH/spaces" />

        <label for="website">Inventory path:</label> 
        <input type="search" name="invhome" required placeholder="/buildings/SDH/inventory" />

        <br>
        <input type="submit" value="Register" />
        </form>
        <form action="depList.php" method="get">
        <input type="submit" value="Cancel" />
        </form>
        </td>

        <td></td>
    </tr>
</table>
</center>


</body>
</html>
