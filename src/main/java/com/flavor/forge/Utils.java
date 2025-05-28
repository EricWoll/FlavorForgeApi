package com.flavor.forge;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

public class Utils {

    public static boolean isValidUUID(UUID possibleUUID) {
        if (possibleUUID == null) {
            return false;
        }
        try {
            UUID.fromString(possibleUUID.toString());
            return true;
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Incorrect UUID format: " + possibleUUID, e);
        }
    }

    public static boolean isValidUUID(List<UUID> possibleUUIDs) {
        if (possibleUUIDs == null) {
            return false;
        }
        for (UUID uuid : possibleUUIDs) {
            if (uuid == null) continue;
            if (!isValidUUID(uuid)) {
                return false;
            }
        }
        return true;
    }

    public static boolean validateUUIDs(UUID... uuids) {
        List<UUID> filtered = Arrays.stream(uuids)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        return filtered.isEmpty() || isValidUUID(filtered);
    }
}
