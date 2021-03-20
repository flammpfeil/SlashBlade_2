package mods.flammpfeil.slashblade.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.entity.EntityAbstractSummonedSword;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class SummonedSwordRenderer<T extends EntityAbstractSummonedSword> extends EntityRenderer<T> {

    @Nullable
    @Override
    public ResourceLocation getEntityTexture(T entity) {
        return entity.getTextureLoc();
    }

    public SummonedSwordRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int packedLightIn) {

        try(MSAutoCloser msac = MSAutoCloser.pushMatrix(matrixStack)){
            Entity hits = entity.getHitEntity();
            boolean hasHitEntity = hits != null;

            if(hasHitEntity){
                matrixStack.rotate(Vector3f.YN.rotationDegrees(MathHelper.lerp(partialTicks, hits.prevRotationYaw, hits.rotationYaw) -90));
                matrixStack.rotate(Vector3f.YN.rotationDegrees(entity.getOffsetYaw()));
            }else{
                matrixStack.rotate(Vector3f.YP.rotationDegrees(MathHelper.lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw) - 90.0F));
            }

            matrixStack.rotate(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTicks, entity.prevRotationPitch, entity.rotationPitch)));

            matrixStack.rotate(Vector3f.XP.rotationDegrees(entity.getRoll()));

            float scale = 0.0075f;
            matrixStack.scale(scale,scale,scale);
            matrixStack.rotate(Vector3f.YP.rotationDegrees(90.0F));


            if(hasHitEntity){
                matrixStack.translate(0,0,-100);
            }

            //matrixStack.blendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);
            WavefrontObject model = BladeModelManager.getInstance().getModel(entity.getModelLoc());
            BladeRenderState.setCol(entity.getColor(), false);
            BladeRenderState.renderOverridedLuminous(ItemStack.EMPTY, model, "ss",getEntityTexture(entity), matrixStack, bufferIn, packedLightIn);
        }
    }
}