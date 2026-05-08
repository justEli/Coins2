package community.coins.plugin.config;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.component.ComponentUtil;
import community.coins.plugin.economy.DefinedCurrency;
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
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

/**
 * @author Eli
 * @since April 28, 2026
 */
public final class CoinsConfig extends FileConfig<DefinedCoin> {
    public CoinsConfig(CoinsCore coins, ConfigService service) {
        super(coins, service, "coins.yml");
    }

    private static final UUID HEAD_UUID = UUID.fromString("00000001-0001-0001-7777-000000000001");

    @Override
    public void parseAndReload() {
        var config = getOrCreateConfig();

        String defaultCurrency = config.getString("default.currency", "vault_balance"); // maybe to 'physical' when available
        Optional<ItemStack> defaultItem = getItemValue(config.getConfigurationSection("default"), null, "defined_coin");
        String defaultSingularName = config.getString("default.name.singular", "Coin");
        String defaultPluralName = config.getString("default.name.plural", "Coins");
        boolean defaultImmutable = config.getBoolean("default.immutable", true);
        boolean defaultEnchanted = config.getBoolean("default.meta.enchanted", false);
        List<String> defaultItemModel = config.getStringList("default.meta.model-strings");
        List<String> defaultLore = config.getStringList("default.meta.lore");
        String defaultGlowColor = config.getString("default.meta.glow-color");
        boolean defaultHologram = config.getBoolean("default.meta.hologram", false);
        boolean defaultItemMerge = config.getBoolean("default.behavior.item-merge", false);
        boolean defaultHopperPickup = config.getBoolean("default.behavior.hopper-pickup", false);
        String defaultDepositSound = config.getString("default.deposit.sound");
        double defaultDepositVolume = config.getDouble("default.deposit.volume", .5);
        double defaultDepositPitch = config.getDouble("default.deposit.pitch", .3);

        var coinsSection = config.getConfigurationSection("coins");
        if (coinsSection == null) {
            addWarn("There are no defined coins in the config, `coins` section missing.");
            return;
        }

        Map<String, DefinedCoin> configured = new HashMap<>();
        for (String name : coinsSection.getKeys(false)) {
            ConfigurationSection section = coinsSection.getConfigurationSection(name);
            if (section == null) {
                continue;
            }

            String id = name.toLowerCase();
            if (id.isEmpty() || configured.containsKey(id)) {
                addWarn("Found already defined coin with id '%s'. Cannot define multiple coins with the same id.".formatted(id));
                continue;
            }

            Optional<ItemStack> item = getItemValue(section, defaultItem.orElse(null), id);
            if (item.isEmpty()) {
                addWarn("Item type not found for coin '%s'.".formatted(name));
                continue;
            }

            String currencyName = section.getString("currency", defaultCurrency);
            Optional<DefinedCurrency> currency = coins.getEconomyService().getCurrency(currencyName);
            if (currency.isEmpty()) {
                addWarn("Currency '%s' not found for coin '%s'.".formatted(currencyName, name));
                continue;
            }

            String singularName = section.getString("name.singular", defaultSingularName);
            String pluralName = section.getString("name.plural", defaultPluralName);
            boolean immutable = section.getBoolean("immutable", defaultImmutable);
            boolean enchanted = section.getBoolean("meta.enchanted", defaultEnchanted);
            List<String> itemModel = section.getStringList("meta.model-strings");
            if (itemModel.isEmpty()) {
                itemModel.addAll(defaultItemModel);
            }
            List<String> lore = section.getStringList("meta.lore");
            if (lore.isEmpty()) {
                lore.addAll(defaultLore);
            }
            String glowColor = section.getString("meta.glow-color", defaultGlowColor);
            boolean hologram = section.getBoolean("meta.hologram", defaultHologram);
            boolean itemMerge = section.getBoolean("behavior.item-merge", defaultItemMerge);
            boolean hopperPickup = section.getBoolean("behavior.hopper-pickup", defaultHopperPickup);
            String depositSound = section.getString("deposit.sound", defaultDepositSound);
            double depositVolume = section.getDouble("deposit.volume", defaultDepositVolume);
            double depositPitch = section.getDouble("deposit.pitch", defaultDepositPitch);

            Component singularNameComponent = ComponentUtil.parse(singularName);
            Component pluralNameComponent = ComponentUtil.parse(pluralName);

            ItemStack itemStack = item.get();
            ItemMeta meta = itemStack.getItemMeta();

            if (meta == null) {
                addWarn("Invalid item found for coin '%s'.".formatted(name));
                continue;
            }

            coins.getComponentApi().setDisplayName(meta, singularNameComponent);
            coins.getCoinService().getCoinMeta().setCoinCurrency(meta, currency.get());

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
                    addWarn("Invalid named color found for coin '%s' at `%s`.".formatted(name, "glow-color"));
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

            if (depositSound != null) {
                coins.getCoinService().getCoinMeta().setSoundProperty(meta, depositSound, depositVolume, depositPitch);
            }

            itemStack.setItemMeta(meta);
            configured.put(id, new DefinedCoin(id, itemStack, singularNameComponent, pluralNameComponent, currency.get()));
        }

        putDefinedItems(configured, "coin", "coins");
    }

    // parsing specific types

    // allows to parse either:
    // item: 'value',
    // item:
    //   type: 'material|player_head'
    //   value: 'value'
    private Optional<ItemStack> getItemValue(@Nullable ConfigurationSection section, @Nullable ItemStack defaultValue, String id) {
        if (section == null) {
            return Optional.ofNullable(defaultValue);
        }

        String type = section.getString("item.type");
        String value = section.getString("item.value");

        if (type == null || value == null) {
            String material = section.getString("item");
            return parseItemStack(material, null, id);
        }

        return parseItemStack(value, type, id);
    }

    private Optional<ItemStack> parseItemStack(String value, @Nullable String type, String coinName) {
        if ("material".equalsIgnoreCase(type)) {
            return coins.getItemParseApi().getFromItemType(value);
        }
        else if ("player_head".equalsIgnoreCase(type)) {
            var stack = new ItemStack(Material.PLAYER_HEAD);
            if (!(stack.getItemMeta() instanceof SkullMeta meta)) {
                return Optional.empty();
            }

            var skullMeta = coins.getItemParseApi().applyMetaFromTexture(meta, value, HEAD_UUID, coinName);
            if (skullMeta.isPresent()) {
                stack.setItemMeta(skullMeta.get());
                return Optional.of(stack);
            }
            return Optional.empty();
        }

        var item = parseItemStack(value, "material", coinName);
        if (item.isPresent()) {
            return item;
        }

        return parseItemStack(value, "player_head", coinName);
    }
}
