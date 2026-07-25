package codechicken.enderstorage.tile;

import codechicken.enderstorage.api.AbstractEnderStorage;
import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.network.EnderStorageNetwork;
import codechicken.lib.block.ModularBlockEntity;
import codechicken.lib.block.component.tile.ValueComponent;
import codechicken.lib.packet.StreamPacket;
import codechicken.lib.vec.Cuboid6;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;

public abstract class TileFrequencyOwner extends ModularBlockEntity {

    public static final Cuboid6 SELECTION_BUTTON = new Cuboid6(-1 / 16D, 0, -2 / 16D, 1 / 16D, 1 / 16D, 2 / 16D);

    protected Frequency frequency = Frequency.DEFAULT;
    private int changeCount;

    public TileFrequencyOwner(BlockEntityType<?> tileEntityTypeIn, BlockPos pos, BlockState state) {
        super(tileEntityTypeIn, pos, state);
    }

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFreq(Frequency frequency) {
        assert level != null;
        this.frequency = frequency;
        onFrequencySet();
        setChanged();
        BlockState state = level.getBlockState(worldPosition);
        level.sendBlockUpdated(worldPosition, state, state, 3);
        if (!level.isClientSide()) {
            sendUpdatePacket();
        }
    }

    public void tick() {
        assert level != null;
        if (getStorage().getChangeCount() > changeCount) {
            level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
            changeCount = getStorage().getChangeCount();
        }
    }

    public abstract AbstractEnderStorage getStorage();

    public void onFrequencySet() {
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        frequency = input.read("Frequency", Frequency.CODEC).orElse(Frequency.DEFAULT);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.store("Frequency", Frequency.CODEC, frequency);
    }

    @Override
    public void setLevel(Level p_155231_) {
        super.setLevel(p_155231_);
        onFrequencySet();
    }

    public boolean activate(Player player, int subHit, InteractionHand hand) {
        return false;
    }

    public void onPlaced(@Nullable LivingEntity entity) {
    }

    protected void sendUpdatePacket() {
        assert level != null;
        createPacket().sendToChunk(this);
    }

    public StreamPacket.ToClient createPacket() {
        var packet = EnderStorageNetwork.TILE_UPDATE.toClient(this);
        packet.writeBlockPos(getBlockPos());
        writeToPacket(packet);
        return packet;
    }

    public void writeToPacket(RegistryFriendlyByteBuf packet) {
        packet.cc$writeWithRegistryCodec(Frequency.STREAM_CODEC, frequency);
    }

    public void readFromPacket(RegistryFriendlyByteBuf packet) {
        frequency = packet.cc$readWithRegistryCodec(Frequency.STREAM_CODEC);
        onFrequencySet();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public void handleUpdateTag(ValueInput input) {
        loadWithComponents(input);
    }

    public int getLightValue() {
        return 0;
    }

    public boolean redstoneInteraction() {
        return false;
    }

    public int comparatorOutput() {
        return 0;
    }
}
