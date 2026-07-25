package codechicken.enderstorage.client.model;

import codechicken.enderstorage.api.Frequency;
import codechicken.enderstorage.manager.EnderStorageManager;
import codechicken.enderstorage.storage.EnderItemStorage;
import com.mojang.serialization.MapCodec;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.conditional.ConditionalItemModelProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * Created by covers1624 on 11/1/25.
 */
public record BagOpenModelCondition() implements ConditionalItemModelProperty {

    public static final MapCodec<BagOpenModelCondition> MAP_CODEC = MapCodec.unit(new BagOpenModelCondition());

    @Override
    public MapCodec<? extends ConditionalItemModelProperty> type() {
        return MAP_CODEC;
    }

    @Override
    public boolean get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
        return EnderStorageManager.instance(true)
                       .getStorage(Frequency.getComponentOrEmpty(stack), EnderItemStorage.TYPE)
                       .openCount() > 0;
    }
}
