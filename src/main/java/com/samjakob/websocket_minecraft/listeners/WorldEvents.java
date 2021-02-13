package com.samjakob.websocket_minecraft.listeners;

import com.samjakob.websocket_minecraft.Main;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.event.Listener;
import org.jglue.fluentjson.JsonBuilderFactory;

public class WorldEvents implements Listener {

    public static void sendWorldUpdates() {
        World overworld = Bukkit.getServer().getWorlds().get(0);

        Main.broadcast(JsonBuilderFactory.buildObject()
            .addObject("world")
                .add("uuid", overworld.getUID().toString())
                .add("name", overworld.getName())
                .add("time", overworld.getTime())
                .add("fullTime", overworld.getFullTime())
                .add("hasStorm", overworld.hasStorm())
                .add("isThundering", overworld.isThundering())
            .getJson().toString()
        );
    }

}
