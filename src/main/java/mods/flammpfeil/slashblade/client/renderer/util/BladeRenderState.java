package mods.flammpfeil.slashblade.client.renderer.util;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import mods.flammpfeil.slashblade.client.renderer.model.obj.Face;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.event.client.RenderOverrideEvent;
import net.minecraft.Util;
import net.minecraft.client.renderer.*;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.blaze3d.vertex.VertexFormatElement;
import net.minecraft.world.item.ItemStack;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.util.Optional;
import java.util.function.Function;

import net.minecraft.client.renderer.RenderStateShard.TextureStateShard;

import net.minecraft.client.renderer.entity.ItemRenderer;

public class BladeRenderState extends RenderStateShard{

    private static final Color defaultColor = Color.white;
    private static Color col = defaultColor;
    public static void setCol(int rgba){
        setCol(rgba, true);
    }
    public static void setCol(int rgb, boolean hasAlpha){
        setCol(new Color(rgb, hasAlpha));
    }
    public static void setCol(Color value) {
        col = value;
    }

    public static final int MAX_LIGHT = 15728864;

    public static void resetCol() {
        col = defaultColor;
    }

    public BladeRenderState(String p_i225973_1_, Runnable p_i225973_2_, Runnable p_i225973_3_) {
        super(p_i225973_1_, p_i225973_2_, p_i225973_3_);
    }

    static public void renderOverrided(ItemStack stack, WavefrontObject model, String target, ResourceLocation texture, PoseStack  matrixStackIn, MultiBufferSource bufferIn, int packedLightIn){

        Face.forceQuad = true;
        renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn, packedLightIn, Util.memoize(RenderType::entitySmoothCutout), true);
        Face.forceQuad = false;

        //renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn, packedLightIn, Util.memoize(BladeRenderState::getSlashBladeBlend), true);
    }

    static public void renderOverridedColorWrite(ItemStack stack, WavefrontObject model, String target, ResourceLocation texture, PoseStack  matrixStackIn, MultiBufferSource bufferIn, int packedLightIn){
        renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn, packedLightIn, Util.memoize(BladeRenderState::getSlashBladeBlendColorWrite), true);
    }

    static public void renderOverridedLuminous(ItemStack stack, WavefrontObject model, String target, ResourceLocation texture, PoseStack  matrixStackIn, MultiBufferSource bufferIn, int packedLightIn){
        renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn, packedLightIn, Util.memoize(BladeRenderState::getSlashBladeBlendLuminous), false);
    }
    static public void renderOverridedLuminousDepthWrite(ItemStack stack, WavefrontObject model, String target, ResourceLocation texture, PoseStack  matrixStackIn, MultiBufferSource bufferIn, int packedLightIn){
        renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn, packedLightIn, Util.memoize(BladeRenderState::getSlashBladeBlendLuminousDepthWrite), false);
    }

    static public void renderOverridedReverseLuminous(ItemStack stack, WavefrontObject model, String target, ResourceLocation texture, PoseStack  matrixStackIn, MultiBufferSource bufferIn, int packedLightIn){
        renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn, packedLightIn, Util.memoize(BladeRenderState::getSlashBladeBlendReverseLuminous), false);
    }


    static public void renderOverrided(ItemStack stack, WavefrontObject model, String target, ResourceLocation texture, PoseStack  matrixStackIn, MultiBufferSource bufferIn, int packedLightIn, Function<ResourceLocation,RenderType> getRenderType, boolean enableEffect){
        RenderOverrideEvent event
                = RenderOverrideEvent.onRenderOverride(stack, model, target, texture, matrixStackIn, bufferIn);

        if(event.isCanceled()) return;

        ResourceLocation loc = event.getTexture();

        RenderType rt = getRenderType.apply(loc);//getSlashBladeBlendLuminous(event.getTexture());
        VertexConsumer vb;
        vb = bufferIn.getBuffer(rt);

        Face.setCol(col);
        Face.setLightMap(packedLightIn);
        Face.setMatrix(matrixStackIn);
        event.getModel().tessellateOnly(vb, event.getTarget());


        if(stack.hasFoil() && enableEffect){
            boolean forceQuad = Face.forceQuad;
            Face.forceQuad = true;
            vb = bufferIn.getBuffer(RenderType.entityGlint());
            event.getModel().tessellateOnly(vb, event.getTarget());
            Face.forceQuad = forceQuad;
        }

        Face.resetMatrix();
        Face.resetLightMap();
        Face.resetCol();

        Face.resetAlphaOverride();
        Face.resetUvOperator();

        resetCol();
    }

    public static VertexConsumer getBuffer(MultiBufferSource bufferIn, RenderType renderTypeIn, boolean glintIn) {
        return null;
    }

    public static final VertexFormat POSITION_TEX = new VertexFormat(ImmutableMap.<String, VertexFormatElement>builder().put("Position",DefaultVertexFormat.ELEMENT_POSITION).put("UV0",DefaultVertexFormat.ELEMENT_UV0).build());
    public static final RenderType BLADE_GLINT =
            RenderType.create(
                    "blade_glint",
                    POSITION_TEX,
                    VertexFormat.Mode.TRIANGLES,
                    256,
                    false,
                    false ,
                    RenderType.CompositeState.builder()
                            .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_GLINT_SHADER)
                            .setTextureState(new TextureStateShard(ItemRenderer.ENCHANTED_GLINT_ITEM, true, false)).setWriteMaskState(COLOR_WRITE).setCullState(NO_CULL).setDepthTestState(EQUAL_DEPTH_TEST).setTransparencyState(GLINT_TRANSPARENCY).setTexturingState(ENTITY_GLINT_TEXTURING).createCompositeState(false));


    public static RenderType getSlashBladeBlend(ResourceLocation p_228638_0_) {

        /*
        RenderType.CompositeState rendertype$compositestate = RenderType.CompositeState.builder()
                .setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                .setTextureState(new RenderStateShard.TextureStateShard(p_173200_, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                .setOutputState(ITEM_ENTITY_TARGET)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(OVERLAY)
                .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                .createCompositeState(true);
        */

        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(RenderStateShard.POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                .setOutputState(RenderStateShard.TRANSLUCENT_TARGET)
                .setTextureState(new RenderStateShard.TextureStateShard(p_228638_0_, false, false))
                .setTransparencyState(RenderStateShard.NO_TRANSPARENCY)
                //.setDiffuseLightingState(DIFFUSE_LIGHTING)
                .setLightmapState(LIGHTMAP)
                .setOverlayState(RenderStateShard.OVERLAY)
                //.overlay(OVERLAY_ENABLED)
                .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                .createCompositeState(true);

        return RenderType.create("slashblade_blend", WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL, VertexFormat.Mode.TRIANGLES, 256, true, false, state);
    }

    public static RenderType getSlashBladeBlendColorWrite(ResourceLocation p_228638_0_) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                .setOutputState(TRANSLUCENT_TARGET)
                .setTextureState(new RenderStateShard.TextureStateShard(p_228638_0_, false, false))
                .setTransparencyState(TRANSLUCENT_TRANSPARENCY)
                //.setDiffuseLightingState(RenderStateShard.NO_DIFFUSE_LIGHTING)
                .setLightmapState(LIGHTMAP)
                //.overlay(OVERLAY_ENABLED)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(true);
        return RenderType.create("slashblade_blend_write_color", WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL, VertexFormat.Mode.TRIANGLES, 256, true, false, state);
    }



    protected static final RenderStateShard.TransparencyStateShard LIGHTNING_ADDITIVE_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("lightning_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });

    public static RenderType getSlashBladeBlendLuminous(ResourceLocation p_228638_0_) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                .setOutputState(PARTICLES_TARGET)
                .setCullState(RenderStateShard.NO_CULL)
                .setTextureState(new RenderStateShard.TextureStateShard(p_228638_0_, true, false))
                .setTransparencyState(LIGHTNING_ADDITIVE_TRANSPARENCY)
                //.setDiffuseLightingState(RenderStateShard.NO_DIFFUSE_LIGHTING)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                //.overlay(OVERLAY_ENABLED)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);
        return RenderType.create("slashblade_blend_luminous", WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL, VertexFormat.Mode.TRIANGLES, 256, true, false, state);
    }
    public static RenderType getSlashBladeBlendLuminousDepthWrite(ResourceLocation p_228638_0_) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                .setOutputState(RenderStateShard.PARTICLES_TARGET)
                .setTextureState(new RenderStateShard.TextureStateShard(p_228638_0_, true, false))
                .setTransparencyState(LIGHTNING_ADDITIVE_TRANSPARENCY)
                //.setDiffuseLightingState(RenderStateShard.NO_DIFFUSE_LIGHTING)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                //.overlay(OVERLAY_ENABLED)
                .setWriteMaskState(COLOR_DEPTH_WRITE)
                .createCompositeState(false);
        return RenderType.create("slashblade_blend_luminous_depth_write", WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL, VertexFormat.Mode.TRIANGLES, 256, true, false, state);
    }


    protected static final RenderStateShard.TransparencyStateShard LIGHTNING_REVERSE_TRANSPARENCY = new RenderStateShard.TransparencyStateShard("lightning_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(
                GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE
                , GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
        RenderSystem.blendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);
    }, () -> {
        RenderSystem.blendEquation(GL14.GL_FUNC_ADD);
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });

    public static RenderType getSlashBladeBlendReverseLuminous(ResourceLocation p_228638_0_) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                .setOutputState(PARTICLES_TARGET)
                .setTextureState(new RenderStateShard.TextureStateShard(p_228638_0_, true, false))
                .setTransparencyState(LIGHTNING_REVERSE_TRANSPARENCY)
                //.setDiffuseLightingState(RenderStateShard.NO_DIFFUSE_LIGHTING)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                //.overlay(OVERLAY_ENABLED)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);
        return RenderType.create("slashblade_blend_reverse_luminous", WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL, VertexFormat.Mode.TRIANGLES, 256, true, false, state);
    }



    public static RenderType getPlacePreviewBlendLuminous(ResourceLocation p_228638_0_) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(POSITION_COLOR_TEX_LIGHTMAP_SHADER)
                .setOutputState(PARTICLES_TARGET)
                .setTextureState(new RenderStateShard.TextureStateShard(p_228638_0_, true, false))
                .setTransparencyState(LIGHTNING_ADDITIVE_TRANSPARENCY)
                //.setDiffuseLightingState(RenderStateShard.NO_DIFFUSE_LIGHTING)
                .setLightmapState(RenderStateShard.LIGHTMAP)
                //.overlay(OVERLAY_ENABLED)
                .setWriteMaskState(COLOR_WRITE)
                .createCompositeState(false);
        return RenderType.create("placepreview_blend_luminous", DefaultVertexFormat.BLOCK, VertexFormat.Mode.QUADS, 256, true, false, state);
    }
}
