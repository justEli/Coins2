package community.coins.plugin.paper.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import community.coins.plugin.item.DefinedCoin;
import community.coins.plugin.paper.CoinsPaper;
import io.papermc.paper.command.brigadier.Commands;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.persistence.PersistentDataType;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Eli
 * @since April 28, 2026
 */
public final class TestLogic implements Listener {
    private final NamespacedKey key;

    public TestLogic(CoinsPaper coins) {
        this.key = NamespacedKey.fromString("test_key", coins);
        coins.parseEventHandlers(this);

        coins.registerCommand(
            Commands.literal("givecoin")
            .requires(source -> source.getSender().hasPermission("coins.admin"))
            .then(
                Commands.literal("reload")
                .executes(context -> {
                    long millis = System.currentTimeMillis();
                    coins.getConfigService().reload();
                    context.getSource().getSender().sendRichMessage("<#00ff00>done reloading in %,dms".formatted(System.currentTimeMillis() - millis));
                    return Command.SINGLE_SUCCESS;
                })
            )
            .then(
                Commands.literal("getdata")
                .executes(context -> {
                    if (!(context.getSource().getSender() instanceof Player player)) {
                        return 0;
                    }

                    Set<NamespacedKey> data = player.getInventory().getItemInMainHand().getItemMeta().getPersistentDataContainer().getKeys();
                    player.sendRichMessage("<#ffff00>Namespaced keys: " + data.stream().map(NamespacedKey::toString).collect(Collectors.joining(", ")));
                    return Command.SINGLE_SUCCESS;
                })
            )
            .then(
                Commands.literal("setdata")
                .executes(context -> {
                    if (!(context.getSource().getSender() instanceof Player player)) {
                        return 0;
                    }

                    var item = player.getInventory().getItemInMainHand();
                    var meta = item.getItemMeta();
                    meta.getPersistentDataContainer().set(key, PersistentDataType.STRING, "hello! test");
                    item.setItemMeta(meta);
                    return Command.SINGLE_SUCCESS;
                })
            )
            .then(
                Commands.literal("give")
                .then(
                    Commands.argument("coin_id", StringArgumentType.word())
                    .executes(context -> {
                        if (!(context.getSource().getSender() instanceof Player player)) {
                            return Command.SINGLE_SUCCESS;
                        }

                        String coinId = StringArgumentType.getString(context, "coin_id");
                        var coin = coins.getConfigService().getCoinsConfig().getDefinedItem(coinId);
                        if (coin.isEmpty()) {
                            player.sendRichMessage("<#ff0000>Not found");
                            return Command.SINGLE_SUCCESS;
                        }

                        player.getInventory().addItem(coin.get().getItemStackClone());
                        player.sendRichMessage("<#00ff00>Gave coin");
                        return Command.SINGLE_SUCCESS;
                    })
                    .suggests((_, builder) -> {
                        for (DefinedCoin coin : coins.getConfigService().getCoinsConfig().getDefinedItems()) {
                            builder.suggest(coin.getId());
                        }

                        return builder.buildFuture();
                    })
                )
            )
            .build(),
            "Test command",
            List.of("coingive")
        );
    }
}
