RasterPlugin
============

[Minecraft plugin to convert GIS raster files into worlds](http://thunderheadxpler.blogspot.com/2013/07/minecraft-gamification-of-gis.html).

Check out [this](http://wiki.bukkit.org/Setting_up_a_server) tutorial on how to install your own Mincraft server. And Check out [this](http://wiki.bukkit.org/Installing_Plugins) link to installing plugins.

## Building and packaging

First, download and install [Apache Maven](http://maven.apache.org/).

    $ mvn clean package

## Installing the Plugin

    $ cp target/RasterPlugin-1.0-SNAPSHOT.jar /your/bukkitserver/folder/plugins

If your server is already up and running, you can create a folder named _update_ in your _plugins_ folder and copy the
packaged jar into it and issue a [reload](http://wiki.bukkit.org/CraftBukkit_commands) command at the server command line prompt.
