package community.coins.plugin.language;

/**
 * @author Eli
 * @since April 27, 2026
 */
public final class Language {
    // also update en-US.json in locale

    @LanguageEntry("command.mintage.reload.success")
    public static FillEntry RELOAD_SUCCESS = new FillEntry("Config of <coins>Coins²</coins> has been reloaded in {duration}ms.");

    @LanguageEntry("command.players_only")
    public static FormatEntry PLAYERS_ONLY = new FormatEntry("<error>This command can only be performed by players in-game.");

    @LanguageEntry("command.coin_not_found")
    public static FillEntry COIN_NOT_FOUND = new FillEntry("<error>Cannot find a defined coin by identifier '{identifier}'.");

    @LanguageEntry("command.mintage.give.success")
    public static FillEntry GIVE_SUCCESS = new FillEntry("Added defined coin '{identifier}' without value to your inventory.");

    @LanguageEntry("command.mintage.setvalue.hold")
    public static FormatEntry HOLD_A_COIN = new FormatEntry("<error>Please hold a coin in your main hand to set a value.");

    @LanguageEntry("command.mintage.setvalue.success")
    public static FillEntry SET_VALUE_SUCCESS = new FillEntry("The value of this coin has been set to {format}.");

    @LanguageEntry("command.currency_not_found")
    public static FillEntry CURRENCY_NOT_FOUND = new FillEntry("<error>Cannot find a defined currency by identifier '{identifier}'.");

    @LanguageEntry("command.full_inventory")
    public static FormatEntry FULL_INVENTORY = new FormatEntry("<error>Cannot perform this action because your inventory is full.");

    @LanguageEntry("command.mintage.drop.dropping")
    public static FillEntry DROP_DROPPING = new FillEntry("Dropping {amount} coins in a radius of {radius} around {target}.");

    @LanguageEntry("command.invalid_range")
    public static FillEntry COMMAND_INVALID_RANGE = new FillEntry("<error>Given input for '{type}' must be between {min} and {max}.");

    @LanguageEntry("command.balance.provide_player")
    public static FormatEntry BALANCE_PROVIDE_PLAYER = new FormatEntry("<error>Please provide a player to get the balance for.");

    @LanguageEntry("command.balance.display")
    public static FillEntry BALANCE_DISPLAY = new FillEntry("Balance for currency '{currency}': {balance}");

    @LanguageEntry("command.balance.display_others")
    public static FillEntry BALANCE_DISPLAY_OTHERS = new FillEntry("Balance of {username} for currency '{currency}': {balance}");

    @LanguageEntry("command.balance.provide_currency")
    public static FormatEntry BALANCE_PROVIDE_CURRENCY = new FormatEntry("<error>Please provide a currency to get the balance for.");

    @LanguageEntry("command.withdraw.maximum_amount")
    public static FormatEntry WITHDRAW_MAX_AMOUNT = new FormatEntry("<error>Withdraw canceled because you cannot withdraw that much.");

    @LanguageEntry("command.withdraw.give")
    public static FillEntry WITHDRAW_GIVE = new FillEntry("Withdrew {amount}× {coin} for {price} as a physical coin.");

    @LanguageEntry("command.invalid_player")
    public static FormatEntry INVALID_PLAYER = new FormatEntry("<error>Cannot find a target player by provided input.");

    @LanguageEntry("command.invalid_number")
    public static FillEntry COMMAND_INVALID_NUMBER = new FillEntry("<error>Given input for '{type}' is an invalid number.");

    @LanguageEntry("command.invalid_location")
    public static FormatEntry COMMAND_INVALID_LOCATION = new FormatEntry("<error>Cannot find a location from given input.");

    @LanguageEntry("command.incomplete")
    public static FillEntry COMMAND_INCOMPLETE = new FillEntry("<error>Incomplete command: Use {command}.");

    @LanguageEntry("command.transaction_fail")
    public static FormatEntry TRANSACTION_FAIL = new FormatEntry("<error>Transaction has been canceled by the economy provider.");

    @LanguageEntry("command.disabled_world")
    public static FormatEntry DISABLED_WORLD = new FormatEntry("<error>This action is disabled in your current world.");

    @LanguageEntry("word.value")
    public static WordEntry WORD_VALUE = new WordEntry("value");

    @LanguageEntry("word.radius")
    public static WordEntry WORD_RADIUS = new WordEntry("radius");

    @LanguageEntry("word.amount")
    public static WordEntry WORD_AMOUNT = new WordEntry("amount");

    @LanguageEntry("word.world")
    public static WordEntry WORD_WORLD = new WordEntry("world");

    @LanguageEntry("word.arguments")
    public static WordEntry WORD_ARGUMENTS = new WordEntry("arguments");
}
