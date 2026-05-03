package community.coins.plugin.type.api;

import community.coins.plugin.CoinsCore;
import community.coins.plugin.type.EventTypeService;
import community.coins.plugin.type.filter.EventFilter;
import community.coins.plugin.type.filter.EventFilterBuilder;
import community.coins.plugin.type.filter.EventFilterForm;
import community.coins.plugin.type.filter.EventFilterFormBuilder;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.CheckReturnValue;
import org.jspecify.annotations.NullMarked;

/**
 * @author Eli
 * @since May 01, 2026
 */
public abstract class EventType implements Listener {
    private final CoinsCore coins;
    private final String identifier;
    private final EventFilter filter;

    // todo could also be expanded by other plugins, so a registrar of some sorts
    @NullMarked
    public EventType(CoinsCore coins, EventTypeService service, String identifier, EventFilterBuilder filter) {
        this.coins = coins;
        this.identifier = identifier;
        this.filter = filter.build(identifier);
        service.registerEventType(identifier, this);
    }

    @CheckReturnValue
    public FilteredEvent filterEvent(EventFilterForm filter) {
        // todo filter the event and get the coins
        // player (initiator) can be retrieved from filter
        return new FilteredEvent(coins, filter);
    }

    @CheckReturnValue
    public EventFilterFormBuilder createForm() {
        return filter.createForm();
    }

    public EventFilter getFilter() {
        return filter;
    }

    public String getIdentifier() {
        return identifier;
    }
}
