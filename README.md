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

Building this plugin relies on having spigot-1.8.8 installed in the local Maven repository.

Other dependencies:

[Utils](https://github.com/curlpipesh/skirtsutils)
[Users](https://github.com/curlpipesh/skirtsusers)
[CommunicationsFramework](https://github.com/iKeirNez/CommunicationsFramework)

Afterwards, just run `mvn clean package`.

## Installation

Have SQLite3 installed on the server, and then place the JAR into the `plugins/` directory. Will likely conflict with other ban-related plugins, anti-ad plugins, and/or adminchat plugins.

## Usage

See the commands in [plugin.yml](https://github.com/curlpipesh/control/blob/master/src/main/resources/plugin.yml). The general format of commands is `/<command> <target> [[t:]time] [reason]`.
