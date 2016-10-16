Auginte event sourced
=====================

This is one of [augitne tools](http://auginte.com).

Next step for personal knowledge management tool is collaboration.
This project was created to address 2 main functions:

 * It should be easy for others to read and update common data
 * It should be easy to view history of changes or undu latest changes

Run during development
----------------------

```
sbt "~auginteEventSourcedJS/fastOptJS"
sbt "~auginteEventSourcedJVM/re-start"
```

Deploy
------

```
sbt "auginteEventSourcedJS/fullOptJS"
sbt "auginteEventSourcedJVM/pack"
```

```
java -cp "$SOFTWARE_PATH/pack/lib/*" \
  -Dauginte.host=0.0.0.0 \
  -Dauginte.port=8111 \
  -Dauginte.storage.path=data \
  -Dauginte.compiledJs.path=js \
  -Dauginte.compiledJs.name=auginte-event-sourced-opt.js \
  -Dauginte.compiledCss.path=css \
  com.auginte.eventsourced.Main
```

Architectural decisions
-----------------------

 * Event sourcing
    * Covers history and undo functionality
    * Immutable messages integrates nicely in distributed environment

 * Separate write and (delayed) read
    * Client updates own state temporary, does not wait for server response - can work offline
    * Listens acknowledge/udpated state from server - guaranteed stored and consistency
    * Storage/view sepations does not block user interface - beter user experience

 * Real time changes from other clients (send first, store later)
    * Makes user interface more usable when collaborating
    * Server overwrites with consistent state - errors could be corrected from histroy later

 * Client is responsible for resending messages:
    * Covers all cases and simplifies whole architecture
    * Integrates nicely with web-offline usage

Development tips
----------------

To test via local network, Set server arguments in `sbt` interactive mode:
```
set javaOptions += "-Dauginte.host=192.168.0.123"
```

Known issues
------------

* `sbt.version=0.13.12` throws deprecation warnings

License
-------

[Apache 2.0](LICENSE)

Author
------

Aurelijus Banelis https://auginte.com