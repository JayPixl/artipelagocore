package io.jaypixl.artipelagocore.eggmoves.runtime;

import com.cobblemon.mod.common.Cobblemon;
import com.cobblemon.mod.common.CobblemonItems;
import com.cobblemon.mod.common.CobblemonSounds;
import com.cobblemon.mod.common.api.moves.BenchedMove;
import com.cobblemon.mod.common.api.moves.Move;
import com.cobblemon.mod.common.api.moves.MoveTemplate;
import com.cobblemon.mod.common.block.entity.PokemonPastureBlockEntity;
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity;
import com.cobblemon.mod.common.pokemon.Pokemon;
import io.jaypixl.artipelagocore.ArtipelagoCoreMod;
import io.jaypixl.artipelagocore.eggmoves.config.EggMovesConfigManager;
import io.jaypixl.artipelagocore.eggmoves.logic.EggMoveTeaching;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Slow-tick Pasture Teaching. Students are found by scanning online players'
 * PC storage for tethered Mirror Herb holders; Teachers come from the same
 * pasture block's tether list. Progress is in-memory only.
 */
public final class PastureTeachingService {
    private record Progress(ServerLevel level, BlockPos pasturePos, long nextProcAtMillis) { }

    private static final Map<UUID, Progress> PROGRESS = new HashMap<>();

    private PastureTeachingService() { }

    public static void clearTimers() {
        PROGRESS.clear();
    }

    public static void tick(MinecraftServer server) {
        if (server == null) return;
        long now = System.currentTimeMillis();
        Set<UUID> seenStudents = new HashSet<>();
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            Iterable<Pokemon> stored;
            try {
                stored = Cobblemon.INSTANCE.getStorage().getPC(player);
            } catch (RuntimeException exception) {
                continue;
            }
            for (Pokemon pokemon : stored) {
                if (pokemon.getTetheringId() == null || !pokemon.heldItem().is(CobblemonItems.MIRROR_HERB)) continue;
                seenStudents.add(pokemon.getUuid());
                try {
                    processStudent(pokemon, now);
                } catch (Exception exception) {
                    ArtipelagoCoreMod.LOGGER.warn("Pasture teaching failed for student {}", pokemon.getUuid(), exception);
                    PROGRESS.remove(pokemon.getUuid());
                }
            }
        }
        PROGRESS.keySet().retainAll(seenStudents);
    }

    private static void processStudent(Pokemon student, long now) {
        UUID studentId = student.getUuid();
        PokemonEntity entity = student.getEntity();

        ServerLevel level;
        BlockPos pasturePos;
        PokemonPastureBlockEntity.Tethering tethering = entity == null ? null : entity.getTethering();
        if (tethering != null && entity.level() instanceof ServerLevel serverLevel) {
            level = serverLevel;
            pasturePos = tethering.getPasturePos();
        } else {
            Progress cached = PROGRESS.get(studentId);
            if (cached == null) return;
            level = cached.level();
            pasturePos = cached.pasturePos();
        }

        BlockEntity blockEntity = level.getBlockEntity(pasturePos);
        if (!(blockEntity instanceof PokemonPastureBlockEntity pasture)) {
            PROGRESS.remove(studentId);
            return;
        }
        boolean stillTethered = pasture.getTetheredPokemon().stream().anyMatch(link ->
                link.getPokemonId().equals(studentId) && Objects.equals(link.getTetheringId(), student.getTetheringId()));
        if (!stillTethered) {
            PROGRESS.remove(studentId);
            return;
        }

        Set<String> teacherMoves = new HashSet<>();
        for (PokemonPastureBlockEntity.Tethering link : pasture.getTetheredPokemon()) {
            if (link.getPokemonId().equals(studentId)) continue;
            Pokemon teacher = link.getPokemon();
            if (teacher == null) continue;
            for (MoveTemplate template : teacher.getMoveSet().getMoveTemplates()) {
                teacherMoves.add(template.getName());
            }
        }

        Set<String> eggPool = new HashSet<>();
        List<MoveTemplate> eggTemplates = student.getForm().getMoves().getEggMoves();
        if (eggTemplates == null) throw new IllegalStateException("Egg pool lookup returned null");
        for (MoveTemplate template : eggTemplates) eggPool.add(template.getName());

        Set<String> known = new HashSet<>();
        for (Move move : student.getMoveSet().getMoves()) known.add(move.getTemplate().getName());
        for (BenchedMove learned : student.getBenchedMoves()) known.add(learned.getMoveTemplate().getName());

        List<String> eligible = EggMoveTeaching.eligibleMoves(eggPool, teacherMoves, known);
        if (eligible.isEmpty()) {
            PROGRESS.remove(studentId);
            return;
        }

        Progress progress = PROGRESS.get(studentId);
        if (progress == null || !progress.level().equals(level) || !progress.pasturePos().equals(pasturePos)) {
            long delay = EggMoveTeaching.nextProcDelayMillis(
                    EggMovesConfigManager.get().minSeconds * 1_000L,
                    EggMovesConfigManager.get().maxSeconds * 1_000L,
                    ThreadLocalRandom.current().nextDouble());
            PROGRESS.put(studentId, new Progress(level, pasturePos, now + delay));
            return;
        }
        if (now < progress.nextProcAtMillis()) return;

        String chosen = EggMoveTeaching.chooseCandidate(eligible, ThreadLocalRandom.current().nextDouble());
        MoveTemplate template = eggTemplates.stream().filter(candidate -> candidate.getName().equals(chosen)).findFirst()
                .orElseThrow(() -> new IllegalStateException("Chosen egg move left the pool: " + chosen));
        if (!student.getBenchedMoves().add(new BenchedMove(template, 0))) {
            PROGRESS.remove(studentId);
            return;
        }
        student.swapHeldItem(ItemStack.EMPTY, true, true);
        if (!student.heldItem().isEmpty()) {
            throw new IllegalStateException("Mirror Herb consume failed");
        }
        celebrate(student, entity, level, pasturePos);
        PROGRESS.remove(studentId);
    }

    private static void celebrate(Pokemon student, PokemonEntity entity, ServerLevel level, BlockPos pasturePos) {
        try {
            BlockPos soundPos = entity == null ? pasturePos : entity.blockPosition();
            level.playSound(null, soundPos, CobblemonSounds.LEVELUP, SoundSource.PLAYERS, 1.0F, 1.0F);
            if (entity != null) entity.cry();
        } catch (RuntimeException exception) {
            ArtipelagoCoreMod.LOGGER.debug("Pasture teaching cosmetics skipped for student {}", student.getUuid(), exception);
        }
    }
}
