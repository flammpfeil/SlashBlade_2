package mods.flammpfeil.slashblade.client.renderer.layers;

import com.mojang.blaze3d.vertex.PoseStack;
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
import mods.flammpfeil.slashblade.event.client.UserPoseOverrider;
import mods.flammpfeil.slashblade.util.TimeValueHelper;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffectUtil;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionHand;
import net.minecraft.resources.ResourceLocation;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraftforge.common.util.LazyOptional;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Arrays;

public class LayerMainBlade<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    public LayerMainBlade(RenderLayerParent<T, M> entityRendererIn) {
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
        if (MobEffectUtil.hasDigSpeed(entity)) {
            modif = 6 - (1 + MobEffectUtil.getDigSpeedAmplification(entity));
        } else if(entity.hasEffect(MobEffects.DIG_SLOWDOWN)) {
            modif = 6 + (1 + entity.getEffect(MobEffects.DIG_SLOWDOWN).getAmplifier()) * 2;
        }

        modif /= 6.0f;

        return baseSpeed / modif;
    }

    @Override
    public void render(PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn, T entity, float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {

        float motionYOffset = 1.5f;
        double motionScale = 1.5 / 12.0;
        double modelScaleBase = 0.0078125F; //0.5^7

        ItemStack stack = entity.getItemInHand(InteractionHand.MAIN_HAND);

        if(stack.isEmpty()) return;

        LazyOptional<ISlashBladeState> state = stack.getCapability(CapabilitySlashBlade.BLADESTATE);
        state.ifPresent(s -> {

            motionPlayer.ifPresent(mmp ->
            {
                ComboState combo = s.getComboSeq();
                //tick to msec
                double time = TimeValueHelper.getMSecFromTicks(Math.max(0, entity.level.getGameTime() - s.getLastActionTime()) + partialTicks);

                while(combo != ComboState.NONE && combo.getTimeoutMS() < time){
                    time -= combo.getTimeoutMS();

                    combo = combo.getNextOfTimeout();
                }
                if(combo == ComboState.NONE){
                    combo = s.getComboRoot();
                }

                MmdVmdMotionMc motion = BladeMotionManager.getInstance().getMotion(combo.getMotionLoc());

                double maxSeconds = 0;
                try {
                    mmp.setVmd(motion);
                    maxSeconds = TimeValueHelper.getMSecFromFrames(motion.getMaxFrame());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                double start = TimeValueHelper.getMSecFromFrames(combo.getStartFrame());
                double end = TimeValueHelper.getMSecFromFrames(combo.getEndFrame());
                double span = Math.abs(end - start);

                span = Math.min(maxSeconds, span);

                boolean isRoop = combo.getRoop();
                if (isRoop) {
                    time = time % span;
                }
                time = Math.min(span, time);

                time = start + time;

                try {
                    mmp.updateMotion((float)time);
                } catch (MmdException e) {
                    e.printStackTrace();
                }


                try(MSAutoCloser msacA = MSAutoCloser.pushMatrix(matrixStack)){

                    UserPoseOverrider.invertRot(matrixStack,entity,partialTicks);

                    //minecraft model neckPoint height = 1.5f
                    //mmd model neckPoint height = 12.0f
                    matrixStack.translate(0, motionYOffset, 0);

                    matrixStack.scale((float)motionScale, (float)motionScale, (float)motionScale);


                    //transpoze mmd to mc
                    matrixStack.mulPose(Vector3f.ZP.rotationDegrees(180));


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
                            PoseStack.Pose entry = matrixStack.last();
                            entry.pose().multiply(mat);
                            matrixStack.scale(-1, 1, 1);
                        }

                        float modelScale = (float)(modelScaleBase * (1.0f / motionScale));
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
                            PoseStack.Pose entry = matrixStack.last();
                            entry.pose().multiply(mat);
                            matrixStack.scale(-1, 1, 1);
                        }


                        float modelScale = (float)(modelScaleBase * (1.0f / motionScale));
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
