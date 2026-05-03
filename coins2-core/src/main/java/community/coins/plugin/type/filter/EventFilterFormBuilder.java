package community.coins.plugin.type.filter;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.data.TransformType;
import org.bukkit.Keyed;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.World;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Boss;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Flying;
import org.bukkit.entity.Golem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.entity.Slime;
import org.bukkit.entity.Snowman;
import org.bukkit.entity.Wolf;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Eli
 * @since May 02, 2026
 */
public final class EventFilterFormBuilder {
    private final CoinsCore coins;
    private final EventFilter filter;

    public EventFilterFormBuilder(CoinsCore coins, EventFilter filter) {
        this.coins = coins;
        this.filter = filter;
    }

    public EventFilterForm build() {
        List<ItemStack> coinItems = new ArrayList<>();
        if (allowed.get()) {
            var coin = coins.getConfigService().getCoinsConfig().getDefinedItems().iterator().next();
            if (coin != null) {
                coinItems.add(coin.getItemStackClone());
            }
        }
        return new EventFilterForm(this, coinItems); // todo properly implement
    }

    private final AtomicBoolean allowed = new AtomicBoolean(true);

    // todo use in EventFilterForm
    public boolean isAllowed() {
        return allowed.get();
    }

    // todo for now, but remove later
    private Entity initiator;
    public Entity getInitiator() {
        return initiator;
    }
    // todo end of remove later

    // initiator

    // can be null if the initiator is 'any'
    // handles: "initiator.permission", "initiator.type", "initiator.any"
    public EventFilterFormBuilder withInitiatorEntity(@Nullable Entity entity) {
        this.initiator = entity;
        var config = filter.getFilterConfig();
        if (config.isEmpty()) {
            return this; // allowed because no config
        }

        if (entity instanceof Player player) {
            // players need permission (if set)
            String permission = config.get().getInitiatorPermission();
            if (permission != null && !player.hasPermission(permission)) {
                allowed.set(false);
                coins.debug("Disallowed '%s' due to lacking initiator permission".formatted(filter.getEventIdentifier()));
            }
        }
        else if (entity == null) {
            // other than entity/player need 'any' to be configured
            Boolean any = config.get().getInitiatorAny();
            if (any != null && !any) {
                allowed.set(false);
                coins.debug("Disallowed '%s' non-entity due to lacking 'any' initiator filter".formatted(
                    filter.getEventIdentifier()
                ));
            }
        }
        else {
            // entities need to be of specific type (if set)
            NamespacedKey type = entity.getType().getKey();
            Set<NamespacedKey> allowedTypes = config.get().getInitiatorType(); // todo allow !type
            if (allowedTypes != null && !allowedTypes.isEmpty() && type != null && !allowedTypes.contains(type)) {
                allowed.set(false);
                coins.debug("Disallowed '%s' due to entity type '%s' not in allowed initiator types".formatted(
                    filter.getEventIdentifier(), type
                ));
            }
        }

        return this;
    }

    // target

    // handles: "target.type"
    public EventFilterFormBuilder withTargetType(Keyed keyed) {
        var config = filter.getFilterConfig();
        if (config.isEmpty()) {
            return this; // allowed because no config
        }

        Set<NamespacedKey> allowedTypes = config.get().getTargetType(); // target types
        if (allowedTypes != null && !allowedTypes.isEmpty() && !allowedTypes.contains(keyed.getKey())) {
            allowed.set(false);
            coins.debug("Disallowed '%s' due to type '%s' not in allowed target types".formatted(
                filter.getEventIdentifier(), keyed.getKey().toString()
            ));
        }
        return this;
    }

    // handles: "target.type"
    /// if any of the types is found, it will allow the event
    public EventFilterFormBuilder withTargetType(Collection<Keyed> types) {
        var config = filter.getFilterConfig();
        if (config.isEmpty()) {
            return this; // allowed because no config
        }

        Set<NamespacedKey> allowedTypes = config.get().getTargetType(); // target types
        if (allowedTypes == null || allowedTypes.isEmpty()) {
            return this; // no configured types
        }

        boolean contains = false;
        for (Keyed keyed : types) {
            if (allowedTypes.contains(keyed.getKey())) {
                contains = true;
                break;
            }
        }
        if (!contains) {
            allowed.set(false);
            coins.debug("Disallowed '%s' due to types '%s' not in allowed target types".formatted(
                filter.getEventIdentifier(), allowedTypes
            ));
        }
        return this;
    }

    // handles: "target.type", "target.category", "target.min-player-damage"
    public EventFilterFormBuilder withTargetEntity(Entity entity) {
        var config = filter.getFilterConfig();
        if (config.isEmpty()) {
            return this; // allowed because no config
        }

        if (entity instanceof LivingEntity livingEntity) {
            Double minPlayerDamage = config.get().getTargetMinPlayerDamage();
            AttributeInstance maxHealth = livingEntity.getAttribute(Attribute.MAX_HEALTH);

            if (minPlayerDamage != null && maxHealth != null) {
                double percentagePlayerDamage = coins.getPersistentData().getPlayerDamage(entity) / maxHealth.getValue();
                if (percentagePlayerDamage < minPlayerDamage) {
                    allowed.set(false);
                    coins.debug("Disallowed '%s' due to minimum player damage (%.2f) not high enough (%.2f)".formatted(
                        filter.getEventIdentifier(), percentagePlayerDamage, minPlayerDamage
                    ));
                }
            }
        }

        withTargetType(entity.getType());
        if (allowed.get()) {
            return this; // already allowed in target.type
        }

        boolean contains = false;
        for (String category : config.get().getTargetCategory()) {
            if (isInCategory(entity, category.toLowerCase())) {
                contains = true;
                break;
            }
        }
        if (!contains) {
            allowed.set(false);
            coins.debug("Disallowed '%s' due to target entity '%s' not in category".formatted(
                filter.getEventIdentifier(), entity.getType().getKey()
            ));
        }

        return this;
    }

    // handles: "target.min-xp-drop"
    public EventFilterFormBuilder withTargetXpDrop(int xp) {
        var config = filter.getFilterConfig();
        if (config.isEmpty()) {
            return this; // allowed because no config
        }

        Integer minXpDrop = config.get().getTargetMinXpDrop();
        if (minXpDrop != null && xp < minXpDrop) {
            allowed.set(false);
            coins.debug("Disallowed '%s' due to target xp drop (%d) not high enough (%d)".formatted(
                filter.getEventIdentifier(), xp, minXpDrop
            ));
        }

        return this;
    }

    // handles: "target.allow-same-block"
    public EventFilterFormBuilder withTargetSameBlock(boolean sameDrop) {
        var config = filter.getFilterConfig();
        if (config.isEmpty()) {
            return this; // allowed because no config
        }

        Boolean allowSameBlock = config.get().getTargetAllowSameBlock();
        if (allowSameBlock != null && sameDrop && !allowSameBlock) {
            allowed.set(false);
            coins.debug("Disallowed '%s' due to target block drop is the same as block type".formatted(
                filter.getEventIdentifier()
            ));
        }

        return this;
    }

    // handles: "target.prevent-alts"
    public EventFilterFormBuilder withTargetPreventAlts(Entity entity0, Entity entity1) {
        if (!(entity0 instanceof Player player0) || !(entity1 instanceof Player player1)) {
            return this;
        }

        var config = filter.getFilterConfig();
        if (config.isEmpty()) {
            return this; // allowed because no config
        }

        Boolean preventAlts = config.get().getTargetPreventAlts();
        if (preventAlts != null && preventAlts && hasSameIp(player0, player1)) {
            allowed.set(false);
            coins.debug("Disallowed '%s' due to both players being alts".formatted(filter.getEventIdentifier()));
        }

        return this;
    }

    // location

    // handles: "location.disabled-worlds"
    public EventFilterFormBuilder withLocationWorld(World world) {
        var config = filter.getFilterConfig();
        if (config.isEmpty()) {
            return this; // allowed because no config
        }

        Set<String> worlds = config.get().getLocationDisabledWorlds();
        if (worlds != null && worlds.contains(world.getName())) {
            allowed.set(false);
            coins.debug("Disallowed '%s' due to being in disabled world".formatted(filter.getEventIdentifier()));
        }

        return this;
    }

    // handles: "location.cooldown.cap-amount", "location.cooldown.duration"
    public EventFilterFormBuilder withLocationCooldown(Location location) {
        var config = filter.getFilterConfig();
        if (config.isEmpty()) {
            return this; // allowed because no config
        }

        // TODO IMPLEMENT

        return this;
    }

    public boolean isInCategory(Entity entity, @NotNull String selector) {
        return switch (selector.toLowerCase()) {
            case "any" -> true;
            case "player" -> entity instanceof Player;
            case "hostile" -> isHostile(entity);
            case "passive" -> isPassive(entity);
            case "from_spawner" -> coins.getPersistentData().isTransformType(entity, TransformType.FROM_SPAWNER);
            case "from_split" ->  coins.getPersistentData().isTransformType(entity, TransformType.FROM_SPLIT);
            case "from_breeding" -> coins.getPersistentData().isTransformType(entity, TransformType.FROM_BREEDING);
            case "from_lightning" -> coins.getPersistentData().isTransformType(entity, TransformType.FROM_LIGHTNING);
            default -> false;
        };
    }

    private static boolean hasSameIp(Player player0, Player player1) {
        var address0 = player0.getAddress();
        var address1 = player1.getAddress();

        return address0 != null && address1 != null
            && address0.getAddress().getHostAddress().equals(address1.getAddress().getHostAddress());
    }

    private static boolean isHostile(Entity entity) {
        return entity instanceof Monster || entity instanceof Flying || entity instanceof Slime || entity instanceof Boss
            || (entity instanceof Golem && !(entity instanceof Snowman))
            || (entity instanceof Wolf wolf && wolf.isAngry());
    }

    private static boolean isPassive(Entity entity) {
        // todo can this be replaced with !isHostile() && type instanceof Mob?
        return !isHostile(entity) && !(entity instanceof Player)
            && entity instanceof LivingEntity && !(entity instanceof ArmorStand);
    }

    public String getEventIdentifier() {
        return filter.getEventIdentifier();
    }
}
