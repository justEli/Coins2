package community.coins.plugin.paper.implement;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.api.ItemParseApi;
import community.coins.plugin.util.Util;
import org.bukkit.Registry;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.net.MalformedURLException;
import java.net.URI;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Eli
 * @since April 27, 2026
 */
@NullMarked
public final class ItemParseApiImpl extends ItemParseApi {
    public final CoinsCore coins;
    public ItemParseApiImpl(CoinsCore coins) {
        this.coins = coins;
    }

    @Override
    public Optional<SkullMeta> applyMetaFromTexture(SkullMeta meta, @Nullable String texture, UUID uuid, String name) {
        Optional<String> url = getSkinUrl(texture);
        if (url.isEmpty()) {
            return Optional.empty();
        }

        var profile = coins.getServer().createProfile(uuid, name);
        try {
            var textures = profile.getTextures();
            textures.setSkin(URI.create(url.get()).toURL());
            profile.setTextures(textures);
        }
        catch (MalformedURLException exception) {
            return Optional.empty();
        }

        meta.setPlayerProfile(profile);
        return Optional.of(meta);
    }

    @Override
    public Optional<ItemStack> getFromItemType(@Nullable String itemType) {
        return Util.getType(itemType, Registry.ITEM).map(item -> item.createItemStack(1));
    }
}
