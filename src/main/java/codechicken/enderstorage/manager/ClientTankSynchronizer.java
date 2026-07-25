package codechicken.enderstorage.manager;

import codechicken.enderstorage.api.Frequency;
import codechicken.lib.util.ClientUtils;
import net.covers1624.quack.util.CrashLock;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderFrameEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

public class ClientTankSynchronizer {

    private static final CrashLock LOCK = new CrashLock("Already Initialized.");

    private static @Nullable PlayerItemTankCache.Client clientState;

    public static void init(IEventBus modBus) {
        LOCK.lock();

        NeoForge.EVENT_BUS.addListener(ClientTankSynchronizer::onTickEnd);
        NeoForge.EVENT_BUS.addListener(ClientTankSynchronizer::onRenderFrame);
    }

    public static void handleClientSync(Frequency freq, FluidStack fluid) {
        getClientPlayerCache()
                .sync(freq, fluid);
    }

    public static FluidStack getClientLiquid(Frequency freq) {
        return getClientPlayerCache()
                .getLiquid(freq);
    }

    private static PlayerItemTankCache.Client getClientPlayerCache() {
        if (clientState == null) {
            clientState = new PlayerItemTankCache.Client();
        }
        return clientState;
    }

    private static void onRenderFrame(RenderFrameEvent.Pre event) {
        if (ClientUtils.inWorld() && clientState != null) {
            clientState.newFrame();
        }
    }

    private static void onTickEnd(ClientTickEvent.Post event) {
        if (ClientUtils.inWorld() && clientState != null) {
            clientState.update();
        }
    }

    @SubscribeEvent
    public void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        clientState = null;
    }
}
