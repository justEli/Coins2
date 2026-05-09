package community.coins.plugin.spigot;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.api.ComponentApi;
import community.coins.plugin.api.ItemParseApi;
import community.coins.plugin.api.PluginAttributes;
import community.coins.plugin.spigot.command.CoinsCommand;
import community.coins.plugin.spigot.implement.ComponentApiImpl;
import community.coins.plugin.spigot.implement.ItemParseApiImpl;
import community.coins.plugin.spigot.implement.PluginAttributesImpl;
import community.coins.plugin.spigot.registrar.AdvancementDisplayRegistrar;
import community.coins.plugin.spigot.registrar.PickupItemRegistrar;
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
    public void loadImplementations() {
        this.componentApi = new ComponentApiImpl(this);
        this.itemParseApi = new ItemParseApiImpl(this);
        this.pluginAttributes = new PluginAttributesImpl(this);

        // registering registrars of events
        new AdvancementDisplayRegistrar(this);
        new PickupItemRegistrar(this);
    }

    @Override
    public void loadBasicFunctionality() {
        new CoinsCommand(this);
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
