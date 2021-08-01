package mods.flammpfeil.slashblade.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.Face;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.entity.EntitySlashEffect;
import mods.flammpfeil.slashblade.init.SBItems;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.LazyValue;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class SlashEffectRenderer<T extends EntitySlashEffect> extends EntityRenderer<T> {

    static private final ResourceLocation modelLocation = new ResourceLocation(SlashBlade.modid, "model/util/slash.obj");
    static private final ResourceLocation textureLocation = new ResourceLocation(SlashBlade.modid, "model/util/slash.png");

    static private LazyValue<ItemStack> enchantedItem = new LazyValue<ItemStack>(() -> new ItemStack(SBItems.proudsoul));

    @Nullable
    @Override
    public ResourceLocation getEntityTexture(T entity) {
        return textureLocation;
    }

    public SlashEffectRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {

        try (MSAutoCloser msac = MSAutoCloser.pushMatrix(matrixStackIn)) {

            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-MathHelper.lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw) - 90.0F));
            matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(MathHelper.lerp(partialTicks, entity.prevRotationPitch, entity.rotationPitch)));
            matrixStackIn.rotate(Vector3f.XP.rotationDegrees(entity.getRotationRoll()));


            WavefrontObject model = BladeModelManager.getInstance().getModel(modelLocation);

            int lifetime = entity.getLifetime();

            float progress = Math.min(lifetime, (entity.ticksExisted+partialTicks)) / lifetime;

            double deathTime = lifetime;
            //double baseAlpha = Math.sin(Math.PI * 0.5 * (Math.min(deathTime, Math.max(0, (lifetime - (entity.ticksExisted) - partialTicks))) / deathTime));
            double baseAlpha = (Math.min(deathTime, Math.max(0, (lifetime - (entity.ticksExisted) - partialTicks))) / deathTime);
            baseAlpha = -Math.pow(baseAlpha - 1, 4.0)+1.0;

            //baseAlpha = Math.sin(-Math.PI + Math.PI * 2 * progress) * 0.5f + 0.5f;
            //baseAlpha = Math.sin(Math.PI * progress);

            //time
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(entity.getRotationOffset() -135.0F * progress));

            matrixStackIn.scale(1,0.25f,1);

            float baseScale = 1.2f;
            matrixStackIn.scale(baseScale,baseScale,baseScale);

            float yscale = 0.03f;
            float scale = entity.getBaseSize() * MathHelper.lerp(progress, 0.03f,0.035f);

            int color = entity.getColor() & 0xFFFFFF;

            ResourceLocation rl = getEntityTexture(entity);

            //baseAlpha = 1.0f;
            int alpha = ((0xFF & (int) (0xFF * baseAlpha)) << 24);

            //black alpha inside
            try (MSAutoCloser msacb = MSAutoCloser.pushMatrix(matrixStackIn)) {
                float windscale = entity.getBaseSize() * MathHelper.lerp(progress, 0.035f,0.03f);
                matrixStackIn.scale(windscale, yscale, windscale);
                Face.setAlphaOverride(Face.alphaOverrideYZZ);
                Face.setUvOperator(1, 1, 0, -0.8f + progress * 0.3f);
                BladeRenderState.setCol(0x222222 | alpha);
                BladeRenderState.renderOverridedColorWrite(ItemStack.EMPTY, model, "base", rl, matrixStackIn, bufferIn, packedLightIn);
            }

            //color alpha base
            try (MSAutoCloser msacb = MSAutoCloser.pushMatrix(matrixStackIn)) {
                matrixStackIn.scale(scale, yscale, scale);
                Face.setAlphaOverride(Face.alphaOverrideYZZ);
                Face.setUvOperator(1,1,0, -0.35f + progress * -0.15f);
                BladeRenderState.setCol(color | alpha);
                BladeRenderState.renderOverridedColorWrite(ItemStack.EMPTY, model, "base", rl, matrixStackIn, bufferIn, packedLightIn);
            }

            //white add outside
            try (MSAutoCloser msacb = MSAutoCloser.pushMatrix(matrixStackIn)) {
                float windscale = entity.getBaseSize() * MathHelper.lerp(progress, 0.03f,0.0375f);
                matrixStackIn.scale(windscale, yscale, windscale);
                Face.setAlphaOverride(Face.alphaOverrideYZZ);
                Face.setUvOperator(1, 1, 0, -0.5f + progress * -0.2f);
                BladeRenderState.setCol(0x404040 | alpha);
                BladeRenderState.renderOverridedLuminous(ItemStack.EMPTY, model, "base", rl, matrixStackIn, bufferIn, packedLightIn);
            }

            //color add base
            try (MSAutoCloser msacb = MSAutoCloser.pushMatrix(matrixStackIn)) {
                matrixStackIn.scale(scale, yscale, scale);
                Face.setAlphaOverride(Face.alphaOverrideYZZ);
                Face.setUvOperator(1, 1, 0, -0.35f + progress * -0.15f);
                BladeRenderState.setCol(color | alpha);
                BladeRenderState.renderOverridedLuminous(ItemStack.EMPTY, model, "base", rl, matrixStackIn, bufferIn, packedLightIn);
            }
        }
    }
}