package codechicken.enderstorage.storage;

import codechicken.enderstorage.api.AbstractEnderStorage;
import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.api.StorageType;
import codechicken.enderstorage.config.EnderStorageConfig;
import codechicken.enderstorage.container.ContainerEnderItemStorage;
import codechicken.enderstorage.manager.EnderStorageManager;
import codechicken.enderstorage.network.EnderStorageNetwork;
import codechicken.lib.inventory.InventoryUtils;
import codechicken.lib.util.ArrayUtils;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

public class EnderItemStorage extends AbstractEnderStorage implements Container {

    public static final StorageType<EnderItemStorage> TYPE = new StorageType<>("item");

    public static final int[] sizes = new int[] { 9, 27, 54 };

    private int size;
    private ItemStack[] items;
    private int open;

    public EnderItemStorage(EnderStorageManager manager, Frequency freq) {
        super(manager, freq);
        size = EnderStorageConfig.storageSize;
        items = ArrayUtils.fill(new ItemStack[getContainerSize()], ItemStack.EMPTY);
    }

    @Override
    public void loadFromTag(ValueInput input) {
        size = input.getIntOr("size", 0);
        empty();
        InventoryUtils.readItemStacksFromInput(input, items);
        if (size != EnderStorageConfig.storageSize) {
            alignSize();
        }
    }

    @Override
    public void saveToTag(ValueOutput output) {
        if (size != EnderStorageConfig.storageSize && open == 0) {
            alignSize();
        }
        output.putByte("size", (byte) size);

        InventoryUtils.writeItemStacksToOutput(output, items);
    }

    private void alignSize() {
        if (EnderStorageConfig.storageSize > size) {
            ItemStack[] newItems = ArrayUtils.fill(new ItemStack[sizes[EnderStorageConfig.storageSize]], ItemStack.EMPTY);
            System.arraycopy(items, 0, newItems, 0, items.length);
            items = newItems;
            size = EnderStorageConfig.storageSize;
            setChanged();
        } else {
            int numStacks = 0;
            for (ItemStack item : items) {
                if (!item.isEmpty()) {
                    numStacks++;
                }
            }

            if (numStacks <= sizes[EnderStorageConfig.storageSize]) {
                ItemStack[] newItems = ArrayUtils.fill(new ItemStack[sizes[EnderStorageConfig.storageSize]], ItemStack.EMPTY);
                int copyTo = 0;
                for (ItemStack item : items) {
                    if (!item.isEmpty()) {
                        newItems[copyTo] = item;
                        copyTo++;
                    }
                }
                items = newItems;
                size = EnderStorageConfig.storageSize;
                setChanged();
            }
        }
    }

    @Override
    public String type() {
        return "item";
    }

    public ItemStack getItem(int slot) {
        synchronized (this) {
            return items[slot];
        }
    }

    public ItemStack removeItemNoUpdate(int slot) {
        synchronized (this) {
            return InventoryUtils.removeStackFromSlot(this, slot);
        }
    }

    public void setItem(int slot, ItemStack stack) {
        synchronized (this) {
            items[slot] = stack;
            setChanged();
        }
    }

    public void openInventory() {
        if (manager.client) {
            return;
        }

        synchronized (this) {
            open++;
            if (open == 1) {
                EnderStorageNetwork.sendOpenUpdateTo(null, freq, true);
            }
        }
    }

    public void closeInventory() {
        if (manager.client) {
            return;
        }

        synchronized (this) {
            open--;
            if (open == 0) {
                EnderStorageNetwork.sendOpenUpdateTo(null, freq, false);
            }
        }
    }

    public int getNumOpen() {
        return open;
    }

    @Override
    public int getContainerSize() {
        return sizes[size];
    }

    @Override
    public boolean isEmpty() {
        return ArrayUtils.count(items, (stack -> !stack.isEmpty())) <= 0;
    }

    public ItemStack removeItem(int slot, int size) {
        synchronized (this) {
            return InventoryUtils.decrStackSize(this, slot, size);
        }
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    @Override
    public void setChanged() {
        setDirty();
    }

    @Override
    public boolean stillValid(Player var1) {
        return true;
    }

    public void empty() {
        items = new ItemStack[getContainerSize()];
        ArrayUtils.fill(items, ItemStack.EMPTY);
    }

    public void openContainer(ServerPlayer player, Component title) {
        player.openMenu(new SimpleMenuProvider((id, inv, p) -> new ContainerEnderItemStorage(id, inv, EnderItemStorage.this), title),
                packet -> {
                    packet.cc$writeWithRegistryCodec(Frequency.STREAM_CODEC, freq);
                    packet.writeByte(size);
                });
    }

    public void handleContainerPacket(RegistryFriendlyByteBuf packet) {
        size = packet.readByte();
        empty();
    }

    public int getSize() {
        return size;
    }

    public int openCount() {
        return open;
    }

    public void setClientOpen(int i) {
        if (manager.client) {
            open = i;
        }
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return true;
    }

    @Override
    public void clearContent() {
    }
}
