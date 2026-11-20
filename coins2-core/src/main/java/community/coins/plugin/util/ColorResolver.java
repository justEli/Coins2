package community.coins.plugin.util;

import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.minimessage.Context;
import net.kyori.adventure.text.minimessage.ParsingException;
import net.kyori.adventure.text.minimessage.tag.Tag;
import net.kyori.adventure.text.minimessage.tag.resolver.ArgumentQueue;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NullMarked;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Eli
 * @since April 23, 2026
 */
@NullMarked
public final class ColorResolver implements TagResolver {
    private static final Map<String, TextColor> TAGS = new HashMap<>();

    // https://coolors.co/bf1111-ff0000-ff7711-ffc430-ffcc88-6dd47e-148c30-95a5a6
    public static TextColor DOWN = TextColor.color(0xBF1111);
    public static TextColor ERROR = TextColor.color(0xFF0000);
    public static TextColor PRIMARY = TextColor.color(0xFF7711);
    public static TextColor COINS = TextColor.color(0xFFC430);
    public static TextColor VAR = TextColor.color(0xFFCC88);
    public static TextColor MONEY = TextColor.color(0x6DD47E);
    public static TextColor UP = TextColor.color(0x148C30);
    public static TextColor USER = TextColor.color(0x95A5A6);

    public static void updateTag(String tag, TextColor color) {
        tag = tag.toLowerCase();
        TAGS.put(tag, color);

        switch (tag) {
            case "down" ->  DOWN = color;
            case "error" -> ERROR = color;
            case "primary" -> PRIMARY = color;
            case "coins" -> COINS = color;
            case "var" -> VAR = color;
            case "money" -> MONEY = color;
            case "up" -> UP = color;
            case "user" -> USER = color;
        }
    }

    static {
        // related to Coins
        TAGS.put("coins", COINS);
        TAGS.put("primary", PRIMARY);
        TAGS.put("error", ERROR);
        TAGS.put("var", VAR);
        TAGS.put("money", MONEY);
        TAGS.put("user", USER);
        TAGS.put("down", DOWN);
        TAGS.put("up", UP);

        // bedrock edition colors
        TAGS.put("minecoin_gold", TextColor.color(0xDDD605));
        TAGS.put("material_quartz", TextColor.color(0xE3D4D1));
        TAGS.put("material_iron", TextColor.color(0xCECACA));
        TAGS.put("material_netherite", TextColor.color(0x443A3B));
        TAGS.put("material_redstone", TextColor.color(0x971607));
        TAGS.put("material_copper", TextColor.color(0xB4684D));
        TAGS.put("material_gold", TextColor.color(0xDEB12D));
        TAGS.put("material_emerald", TextColor.color(0x47A036));
        TAGS.put("material_diamond", TextColor.color(0x2CBAA8));
        TAGS.put("material_lapis", TextColor.color(0x21497B));
        TAGS.put("material_amethyst", TextColor.color(0x9A5CC6));
        TAGS.put("material_resin", TextColor.color(0xEB7114));
    }

    @Override
    public @Nullable Tag resolve(String name, ArgumentQueue arguments, Context ctx) throws ParsingException {
        if (TAGS.containsKey(name)) {
            return Tag.styling(TAGS.get(name));
        }

        return null;
    }

    @Override
    public boolean has(String name) {
        return TAGS.containsKey(name);
    }
}
