package community.coins.plugin.type.filter;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.misc.TransformType;
import community.coins.plugin.drops.DefinedDrop;
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
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * one of this object is created (and soon after removed) for every time an event is called
 * @author Eli
 * @since May 02, 2026
 */
public final class EventFilterForm {
    private final CoinsCore coins;
    private final String eventIdentifier;

    public EventFilterForm(CoinsCore coins, String eventIdentifier) {
        this.coins = coins;
        this.eventIdentifier = eventIdentifier;
    }

    // initiator

    private Entity initiatorEntity;
    public EventFilterForm withInitiatorEntity(@Nullable Entity entity) {
        this.initiatorEntity = entity;
        return this;
    }

    public @Nullable Entity getInitiatorEntity() {
        return initiatorEntity;
    }

    // target

    // todo allow multiplier to be set on the type. for example
    // type:
    //   'diamond_ore': 5
    //   'gold_ore': 2
    // instead of
    // type: [diamond_ore, gold_ore]
    private NamespacedKey targetType;
    public EventFilterForm withTargetType(Keyed keyed) {
        this.targetType = keyed.getKey();
        return this;
    }

    private Set<NamespacedKey> targetTypes;
    public EventFilterForm withTargetType(Collection<Keyed> types) {
        this.targetTypes = types.stream().map(Keyed::getKey).collect(Collectors.toSet());
        return this;
    }

    private Entity targetEntity;
    public EventFilterForm withTargetEntity(Entity entity) {
        this.targetEntity = entity;
        return this;
    }

    private Integer targetXpDrop;
    public EventFilterForm withTargetXpDrop(int xp) {
        this.targetXpDrop = xp;
        return this;
    }

    private Boolean targetSameBlock;
    public EventFilterForm withTargetSameBlock(boolean sameBlock) {
        this.targetSameBlock = sameBlock;
        return this;
    }

    // location

    private World locationWorld;
    public EventFilterForm withLocationWorld(World world) {
        this.locationWorld = world;
        return this;
    }

    private Location locationCooldown;
    public EventFilterForm withLocationCooldown(Location location) {
        this.locationCooldown = location;
        return this;
    }

    // parsing

    // todo parsing for action: 'projectile', 'stabbing'
    private boolean isInitiatorAllowed(@NotNull EventFilterConfig config) {
        // initiator entity; can be null if the initiator is 'any'
        // handles: "initiator.permission", "initiator.type", "initiator.any"
        if (initiatorEntity == null) {
            // other than entity/player need 'any' to be configured
            Boolean any = config.getInitiatorAny();
            if (any != null && !any) {
                coins.debug("Disallowed '%s' non-entity due to 'any: false' filter".formatted(eventIdentifier));
                return false;
            }
        }
        else {
            if (initiatorEntity instanceof Player player) {
                // if the initiator is a player and a permission is set
                String permission = config.getInitiatorPermission();
                if (permission != null && !player.hasPermission(permission)) {
                    coins.debug("Disallowed '%s' due to lacking initiator permission".formatted(eventIdentifier));
                    return false;
                }
            }

            // entities need to be of specific type, if type list is not empty
            Set<NamespacedKey> allowedTypes = config.getInitiatorType();
            if (allowedTypes != null && !allowedTypes.isEmpty()) {
                NamespacedKey type = initiatorEntity.getType().getKey();
                if (!allowedTypes.contains(type)) {
                    coins.debug("Disallowed '%s' due to entity type '%s' not in allowed initiator types".formatted(
                        eventIdentifier, type
                    ));
                    return false;
                }
            }
        }
        return true;
    }

    private boolean isTargetAllowed(@NotNull EventFilterConfig config) {
        // quick hack for target entity and "target.type"
        if (targetType == null && targetEntity != null) {
            this.targetType = targetEntity.getType().getKey();
        }

        // target type, handles: "target.type"
        Set<NamespacedKey> allowedTypes = config.getTargetType(); // target types
        if (allowedTypes != null && !allowedTypes.isEmpty()) {
            if (targetType != null && !allowedTypes.contains(targetType)) {
                coins.debug("Disallowed '%s' due to type '%s' not in allowed target types".formatted(
                    eventIdentifier, targetType.toString()
                ));
                return false;
            }
            else if (targetTypes != null && !targetTypes.isEmpty()) {
                // if any of the types is found, it will allow the event
                boolean contains = false;
                for (NamespacedKey key : targetTypes) {
                    if (allowedTypes.contains(key)) {
                        contains = true;
                        break;
                    }
                }
                if (!contains) {
                    coins.debug("Disallowed '%s' due to types '%s' not in allowed target types".formatted(
                        eventIdentifier, allowedTypes
                    ));
                    return false;
                }
            }
        }

        // first, types get priority (up here), and then category gets priority
        Set<String> targetCategory = config.getTargetCategory();
        if (targetEntity != null && targetCategory != null && !isInCategory(targetEntity, targetCategory)) {
            coins.debug("Disallowed '%s' due to target entity '%s' not in category %s".formatted(
                eventIdentifier, targetEntity.getType().getKey(), targetCategory.toString()
            ));
            return false;
        }

        // target entity, handles: "target.type", "target.category", "target.min-player-damage"
        if (targetEntity instanceof LivingEntity livingEntity) {
            Double minPlayerDamage = config.getTargetMinPlayerDamage();
            AttributeInstance maxHealth = livingEntity.getAttribute(Attribute.MAX_HEALTH);

            if (minPlayerDamage != null && maxHealth != null) {
                double percentagePlayerDamage = coins.getPersistentData().getPlayerDamage(targetEntity) / maxHealth.getValue();
                if (percentagePlayerDamage < minPlayerDamage) {
                    coins.debug("Disallowed '%s' due to minimum player damage (%.2f) not high enough (%.2f)".formatted(
                        eventIdentifier, percentagePlayerDamage, minPlayerDamage
                    ));
                    return false;
                }
            }
        }

        // handles: "target.min-xp-drop"
        Integer minXpDrop = config.getTargetMinXpDrop();
        if (minXpDrop != null && targetXpDrop != null && targetXpDrop < minXpDrop) {
            coins.debug("Disallowed '%s' due to target xp drop (%d) not high enough (%d)".formatted(
                eventIdentifier, targetXpDrop, minXpDrop
            ));
            return false;
        }

        // handles: "target.allow-same-block"
        Boolean allowSameBlock = config.getTargetAllowSameBlock();
        if (allowSameBlock != null && targetSameBlock != null && targetSameBlock && !allowSameBlock) {
            coins.debug("Disallowed '%s' due to target block drop is the same as block type".formatted(
                eventIdentifier
            ));
            return false;
        }

        // handles: "target.prevent-alts"
        Boolean preventAlts = config.getTargetPreventAlts();
        if (preventAlts != null && preventAlts) {
            if (initiatorEntity instanceof Player p0 && targetEntity instanceof Player p1 && hasSameIp(p0, p1)) {
                coins.debug("Disallowed '%s' due to both players being alts".formatted(eventIdentifier));
                return false;
            }
        }
        return true;
    }

    private boolean isLocationAllowed(@NotNull EventFilterConfig config, DefinedDrop drop) {
        // handles: "location.disabled-worlds"
        Set<String> worlds = config.getLocationDisabledWorlds();
        if (worlds != null && locationWorld != null && worlds.contains(locationWorld.getName())) {
            coins.debug("Disallowed '%s' due to being in disabled world".formatted(eventIdentifier));
            return false;
        }

        // handles: "location.cooldown.cap-amount", "location.cooldown.duration"
        if (locationCooldown != null) {
            Integer cooldownCapAmount = config.getLocationCooldownCapAmount();
            Integer cooldownDuration = config.getLocationCooldownDurationMillis();
            if (cooldownCapAmount != null && cooldownDuration != null) {
                if (!drop.isLocationAvailableAndSet(locationCooldown, cooldownCapAmount, cooldownDuration)) {
                    coins.debug("Disallowed '%s' due to location cooldown cap reached".formatted(eventIdentifier));
                    return false;
                }
            }
        }

        return true;
    }

    public boolean isAllowed(DefinedDrop drop) {
        EventFilterConfig config = drop.getEventFilterConfig();
        return isInitiatorAllowed(config)
            && isTargetAllowed(config)
            && isLocationAllowed(config, drop);
    }

    private boolean isInCategory(Entity entity, @NotNull Set<String> selector) {
        if (selector.isEmpty()) {
            return true; // empty lists accept anything
        }

        // split up listed categories into allowed (no !) and disallowed (starts with !)
        Set<String> allowedCategories = new HashSet<>();
        Set<String> disallowedCategories = new HashSet<>();
        for (String category : selector) {
            if (category.startsWith("!")) {
                disallowedCategories.add(category.substring(1));
            }
            else {
                allowedCategories.add(category);
            }
        }

        boolean allowed = false;

        // first check the allowed categories
        for (String category : allowedCategories) {
            if (isInCategory(entity, category)) {
                allowed = true;
                break;
            }
        }

        if (!allowed) {
            return false;
        }

        // then check the disallowed ones
        for (String category : disallowedCategories) {
            if (isInCategory(entity, category)) {
                return false;
            }
        }
        return true;
    }

    private boolean isInCategory(Entity entity, String selector) {
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
}
