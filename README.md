Auginte event sourced
=====================

Compile and run
---------------

```
sbt pack
```

```
java -cp "$SOFTWARE_PATH/pack/lib/*" \
 -Dauginte.host=0.0.0.0 \
 -Dauginte.port=8111 \
 com.auginte.eventsourced.Main
```

Known issues
------------

`sbt.version=0.13.12` throws deprecation warnings