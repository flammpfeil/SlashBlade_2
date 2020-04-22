package mods.flammpfeil.slashblade.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;
import jp.nyatla.nymmd.*;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.CapabilitySlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.ComboState;
import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.BladeMotionManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.util.TimeValueHelper;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix3f;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectUtils;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import org.lwjgl.opengl.GL11;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class LayerMainBlade<T extends LivingEntity, M extends EntityModel<T>> extends LayerRenderer<T, M> {

    public LayerMainBlade(IEntityRenderer<T, M> entityRendererIn) {
        super(entityRendererIn);
    }

    final LazyOptional<MmdPmdModelMc> bladeholder =
            LazyOptional.of(() -> {
                try {
                    return new MmdPmdModelMc(new ResourceLocation(SlashBlade.modid, "model/bladeholder.pmd"));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                } catch (MmdException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            });

    final LazyOptional<MmdMotionPlayerGL2> motionPlayer =
            LazyOptional.of(() -> {
                MmdMotionPlayerGL2 mmp = new MmdMotionPlayerGL2();;

                bladeholder.ifPresent(pmd -> {
                    try {
                        mmp.setPmd(pmd);
                    } catch (MmdException e) {
                        e.printStackTrace();
                    }
                });

                return mmp;
            });


    private float modifiedSpeed(float baseSpeed, LivingEntity entity) {
        float modif = 6.0f;
        if (EffectUtils.hasMiningSpeedup(entity)) {
            modif = 6 - (1 + EffectUtils.getMiningSpeedup(entity));
        } else if(entity.isPotionActive(Effects.MINING_FATIGUE)) {
            modif = 6 + (1 + entity.getActivePotionEffect(Effects.MINING_FATIGUE).getAmplifier()) * 2;
        }

        modif /= 6.0f;

        return baseSpeed / modif;
    }

    @Override
    public void render(MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int lightIn, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        float motionYOffset = 1.5f;
        float motionScale = 1.5f / 12.0f;
        float modelScaleBase = 0.0078125F; //0.5^7

        ItemStack stack = entity.getHeldItem(Hand.MAIN_HAND);

        LazyOptional<ISlashBladeState> state = stack.getCapability(CapabilitySlashBlade.BLADESTATE);
        state.ifPresent(s -> {

            motionPlayer.ifPresent(mmp ->
            {
                ComboState combo = s.getComboSeq();
                MmdVmdMotionMc motion = BladeMotionManager.getInstance().getMotion(combo.getMotionLoc());

                float maxSeconds = 0;
                try {
                    mmp.setVmd(motion);
                    maxSeconds = TimeValueHelper.getMSecFromFrames(motion.getMaxFrame());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                //tick to msec
                float time = TimeValueHelper.getMSecFromTicks(Math.max(0, entity.world.getGameTime() - s.getLastActionTime()) + partialTicks);

                ComboState next = combo.checkTimeOut(time);
                if(combo != next){
                    time -= combo.getTimeoutMS();
                    combo = next;
                }


                float start = TimeValueHelper.getMSecFromFrames(combo.getStartFrame());
                float end = TimeValueHelper.getMSecFromFrames(combo.getEndFrame());
                float span = end - start;

                span = Math.min(maxSeconds, span);


                time *= modifiedSpeed(combo.getSpeed(), entity);

                boolean isRoop = combo.getRoop();
                if (isRoop) {
                    time = time % span;
                }
                time = Math.min(span, time);

                time = start + time;

                try {
                    mmp.updateMotion(time);
                } catch (MmdException e) {
                    e.printStackTrace();
                }


                try(MSAutoCloser msacA = MSAutoCloser.pushMatrix(matrixStack)){
                    //minecraft model neckPoint height = 1.5f
                    //mmd model neckPoint height = 12.0f
                    matrixStack.translate(0, motionYOffset, 0);

                    matrixStack.scale(motionScale, motionScale, motionScale);


                    //transpoze mmd to mc
                    matrixStack.rotate(Vector3f.ZP.rotationDegrees(180));


                    ResourceLocation textureLocation = s.getTexture().orElseGet(() -> BladeModelManager.resourceDefaultTexture);
                    //bindTexture(textureLocation);

                    WavefrontObject obj = BladeModelManager.getInstance().getModel(s.getModel().orElse(null));

                    try(MSAutoCloser msac = MSAutoCloser.pushMatrix(matrixStack)){
                        int idx = mmp.getBoneIndexByName("hardpointA");

                        if (0 <= idx) {
                            float[] buf = new float[16];
                            mmp._skinning_mat[idx].getValue(buf);

                            Matrix4f mat = new Matrix4f(buf);
                            mat.transpose();

                            matrixStack.scale(-1, 1, 1);
                            MatrixStack.Entry entry = matrixStack.getLast();
                            entry.getMatrix().mul(mat);
                            matrixStack.scale(-1, 1, 1);
                        }

                        float modelScale = modelScaleBase * (1.0f / motionScale);
                        matrixStack.scale(modelScale, modelScale, modelScale);

                        //matrixStack.rotate(Vector3f.YP.rotationDegrees(180));


                        String part;
                        if(s.isBroken()){
                            part = "blade_damaged";
                        }else{
                            part = "blade";
                        }

                        BladeRenderState.renderOverrided(stack, obj, part, textureLocation, matrixStack, bufferIn, lightIn);
                        BladeRenderState.renderOverridedLuminous(stack, obj, part + "_luminous", textureLocation, matrixStack, bufferIn, lightIn);
                    }
                    try(MSAutoCloser msac = MSAutoCloser.pushMatrix(matrixStack)){
                        int idx = mmp.getBoneIndexByName("hardpointB");

                        if (0 <= idx) {
                            float[] buf = new float[16];
                            mmp._skinning_mat[idx].getValue(buf);

                            Matrix4f mat = new Matrix4f(buf);
                            mat.transpose();

                            matrixStack.scale(-1, 1, 1);
                            MatrixStack.Entry entry = matrixStack.getLast();
                            entry.getMatrix().mul(mat);
                            matrixStack.scale(-1, 1, 1);
                        }


                        float modelScale = modelScaleBase * (1.0f / motionScale);
                        matrixStack.scale(modelScale, modelScale, modelScale);

                        //matrixStack.rotate(Vector3f.YP.rotationDegrees(180));

                        BladeRenderState.renderOverrided(stack, obj, "sheath", textureLocation, matrixStack, bufferIn, lightIn);
                        BladeRenderState.renderOverridedLuminous(stack, obj, "sheath_luminous", textureLocation, matrixStack, bufferIn, lightIn);

                        if(s.isCharged(entity)){
                            //todo : charge effect
                        }
                    }
                    /*
                    try(MSAutoCloser msac = MSAutoCloser.pushMatrix(matrixStack)){
                        matrixStack.scale(1,1,-1);
                        //mmp.render();
                    }
                    */

                }

            });

        });
    }
}
