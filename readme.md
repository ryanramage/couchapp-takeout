Couchapp Takeout provides couchapps with an installer and desktop experience. 

From one link in your couchapp, it will install all the pieces on an end users computer, to run it locally. It solves these problems:

* Do you have a couchapp that you want to distribute to end users?
* Want a simple way for users to use your couchapp online and offline?
* Do you want to provide a way for a user to liberate their data?

If you want to see it on an existing application, install 'Couch Tasks' on your computer:

http://ecko-it.couchone.com:5984/couchtasks/_design/takeout/install.html


You can add it into your couchapp in about two minutes. 

# How to use

### Replicate Couchapp-Takeout into your application

replicate this database into your couchapp:
http://ecko-it.iriscouch.com/takeout

It has two design docs: 

* _design/takeout
* _design/takeout-settings.jnlp

so make sure these do not conflict with any of your existing design docs.

### Configure Couchapp-Takeout 

Edit the design document _design/takeout-settings.jnlp in your database. Here are the settings to change:

```json
{
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

### Brand Couchapp-Takeout 

On the design doc '_design/takeout-settings.jnlp', replace the two attachements called logo.png and splash.png with your own logo and splash image. 
Keep the names exactly the same.


### Link to your install page. 

You will now have a install page ready to give to users. 

_design/takeout/install.html





Some notes:

* End users will need to have up to date java on their machine. All mac osx meet this requirement, and most windows users will have it installed.
* Currently we only have couch binaries for windows and mac. Other operating systems to come.
* If you have a couch installed locally on port 5984 couchapp-desktop will just reuse that db. If you to see the full install, turn off couch on that port. A new couch will be installed on a random port.








## License

This project is under an [Apache 2.0 license](https://github.com/ryanramage/couchapp-takeout/blob/master/LICENSE.txt) .





