package mods.flammpfeil.slashblade.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModel;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.client.renderer.util.RenderHandler;
import mods.flammpfeil.slashblade.event.client.RenderOverrideEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.SwordType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.model.ModelBakery;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import javax.vecmath.Color4f;
import java.awt.*;
import java.util.EnumSet;

public class BladeItemEntityRenderer extends ItemRenderer {
    public BladeItemEntityRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, Minecraft.getInstance().getItemRenderer());
    }

    @Override
    public boolean shouldSpreadItems() {
        return false;
    }

    @Override
    public boolean shouldBob() {
        return false;
    }

    @Override
    public void doRender(ItemEntity itemIn, double x, double y, double z, float entityYaw, float partialTicks) {

        this.shadowSize = 0;

        if(!itemIn.getItem().isEmpty()){
            renderBlade(itemIn, x, y, z, entityYaw, partialTicks);
        }else{
            partialTicks = (float)(itemIn.hoverStart * 20.0 - (double)itemIn.getAge());
            super.doRender(itemIn, x, y, z, entityYaw, partialTicks);
        }
    }

    private void renderBlade(ItemEntity itemIn, double x, double y, double z, float entityYaw, float partialTicks) {

        if (this.renderOutlines) {
            GlStateManager.enableColorMaterial();
            GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(itemIn));
        }

        try (MSAutoCloser msac = MSAutoCloser.pushMatrix()) {
            GlStateManager.pushLightingAttributes();

            GlStateManager.disableLighting();
            GlStateManager.translated(x, y, z);
            GlStateManager.rotatef(entityYaw ,0,1 ,0);

            ItemStack current = itemIn.getItem();

            EnumSet<SwordType> types = SwordType.from(current);
            ResourceLocation modelLocation =
                    current.getCapability(ItemSlashBlade.BLADESTATE)
                            .map((state) -> state.getModel().orElseGet(() -> BladeModelManager.resourceDefaultModel))
                            .orElseGet(()->{
                                if(current.hasTag() && current.getTag().contains("Model"))
                                    return new ResourceLocation(current.getTag().getString("Model"));
                                else
                                    return  BladeModelManager.resourceDefaultModel;
                            });
            ResourceLocation textureLocation =
                    current.getCapability(ItemSlashBlade.BLADESTATE)
                            .map((state) -> state.getTexture().orElseGet(() -> BladeModelManager.resourceDefaultTexture))
                            .orElseGet(()->{
                                if(current.hasTag() && current.getTag().contains("Texture"))
                                    return new ResourceLocation(current.getTag().getString("Texture"));
                                else
                                    return  BladeModelManager.resourceDefaultTexture;
                            });
            WavefrontObject model = BladeModelManager.getInstance().getModel(modelLocation);

            float scale = 0.00625f;

            GlStateManager.shadeModel(GL11.GL_SMOOTH);

            try (MSAutoCloser msac2 = MSAutoCloser.pushMatrix()) {

                float heightOffset;
                float xOffset = 0;
                String renderTarget;
                if (types.contains(SwordType.EdgeFragment)) {
                    heightOffset = 225;
                    xOffset = 200;
                    renderTarget = "blade_fragment";
                }else if (types.contains(SwordType.Broken)) {
                    heightOffset = 100;
                    xOffset = 30;
                    renderTarget = "blade_damaged";
                }else {
                    heightOffset = 225;
                    xOffset = 120;
                    renderTarget = "blade";
                }


                if(itemIn.isInWater()){

                    GlStateManager.translatef(0, 0.025f, 0);
                    GlStateManager.rotatef(itemIn.hoverStart ,0 ,1, 0);

                    GlStateManager.scalef(scale, scale, scale);

                    GlStateManager.rotatef(90, 1, 0, 0);

                }else if(!itemIn.onGround)
                {
                    GlStateManager.scalef(scale, scale, scale);

                    float speed = -81f;
                    GlStateManager.rotatef(speed * (itemIn.ticksExisted + partialTicks) ,0,0,1);
                    GlStateManager.translatef(xOffset, 0 , 0);
                }else{
                    GlStateManager.scalef(scale, scale, scale);

                    GlStateManager.rotatef(60 + (float)Math.toDegrees(itemIn.hoverStart / 6.0),0,0,1);
                    GlStateManager.translatef(heightOffset, 0 , 0);
                }



                RenderHandler.renderOverrided(current, model, renderTarget, textureLocation);

                GL11.glEnable(GL11.GL_BLEND);
                GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

                RenderHandler.renderOverrided(current, model, renderTarget + "_luminous", textureLocation);

                GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

            }

            if((itemIn.isInWater() || itemIn.onGround) && !types.contains(SwordType.NoScabbard)) {

                try (MSAutoCloser msac2 = MSAutoCloser.pushMatrix()) {

                    GlStateManager.translatef(0, 0.025f, 0);

                    GlStateManager.rotatef(itemIn.hoverStart ,0 ,1, 0);

                    if(!itemIn.isInWater()){
                        GlStateManager.translated(0.75, 0, -0.4);
                    }

                    GlStateManager.scalef(scale, scale, scale);

                    GlStateManager.rotatef(90, 1, 0, 0);

                    String renderTarget = "sheath";

                    RenderHandler.renderOverrided(current, model, renderTarget, textureLocation);

                    GL11.glEnable(GL11.GL_BLEND);
                    GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

                    RenderHandler.renderOverrided(current, model, renderTarget + "_luminous", textureLocation);

                    GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
                }
            }


            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GlStateManager.enableLighting();
            GL11.glEnable(GL11.GL_CULL_FACE);

            GlStateManager.shadeModel(GL11.GL_FLAT);
            GlStateManager.popAttributes();
        }



        if (this.renderOutlines) {
            GlStateManager.tearDownSolidRenderingTextureCombine();
            GlStateManager.disableColorMaterial();
        }

    }


    @Override
    public void doRenderShadowAndFire(Entity entityIn, double x, double y, double z, float yaw, float partialTicks) {

        GlStateManager.enableBlend();
        GlStateManager.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_COLOR, GlStateManager.DestFactor.ONE
                , GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);

        GlStateManager.pushMatrix();
        GlStateManager.translatef((float)x, (float)y, (float)z);
        GlStateManager.scaled(1.4,1.8, 1.4);
        GlStateManager.translatef((float)-x, (float)-y, (float)-z);

        //core
        super.doRenderShadowAndFire(entityIn, x, y, z, yaw, partialTicks);


        //dark fire
        GlStateManager.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE
                , GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
        GlStateManager.translatef((float)x, (float)y, (float)z);
        GlStateManager.scaled(1.5,1.6,1.5);
        GlStateManager.translatef((float)-x, (float)-y, (float)-z);
        super.doRenderShadowAndFire(entityIn, x, y, z, yaw, partialTicks);
        GlStateManager.blendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);
        super.doRenderShadowAndFire(entityIn, x, y, z, yaw, partialTicks);


        GlStateManager.popMatrix();
        GlStateManager.blendEquation(GL14.GL_FUNC_ADD);
        GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        GlStateManager.disableBlend();
    }
}
