package community.coins.plugin.spigot.implement;

import community.coins.plugin.platform.ComponentApi;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Item;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;
import org.bukkit.scoreboard.Team;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Eli
 * @since April 30, 2026
 */
@NullMarked
public final class ComponentApiSpigot implements ComponentApi {
    private final BukkitAudiences audiences;
    public ComponentApiSpigot(Plugin plugin) {
        this.audiences = BukkitAudiences.create(plugin);
    }

    private static final LegacyComponentSerializer HEX_SERIALIZER =
        LegacyComponentSerializer.builder().hexColors().useUnusualXRepeatedCharacterHexFormat().build();

    @Override
    public void setDisplayName(ItemMeta meta, Component component) {
        var string = HEX_SERIALIZER.serialize(component);
        meta.setItemName(string);
        meta.setDisplayName(string);
    }

    @Override
    public void setLore(ItemMeta meta, List<Component> components) {
        List<String> lore = new ArrayList<>();
        for (Component component : components) {
            lore.add(HEX_SERIALIZER.serialize(component));
        }
        meta.setLore(lore);
    }

    @Override
    public void setTeamColor(Team team, NamedTextColor color) {
        var name = NamedTextColor.NAMES.key(color);
        if (name == null) {
            return;
        }

        team.setColor(ChatColor.valueOf(name.toUpperCase()));
    }

    @Override
    public void applyDisplayName(Item item) {
        var meta = item.getItemStack().getItemMeta();
        if (meta == null) {
            return;
        }

        item.setCustomName(meta.getDisplayName());
    }

    @Override
    public Audience getAudience(CommandSender sender) {
        return audiences.sender(sender);
    }
}
