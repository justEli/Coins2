package community.coins.plugin.type.filter;

import community.coins.plugin.CoinsCore;

import java.util.HashSet;
import java.util.Set;

/**
 * @author Eli
 * @since May 02, 2026
 */
public final class EventFilterBuilder {
    private final CoinsCore coins;
    public EventFilterBuilder(CoinsCore coins) {
        this.coins = coins;
    }

    public EventFilter build(String identifier) {
        return new EventFilter(coins, allowedPaths, identifier);
    }

    // allowed paths in the config
    // of all these are implemented in EventFilter
    private final Set<String> allowedPaths = new HashSet<>();

    // initiator

    private void allows(String type, String path) {
        allowedPaths.add(type + "." + path);
    }

    public EventFilterBuilder hasInitiatorPlayer() {
        allows("initiator", "permission");
        return this;
    }

    public EventFilterBuilder hasInitiatorEntity() {
        allows("initiator", "type");
        return this;
    }

    public EventFilterBuilder hasInitiatorAny() {
        allows("initiator", "any");
        return this;
    }

    // target

    public EventFilterBuilder hasTargetType() {
        allows("target", "type");
        return this;
    }

    public EventFilterBuilder hasTargetEntity() {
        allows("target", "type");
        allows("target", "category");
        return this;
    }

    public EventFilterBuilder hasTargetMinXpDrop() {
        allows("target", "min-xp-drop");
        return this;
    }

    public EventFilterBuilder hasTargetAllowSameBlock() {
        allows("target", "allow-same-block");
        return this;
    }

    public EventFilterBuilder hasTargetPreventAlts() {
        allows("target", "prevent-alts");
        return this;
    }

    public EventFilterBuilder hasTargetMinPlayerDamage() {
        allows("target", "min-player-damage");
        return this;
    }

    // location

    public EventFilterBuilder hasLocationWorld() {
        allows("location", "disabled-worlds");
        return this;
    }

    public EventFilterBuilder hasLocationCooldown() {
        allows("location", "cooldown.cap-amount");
        allows("location", "cooldown.duration");
        return this;
    }
}
