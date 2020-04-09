package mods.flammpfeil.slashblade.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import mods.flammpfeil.slashblade.ability.LockOnManager;
import mods.flammpfeil.slashblade.capability.imputstate.CapabilityImputState;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.ImputCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class LockonCircleRender {
    private static final class SingletonHolder {
        private static final LockonCircleRender instance = new LockonCircleRender();
    }

    public static LockonCircleRender getInstance() {
        return LockonCircleRender.SingletonHolder.instance;
    }

    private LockonCircleRender() {
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }


    static final ResourceLocation modelLoc = new ResourceLocation("slashblade","model/util/lockon.obj");
    static final ResourceLocation textureLoc = new ResourceLocation("slashblade","model/util/lockon.png");

    @SubscribeEvent
    public void onRenderLiving(RenderWorldLastEvent event){
        PlayerEntity player = Minecraft.getInstance().player;
        if(player == null) return;
        if(!player.getCapability(CapabilityImputState.IMPUT_STATE).filter(imput->imput.getCommands().contains(ImputCommand.SNEAK)).isPresent()) return;

        ItemStack stack = player.getHeldItemMainhand();
        if(stack.isEmpty()) return;
        if(!(stack.getItem() instanceof ItemSlashBlade)) return;

        stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s->{
            Entity tmpTarget = s.getTargetEntity(player.world);

            if(tmpTarget == null) return;
            if(!tmpTarget.isAlive()) return;
            if(!tmpTarget.isLiving()) return;

            LivingEntity target = (LivingEntity)tmpTarget;
            float health = 1.0f - target.getHealth() / target.getMaxHealth();

            float partialTicks = event.getPartialTicks();

            ActiveRenderInfo ari = Minecraft.getInstance().gameRenderer.getActiveRenderInfo();

            Vec3d pos = target.getEyePosition(partialTicks)
                    .subtract(0, target.getEyeHeight() / 2.0, 0)
                    .subtract(ari.getProjectedView());


            float[] col = s.getEffectColor().getColorComponents(null);
            final float alpha = 0xAA / 256.0f;

            try(MSAutoCloser m = MSAutoCloser.pushMatrix()){
                GlStateManager.translated(pos.x,pos.y,pos.z);

                double scale = 0.00625;
                GlStateManager.scaled(scale, scale, scale);

                double rotYaw = ari.getYaw();
                double rotPitch = ari.getPitch();
                GlStateManager.rotated(rotYaw + 180.0,0,-1,0);
                GlStateManager.rotated(rotPitch,-1,0,0);


                GlStateManager.disableCull();

                GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                GlStateManager.alphaFunc(GL11.GL_ALWAYS, 0.05F);
                GlStateManager.enableBlend();
                GlStateManager.depthMask(true);
                GlStateManager.enableDepthTest();
                GlStateManager.depthFunc(GL11.GL_ALWAYS);
                GlStateManager.shadeModel(GL11.GL_SMOOTH);


                WavefrontObject model = BladeModelManager.getInstance().getModel(modelLoc);
                ResourceLocation resourceTexture = textureLoc;


                Minecraft.getInstance().getTextureManager().bindTexture(resourceTexture);

                GlStateManager.color4f(col[0],col[1],col[2], alpha);
                model.renderPart("lockonBase");
                try(MSAutoCloser mm = MSAutoCloser.pushMatrix()){
                    GlStateManager.translatef(0,0, health * 10.0f);
                    GlStateManager.color4f(0,0,0,0);
                    model.renderPart("lockonHealthMask");
                }
                GlStateManager.depthFunc(GL11.GL_LEQUAL);
                GlStateManager.color4f(col[0],col[1],col[2], alpha);
                model.renderPart("lockonHealth");



                GlStateManager.alphaFunc(GL11.GL_GEQUAL, 0.01F);
                GlStateManager.depthMask(true);
                GlStateManager.shadeModel(GL11.GL_FLAT);
                GlStateManager.enableCull();
                GlStateManager.disableBlend();
                GlStateManager.disableFog();
            }
        });

    }
}
