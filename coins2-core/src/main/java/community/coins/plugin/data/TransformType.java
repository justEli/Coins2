package community.coins.plugin.data;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Eli
 * @since April 29, 2026
 */
public enum TransformType {
    TO_DIFFERENT_TYPE(1), // i.e. from Zombie to Drowned
    FROM_SPAWNER(2), // cave spiders and other spawner mobs
    FROM_SPLIT(3), // slimes and magma cubes
    FROM_BREEDING(4),
    FROM_LIGHTNING(5)
    ;

    private final int id;
    TransformType(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    private static final Map<Integer, TransformType> REVERSED = new HashMap<>();

    static {
        for (TransformType type : TransformType.values()) {
            REVERSED.put(type.getId(), type);
        }
    }

    public static Optional<TransformType> fromId(int id) {
        return Optional.ofNullable(REVERSED.get(id));
    }
}
