package mods.flammpfeil.slashblade.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import mods.flammpfeil.slashblade.capability.inputstate.CapabilityInputState;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.Face;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ActiveRenderInfo;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class LockonCircleRender {
    private static final class SingletonHolder {
        private static final LockonCircleRender instance = new LockonCircleRender();
    }

    public static LockonCircleRender getInstance() {
        return SingletonHolder.instance;
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
        //todo : render faled
        PlayerEntity player = Minecraft.getInstance().player;
        if(player == null) return;
        if(!player.getCapability(CapabilityInputState.INPUT_STATE).filter(input->input.getCommands().contains(InputCommand.SNEAK)).isPresent()) return;

        ItemStack stack = player.getHeldItemMainhand();
        if(stack.isEmpty()) return;
        if(!(stack.getItem() instanceof ItemSlashBlade)) return;

        stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s->{
            Entity tmpTarget = s.getTargetEntity(player.world);

            if(tmpTarget == null) return;
            if(!tmpTarget.isAlive()) return;

            float health = 1.0f;
            if(tmpTarget.isLiving()){
                LivingEntity target = (LivingEntity)tmpTarget;
                health = 1.0f - target.getHealth() / target.getMaxHealth();
            }

            float partialTicks = event.getPartialTicks();

            ActiveRenderInfo ari = Minecraft.getInstance().gameRenderer.getActiveRenderInfo();

            Vector3d pos = tmpTarget.getEyePosition(partialTicks)
                    .subtract(0, tmpTarget.getEyeHeight() / 2.0, 0)
                    .subtract(ari.getProjectedView());


            float[] col = s.getEffectColor().getColorComponents(null);
            final float alpha = 0xAA / 256.0f;

            try{
                RenderSystem.pushMatrix();

                RenderSystem.multMatrix( event.getMatrixStack().getLast().getMatrix() );

                RenderSystem.translated(pos.x,pos.y,pos.z);

                double scale = 0.00625;
                RenderSystem.scaled(scale, scale, scale);

                double rotYaw = ari.getYaw();
                double rotPitch = ari.getPitch();
                RenderSystem.rotatef((float)(rotYaw + 180.0),0,-1,0);
                RenderSystem.rotatef((float)rotPitch,-1,0,0);


                RenderSystem.disableCull();

                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
                RenderSystem.alphaFunc(GL11.GL_ALWAYS, 0.05F);
                RenderSystem.enableBlend();
                RenderSystem.depthMask(true);
                RenderSystem.enableDepthTest();
                RenderSystem.depthFunc(GL11.GL_ALWAYS);
                RenderSystem.shadeModel(GL11.GL_SMOOTH);


                WavefrontObject model = BladeModelManager.getInstance().getModel(modelLoc);
                ResourceLocation resourceTexture = textureLoc;


                Minecraft.getInstance().getTextureManager().bindTexture(resourceTexture);

                BufferBuilder bb = Tessellator.getInstance().getBuffer();
                bb.begin(GL11.GL_TRIANGLES, WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL);

                Face.setCol(new Color(s.getEffectColor().getRGB() & 0xFFFFFF | 0xAA000000, true));
                model.tessellatePart(bb,"lockonBase");
                Tessellator.getInstance().draw();
                try{
                    RenderSystem.pushMatrix();

                    RenderSystem.translatef(0,0, health * 10.0f);


                    bb.begin(GL11.GL_TRIANGLES, WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL);
                    Face.setCol(new Color(0, true));
                    model.tessellatePart(bb,"lockonHealthMask");
                    Tessellator.getInstance().draw();
                }finally {
                    RenderSystem.popMatrix();
                }
                RenderSystem.depthFunc(GL11.GL_LEQUAL);
                bb.begin(GL11.GL_TRIANGLES, WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL);
                Face.setCol(new Color(s.getEffectColor().getRGB() & 0xFFFFFF | 0xAA000000, true));
                model.tessellatePart(bb,"lockonHealth");
                Tessellator.getInstance().draw();
                Face.resetCol();



                RenderSystem.alphaFunc(GL11.GL_GEQUAL, 0.01F);
                RenderSystem.depthMask(true);
                RenderSystem.shadeModel(GL11.GL_FLAT);
                RenderSystem.enableCull();
                RenderSystem.disableBlend();
                RenderSystem.disableFog();
                RenderSystem.defaultBlendFunc();
            }finally {
                RenderSystem.popMatrix();
            }
        });

    }
}
