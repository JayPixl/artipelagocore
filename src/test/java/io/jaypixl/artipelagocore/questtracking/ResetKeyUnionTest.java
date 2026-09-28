package io.jaypixl.artipelagocore.questtracking;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class ResetKeyUnionTest {

    private static final UUID ADMIN = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID MEMBER_A = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID MEMBER_B = UUID.fromString("33333333-3333-3333-3333-333333333333");

    @Test
    void includesActorAndEveryMember() {
        Set<UUID> keys = QuestTrackingRouter.unionResetKeys(
                ADMIN, List.of(Set.of(MEMBER_A), Set.of(MEMBER_B)));

        assertEquals(Set.of(ADMIN, MEMBER_A, MEMBER_B), keys);
    }

    @Test
    void dedupesAcrossTeamsAndToleratesNulls() {
        Set<UUID> keys = QuestTrackingRouter.unionResetKeys(
                ADMIN, Arrays.asList(Set.of(ADMIN, MEMBER_A), null, Set.of(MEMBER_A, MEMBER_B)));

        assertEquals(Set.of(ADMIN, MEMBER_A, MEMBER_B), keys);
    }

    @Test
    void emptyWithoutActorOrMembers() {
        assertTrue(QuestTrackingRouter.unionResetKeys(null, List.of()).isEmpty());
        assertTrue(QuestTrackingRouter.unionResetKeys(null, null).isEmpty());
    }
}
