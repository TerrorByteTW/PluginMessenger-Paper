package org.reprogle.pluginmessenger;

import com.google.common.io.ByteArrayDataInput;
import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.plugin.messaging.PluginMessageListener;
import org.jetbrains.annotations.NotNull;

public final class PluginMessenger extends JavaPlugin implements CommandExecutor, PluginMessageListener {

    public static PluginMessenger plugin;

    @Override
    public void onEnable() {
        plugin = this;
        this.getCommand("broadcast").setExecutor(this);
        this.getCommand("creeper").setExecutor(this);
        this.getLogger().info("Plugin messenger booted up!");
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");

        getServer().getMessenger().registerOutgoingPluginChannel(this, "demo:main");
        getServer().getMessenger().registerIncomingPluginChannel(this, "demo:main", this);
    }

    @Override
    public void onDisable() {
        this.getLogger().info("Plugin messenger shutting down");
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("You must be a player to use this command, as plugin messages rely on the player connection"));
            return false;
        }

        if(command.getName().equals("broadcast")) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("MessageRaw");
            out.writeUTF("ALL");


            StringBuilder builder = new StringBuilder();
            for (String string : args) {
                builder.append(string).append(" ");
            }

            String message = builder.toString().trim();
            out.writeUTF(GsonComponentSerializer.gson().serialize(MiniMessage.miniMessage().deserialize(message)));

            player.sendPluginMessage(this, "BungeeCord", out.toByteArray());
            player.sendMessage("Broadcasting your message!");

        } else if (command.getName().equals("creeper")){
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeUTF("RequestCreeper");

            player.sendPluginMessage(this, "demo:main", out.toByteArray());
            player.sendMessage("Requesting creepers be spawned near all players on all servers");
        }

        return true;
    }

    @Override
    public void onPluginMessageReceived(@NotNull String channel, @NotNull Player player, byte[] bytes) {
        if(!channel.equals("demo:main")) return;

        ByteArrayDataInput in = ByteStreams.newDataInput(bytes);
        String subchannel = in.readUTF();

        if(subchannel.equals("SpawnCreeper")) {
            Location location = player.getLocation();
            location.getWorld().spawnEntity(location, EntityType.CREEPER);
        }
    }
}
