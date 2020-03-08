package mods.flammpfeil.slashblade.client.renderer.model;

import com.mojang.blaze3d.platform.GlStateManager;
import mods.flammpfeil.slashblade.client.renderer.LayerMainBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * Created by Furia on 2016/02/07.
 */
public class BladeFirstPersonRender {
    private LayerMainBlade layer;
    private BladeFirstPersonRender(){
        Minecraft mc = Minecraft.getInstance();
        layer = new LayerMainBlade(mc.getRenderManager().getRenderer(mc.player));
    }
    private static final class SingletonHolder {
        private static final BladeFirstPersonRender instance = new BladeFirstPersonRender();
    }
    public static BladeFirstPersonRender getInstance(){
        return SingletonHolder.instance;
    }

    public void render(){
        Minecraft mc = Minecraft.getInstance();
        boolean flag = mc.getRenderViewEntity() instanceof LivingEntity && ((LivingEntity) mc.getRenderViewEntity()).isSleeping();
        if (!(mc.gameSettings.thirdPersonView == 0 && !flag && !mc.gameSettings.hideGUI && !mc.playerController.isSpectatorMode())) {
            return;
        }
        ClientPlayerEntity player = mc.player;
        ItemStack stack = player.getHeldItem(Hand.MAIN_HAND);
        if (stack.isEmpty()) return;
        if (!(stack.getItem() instanceof ItemSlashBlade)) return;
        GlStateManager.pushLightingAttributes();
        GlStateManager.pushMatrix();
        GlStateManager.loadIdentity();

        GlStateManager.translatef(0.0f, 0.0f, -0.5f);
        GlStateManager.rotatef(180.0F, 0.0F, 0.0F, 1.0f);
        GlStateManager.scalef(1.2F, 1.0F, 1.0F);

        //no sync pitch
        GlStateManager.rotatef(-mc.player.rotationPitch,1,0,0);

        //layer.disableOffhandRendering();
        GlStateManager.disableCull();
        float partialTicks = mc.getRenderPartialTicks();
        layer.render(mc.player, 0, 0, partialTicks, 0, 0, 0, 0);
        GlStateManager.enableCull();
        //layer.doRenderLayer(mc.player, 0, 0, partialTicks, 0, 0, 0, 0);
        //layer.enableOffhandRendering();
        GlStateManager.popMatrix();
        GlStateManager.popAttributes();
    }
/*
    public void renderVR() {

        Minecraft mc = Minecraft.getMinecraft();
        boolean flag = mc.getRenderViewEntity() instanceof EntityLivingBase && ((EntityLivingBase) mc.getRenderViewEntity()).isPlayerSleeping();
        if (!(mc.gameSettings.thirdPersonView == 0 && !flag && !mc.gameSettings.hideGUI && !mc.playerController.isSpectator())) {
            return;
        }
        EntityPlayerSP player = mc.player;
        ItemStack stack = player.getHeldItem(EnumHand.MAIN_HAND);
        if (stack.isEmpty()) return;
        if (!(stack.getItem() instanceof ItemSlashBlade)) return;



        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();
        GlStateManager.popMatrix();



        GlStateManager.pushAttrib();
        GlStateManager.pushMatrix();



        GlStateManager.scale(0.3f, 0.3F, 0.3F);
        //GlStateManager.translate(0.375*-f, 0, .75);

        GlStateManager.translate(-0.45, -0.85, -1.5);

        GlStateManager.rotate(-180, 1, 0,0);

        float partialTicks = mc.getRenderPartialTicks();
        GlStateManager.rotate(interpolateRotation(player.prevRotationPitch, player.rotationPitch, partialTicks), 1 , 0 , 0);

        GlStateManager.rotate(180, 0, 1, 0);


        GlStateManager.scale(1.25f, 1.25F, 1.25F);


        Face.resetColor();
        layer.disableOffhandRendering();
        layer.doRenderLayer(mc.player, 0, 0, partialTicks, 0, 0, 0, 0);
        layer.enableOffhandRendering();
        GlStateManager.popMatrix();
        GlStateManager.popAttrib();


        GlStateManager.pushMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.pushMatrix();
        GlStateManager.pushMatrix();


    }*/

    protected float interpolateRotation(float prevYawOffset, float yawOffset, float partialTicks)
    {
        float f;

        for (f = yawOffset - prevYawOffset; f < -180.0F; f += 360.0F)
        {
            ;
        }

        while (f >= 180.0F)
        {
            f -= 360.0F;
        }

        return prevYawOffset + partialTicks * f;
    }
}