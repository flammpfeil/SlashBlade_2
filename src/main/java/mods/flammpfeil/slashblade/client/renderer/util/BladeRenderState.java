package mods.flammpfeil.slashblade.client.renderer.util;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mojang.blaze3d.vertex.VertexBuilderUtils;
import mods.flammpfeil.slashblade.client.renderer.model.obj.Face;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.event.client.RenderOverrideEvent;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.renderer.vertex.VertexFormatElement;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import java.awt.*;
import java.util.function.Function;

public class BladeRenderState extends RenderState{

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

    static public void renderOverrided(ItemStack stack, WavefrontObject model, String target, ResourceLocation texture, MatrixStack  matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn){
        renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn, packedLightIn, BladeRenderState::getSlashBladeBlend, true);
    }

    static public void renderOverridedColorWrite(ItemStack stack, WavefrontObject model, String target, ResourceLocation texture, MatrixStack  matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn){
        renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn, packedLightIn, BladeRenderState::getSlashBladeBlendColorWrite, true);
    }

    static public void renderOverridedLuminous(ItemStack stack, WavefrontObject model, String target, ResourceLocation texture, MatrixStack  matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn){
        renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn, MAX_LIGHT, BladeRenderState::getSlashBladeBlendLuminous, false);
    }

    static public void renderOverridedReverseLuminous(ItemStack stack, WavefrontObject model, String target, ResourceLocation texture, MatrixStack  matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn){
        renderOverrided(stack, model, target, texture, matrixStackIn, bufferIn, MAX_LIGHT, BladeRenderState::getSlashBladeBlendReverseLuminous, false);
    }


    static public void renderOverrided(ItemStack stack, WavefrontObject model, String target, ResourceLocation texture, MatrixStack  matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn, Function<ResourceLocation,RenderType> getRenderType, boolean enableEffect){
        RenderOverrideEvent event
                = RenderOverrideEvent.onRenderOverride(stack, model, target, texture, matrixStackIn, bufferIn);

        if(event.isCanceled()) return;

        RenderType rt = getRenderType.apply(event.getTexture());//getSlashBladeBlendLuminous(event.getTexture());
        IVertexBuilder vb = bufferIn.getBuffer(rt);

        Face.setCol(col);
        Face.setLightMap(packedLightIn);
        Face.setMatrix(matrixStackIn);
        event.getModel().tessellateOnly(vb, event.getTarget());

        if(stack.hasEffect()){
            vb = bufferIn.getBuffer(BLADE_GLINT);
            event.getModel().tessellateOnly(vb, event.getTarget());
        }

        Face.resetMatrix();
        Face.resetLightMap();
        Face.resetCol();

        Face.resetAlphaOverride();
        Face.resetUvOperator();

        resetCol();
    }

    public static IVertexBuilder getBuffer(IRenderTypeBuffer bufferIn, RenderType renderTypeIn, boolean glintIn) {
        return null;
    }

    public static final VertexFormat POSITION_TEX = new VertexFormat(ImmutableList.<VertexFormatElement>builder().add(DefaultVertexFormats.POSITION_3F).add(DefaultVertexFormats.TEX_2F).build());
    public static final RenderType BLADE_GLINT = RenderType.makeType("blade_glint",POSITION_TEX, GL11.GL_TRIANGLES, 256,RenderType.State.getBuilder().texture(new TextureState(ItemRenderer.RES_ITEM_GLINT, true, false)).writeMask(COLOR_WRITE).cull(CULL_DISABLED).depthTest(DEPTH_EQUAL).transparency(GLINT_TRANSPARENCY).texturing(ENTITY_GLINT_TEXTURING).build(false));


    public static RenderType getSlashBladeBlend(ResourceLocation p_228638_0_) {
        RenderType.State state = RenderType.State.getBuilder()
                .texture(new RenderState.TextureState(p_228638_0_, false, false))
                .transparency(TRANSLUCENT_TRANSPARENCY)
                .diffuseLighting(DIFFUSE_LIGHTING_ENABLED)
                .alpha(DEFAULT_ALPHA)
                .lightmap(LIGHTMAP_ENABLED)
                //.overlay(OVERLAY_ENABLED)
                .shadeModel(SHADE_ENABLED)
                .build(true);
        return RenderType.makeType("slashblade_blend", WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL, GL11.GL_TRIANGLES, 256, true, false, state);
    }

    public static RenderType getSlashBladeBlendColorWrite(ResourceLocation p_228638_0_) {
        RenderType.State state = RenderType.State.getBuilder()
                .texture(new RenderState.TextureState(p_228638_0_, false, false))
                .transparency(TRANSLUCENT_TRANSPARENCY)
                .diffuseLighting(RenderState.DIFFUSE_LIGHTING_DISABLED)
                .alpha(DEFAULT_ALPHA)
                .lightmap(LIGHTMAP_ENABLED)
                //.overlay(OVERLAY_ENABLED)
                .shadeModel(SHADE_ENABLED)
                .writeMask(COLOR_WRITE)
                .build(true);
        return RenderType.makeType("slashblade_blend", WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL, GL11.GL_TRIANGLES, 256, true, false, state);
    }



    protected static final RenderState.TransparencyState LIGHTNING_ADDITIVE_TRANSPARENCY = new RenderState.TransparencyState("lightning_transparency", () -> {
        RenderSystem.enableBlend();
        RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ZERO);
    }, () -> {
        RenderSystem.disableBlend();
        RenderSystem.defaultBlendFunc();
    });

    public static RenderType getSlashBladeBlendLuminous(ResourceLocation p_228638_0_) {
        RenderType.State state = RenderType.State.getBuilder()
                .texture(new RenderState.TextureState(p_228638_0_, true, false))
                .transparency(LIGHTNING_ADDITIVE_TRANSPARENCY)
                .diffuseLighting(RenderState.DIFFUSE_LIGHTING_DISABLED)
                .alpha(DEFAULT_ALPHA)
                .lightmap(RenderState.LIGHTMAP_ENABLED)
                //.overlay(OVERLAY_ENABLED)
                .shadeModel(SHADE_ENABLED)
                .writeMask(COLOR_WRITE)
                .build(false);
        return RenderType.makeType("slashblade_blend_luminous", WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL, GL11.GL_TRIANGLES, 256, true, false, state);
    }


    protected static final RenderState.TransparencyState LIGHTNING_REVERSE_TRANSPARENCY = new RenderState.TransparencyState("lightning_transparency", () -> {
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
        RenderType.State state = RenderType.State.getBuilder()
                .texture(new RenderState.TextureState(p_228638_0_, true, false))
                .transparency(LIGHTNING_REVERSE_TRANSPARENCY)
                .diffuseLighting(RenderState.DIFFUSE_LIGHTING_DISABLED)
                .alpha(DEFAULT_ALPHA)
                .lightmap(RenderState.LIGHTMAP_ENABLED)
                //.overlay(OVERLAY_ENABLED)
                .shadeModel(SHADE_ENABLED)
                .writeMask(COLOR_WRITE)
                .build(false);
        return RenderType.makeType("slashblade_blend_luminous", WavefrontObject.POSITION_TEX_LMAP_COL_NORMAL, GL11.GL_TRIANGLES, 256, true, false, state);
    }



    public static RenderType getPlacePreviewBlendLuminous(ResourceLocation p_228638_0_) {
        RenderType.State state = RenderType.State.getBuilder()
                .texture(new RenderState.TextureState(p_228638_0_, true, false))
                .transparency(LIGHTNING_ADDITIVE_TRANSPARENCY)
                .diffuseLighting(RenderState.DIFFUSE_LIGHTING_DISABLED)
                .alpha(DEFAULT_ALPHA)
                .lightmap(RenderState.LIGHTMAP_ENABLED)
                //.overlay(OVERLAY_ENABLED)
                .shadeModel(SHADE_ENABLED)
                .writeMask(COLOR_WRITE)
                .build(false);
        return RenderType.makeType("placepreview_blend_luminous", DefaultVertexFormats.BLOCK, GL11.GL_QUADS, 256, true, false, state);
    }
}
