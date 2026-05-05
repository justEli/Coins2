package community.coins.plugin.paper.implement;

import community.coins.plugin.api.ComponentApi;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
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
public final class ComponentApiPaper implements ComponentApi {
    @Override
    public void setDisplayName(ItemMeta meta, Component component) {
        meta.itemName(component);
        meta.displayName(component.decoration(TextDecoration.ITALIC, false));
    }

    @Override
    public void setLore(ItemMeta meta, List<Component> components) {
        meta.lore(components);
    }

    @Override
    public void setTeamColor(Team team, NamedTextColor color) {
        team.color(color);
    }

    @Override
    public void applyDisplayName(Item item) {
        item.customName(item.getItemStack().getItemMeta().displayName());
    }
}
