package codechicken.enderstorage.client.model;

import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;

import static codechicken.enderstorage.EnderStorage.MOD_ID;

/**
 * Created by covers1624 on 11/1/25.
 */
public class EnderChestModel extends Model<EnderChestModel.State> {

    public static final RenderType RENDER_TYPE = RenderTypes.entitySolid(Identifier.fromNamespaceAndPath(MOD_ID, "textures/enderchest.png"));

    private final ModelPart lid;
    private final ModelPart lock;
    private final ModelPart diamondLock;

    public EnderChestModel() {
        super(createModel(), RenderTypes::entitySolid);
        lid = root.getChild("lid");
        lock = root.getChild("lock");
        diamondLock = root.getChild("diamond_lock");
    }

    public static ModelPart createModel() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        root.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 19).addBox(0.0F, 0.0F, 0.0F, 14, 10, 14), PartPose.offset(1.0F, 6F, 1.0F));
        root.addOrReplaceChild("lid", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -5F, -14F, 14, 5, 14), PartPose.offset(1.0F, 7F, 15F));
        root.addOrReplaceChild("lock", CubeListBuilder.create().texOffs(0, 0).addBox(-1F, -2F, -15F, 2, 4, 1), PartPose.offset(8F, 7F, 15F));
        root.addOrReplaceChild("diamond_lock", CubeListBuilder.create().texOffs(0, 5).addBox(-1F, -2F, -15F, 2, 4, 1), PartPose.offset(8F, 7F, 15F));
        return LayerDefinition.create(mesh, 64, 64).bakeRoot();
    }

    @Override
    public void setupAnim(State state) {
        super.setupAnim(state);
        lid.xRot = state.lidAngle;
        lock.xRot = state.lidAngle;
        diamondLock.xRot = state.lidAngle;

        lock.visible = !state.owned;
        diamondLock.visible = state.owned;
    }

    public record State(float lidAngle, boolean owned) {
    }
}
