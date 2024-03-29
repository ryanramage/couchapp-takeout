Couchapp Takeout provides couchapps with an installer and desktop experience. 

From one link in your couchapp, it will install all the pieces on an end users computer, to run it locally. It solves these problems:

* Do you have a couchapp that you want to distribute to end users?
* Do you want updates be pushed to them automatically?
* [Optional] Do you want to have their local data sync with an online couch?

Here are some projects using couchapp-takeout:

* The Little Library [http://library.ic.tl/library/_design/takeout/install.html]
* Ecko-It [http://eckoit.com/download.html]
* CouchTasks [http://ecko-it.couchone.com:5984/couchtasks/_design/takeout/install.html]



# How to use

You can add an installer to your couchapp in about two minutes.

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

* 'bi-directional' useful for online/offline style applications. Users local changes will be synced with the database takeout is installed from.
* 'pull' useful want to just distrubte your couchapp to end users. Allows them to receive updates to their local couchapp automatically, when the online couchapp is updated.
* 'push' not sure when this would be usefull, but for completeness.
* 'none' no continuous replication is started.

### Brand Couchapp-Takeout 

On the design doc '_design/takeout-settings.jnlp', replace the two attachements called logo.png and splash.png with your own logo and splash image. 
Keep the names exactly the same.


### Link to your install page. 

You will now have a install page ready to give to users. 

_design/takeout/install.html





# Some notes:

* End users will need to have up to date java on their machine. All mac osx meet this requirement, and most windows users will have it installed.
* Currently we only have couch binaries for windows and mac. Other operating systems to come.
* If you have a couch installed locally on port 5984 couchapp-desktop will just install into that db. If you experience the full install, turn off couch on that port. A new couch will be installed on a random port.
* CouchDB is installed in .couchapptakeout in the users home directory.


# System Processes

While Couchapp-Takeout is running, the following processes will be seen:

1. The initial java process. 
2. Couchdb and it's associated erlang processes. 

# Uninstalling

## Under Windows

1. Click on the program icon in the taskbar (or on the desktop), and click on "Quit" .
2. Go to Start>Control Panel>Uninstall Programs, and find the app's name in the list of installed programs, and click on it to uninstall.
3. Delete the .couchapptakeout folder in your home directory.



# The Distant Future 

At this point we have ironed out most of the workflow, but Couchapp Takeout still feels a bit heavy from the launch. I really need to spend some time reducing dependancies, making smaller couch binaries, and making it feel more snappy. 

For mobile users, on the install page, if a mobile os is detected we can generate a link, or intent, that takes them to a mobile couch in the app store, and somehow passes the replication params.

Create plugins that can run code on the users desktop. For those tasks where a couchapp does not have enough horse power. I have started this here:
https://github.com/ryanramage/couchapp-takeout-plugins

And some upcoming plugins:

* https://github.com/ryanramage/couch-audio-recorder
* a couchdb-lucene or elasticsearch plugin 

Maybe try and integrate with other less 'heavyweight' replicators, like

* https://github.com/drsm79/ReplicateMe
* https://chrome.google.com/webstore/detail/clbdmcdmjlpgedncppkbkepbilnhgddh
* https://github.com/mikeal/pouchdb

  


## License

This project is under an [Apache 2.0 license](https://github.com/ryanramage/couchapp-takeout/blob/master/LICENSE.txt) .


## Jenkins Build
CloudBees graciously offers free Jenkins builds to open source projects. See the build for Couchapp-Takeout here:

https://reupholster.ci.cloudbees.com/job/Takeout%20Java%20Build/




