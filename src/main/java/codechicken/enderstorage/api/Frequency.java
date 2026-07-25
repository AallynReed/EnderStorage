package codechicken.enderstorage.api;

import codechicken.enderstorage.init.EnderStorageModContent;
import codechicken.lib.colour.EnumColour;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

/**
 * Created by covers1624 on 4/26/2016.
 */
public record Frequency(
        EnumColour left,
        EnumColour middle,
        EnumColour right,
        Optional<UUID> owner,
        Optional<Component> ownerName
) {

    public static final Frequency DEFAULT = new Frequency(EnumColour.WHITE, EnumColour.WHITE, EnumColour.WHITE);

    public static final Codec<Frequency> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                    EnumColour.CODEC.fieldOf("left").forGetter(Frequency::left),
                    EnumColour.CODEC.fieldOf("middle").forGetter(Frequency::middle),
                    EnumColour.CODEC.fieldOf("right").forGetter(Frequency::right),
                    UUIDUtil.CODEC.optionalFieldOf("owner").forGetter(Frequency::owner),
                    ComponentSerialization.CODEC.optionalFieldOf("ownerName").forGetter(Frequency::ownerName)
            ).apply(builder, Frequency::new)
    );
    public static final StreamCodec<RegistryFriendlyByteBuf, Frequency> STREAM_CODEC = StreamCodec.composite(
            EnumColour.STREAM_CODEC, Frequency::left,
            EnumColour.STREAM_CODEC, Frequency::middle,
            EnumColour.STREAM_CODEC, Frequency::right,
            ByteBufCodecs.optional(UUIDUtil.STREAM_CODEC), Frequency::owner,
            ByteBufCodecs.optional(ComponentSerialization.STREAM_CODEC), Frequency::ownerName,
            Frequency::new
    );

    public Frequency(EnumColour left, EnumColour middle, EnumColour right) {
        this(left, middle, right, Optional.empty(), Optional.empty());
    }

    public Frequency withLeft(@Nullable EnumColour left) {
        if (left != null) {
            return new Frequency(left, middle, right, owner, ownerName);
        }
        return this;
    }

    public Frequency withMiddle(@Nullable EnumColour middle) {
        if (middle != null) {
            return new Frequency(left, middle, right, owner, ownerName);
        }
        return this;
    }

    public Frequency withRight(@Nullable EnumColour right) {
        if (right != null) {
            return new Frequency(left, middle, right, owner, ownerName);
        }
        return this;
    }

    public Frequency withOwner(Player player) {
        return new Frequency(left, middle, right, Optional.of(player.getUUID()), Optional.of(player.getName()));
    }

    public Frequency withoutOwner() {
        return new Frequency(left, middle, right, Optional.empty(), Optional.empty());
    }

    public boolean hasOwner() {
        return owner.isPresent() && ownerName.isPresent();
    }

    public Frequency withColours(@Nullable EnumColour[] colours) {
        return withLeft(colours[0])
                .withMiddle(colours[1])
                .withRight(colours[2]);
    }

    public EnumColour[] toArray() {
        return new EnumColour[] { left, middle, right };
    }

    public static Frequency getComponentOrEmpty(ItemStack stack) {
        return stack.getOrDefault(EnderStorageModContent.FREQUENCY_DATA_COMPONENT, Frequency.DEFAULT);
    }

    public ItemStack putComponent(ItemStack stack) {
        stack.set(EnderStorageModContent.FREQUENCY_DATA_COMPONENT, this);
        return stack;
    }

    @Override
    public String toString() {
        String owner = "";
        if (hasOwner()) {
            owner = ",owner=" + this.owner;
        }
        return "left=" + left().getSerializedName() + ",middle=" + middle().getSerializedName() + ",right=" + right().getSerializedName() + owner;
    }

    public Component getTooltip() {
        return Component.translatable(left().getUnlocalizedName())
                .append("/")
                .append(Component.translatable(middle().getUnlocalizedName()))
                .append("/")
                .append(Component.translatable(right().getUnlocalizedName()));
    }
}
