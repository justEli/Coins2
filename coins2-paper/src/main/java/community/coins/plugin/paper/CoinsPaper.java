package community.coins.plugin.paper;

import com.mojang.brigadier.tree.LiteralCommandNode;
import community.coins.plugin.CoinsCore;
import community.coins.plugin.api.ComponentApi;
import community.coins.plugin.api.ItemParseApi;
import community.coins.plugin.api.PluginAttributes;
import community.coins.plugin.paper.commands.TestLogic;
import community.coins.plugin.paper.implement.ComponentApiPaper;
import community.coins.plugin.paper.implement.ItemParseApiPaper;
import community.coins.plugin.paper.implement.PluginAttributesPaper;
import community.coins.plugin.paper.type.AdvancementDisplayRegistrar;
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
    public void beforeCoreLoaded() {
        this.componentApi = new ComponentApiPaper();
        this.itemParseApi = new ItemParseApiPaper(this);
        this.pluginAttributes = new PluginAttributesPaper(this);

        // registering events
        new AdvancementDisplayRegistrar(this);
    }

    @Override
    public void afterCoreLoaded() {
        new TestLogic(this);
        getLogger().info("Loaded CoinsPaper");
    }

    public void registerCommand(LiteralCommandNode<CommandSourceStack> node, String description, Collection<String> aliases) {
        getLifecycleManager().registerEventHandler(
            LifecycleEvents.COMMANDS,
            event -> event.registrar().register(node, description, aliases)
        );
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
