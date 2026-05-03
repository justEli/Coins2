package community.coins.plugin.config;

/**
 * @author Eli
 * @since April 27, 2026
 */
public enum EconomyType {
    NONE("none"),
    VAULT("Vault"), // well, replace to 'virtual' probably with Vault and Treasury as option
                    // or VAULT(Type.VIRTUAL) maybe
    PHYSICAL("physical");

    // for currencies.yml
    private final String name;
    EconomyType(final String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
