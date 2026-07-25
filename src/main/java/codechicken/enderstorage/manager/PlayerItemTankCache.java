package codechicken.enderstorage.manager;

import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.network.EnderStorageNetwork;
import com.google.common.collect.Sets;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Created by covers1624 on 11/2/25.
 */
public abstract class PlayerItemTankCache<T extends TankState> {

    protected final Map<Frequency, T> tankStates = new HashMap<>();

    public void update() {
        for (var entry : tankStates.entrySet()) {
            entry.getValue().update();
        }
    }

    public static class Server extends PlayerItemTankCache<PlayerItemTankState.Server> {

        private final ServerPlayer player;

        public Server(ServerPlayer player) {
            this.player = player;
        }

        public void handleVisibilityPacket(RegistryFriendlyByteBuf packet) {
            int k = packet.readVarInt();
            for (int i = 0; i < k; i++) {
                track(packet.cc$readWithRegistryCodec(Frequency.STREAM_CODEC), true);
            }
            k = packet.readVarInt();
            for (int i = 0; i < k; i++) {
                track(packet.cc$readWithRegistryCodec(Frequency.STREAM_CODEC), false);
            }
        }

        public void track(Frequency freq, boolean t) {
            var state = tankStates.get(freq);
            if (state == null) {
                if (!t) {
                    return;
                }

                state = new PlayerItemTankState.Server(player);
                state.setFrequency(freq);
                tankStates.put(freq, state);
            }
            state.setTracking(t);
        }
    }

    public static class Client extends PlayerItemTankCache<PlayerItemTankState.Client> {

        private final Set<Frequency> a_visible = new HashSet<>();
        private final Set<Frequency> b_visible = new HashSet<>();

        public void newFrame() {
            a_visible.clear();
        }

        @Override
        public void update() {
            super.update();

            var new_visible = Sets.difference(a_visible, b_visible);
            var old_visible = Sets.difference(b_visible, a_visible);

            if (!new_visible.isEmpty() || !old_visible.isEmpty()) {
                var packet = EnderStorageNetwork.TANK_VISIBILITY.toServer();

                packet.writeVarInt(new_visible.size());
                new_visible.forEach(freq -> packet.cc$writeWithRegistryCodec(Frequency.STREAM_CODEC, freq));

                packet.writeVarInt(old_visible.size());
                old_visible.forEach(freq -> packet.cc$writeWithRegistryCodec(Frequency.STREAM_CODEC, freq));

                packet.sendToServer();
            }

            b_visible.clear();
            b_visible.addAll(a_visible);
        }

        public void sync(Frequency freq, FluidStack liquid) {
            var state = tankStates.computeIfAbsent(freq, k -> new PlayerItemTankState.Client());
            state.sync(liquid);
        }

        public FluidStack getLiquid(Frequency freq) {
            a_visible.add(freq);

            var state = tankStates.get(freq);
            return state == null ? FluidStack.EMPTY : state.c_liquid;
        }
    }
}
