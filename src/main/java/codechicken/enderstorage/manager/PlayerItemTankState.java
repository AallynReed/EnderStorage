package codechicken.enderstorage.manager;

import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.network.EnderStorageNetwork;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.fluids.FluidStack;

/**
 * Created by covers1624 on 11/2/25.
 */
public final class PlayerItemTankState  {

    public static class Server extends TankState.Server {

        private final ServerPlayer player;
        private boolean tracking;

        public Server(ServerPlayer player) {
            this.player = player;
            tracking = true;
        }

        public void sendSyncPacket() {
            if (!tracking) {
                return;
            }

            var packet = EnderStorageNetwork.TANK_SYNC.toClient(player);
            packet.cc$writeWithRegistryCodec(Frequency.STREAM_CODEC, frequency);
            packet.cc$writeWithRegistryCodec(FluidStack.OPTIONAL_STREAM_CODEC, s_liquid);
            packet.sendToPlayer(player);
        }

        public void setTracking(boolean t) {
            tracking = t;
        }
    }

    public static class Client extends TankState.Client {

    }
}
