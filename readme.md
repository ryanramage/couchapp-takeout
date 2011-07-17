Couchapp Takeout provides couchapps with an installer and desktop experience. 

From one link in your couchapp, it will install all the pieces on an end users computer, to run it locally. It solves these problems:

* Do you have a couchapp that you want to distribute to end users?
* Want a simple way for users to use your couchapp online and offline?
* Do you want to provide a way for a user to liberate their data?

It is incredibly easy to get it into your couchapp, and it can be branded to with your apps name and icons. Here is how to get started:

## Replicate Couchapp-Takeout into your application

replicate this database into your couchapp:
http://ecko-it.couchone.com/takeout

It has one design doc named _design/takeout so make sure this does not conflict with an existing doc in your db.


## Configure Couchapp-Takeout 

Edit the design document _design/takeout in your database. Here are the settings to change:

```json
takeout: {
   "appName": "Takeout",
   "vendor": "Ecko-it",
   "homepage": "http://eckoit.com",
   "description": "A great couchapp that does amazing stuff",
   "localStartUrl": "_design/takeout/index.html",
   "advanced": {
       "syncType": "bi-directional",
       "main-jar": "couchapp-takeout-1.0-SNAPSHOT.jar",
       "main-class": "com.github.couchapptakeout.App"
   }
}
```

change the *appName*, *vendor*, *homepage* and *description* to match your application.

*localStartUrl* is the page that is shown on the users computer after the install, and everytime they launch from their desktop.
You probably want something like _design/app/index.html   But maybe you want a different interface when the couchapp is running locally? 
You could have a sepereate design doc like _design/installed/index.html

*syncType* is the type of continuous replication that is started on the users machine to the couchdb it was launched from. Valid values are:

* 'bi-directional' useful for online/offline style applications. The perfect fit for couch!
* 'pull' useful for when you dont want to host user data, but want to distrubte your couchapp to end users. Allows them to receive updates to the couchapp automatically.
* 'push' not sure when this would be usefull, but for completeness.
* 'none' no replication is started.

## Brand Couchapp-Takeout 

Replace the two attachements on the design doc called logo.png and splash.png with your own logo and splash image. Keep the names exactly the same.


## Add a link from your application

Now you need a link in your application called 'install' or 'download' that launches the installer. Here is how to do it.

Import the takeout jquery plugin into your page (make sure you have jquery as well).
Add a div where the link will appear,
And call the takeout plugin with some information.
Here is some example html:

```html
<head>
    <script type="text/javascript" src="jquery-1.6.2.min.js" ></script>
    <script type="text/javascript" src="_design/takeout/takeout.js" ></script>
    <script type="text/javascript">
        $(function() {
            $('#takeout').takeout({linkText : "Download", localText : "Installed"});
        });
    </script>
</head>
<body>

    <h3>Example 1 - As a link</h3>
    <a href="_show/takeout.jnlp">Take out!</a>

    <h3>Example 2 - Changes based on running remote or local.</h3>
    <p>
        On the remote app (where you are installing from) this will be a button.
        On the local app this will just be text. It  would confuse the user if they see the button again on the local app!.
    </p>
    <div id="takeout"></div>
</body>
```


## Try It Out!


Go to your application's page where you have the link to the installer. Click the link and see what happens!

Ok, also a few things

* End users will need to have java on their machine. All mac osx meet this requirement, and most windows users will have it installed.
* Currently we only have couch binaries for windows and mac. Other operating systems to come.
* Note for Google Chrome. By default Chrome will list the download on the bottom of the page, and to the right of the filename is a small drop down arrow, click it. Select 'Always open files of this type'.
* If you have a couch installed locally on port 5984 couchapp-desktop will just reuse that db. If you to see the full install, turn off couch on that port. A new couch will be installed on a random port.


If you want to see it on an existing application, the links on this page will install 'Couch Tasks' on your computer:

http://ecko-it.couchone.com:5984/couchtasks/_design/takeout/index.html


## The Desktop

Couchapp-desktop will give a desktop icon for the application, and when running it will appear in the system tray. There is a simple menu on the tray icon to open the start url, and close the application.









