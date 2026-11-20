package community.coins.plugin.spigot;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.api.ComponentApi;
import community.coins.plugin.api.ItemParseApi;
import community.coins.plugin.api.PluginAttributes;
import community.coins.plugin.command.BalanceCommandIdentifier;
import community.coins.plugin.command.MintageCommandIdentifier;
import community.coins.plugin.command.WithdrawCommandIdentifier;
import community.coins.plugin.spigot.command.BalanceCommand;
import community.coins.plugin.spigot.command.CommandRegistry;
import community.coins.plugin.spigot.command.MintageCommand;
import community.coins.plugin.spigot.command.WithdrawCommand;
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
    private CommandRegistry commandRegistry;
    private ComponentApi componentApi;
    private ItemParseApi itemParseApi;
    private PluginAttributes pluginAttributes;

    @Override
    public void loadImplementations() {
        this.commandRegistry = new CommandRegistry(this);
        this.componentApi = new ComponentApiImpl(this);
        this.itemParseApi = new ItemParseApiImpl(this);
        this.pluginAttributes = new PluginAttributesImpl(this);
    }

    @Override
    public void loadRegistrars() {
        new AdvancementDisplayRegistrar(this);
        new PickupItemRegistrar(this);
    }

    @Override
    public void loadCommands() {
        new BalanceCommandIdentifier(this, defined -> new BalanceCommand(this, defined));
        new MintageCommandIdentifier(this, defined -> new MintageCommand(this, defined));
        new WithdrawCommandIdentifier(this, defined -> new WithdrawCommand(this, defined));
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

    public CommandRegistry getCommandRegistry() {
        return commandRegistry;
    }
}
