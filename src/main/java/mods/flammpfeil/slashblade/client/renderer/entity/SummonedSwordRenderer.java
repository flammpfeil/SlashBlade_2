package mods.flammpfeil.slashblade.client.renderer.entity;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.entity.EntityAbstractSummonedSword;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL15;

import javax.annotation.Nullable;
import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class SummonedSwordRenderer<T extends EntityAbstractSummonedSword> extends EntityRenderer<T> {

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        return entity.getTextureLoc();
    }

    public SummonedSwordRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public boolean isMultipass() {
        return true;
    }

    @Override
    public void renderMultipass(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
        this.bindEntityTexture(entity);

        float[] colf = (new Color(entity.getColor())).getColorComponents(null);
        GlStateManager.color4f(colf[0],colf[1],colf[2],1.0f);
        //GlStateManager.color4f(0.5F, 0.5F, 0.5F, 1.0F);

        GlStateManager.pushMatrix();
        //GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.translatef((float)x, (float)y, (float)z);
        GlStateManager.rotatef(MathHelper.lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw) - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(MathHelper.lerp(partialTicks, entity.prevRotationPitch, entity.rotationPitch), 0.0F, 0.0F, 1.0F);


        {
            GlStateManager.enableBlend();
            GlStateManager.disableAlphaTest();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            //GlStateManager.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_SRC_COLOR, GlStateManager.DestFactor.DST_COLOR, GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            //GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            if (entity.isInvisible()) {
                GlStateManager.depthMask(false);
            } else {
                GlStateManager.depthMask(true);
            }

            int i = 61680;
            int j = i % 65536;
            int k = i / 65536;
            GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float) j, (float) k);
            //GlStateManager.color4f(1.0F, 1.0F, 1.0F, 1.0F);
            GameRenderer gamerenderer = Minecraft.getInstance().gameRenderer;
            gamerenderer.setupFogColor(true);

            {

                float scale = 0.0075f;
                GlStateManager.scalef(scale,scale,scale);
                GlStateManager.rotatef(90.0F, 0.0F, 1.0F, 0.0F);

                if (this.renderOutlines) {
                    GlStateManager.enableColorMaterial();
                    GlStateManager.setupSolidRenderingTextureCombine( this.getTeamColor(entity));
                }

                //GlStateManager.blendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);
                WavefrontObject model = BladeModelManager.getInstance().getModel(entity.getModelLoc());
                model.renderPart("ss");
                //GlStateManager.blendEquation(GL14.GL_FUNC_ADD);

                if (this.renderOutlines) {
                    GlStateManager.tearDownSolidRenderingTextureCombine();
                    GlStateManager.disableColorMaterial();
                }
            }

            gamerenderer.setupFogColor(false);

            i = entity.getBrightnessForRender();
            j = i % 65536;
            k = i / 65536;
            GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float)j, (float)k);

            GlStateManager.depthMask(true);
            GlStateManager.disableBlend();
            GlStateManager.enableAlphaTest();
        }

        GlStateManager.disableRescaleNormal();
        GlStateManager.enableLighting();
        GlStateManager.popMatrix();
    }

    public void doRender(T entity, double x, double y, double z, float entityYaw, float partialTicks) {

        if(this.renderOutlines){
            renderMultipass(entity, x, y, z, entityYaw, partialTicks);
        }

        super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    protected int getTeamColor(T entityIn) {
        ScorePlayerTeam scoreplayerteam = (ScorePlayerTeam)entityIn.getTeam();
        return scoreplayerteam != null && scoreplayerteam.getColor().getColor() != null ? scoreplayerteam.getColor().getColor() : entityIn.getColor();
    }
}