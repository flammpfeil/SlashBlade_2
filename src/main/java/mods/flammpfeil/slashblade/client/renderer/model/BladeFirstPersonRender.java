package mods.flammpfeil.slashblade.client.renderer.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.flammpfeil.slashblade.client.renderer.LayerMainBlade;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;

/**
 * Created by Furia on 2016/02/07.
 */
public class BladeFirstPersonRender {
    private LayerMainBlade layer = null;
    private BladeFirstPersonRender(){
        Minecraft mc = Minecraft.getInstance();

        EntityRenderer renderer = mc.getRenderManager().getRenderer(mc.player);
        if(renderer instanceof IEntityRenderer)
            layer = new LayerMainBlade((IEntityRenderer)renderer);
    }
    private static final class SingletonHolder {
        private static final BladeFirstPersonRender instance = new BladeFirstPersonRender();
    }
    public static BladeFirstPersonRender getInstance(){
        return SingletonHolder.instance;
    }

    public void render(MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn){
        if(layer == null)
            return;
        
        Minecraft mc = Minecraft.getInstance();
        boolean flag = mc.getRenderViewEntity() instanceof LivingEntity && ((LivingEntity) mc.getRenderViewEntity()).isSleeping();
        if (!(mc.gameSettings.thirdPersonView == 0 && !flag && !mc.gameSettings.hideGUI && !mc.playerController.isSpectatorMode())) {
            return;
        }
        ClientPlayerEntity player = mc.player;
        ItemStack stack = player.getHeldItem(Hand.MAIN_HAND);
        if (stack.isEmpty()) return;
        if (!(stack.getItem() instanceof ItemSlashBlade)) return;

        try(MSAutoCloser msac = MSAutoCloser.pushMatrix(matrixStack)){
            MatrixStack.Entry me = matrixStack.getLast();
            me.getMatrix().setIdentity();
            me.getNormal().setIdentity();

            matrixStack.translate(0.0f, 0.0f, -0.5f);
            matrixStack.rotate(Vector3f.ZP.rotationDegrees(180.0f));
            matrixStack.scale(1.2F, 1.0F, 1.0F);

            //no sync pitch
            matrixStack.rotate(Vector3f.XP.rotationDegrees(-mc.player.rotationPitch));

            //layer.disableOffhandRendering();
            float partialTicks = mc.getRenderPartialTicks();
            layer.render(matrixStack, bufferIn, combinedLightIn, mc.player, 0, 0, partialTicks, 0, 0, 0);
        }
    }
}