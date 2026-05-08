package community.coins.plugin.spigot;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.platform.ComponentApi;
import community.coins.plugin.platform.ItemParseApi;
import community.coins.plugin.platform.PluginAttributes;
import community.coins.plugin.spigot.command.CoinsCommand;
import community.coins.plugin.spigot.implement.ComponentApiSpigot;
import community.coins.plugin.spigot.implement.ItemParseApiSpigot;
import community.coins.plugin.spigot.implement.PluginAttributesSpigot;
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
        this.componentApi = new ComponentApiSpigot(this);
        this.itemParseApi = new ItemParseApiSpigot(this);
        this.pluginAttributes = new PluginAttributesSpigot(this);

        // registering registrars of events
        new AdvancementDisplayRegistrar(this);
        new PickupItemRegistrar(this);
    }

    @Override
    public void loadBasicFunctionality() {
        new CoinsCommand(this);
    }

    @Override
    public void loadAfterCore() {

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
