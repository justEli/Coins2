package community.coins.plugin.config;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.component.ComponentUtil;
import community.coins.plugin.item.DefinedCoin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

/**
 * @author Eli
 * @since April 28, 2026
 */
public final class CoinsConfig implements FileConfig<DefinedCoin> {
    private final CoinsCore coins;
    private final ConfigService service;

    // todo if we ever decide to add currencies, add it to properties of the coin (not drop)
    // todo add support for allow-modification (immutable but not only coin display name)
    public CoinsConfig(CoinsCore coins, ConfigService service) {
        this.coins = coins;
        this.service = service;
    }

    @Override
    public String getFileName() {
        return "coins.yml";
    }

    private static final UUID HEAD_UUID = UUID.fromString("00000001-0001-0001-7777-000000000001");
    private final Map<String, DefinedCoin> definedCoins = new HashMap<>();

    @Override
    public Optional<DefinedCoin> getDefinedItem(@NotNull String key) {
        return Optional.ofNullable(definedCoins.get(key.toLowerCase()));
    }

    @Override
    public Collection<DefinedCoin> getDefinedItems() {
        return definedCoins.values();
    }

    @Override
    public void parseAndReload() {
        var config = service.getOrCreateConfig(getFileName());

        Optional<ItemStack> defaultItem = getItemValue(config.getConfigurationSection("default"), null);
        String defaultSingularName = config.getString("default.name.singular", "Coin");
        String defaultPluralName = config.getString("default.name.plural", "Coins");
        boolean defaultImmutable = config.getBoolean("default.name.immutable", true);
        boolean defaultEnchanted = config.getBoolean("default.meta.enchanted", false);
        List<String> defaultItemModel = config.getStringList("default.meta.item-model"); // todo maybe rename to model-strings?
        List<String> defaultLore = config.getStringList("default.meta.lore");
        String defaultGlowColor = config.getString("default.meta.glow-color");
        boolean defaultHologram = config.getBoolean("default.meta.hologram", false);
        boolean defaultItemMerge = config.getBoolean("default.behavior.item-merge", false);
        boolean defaultHopperPickup = config.getBoolean("default.behavior.hopper-pickup", false);

        var section = config.getConfigurationSection("coins");
        if (section == null) {
            service.printConfigWarning(getFileName(), "There are no defined coins in the config, `coins` section missing.");
            return;
        }

        Map<String, DefinedCoin> configured = new HashMap<>();
        for (String coinName : section.getKeys(false)) {
            ConfigurationSection coin = section.getConfigurationSection(coinName);
            if (coin == null) {
                continue; // todo maybe a warning
            }

            String id = coinName.toLowerCase();
            if (id.isEmpty() || configured.containsKey(id)) {
                service.printConfigWarning(
                    getFileName(),
                    "Found already defined coin with id '%s'. Cannot define multiple coins with the same id.".formatted(id)
                );
                continue;
            }

            Optional<ItemStack> item = getItemValue(coin, defaultItem.orElse(null));
            if (item.isEmpty()) {
                service.printConfigWarning(getFileName(), "Invalid item name found for coin '%s'.".formatted(id));
                continue;
            }

            String singularName = coin.getString("name.singular", defaultSingularName);
            String pluralName = coin.getString("name.plural", defaultPluralName);
            boolean immutable = coin.getBoolean("name.immutable", defaultImmutable);
            boolean enchanted = coin.getBoolean("meta.enchanted", defaultEnchanted);
            List<String> itemModel = coin.getStringList("meta.item-model");
            if (itemModel.isEmpty()) {
                itemModel.addAll(defaultItemModel);
            }
            List<String> lore = coin.getStringList("meta.lore");
            if (lore.isEmpty()) {
                lore.addAll(defaultLore);
            }
            String glowColor = coin.getString("meta.glow-color", defaultGlowColor);
            boolean hologram = coin.getBoolean("meta.hologram", defaultHologram);
            boolean itemMerge = coin.getBoolean("behavior.item-merge", defaultItemMerge);
            boolean hopperPickup = coin.getBoolean("behavior.hopper-pickup", defaultHopperPickup);

            Component singularNameComponent = ComponentUtil.parse(singularName);
            Component pluralNameComponent = ComponentUtil.parse(pluralName);

            ItemStack itemStack = item.get();
            ItemMeta meta = itemStack.getItemMeta();

            if (meta == null) {
                service.printConfigWarning(getFileName(), "Invalid item found for coin '%s'.".formatted(id));
                continue;
            }

            coins.getComponentApi().setDisplayName(meta, singularNameComponent, immutable);

            if (immutable) {
                coins.getCoinService().getCoinMeta().setImmutableProperty(meta, true);
            }

            if (enchanted) {
                meta.addEnchant(Enchantment.UNBREAKING, 1, true);
                meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            }

            if (!itemModel.isEmpty()) {
                //noinspection UnstableApiUsage stable in 26.1
                var model = meta.getCustomModelDataComponent();
                //noinspection UnstableApiUsage
                model.setStrings(itemModel);
                //noinspection UnstableApiUsage
                meta.setCustomModelDataComponent(model);
            }

            List<Component> components = new ArrayList<>();
            for (String miniMessage : lore) {
                components.add(ComponentUtil.parse(miniMessage));
            }
            if (!components.isEmpty()) {
                coins.getComponentApi().setLore(meta, components);
            }

            if (glowColor != null && !glowColor.equalsIgnoreCase("none") && !glowColor.equalsIgnoreCase("false")) {
                try {
                    var color = NamedTextColor.NAMES.valueOrThrow(glowColor);
                    coins.getCoinService().getCoinMeta().setGlowProperty(meta, color);
                }
                catch (NoSuchElementException exception) {
                    service.printConfigWarning(
                        getFileName(),
                        "Invalid named color found for coin '%s' at `%s`.".formatted(id, "glow-color")
                    );
                }
            }

            if (hologram) {
                coins.getCoinService().getCoinMeta().setHologramProperty(meta, true);
            }

            if (!itemMerge) {
                coins.getCoinService().getCoinMeta().setUniqueProperty(meta, true);
            }

            if (!hopperPickup) {
                coins.getCoinService().getCoinMeta().setNoHopperPickupProperty(meta, true);
            }

            itemStack.setItemMeta(meta);
            configured.put(id, new DefinedCoin(id, itemStack, singularNameComponent, pluralNameComponent));
        }

        definedCoins.clear();
        definedCoins.putAll(configured);

        coins.log(Level.INFO, "Loaded %,d defined coin(s) from '%s'.".formatted(definedCoins.size(), getFileName()));
    }

    // parsing specific types

    // allows to parse either:
    // item: 'value',
    // item:
    //   type: 'material|player_head'
    //   value: 'value'
    private Optional<ItemStack> getItemValue(@Nullable ConfigurationSection section, @Nullable ItemStack defaultValue) {
        if (section == null) {
            return Optional.ofNullable(defaultValue);
        }

        String type = section.getString("item.type");
        String value = section.getString("item.value");

        if (type == null || value == null) {
            String material = section.getString("item");
            return parseItemStack(material, null);
        }

        return parseItemStack(value, type);
    }

    private Optional<ItemStack> parseItemStack(String value, @Nullable String type) {
        if ("material".equalsIgnoreCase(type)) {
            return coins.getItemParseApi().getFromItemType(value);
        }
        else if ("player_head".equalsIgnoreCase(type)) {
            var stack = new ItemStack(Material.PLAYER_HEAD);
            if (!(stack.getItemMeta() instanceof SkullMeta meta)) {
                return Optional.empty();
            }

            // todo setCustomNameVisible shows defined_coin's Head even though custom name is set
            var skullMeta = coins.getItemParseApi().applyMetaFromTexture(meta, value, HEAD_UUID, "defined_coin");
            if (skullMeta.isPresent()) {
                stack.setItemMeta(skullMeta.get());
                return Optional.of(stack);
            }
            return Optional.empty();
        }

        var item = parseItemStack(value, "material");
        if (item.isPresent()) {
            return item;
        }

        return parseItemStack(value, "player_head");
    }
}
