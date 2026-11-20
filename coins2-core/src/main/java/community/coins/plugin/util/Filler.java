package community.coins.plugin.util;

import community.coins.plugin.language.EntryReplacement;

/**
 * @author Eli
 * @since November 20, 2026
 */
public final class Filler {
    public static void create() {
        AMOUNT = new EntryReplacement("amount");
        TYPE = new EntryReplacement("type");
        MIN_1 = new EntryReplacement("min").filled(1);
        MAX = new EntryReplacement("max");
        COIN = new EntryReplacement("coin");
        PRICE = new EntryReplacement("price");
        DURATION = new EntryReplacement("duration");
        IDENTIFIER = new EntryReplacement("identifier");
        FORMAT = new EntryReplacement("format");
        RADIUS = new EntryReplacement("radius");
        TARGET = new EntryReplacement("target");
        CURRENCY = new EntryReplacement("currency");
        BALANCE = new EntryReplacement("balance");
        USERNAME = new EntryReplacement("username", ColorResolver.USER);
        COMMAND = new EntryReplacement("command");
    }

    static {
        create();
    }

    public static EntryReplacement AMOUNT;
    public static EntryReplacement TYPE;
    public static EntryReplacement.Filled MIN_1;
    public static EntryReplacement MAX;
    public static EntryReplacement COIN;
    public static EntryReplacement PRICE;
    public static EntryReplacement DURATION;
    public static EntryReplacement IDENTIFIER;
    public static EntryReplacement FORMAT;
    public static EntryReplacement RADIUS;
    public static EntryReplacement TARGET;
    public static EntryReplacement CURRENCY;
    public static EntryReplacement BALANCE;
    public static EntryReplacement USERNAME;
    public static EntryReplacement COMMAND;
}
