package io.jaypixl.artipelagocore.questtasks.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceIdsTest {

    @Test
    void blankValuesAreAcceptedByTheConfigPattern() {
        assertTrue(ResourceIds.PATTERN.matcher("").matches());
        assertTrue(ResourceIds.PATTERN.matcher("   ").matches());
        assertEquals("", ResourceIds.normalize(""));
        assertEquals("", ResourceIds.normalize("   "));
    }

    @Test
    void validIdsAreAccepted() {
        assertTrue(ResourceIds.PATTERN.matcher("minecraft:oak_log").matches());
        assertTrue(ResourceIds.PATTERN.matcher(" #minecraft:logs ").matches());
        assertEquals("minecraft:oak_log", ResourceIds.normalize(" minecraft:oak_log "));
        assertEquals("minecraft:logs", ResourceIds.normalize(" #minecraft:logs "));
    }

    @Test
    void malformedIdsAreStillRejected() {
        assertFalse(ResourceIds.PATTERN.matcher("oak_log").matches());
        assertFalse(ResourceIds.PATTERN.matcher("minecraft:").matches());
        assertFalse(ResourceIds.PATTERN.matcher(":oak_log").matches());
        assertFalse(ResourceIds.PATTERN.matcher("minecraft:oak log").matches());
        assertEquals("", ResourceIds.normalize("oak_log"));
    }
}
