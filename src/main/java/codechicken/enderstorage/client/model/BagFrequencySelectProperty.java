package codechicken.enderstorage.client.model;

import codechicken.enderstorage.api.Frequency;
import codechicken.lib.colour.EnumColour;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.select.SelectItemModelProperty;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/**
 * Created by covers1624 on 11/1/25.
 */
public record BagFrequencySelectProperty(ColourLocation location) implements SelectItemModelProperty<EnumColour> {

    private static final StringRepresentable.EnumCodec<ColourLocation> LOCATION_CODEC = StringRepresentable.fromEnum(ColourLocation::values);
    private static final MapCodec<BagFrequencySelectProperty> CODEC = RecordCodecBuilder.mapCodec(builder ->
            builder.group(
                    LOCATION_CODEC.fieldOf("location").forGetter(e -> e.location)
            ).apply(builder, BagFrequencySelectProperty::new)
    );

    public static final Type<BagFrequencySelectProperty, EnumColour> TYPE = Type.create(CODEC, EnumColour.CODEC);

    @Override
    public EnumColour get(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity, int seed, ItemDisplayContext displayContext) {
        var frequency = Frequency.getComponentOrEmpty(stack);
        return switch (location) {
            case LEFT -> frequency.left();
            case MIDDLE -> frequency.middle();
            case RIGHT -> frequency.right();
        };
    }

    @Override
    public Codec<EnumColour> valueCodec() {
        return EnumColour.CODEC;
    }

    @Override
    public Type<? extends SelectItemModelProperty<EnumColour>, EnumColour> type() {
        return TYPE;
    }

    public enum ColourLocation implements StringRepresentable {
        LEFT,
        MIDDLE,
        RIGHT,
        ;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }
}
