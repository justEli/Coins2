package community.coins.plugin.language;

/**
 * @author Eli
 * @since April 27, 2026
 */
public final class Language {
    @LanguageEntry("command.coins.reload.success")
    public static FillEntry RELOAD_SUCCESS = new FillEntry("Config of <coins>Coins</coins> has been reloaded in {duration}ms.");

    @LanguageEntry("command.players_only")
    public static FormatEntry PLAYERS_ONLY = new FormatEntry("<error>This command can only be performed by players in-game.");

    @LanguageEntry("command.coin_not_found")
    public static FillEntry COIN_NOT_FOUND = new FillEntry("<error>Cannot find a defined coin by identifier '{identifier}'.");

    @LanguageEntry("command.coins.give.success")
    public static FillEntry GIVE_SUCCESS = new FillEntry("Added a worthless coin of '{identifier}' to your inventory.");

    @LanguageEntry("command.coins.setvalue.hold")
    public static FormatEntry HOLD_A_COIN = new FormatEntry("<error>Please hold a coin in your main hand to set a value.");

    @LanguageEntry("command.coins.setvalue.success")
    public static FillEntry SET_VALUE_SUCCESS = new FillEntry("The value of this coin has been set to {format}.");

    @LanguageEntry("command.currency_not_found")
    public static FillEntry CURRENCY_NOT_FOUND = new FillEntry("<error>Cannot find a defined currency by identifier '{identifier}'.");

    @LanguageEntry("command.full_inventory")
    public static FormatEntry FULL_INVENTORY = new FormatEntry("<error>Cannot perform this action because your inventory is full.");

    @LanguageEntry("word.value")
    public static WordEntry WORD_VALUE = new WordEntry("value");
}
