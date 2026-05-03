package community.coins.plugin.drops;

import community.coins.plugin.type.api.EventType;

/**
 * @author Eli
 * @since May 01, 2026
 */
public final class DefinedDrop {
    private final String identifier;
    private final EventType usedEventType;

    public DefinedDrop(String identifier, EventType usedEventType) {
        this.identifier = identifier;
        this.usedEventType = usedEventType;
    }

    public String getIdentifier() {
        return identifier;
    }

    public EventType getUsedEventType() {
        return usedEventType;
    }
}
