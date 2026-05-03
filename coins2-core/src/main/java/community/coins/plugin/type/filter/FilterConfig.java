package community.coins.plugin.type.filter;

import org.bukkit.NamespacedKey;

import java.util.Set;

/**
 * @author Eli
 * @since May 02, 2026
 */
public final class FilterConfig {
    // initiator
    String initiatorPermission;

    public String getInitiatorPermission() {
        return initiatorPermission;
    }

    Set<NamespacedKey> initiatorType;

    public Set<NamespacedKey> getInitiatorType() {
        return initiatorType;
    }

    Boolean initiatorAny;

    public Boolean getInitiatorAny() {
        return initiatorAny;
    }

    // target
    Set<NamespacedKey> targetType;

    public Set<NamespacedKey> getTargetType() {
        return targetType;
    }

    Set<String> targetCategory;

    public Set<String> getTargetCategory() {
        return targetCategory;
    }

    Integer targetMinXpDrop;

    public Integer getTargetMinXpDrop() {
        return targetMinXpDrop;
    }

    Boolean targetAllowSameBlock;

    public Boolean getTargetAllowSameBlock() {
        return targetAllowSameBlock;
    }

    Boolean targetPreventAlts;

    public Boolean getTargetPreventAlts() {
        return targetPreventAlts;
    }

    Double targetMinPlayerDamage;

    public Double getTargetMinPlayerDamage() {
        return targetMinPlayerDamage;
    }

    // location
    Set<String> locationDisabledWorlds;

    public Set<String> getLocationDisabledWorlds() {
        return locationDisabledWorlds;
    }

    Integer locationCooldownCapAmount;

    public Integer getLocationCooldownCapAmount() {
        return locationCooldownCapAmount;
    }

    String locationCooldownDuration;

    public String getLocationCooldownDuration() {
        return locationCooldownDuration;
    }
}
