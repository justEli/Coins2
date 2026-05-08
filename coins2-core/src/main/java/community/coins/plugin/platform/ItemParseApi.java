package community.coins.plugin.platform;

import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Eli
 * @since April 27, 2026
 */
@NullMarked
public abstract class ItemParseApi {
    private static final Base64.Decoder DECODER = Base64.getDecoder();

    protected static Optional<String> getSkinUrl(@Nullable String texture) {
        if (texture == null || texture.isEmpty()) {
            return Optional.empty();
        }
        else if (texture.startsWith("http://textures.minecraft.net/texture/")) {
            // is already in the right format
            return Optional.of(texture);
        }
        else if (texture.length() > 60 && texture.length() <= 70) {
            // is probably the id without the url
            return Optional.of("http://textures.minecraft.net/texture/" + texture);
        }
        else {
            // is probably base64 texture
            try {
                String decoded = new String(DECODER.decode(texture));
                return Optional.of(decoded.split("\"url\":\"")[1].split("\"")[0].strip());
            }
            catch (Throwable throwable) {
                return Optional.empty();
            }
        }
    }

    public abstract Optional<SkullMeta> applyMetaFromTexture(SkullMeta meta, String texture, UUID uuid, String name);

    /// as intended from Registry.ITEM, allows creating NBT items
    public abstract Optional<ItemStack> getFromItemType(@Nullable String itemType);
}
