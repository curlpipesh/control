# Control

A magic bans-and-stuff plugin for Bukkit/Spigot. 

## Features

 * Bans (temp. and perm.)
 * Mutes (temp. and perm.)
 * Command-mutes (temp. and perm.)
 * Kicks
 * Adminchat
 * Automated advertisement detection

## Building

Building this plugin relies on having spigot-1.8.7+ and Essentials-2.x installed in the local Maven repository. If you don't already have this set up, installing them might look like:

```
mvn install:install-file -Dfile=./spigot-1.8.7.jar -DgroupId=org.spigotmc.spigot -DartifactId=spigot -Dversion=1.8.7 -Dpackaging=JAR
mvn install:install-file -Dfile=./Essentials-2.x.jar -DgroupId=com.earth2me.essentials -DartifactId=essentials -Dversion=2.x -Dpackaging=JAR
```

Afterwards, just run `mvn clean package`.

## Installation

Have SQLite3 installed on the server, and then place the JAR into the `plugins/` directory. Will likely conflict with other ban-related plugins, anti-ad plugins, and/or adminchat plugins.

## Usage

See the commands in [plugin.yml](https://github.com/curlpipesh/control/blob/master/src/main/resources/plugin.yml). The general format of commands is `/<command> <target> [[t:]time] [reason]`.
