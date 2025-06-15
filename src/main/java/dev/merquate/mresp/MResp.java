package dev.merquate.mresp;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.time.Duration;
import java.util.Arrays;

public final class MResp extends JavaPlugin implements Listener {
    private Component title;
    private Component subtitle;
    private Sound sound;
    private float volume;
    private float pitch;
    private Duration fadeIn;
    private Duration stay;
    private Duration fadeOut;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
        loadConfig();
        getCommand("mresp").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("mresp.reload")) {
                sender.sendMessage("§cУ вас нет прав на эту команду!");
                return true;
            }
            loadConfig();
            sender.sendMessage("§aКонфиг плагина успешно перезагружен!");
            return true;
        }
        return false;
    }

    private void loadConfig() {
        reloadConfig();

        MiniMessage mm = MiniMessage.miniMessage();

        String titleText = getConfig().getString("title", "<gradient:#ff0000:#ffff00>Возрождение</gradient>");
        String subtitleText = getConfig().getString("subtitle", "<gray>Вы были воскрешены</gray>");
        String soundConfig = getConfig().getString("sound", "ENTITY_PLAYER_LEVELUP,1.0,1.0");

        this.title = !titleText.equalsIgnoreCase("none") ? mm.deserialize(titleText) : Component.empty();
        this.subtitle = !subtitleText.equalsIgnoreCase("none") ? mm.deserialize(subtitleText) : Component.empty();

        // Парсинг настроек звука
        if (!soundConfig.equalsIgnoreCase("none")) {
            String[] soundParts = soundConfig.split(",");
            try {
                this.sound = Sound.valueOf(soundParts[0].trim());
                this.volume = Float.parseFloat(soundParts[1].trim());
                this.pitch = Float.parseFloat(soundParts[2].trim());
            } catch (Exception e) {
                getLogger().warning("Некорректные настройки звука! Использую значения по умолчанию");
                this.sound = Sound.ENTITY_PLAYER_LEVELUP;
                this.volume = 1.0f;
                this.pitch = 1.0f;
            }
        } else {
            this.sound = null;
        }

        int fadeInTicks = getConfig().getInt("times.fade_in", 10);
        int stayTicks = getConfig().getInt("times.stay", 40);
        int fadeOutTicks = getConfig().getInt("times.fade_out", 10);

        this.fadeIn = Duration.ofMillis(fadeInTicks * 50L);
        this.stay = Duration.ofMillis(stayTicks * 50L);
        this.fadeOut = Duration.ofMillis(fadeOutTicks * 50L);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();

        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) return;

                player.spigot().respawn();

                // Показ тайтла
                Title displayTitle = Title.title(
                        title,
                        subtitle,
                        Title.Times.times(fadeIn, stay, fadeOut)
                );
                player.showTitle(displayTitle);

                // Воспроизведение звука
                if (sound != null) {
                    player.playSound(player.getLocation(), sound, volume, pitch);
                }
            }
        }.runTaskLater(this, 1L);
    }
}