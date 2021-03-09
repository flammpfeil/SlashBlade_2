package mods.flammpfeil.slashblade.event.client;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class UserPoseOverrider {
    private static final class SingletonHolder {
        private static final UserPoseOverrider instance = new UserPoseOverrider();
    }
    public static UserPoseOverrider getInstance() {
        return SingletonHolder.instance;
    }
    private UserPoseOverrider(){}
    public void register(){
        MinecraftForge.EVENT_BUS.register(this);
    }

    private static final String TAG_ROT = "sb_yrot";
    private static final String TAG_ROT_PREV = "sb_yrot_prev";

    @SubscribeEvent
    public void onRenderPlayerEventPre(RenderLivingEvent.Pre event){
        ItemStack stack = event.getEntity().getHeldItemMainhand();

        if(stack.isEmpty()) return;
        if(!(stack.getItem() instanceof ItemSlashBlade)) return;

        float rot = event.getEntity().getPersistentData().getFloat(TAG_ROT);
        float rotPrev = event.getEntity().getPersistentData().getFloat(TAG_ROT_PREV);

        MatrixStack matrixStackIn = event.getMatrixStack();
        LivingEntity entityLiving = event.getEntity();
        float partialTicks = event.getPartialRenderTick();

        float f = MathHelper.interpolateAngle(partialTicks, entityLiving.prevRenderYawOffset, entityLiving.renderYawOffset);
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180.0F - f));
        anotherPoseRotP(matrixStackIn, entityLiving, partialTicks);

        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(MathHelper.interpolateAngle(partialTicks,rot,rotPrev)));

        anotherPoseRotN(matrixStackIn, entityLiving, partialTicks);
        matrixStackIn.rotate(Vector3f.YN.rotationDegrees(180.0F - f));
    }

    static public void anotherPoseRotP(MatrixStack matrixStackIn, LivingEntity entityLiving, float partialTicks){
        boolean isPositive = true;
        float np = isPositive ? 1 : -1;

        float f = entityLiving.getSwimAnimation(partialTicks);
        if (entityLiving.isElytraFlying()) {
            float f1 = (float)entityLiving.getTicksElytraFlying() + partialTicks;
            float f2 = MathHelper.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
            if (!entityLiving.isSpinAttacking()) {
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(np * f2 * (-90.0F - entityLiving.rotationPitch)));
            }

            Vector3d vector3d = entityLiving.getLook(partialTicks);
            Vector3d vector3d1 = entityLiving.getMotion();
            double d0 = Entity.horizontalMag(vector3d1);
            double d1 = Entity.horizontalMag(vector3d);
            if (d0 > 0.0D && d1 > 0.0D) {
                double d2 = (vector3d1.x * vector3d.x + vector3d1.z * vector3d.z) / Math.sqrt(d0 * d1);
                double d3 = vector3d1.x * vector3d.z - vector3d1.z * vector3d.x;
                matrixStackIn.rotate(Vector3f.YP.rotation((float)(np * Math.signum(d3) * Math.acos(d2))));
            }
        } else if (f > 0.0F) {
            float f3 = entityLiving.isInWater() ? -90.0F - entityLiving.rotationPitch : -90.0F;
            float f4 = MathHelper.lerp(f, 0.0F, f3);
            matrixStackIn.rotate(Vector3f.XP.rotationDegrees(np * f4));
            if (entityLiving.isActualySwimming()) {
                matrixStackIn.translate(0.0D, np * -1.0D, (double) np * 0.3F);
            }
        }
    }
    static public void anotherPoseRotN(MatrixStack matrixStackIn, LivingEntity entityLiving, float partialTicks){
        boolean isPositive = false;
        float np = isPositive ? 1 : -1;

        float f = entityLiving.getSwimAnimation(partialTicks);
        if (entityLiving.isElytraFlying()) {
            Vector3d vector3d = entityLiving.getLook(partialTicks);
            Vector3d vector3d1 = entityLiving.getMotion();
            double d0 = Entity.horizontalMag(vector3d1);
            double d1 = Entity.horizontalMag(vector3d);
            if (d0 > 0.0D && d1 > 0.0D) {
                double d2 = (vector3d1.x * vector3d.x + vector3d1.z * vector3d.z) / Math.sqrt(d0 * d1);
                double d3 = vector3d1.x * vector3d.z - vector3d1.z * vector3d.x;
                matrixStackIn.rotate(Vector3f.YP.rotation((float)(np * Math.signum(d3) * Math.acos(d2))));
            }

            float f1 = (float)entityLiving.getTicksElytraFlying() + partialTicks;
            float f2 = MathHelper.clamp(f1 * f1 / 100.0F, 0.0F, 1.0F);
            if (!entityLiving.isSpinAttacking()) {
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(np * f2 * (-90.0F - entityLiving.rotationPitch)));
            }
        } else if (f > 0.0F) {
            if (entityLiving.isActualySwimming()) {
                matrixStackIn.translate(0.0D, np * -1.0D, (double) np * 0.3F);
            }

            float f3 = entityLiving.isInWater() ? -90.0F - entityLiving.rotationPitch : -90.0F;
            float f4 = MathHelper.lerp(f, 0.0F, f3);
            matrixStackIn.rotate(Vector3f.XP.rotationDegrees(np * f4));
        }
    }

    static public void setRot(Entity target, float rotYaw, boolean isOffset){
        CompoundNBT tag = target.getPersistentData();

        float prevRot = tag.getFloat(TAG_ROT);
        tag.putFloat(TAG_ROT_PREV, prevRot);

        if(isOffset)
            rotYaw += prevRot;

        tag.putFloat(TAG_ROT, rotYaw);
    }
}
