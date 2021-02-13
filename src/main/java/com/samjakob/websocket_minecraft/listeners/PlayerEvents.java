package com.samjakob.websocket_minecraft.listeners;

import com.samjakob.websocket_minecraft.Main;
import org.bukkit.attribute.Attributable;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Damageable;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.*;
import org.jglue.fluentjson.JsonBuilderFactory;

public class PlayerEvents implements Listener {

    @EventHandler(priority = EventPriority.MONITOR)
    public void onAttack(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            Player player = (Player) event.getDamager();
            Entity target = event.getEntity();

            if (!(target instanceof Damageable) || !(target instanceof Attributable)) return;

            Main.broadcast(JsonBuilderFactory.buildObject()
                .addObject("player")
                    .add("isOnline", true)
                    .add("uuid", player.getUniqueId().toString())
                    .add("name", player.getName())
                    .add("ip", player.getAddress().getHostName())
                    .add("port", player.getAddress().getPort())
                    .end()
                .addObject("target")
                    .add("isPlayer", target instanceof Player)
                    .add("uuid", target.getUniqueId().toString())
                    .add("type", target.getType().toString())
                    .add("name", target.getName())
                    .add("maxHealth", ((Attributable) target).getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue())
                    .add("health", ((Damageable) target).getHealth())
                .addObject("event")
                    .add("damage", event.getFinalDamage())
                .getJson().toString()
            );
        }
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        sendPlayerUpdate(event.getPlayer());
    }

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        sendPlayerUpdate(event.getPlayer());
    }

    @EventHandler
    public void onSlotChange(PlayerItemHeldEvent event) {
        sendPlayerUpdate(event.getPlayer());
    }

    @EventHandler
    public void onDropItem(PlayerDropItemEvent event) {
        sendPlayerUpdate(event.getPlayer());
    }

    private void sendPlayerUpdate(Player player) {
        Main.broadcast(JsonBuilderFactory.buildObject()
            .addObject("player")
                .add("isOnline", true)
                .add("uuid", player.getUniqueId().toString())
                .add("name", player.getName())
                .add("ip", player.getAddress().getHostName())
                .add("port", player.getAddress().getPort())
                .end()
            .addObject("location")
                .add("x", player.getLocation().getX())
                .add("y", player.getLocation().getY())
                .add("z", player.getLocation().getZ())
                .add("pitch", player.getLocation().getPitch())
                .add("yaw", player.getLocation().getYaw())
                .end()
            .addObject("world")
                .add("uuid", player.getWorld().getUID().toString())
                .add("name", player.getWorld().getName())
                .end()
            .addObject("inventory")
                .add("heldItemSlot", player.getInventory().getHeldItemSlot())
                .add("heldItem", player.getInventory().getItemInMainHand().getType().toString())
            .getJson().toString()
        );
    }

    @EventHandler
    public void onLeave(PlayerQuitEvent event) {
        Main.broadcast(JsonBuilderFactory.buildObject()
            .addObject("player")
                .add("isOnline", false)
                .add("uuid", event.getPlayer().getUniqueId().toString())
                .add("name", event.getPlayer().getName())
                .end()
            .getJson().toString()
        );
    }

}