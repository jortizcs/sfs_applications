<!DOCTYPE html>
<html lang="en">
  <head>
    <meta charset="utf-8">
    <title>Energy Lens Application</title>
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description" content="">
    <meta name="Jorge Ortiz" content="">

    <!-- Le styles -->
    <link href="lib/bootstrap/css/bootstrap.css" rel="stylesheet">
    <style type="text/css">
      body {
        padding-top: 1px;
        padding-bottom: 1px;
      }
    </style>
    <link href="lib/bootstrap/css/bootstrap-responsive.css" rel="stylesheet">

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
              <li class="active"><a href="#">Home</a></li>
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

    <div class="container">
	<div class="hero-unit">
	<h2>Energy Lens on the Physical World</h2>
        <div class='big-media'>
          <center><img alt='Berkeley Mobile Auditing System.' height='340' src='bmas_arch.png' width='740' /></center>
        </div>
	</div>
      <!-- Main hero unit for a primary marketing message or call to action -->
      <!--<div class="hero-unit">
        <h1>Hello, world!</h1>
        <p>This is a template for a simple marketing or informational website. It includes a large callout called the hero unit and three supporting pieces of content. Use it as a starting point to create something more unique.</p>
        <p><a class="btn btn-primary btn-large">Learn more &raquo;</a></p>
      </div>-->

      <!-- Example row of columns -->
      <div class="row">
        <div class="span4">
          <h2>Tag</h2>
           <p>Generate QR Codes to place on your personal items.  Tagging items allows you to create a virtual representation of your device that
		enables the coupling of virtual services on your devices, such as the <a href="">device energy viewer</a> or 
		the <a href="">personal energy view</a> or control it remotely with the <a href="">remote control</a> app.</p>
          <p><a class="btn" href="qrcgen.php?num=4">Generate now &raquo;</a></p>
        </div>
        <div class="span4">
          <h2>Register</h2>
           <p>Register your tagged items using the <a href="">Android-based mobile application.</a>.  Using the mobile application you can register your item, input
		information about the item and attach virtual services on it as well.  Make sure to <a href="depList.php">set the deployment configuration</a>
            on the phone before getting started.</p>
          <p><a class="btn" href="#">View details &raquo;</a></p>
       </div>
        <div class="span4">
          <h2>Swipe</h2>
          <p>Swipe the QR codes in the world around you!  By swiping QR codes in the physical world, you can pull up the virtual services assocaited with that item.
		If an item moves, make sure to re-swipe the item in the new location, that way we can provide you with context-aware virtual services.</p>
          <p><a class="btn" href="#">View details &raquo;</a></p>
        </div>
      </div>

      <hr>

      <footer>
        <p>&copy; Company 2012</p>
      </footer>

    </div> <!-- /container -->

    <!-- Le javascript
    ================================================== -->
    <!-- Placed at the end of the document so the pages load faster -->
    <script src="lib/bootstrap/js/bootstrap.min.js"></script>
    <!--<script src="../assets/js/jquery.js"></script>
    <script src="../assets/js/bootstrap-transition.js"></script>
    <script src="../assets/js/bootstrap-alert.js"></script>
    <script src="../assets/js/bootstrap-modal.js"></script>
    <script src="../assets/js/bootstrap-dropdown.js"></script>
    <script src="../assets/js/bootstrap-scrollspy.js"></script>
    <script src="../assets/js/bootstrap-tab.js"></script>
    <script src="../assets/js/bootstrap-tooltip.js"></script>
    <script src="../assets/js/bootstrap-popover.js"></script>
    <script src="../assets/js/bootstrap-button.js"></script>
    <script src="../assets/js/bootstrap-collapse.js"></script>
    <script src="../assets/js/bootstrap-carousel.js"></script>
    <script src="../assets/js/bootstrap-typeahead.js"></script>-->

  </body>
</html>
