package community.coins.plugin.spigot;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.api.ComponentApi;
import community.coins.plugin.api.ItemParseApi;
import community.coins.plugin.api.PluginAttributes;
import community.coins.plugin.spigot.implement.ComponentApiSpigot;
import community.coins.plugin.spigot.implement.ItemParseApiSpigot;
import community.coins.plugin.spigot.implement.PluginAttributesSpigot;
import community.coins.plugin.spigot.type.AdvancementDisplayRegistrar;
import org.jetbrains.annotations.NotNull;

/**
 * @author Eli
 * @since April 27, 2026
 */
public final class CoinsSpigot extends CoinsCore {
    private ComponentApi componentApi;
    private ItemParseApi itemParseApi;
    private PluginAttributes pluginAttributes;

    @Override
    public void beforeCoreLoaded() {
        this.componentApi = new ComponentApiSpigot();
        this.itemParseApi = new ItemParseApiSpigot(this);
        this.pluginAttributes = new PluginAttributesSpigot(this);

        // registering events
        new AdvancementDisplayRegistrar(this);
    }

    @Override
    public void afterCoreLoaded() {
        getLogger().info("Loaded CoinsSpigot");
    }

    @Override
    public @NotNull ComponentApi getComponentApi() {
        return componentApi;
    }

    @Override
    public @NotNull ItemParseApi getItemParseApi() {
        return itemParseApi;
    }

    @Override
    public @NotNull PluginAttributes getAttributes() {
        return pluginAttributes;
    }
}
