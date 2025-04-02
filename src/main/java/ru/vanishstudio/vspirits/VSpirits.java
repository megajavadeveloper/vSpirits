package ru.vanishstudio.vspirits;

import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import ru.vanishstudio.vspirits.Utils.ColorText;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class VSpirits extends JavaPlugin implements Listener {

    private final Map<UUID, SpiritType> activeSpirits = new HashMap<>();
    private final Map<UUID, ArmorStand> spiritHeads = new HashMap<>();
    private Map<String, SpiritType> spiritTypes = new HashMap<>();

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadSpiritTypes();

        getLogger().info(ColorText.color("&d[vSpirits] Плагин успешно запущен!"));
        getLogger().info(ColorText.color("&d[vSpirits] t.me/vanishstudio"));

        getServer().getPluginManager().registerEvents(this, this);
        startParticleTask();
        getCommand("spirit").setExecutor(this);
    }

    @Override
    public void onDisable() {
        for (ArmorStand armorStand : spiritHeads.values()) {
            armorStand.remove();
        }
        spiritHeads.clear();
    }

    private void loadSpiritTypes() {
        spiritTypes.clear();
        FileConfiguration config = getConfig();

        for (String spiritId : config.getConfigurationSection("spirits").getKeys(false)) {
            try {
                ConfigurationSection section = config.getConfigurationSection("spirits." + spiritId);
                SpiritType spirit = new SpiritType(spiritId, section);
                spiritTypes.put(spiritId.toLowerCase(), spirit);
            } catch (Exception e) {
                getLogger().warning("&4[vSpirits] Ошибка загрузки духа " + spiritId + ": " + e.getMessage());
            }
        }
    }

    private void startParticleTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                activeSpirits.forEach((uuid, spirit) -> {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null || !player.isOnline()) return;

                    spawnParticles(player, spirit);
                    applyEffects(player, spirit);
                    updateHeadPosition(player, spirit);
                });
            }
        }.runTaskTimer(this, 0, 5);
    }

    private void spawnParticles(Player player, SpiritType spirit) {
        Location particleLoc = player.getLocation().add(0, 0.5, 0);
        player.getWorld().spawnParticle(
                spirit.getParticle(),
                particleLoc,
                spirit.getParticleCount(),
                0.3,
                0.3,
                0.3,
                0.1
        );
    }

    private void updateHeadPosition(Player player, SpiritType spirit) {
        ArmorStand armorStand = spiritHeads.get(player.getUniqueId());
        if (armorStand == null) {

            armorStand = player.getWorld().spawn(player.getLocation().add(-0.5, 1, 0), ArmorStand.class);
            armorStand.setVisible(false);
            armorStand.setGravity(false);
            armorStand.setCustomName(player.getName() + "'s Spirit Head");
            armorStand.setCustomNameVisible(false);
            armorStand.setMarker(true);

            ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skullMeta = (SkullMeta) skull.getItemMeta();
            if (skullMeta != null) {
                skullMeta.setOwningPlayer(Bukkit.getOfflinePlayer(spirit.getHeadTexture()));
                skull.setItemMeta(skullMeta);
            }
            armorStand.getEquipment().setHelmet(skull);
            spiritHeads.put(player .getUniqueId(), armorStand);
        } else {
            armorStand.teleport(player.getLocation().add(-0.5, 1, 0));
        }
    }

    private void applyEffects(Player player, SpiritType spirit) {
        player.addPotionEffect(spirit.getEffect());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        UUID playerId = event.getPlayer().getUniqueId();
        activeSpirits.remove(playerId);
        ArmorStand armorStand = spiritHeads.remove(playerId);
        if (armorStand != null) {
            armorStand.remove();
        }
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ColorText.color("&c[vSpirits] Эта команда только для игроков!"));
            return true;
        }

        Player player = (Player) sender;

        if (args.length == 1) {
            String spiritId = args[0].toLowerCase();
            if (spiritId.equals("off")) {
                if (activeSpirits.containsKey(player.getUniqueId())) {
                    activeSpirits.remove(player.getUniqueId());
                    ArmorStand armorStand = spiritHeads.remove(player.getUniqueId());
                    if (armorStand != null) {
                        armorStand.remove();
                    }
                    player.sendMessage(ChatColor.GREEN + "Вы отключили духа.");
                } else {
                    player.sendMessage(ChatColor.RED + "У вас нет активированного духа.");
                }
                return true;
            }

            SpiritType spirit = spiritTypes.get(spiritId);

            if (spirit != null) {
                if(!player.hasPermission(spirit.getPermission())) {
                    player.sendMessage(ColorText.color("&cУ вас недостаточно прав!"));
                    return true;
                } else {
                    activeSpirits.put(player.getUniqueId(), spirit);
                    player.sendMessage(ColorText.color(spirit.getDisplayName() + " активирован!"));
                }
            } else {
                player.sendMessage(ColorText.color("&cДоступные типы духов: " + String.join(", ", spiritTypes.keySet())));
            }
            return true;
        }

        player.sendMessage(ColorText.color("&cИспользование: /spirit <тип>"));
        return false;
    }

    @Override
    public void reloadConfig() {
        super.reloadConfig();
        loadSpiritTypes();
    }
}