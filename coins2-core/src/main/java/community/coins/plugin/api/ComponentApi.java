package community.coins.plugin.api;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Item;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scoreboard.Team;
import org.jspecify.annotations.NullMarked;

import java.util.List;

/**
 * @author Eli
 * @since April 30, 2026
 */
@NullMarked
public interface ComponentApi {
    /// don't forget to apply ItemMeta to ItemStack afterward
    void setDisplayName(ItemMeta meta, Component component);

    /// don't forget to apply ItemMeta to ItemStack afterward
    void setLore(ItemMeta meta, List<Component> components);

    void setTeamColor(Team team, NamedTextColor color);

    /// set the display name of an item entity to its custom name
    void applyDisplayName(Item item);
}
