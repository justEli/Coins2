package community.coins.plugin.paper;

import com.mojang.brigadier.tree.LiteralCommandNode;
import community.coins.plugin.CoinsCore;
import community.coins.plugin.api.ComponentApi;
import community.coins.plugin.api.ItemParseApi;
import community.coins.plugin.api.PluginAttributes;
import community.coins.plugin.paper.command.CoinsCommand;
import community.coins.plugin.paper.implement.ComponentApiImpl;
import community.coins.plugin.paper.implement.ItemParseApiImpl;
import community.coins.plugin.paper.implement.PluginAttributesImpl;
import community.coins.plugin.paper.registrar.AdvancementDisplayRegistrar;
import community.coins.plugin.paper.registrar.PickupItemRegistrar;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

/**
 * @author Eli
 * @since April 27, 2026
 */
public final class CoinsPaper extends CoinsCore {
    private ComponentApi componentApi;
    private ItemParseApi itemParseApi;
    private PluginAttributes pluginAttributes;

    @Override
    public void loadImplementations() {
        this.componentApi = new ComponentApiImpl();
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

    public void registerCommand(LiteralCommandNode<CommandSourceStack> node, String description, Collection<String> aliases) {
        getLifecycleManager().registerEventHandler(
            LifecycleEvents.COMMANDS,
            event -> event.registrar().register(node, description, aliases)
        );
    }
}
