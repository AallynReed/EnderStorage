package codechicken.enderstorage.manager;

import net.covers1624.quack.util.CrashLock;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by covers1624 on 11/2/25.
 */
public class ServerTankSynchronizer {

    private static final CrashLock LOCK = new CrashLock("Already Initialized.");

    private static final Map<UUID, PlayerItemTankCache.Server> playerItemTankStates = new HashMap<>();

    public static void handleVisibilityPacket(ServerPlayer player, RegistryFriendlyByteBuf packet) {
        getServerPlayerCache(player)
                .handleVisibilityPacket(packet);
    }

    public static void init(IEventBus modBus) {
        LOCK.lock();

        NeoForge.EVENT_BUS.addListener(ServerTankSynchronizer::onPlayerLogout);
        NeoForge.EVENT_BUS.addListener(ServerTankSynchronizer::onPlayerChangedDimension);
        NeoForge.EVENT_BUS.addListener(ServerTankSynchronizer::onTickEnd);
        NeoForge.EVENT_BUS.addListener(ServerTankSynchronizer::onWorldUnload);
    }

    private static void onPlayerLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        removeServerPlayerTankCache(event.getEntity());
    }

    private static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        removeServerPlayerTankCache(event.getEntity());
    }

    private static void onTickEnd(ServerTickEvent.Post event) {
        for (var cache : playerItemTankStates.values()) {
            cache.update();
        }
    }

    private static void onWorldUnload(LevelEvent.Unload event) {
        if (event.getLevel() instanceof ServerLevel serverLevel && !serverLevel.getServer().isRunning()) {
            playerItemTankStates.clear();
        }
    }

    private static void removeServerPlayerTankCache(Player uuid) {
        playerItemTankStates.remove(uuid.getUUID());
    }

    private static PlayerItemTankCache.Server getServerPlayerCache(ServerPlayer player) {
        var cache = playerItemTankStates.get(player.getUUID());
        if (cache == null) {
            cache = new PlayerItemTankCache.Server(player);
            playerItemTankStates.put(player.getUUID(), cache);
        }
        return cache;
    }
}
