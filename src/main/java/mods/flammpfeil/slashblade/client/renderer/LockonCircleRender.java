package mods.flammpfeil.slashblade.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import com.mojang.math.Vector4f;
import mods.flammpfeil.slashblade.capability.inputstate.CapabilityInputState;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.Face;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Camera;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.Tesselator;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.system.CallbackI;

import java.awt.*;
import java.util.Optional;

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
    public void onRenderLiving(RenderLivingEvent event){
        Player player = Minecraft.getInstance().player;
        if(player == null) return;
        if(!player.getCapability(CapabilityInputState.INPUT_STATE).filter(input->input.getCommands().contains(InputCommand.SNEAK)).isPresent()) return;

        ItemStack stack = player.getMainHandItem();

        Optional<Color> effectColor = stack.getCapability(ItemSlashBlade.BLADESTATE)
                .filter(s->event.getEntity().equals(s.getTargetEntity(player.level)))
                .map(s->s.getEffectColor());

        if(effectColor.isEmpty()) return;

        LivingEntityRenderer renderer = event.getRenderer();
        LivingEntity livingEntity = event.getEntity();



        if(!livingEntity.isAlive()) return;

        float health = livingEntity.getHealth() / livingEntity.getMaxHealth();

        Color col = new Color(effectColor.get().getRGB() & 0xFFFFFF | 0xAA000000, true);


        PoseStack poseStack = event.getMatrixStack();

        float f = livingEntity.getBbHeight() * 0.5f;
        float partialTicks = event.getPartialRenderTick();

        poseStack.pushPose();
        poseStack.translate(0.0D, (double)f, 0.0D);

        Vec3 offset = renderer.entityRenderDispatcher.camera.getPosition()
                .subtract(livingEntity.getPosition(partialTicks).add(0,f,0));
        offset = offset.scale(0.5f);
        poseStack.translate(offset.x(), offset.y(), offset.z());

        poseStack.mulPose(renderer.entityRenderDispatcher.cameraOrientation());
        //poseStack.scale(-0.025F, -0.025F, 0.025F);

        float scale = 0.0025f;
        poseStack.scale(scale, -scale, scale);

        WavefrontObject model = BladeModelManager.getInstance().getModel(modelLoc);
        ResourceLocation resourceTexture = textureLoc;

        MultiBufferSource buffer = event.getBuffers();

        final String base = "lockonBase";
        final String mask = "lockonHealthMask";
        final String value = "lockonHealth";

        BladeRenderState.setCol(col);
        BladeRenderState.renderOverridedLuminous(ItemStack.EMPTY, model, base, resourceTexture, poseStack, buffer, BladeRenderState.MAX_LIGHT );
        {
            poseStack.pushPose();
            poseStack.translate(0,0, health * 10.0f);
            BladeRenderState.setCol(new Color(0x20000000, true));
            BladeRenderState.renderOverridedLuminousDepthWrite(ItemStack.EMPTY, model, mask, resourceTexture, poseStack, buffer, BladeRenderState.MAX_LIGHT );
            poseStack.popPose();
        }
        BladeRenderState.setCol(col);
        BladeRenderState.renderOverridedLuminousDepthWrite(ItemStack.EMPTY, model, value, resourceTexture, poseStack, buffer, BladeRenderState.MAX_LIGHT );

        poseStack.popPose();
    }

    //todo: この方式はダメっぽい　GUI側じゃなくEntityの名札と同じ描画タイミングに差し込みたい
    //@SubscribeEvent
    public void onRenderLiving(RenderWorldLastEvent event){
        //todo : render faled
        Player player = Minecraft.getInstance().player;
        if(player == null) return;
        if(!player.getCapability(CapabilityInputState.INPUT_STATE).filter(input->input.getCommands().contains(InputCommand.SNEAK)).isPresent()) return;

        ItemStack stack = player.getMainHandItem();
        if(stack.isEmpty()) return;
        if(!(stack.getItem() instanceof ItemSlashBlade)) return;

        stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s->{
            Entity tmpTarget = s.getTargetEntity(player.level);

            if(tmpTarget == null) return;
            if(!tmpTarget.isAlive()) return;

            float health = 1.0f;
            if(tmpTarget.showVehicleHealth()){
                LivingEntity target = (LivingEntity)tmpTarget;
                health = 1.0f - target.getHealth() / target.getMaxHealth();
            }

            float partialTicks = event.getPartialTicks();

            Camera ari = Minecraft.getInstance().gameRenderer.getMainCamera();

            Vec3 pos = tmpTarget.getEyePosition(partialTicks)
                    .subtract(0, tmpTarget.getEyeHeight() / 2.0, 0)
                    .subtract(ari.getPosition());


            float[] col = s.getEffectColor().getColorComponents(null);
            final float alpha = 0xAA / 256.0f;

            PoseStack poseStack = new PoseStack();//event.getMatrixStack();
            try{
                poseStack.pushPose();

                poseStack.translate(pos.x,pos.y,pos.z);

                float scale = 0.00625f;
                poseStack.scale(scale, scale, scale);

                double rotYaw = ari.getYRot();
                double rotPitch = ari.getXRot();
                poseStack.mulPose(Vector3f.YN.rotation((float)(rotYaw + 180.0)));
                poseStack.mulPose(Vector3f.XN.rotation((float)(rotPitch)));


                RenderSystem.disableCull();

                RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

                //RenderSystem.alphaFunc(GL11.GL_ALWAYS, 0.05F);
                RenderSystem.enableBlend();
                RenderSystem.depthMask(true);
                RenderSystem.disableDepthTest();
                RenderSystem.depthFunc(GL11.GL_ALWAYS);
                RenderSystem.setShader(GameRenderer::getPositionColorTexLightmapShader);

                WavefrontObject model = BladeModelManager.getInstance().getModel(modelLoc);
                ResourceLocation resourceTexture = textureLoc;


                RenderSystem.enableTexture();
                TextureManager texturemanager = Minecraft.getInstance().getTextureManager();
                texturemanager.getTexture(resourceTexture).setFilter(false,false);
                RenderSystem.setShaderTexture(0, resourceTexture);

                BufferBuilder bb = Tesselator.getInstance().getBuilder();
                bb.begin(VertexFormat.Mode.TRIANGLES, WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL);

                Face.setCol(new Color(s.getEffectColor().getRGB() & 0xFFFFFF | 0xAA000000, true));
                Face.setMatrix(poseStack);
                model.tessellatePart(bb,"lockonBase");
                bb.end();
                //Tesselator.getInstance().end();

                Face.resetMatrix();
                try{
                    poseStack.pushPose();

                    poseStack.translate(0,0, health * 10.0f);

                    bb.begin(VertexFormat.Mode.TRIANGLES, WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL);
                    Face.setCol(new Color(0, true));
                    Face.setMatrix(poseStack);
                    model.tessellatePart(bb,"lockonHealthMask");
                    bb.end();
                    //Tesselator.getInstance().end();
                    Face.resetMatrix();
                }finally {
                    poseStack.popPose();
                }
                RenderSystem.depthFunc(GL11.GL_LEQUAL);
                bb.begin(VertexFormat.Mode.TRIANGLES, WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL);
                Face.setCol(new Color(s.getEffectColor().getRGB() & 0xFFFFFF | 0xAA000000, true));
                Face.setMatrix(poseStack);
                model.tessellatePart(bb,"lockonHealth");
                bb.end();
                //Tesselator.getInstance().end();
                Face.resetMatrix();
                Face.resetCol();



                //RenderSystem.alphaFunc(GL11.GL_GEQUAL, 0.01F);
                RenderSystem.depthMask(true);
                //RenderSystem.shadeModel(GL11.GL_FLAT);
                RenderSystem.enableCull();
                RenderSystem.disableBlend();
                //RenderSystem.disableFog();
                RenderSystem.defaultBlendFunc();
            }finally {
                poseStack.popPose();
            }
        });

    }
}
