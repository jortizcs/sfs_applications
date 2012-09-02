
##Development server
* [ec2-204-236-167-113.us-west-1.compute.amazonaws.com](http://ec2-204-236-167-113.us-west-1.compute.amazonaws.com)  

###Login instructions  
        ssh -i /path/to/privkey [username]@ec2-204-236-167-113.us-west-1.compute.amazonaws.com  

###Wikis
[TOC](https://github.com/jortizcs/sfs_applications/wiki)  
[Ideas and Documentation](https://github.com/jortizcs/sfs_applications/wiki/Energylens-ideas)  
[Developer Info](https://github.com/jortizcs/sfs_applications/wiki/EnergyLensDevelopment)

###Markdown Reference
[About Markdown](http://en.wikipedia.org/wiki/Markdown)  
[Markdown Syntax](http://daringfireball.net/projects/markdown/syntax)

###WireIt Grapher
* [WireIt](https://github.com/neyric/wireit)
* [More API Docs](http://dev.lshift.net/james/wireit/wireit/guide.html)
###Restarting the server  
        apachectl restart
###Getting php and django to coexist
* Look at httpd.conf in /etc/apache2
***


#EnergyLens site plan by page**

* Link-header batch
* Link to landing page
* Link to mobile app download page
* Link to real-time graph page

* Landing page (partially completed)
  * Information about the project
* video that explains how it works (can be done last)
  * Link to account creation
  * re-directs to carnet login page if user not authenticated as Berkeley affiliate
  * Link to QR code printing page
  * goes to carnet login page if user not yet authenticated as Berkeley affiliate
  * Link to mobile app download
  * goes to carnet login page if the user not yet authenticated as Berkeley affiliate

  ====
  * Calnet login
  * if the user has never used the site, we should try to use authentication info (login, pw-hash?) to create an account on streamfs as well

* Account creation page (if necessary) 
  * Link-header batch
  * creates an account in streamfs
  * Form that includes
  * Full name, login, pw, floor, room/cubicle, email address.  If successful, re-direct to mobile app download page

  ===

  * QR code generation page (already written; needs some formatting)
  * Link-header batch
* Generate in a single batch of QR codes (6)

  * Mobile app download page
  * Installation instructions
  * download link
  * QR code to set up the app configuration 
  * Link-header batch

* Graph page (basic browser style)
  * Side drop-list of resources viewable by the user and floor-level aggregates
  * power over day, week, month
  * energy over day, week, month
  * Statistics:
  * the number of items the user "owns"
  * the number of items the user has viewing access to
  * groups this user belongs to
  * pie chart of device types registered
  * broken down by  floor
  * broken down by user

  * Graph page (mobile style)
* Side drop-list of resources viewable by the user and floor-level aggregates (only show one graph at a time)
  * power over day, week, month
  * energy over day, week, month
* Statistics (links)
  * the number of items the user "owns"
  * the number of items the user has viewing access to
  * groups this user belongs to
  * pie chart of device types registered
  * broken down per floor
  * broken down for the specific user

  * Backend work
  * Creation of users and groups (streamfs)
  * Linear interpolator (nodejs)
* Aggregator (nodejs)
  * average
  * total
* Mobile app (android)
  * transactions
  * caching
  * create downloadable erver
* [ec2-204-236-167-113.us-west-1.compute.amazonaws.com](http://ec2-204-236-167-113.us-west-1.compute.amazonaws.com/energylens)


Login instructions
`ssh -i /path/to/privkey [username]@ec2-204-236-167-113.us-west-1.compute.amazonaws.com`

***

**EnergyLens site plan by page**

* Link-header batch
  * Link to landing page
  * Link to mobile app download page
  * Link to real-time graph page

* Landing page (partially completed)
  * Information about the project
  * video that explains how it works (can be done last)
  * Link to account creation
    * re-directs to carnet login page if user not authenticated as Berkeley affiliate
  * Link to QR code printing page
    * goes to carnet login page if user not yet authenticated as Berkeley affiliate
  * Link to mobile app download
    * goes to carnet login page if the user not yet authenticated as Berkeley affiliate

====
* Calnet login
  * if the user has never used the site, we should try to use authentication info (login, pw-hash?) to create an account on streamfs as well

* Account creation page (if necessary) 
  * Link-header batch
  * creates an account in streamfs
  * Form that includes
    * Full name, login, pw, floor, room/cubicle, email address.  If successful, re-direct to mobile app download page

===

* QR code generation page (already written; needs some formatting)
  * Link-header batch
  * Generate in a single batch of QR codes (6)

* Mobile app download page
  * Installation instructions
  * download link
  * QR code to set up the app configuration 
  * Link-header batch

* Graph page (basic browser style)
  * Side drop-list of resources viewable by the user and floor-level aggregates
    * power over day, week, month
    * energy over day, week, month
  * Statistics:
    * the number of items the user "owns"
    * the number of items the user has viewing access to
    * groups this user belongs to
    * pie chart of device types registered
      * broken down by  floor
      * broken down by user

* Graph page (mobile style)
  * Side drop-list of resources viewable by the user and floor-level aggregates (only show one graph at a time)
    * power over day, week, month
    * energy over day, week, month
  * Statistics (links)
    * the number of items the user "owns"
    * the number of items the user has viewing access to
    * groups this user belongs to
    * pie chart of device types registered
      * broken down per floor
      * broken down for the specific user

* Backend work
  * Creation of users and groups (streamfs)
  * Linear interpolator (nodejs)
  * Aggregator (nodejs)
    * average
    * total
  * Mobile app (android)
    * transactions
    * caching
    * create downloadable image
    * personalized tagging
  * Register application to enable Calnet id usage

* General statistics
  * number of registered users
  * number of registered devices
  * number of mobile app downloads


***
  * personalized tagging
  * Register application to enable Calnet id usage

  * General statistics
  * number of registered users
  * number of registered devices
  * number of mobile app downloads

