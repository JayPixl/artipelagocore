package io.jaypixl.artipelagocore.questtasks.trade;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TraderOwnershipTest {

    @Test
    void playerAndTeamShopsAlwaysCountForCustomers() {
        for (boolean allowServerOwned : new boolean[]{true, false}) {
            assertTrue(TraderOwnership.PLAYER.countsForCustomer(allowServerOwned));
            assertTrue(TraderOwnership.TEAM.countsForCustomer(allowServerOwned));
        }
    }

    @Test
    void onlyServerShopsFollowTheToggle() {
        assertTrue(TraderOwnership.SERVER.countsForCustomer(true));
        assertFalse(TraderOwnership.SERVER.countsForCustomer(false));
    }
}
