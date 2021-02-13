package com.samjakob.websocket_minecraft;

import com.google.gson.JsonObject;
import com.samjakob.websocket_minecraft.listeners.PlayerEvents;
import com.samjakob.websocket_minecraft.listeners.WorldEvents;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jglue.fluentjson.JsonBuilderFactory;
import org.jglue.fluentjson.JsonObjectBuilder;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class Main extends JavaPlugin {

    private static Main instance;

    private static ThornhillAPI websocketEndpoint;

    @Override
    public void onEnable() {
        super.onEnable();
        instance = this;

        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(new PlayerEvents(), this);

        getLogger().log(Level.INFO, "Starting WebSocket endpoint.");
        websocketEndpoint = new ThornhillAPI(new InetSocketAddress(
            getConfig().getString("websocket-server.host"),
            getConfig().getInt("websocket-server.port")
        ));

        new Thread(){

            @Override
            public void run() {
                super.run();
                websocketEndpoint.run();
            }
        }.start();

        new BukkitRunnable(){
            @Override
            public void run() {
                WorldEvents.sendWorldUpdates();
            }
        }.runTaskTimer(this, 0L, 20L * 2);

        new BukkitRunnable(){
            List<String> handledUUIDs;

            @Override
            public void run() {
                handledUUIDs = new ArrayList<>();

                for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                    List<Entity> nearbyEntities = player.getNearbyEntities(
                        getConfig().getDouble("distance"),
                        getConfig().getDouble("distance"),
                        getConfig().getDouble("distance")
                    );

                    for (Entity entity : nearbyEntities) {
                        if (entity instanceof Player)
                            continue;

                        // Ensure entities are not handled more often than necessary.
                        if (handledUUIDs.contains(entity.getUniqueId().toString()))
                            continue;

                        handledUUIDs.add(entity.getUniqueId().toString());

                        // Send entity data to Thornhill.
                        sendEntityData(entity);
                    }
                }
            }

            private void sendEntityData(Entity entity) {
                JsonObjectBuilder object = JsonBuilderFactory.buildObject()
                    .addObject("entity")
                        .add("uuid", entity.getUniqueId().toString())
                        .add("type", entity.getType().toString())
                        .add("name", entity.getName())
                        .end()
                    .addObject("location")
                        .add("x", entity.getLocation().getX())
                        .add("y", entity.getLocation().getY())
                        .add("z", entity.getLocation().getZ())
                        .add("pitch", entity.getLocation().getPitch())
                        .add("yaw", entity.getLocation().getYaw())
                        .end()
                    .addObject("world")
                        .add("uuid", entity.getWorld().getUID().toString())
                        .add("name", entity.getWorld().getName())
                        .end();

                if (entity instanceof Item) {
                    object.addObject("item")
                        .add("itemType", ((Item) entity).getItemStack().getType().toString())
                        .end();
                }

                if (entity instanceof Tameable) {
                    object.addObject("owner")
                        .add("uuid", ((Tameable) entity).getOwner().getUniqueId().toString())
                        .add("name", ((Tameable) entity).getOwner().getName())
                        .end();
                }

                Main.broadcast(object.getJson().toString());
            }
        }.runTaskTimer(this, 0L, 10L);
    }

    @Override
    public void onDisable() {
        super.onDisable();

        try {
            websocketEndpoint.stop();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static Main getInstance() {
        return instance;
    }

    public static void broadcast(String message) {
        if (websocketEndpoint == null) return;

        websocketEndpoint.broadcastThornhillMessage(message);
    }

}
