package ru.vanishstudio.vspirits;

import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

public class SpiritType {
    private final String id;
    private final Particle particle;
    private final int particleCount;
    private final PotionEffect effect;
    private final String displayName;
    private final String permission;
    private final String headTexture;

    public SpiritType(String id, ConfigurationSection config) {
        this.id = id;
        this.particle = Particle.valueOf(config.getString("particle"));
        this.particleCount = config.getInt("count", 5);
        this.displayName = config.getString("display_name");
        this.permission = config.getString("permission", "vspirits." + id);
        this.headTexture = config.getString("headbase64", "");

        PotionEffectType effectType = PotionEffectType.getByName(config.getString("effect.type"));
        int duration = config.getInt("effect.duration", 200);
        int amplifier = config.getInt("effect.amplifier", 0);
        this.effect = new PotionEffect(effectType, duration, amplifier);
    }

    public int getParticleCount() { return particleCount; }
    public String getId() { return id; }
    public Particle getParticle() { return particle; }
    public PotionEffect getEffect() { return effect; }
    public String getDisplayName() { return displayName; }
    public String getPermission() { return permission; }
    public String getHeadTexture() { return headTexture; }
}