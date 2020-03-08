package mods.flammpfeil.slashblade.client.renderer.entity;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.entity.EntityAbstractSummonedSword;
import mods.flammpfeil.slashblade.entity.EntityJudgementCut;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;

import javax.annotation.Nullable;
import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class JudgementCutRenderer<T extends EntityJudgementCut> extends EntityRenderer<T> {

    static private final ResourceLocation modelLocation = new ResourceLocation(SlashBlade.modid,"model/util/slashdim.obj");
    static private final ResourceLocation textureLocation = new ResourceLocation(SlashBlade.modid,"model/util/slashdim.png");

    @Nullable
    @Override
    protected ResourceLocation getEntityTexture(T entity) {
        return textureLocation;
    }

    public JudgementCutRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public boolean isMultipass() {
        return true;
    }

    @Override
    public void renderMultipass(T entity, double x, double y, double z, float entityYaw, float partialTicks) {
        this.bindEntityTexture(entity);

        /*
        float[] colf = (new Color(entity.getColor())).getColorComponents(null);
        GlStateManager.color4f(colf[0],colf[1],colf[2],1.0f);
        */
        //GlStateManager.color4f(0.5F, 0.5F, 0.5F, 1.0F);

        GlStateManager.pushMatrix();
        GlStateManager.disableLighting();
        GlStateManager.enableRescaleNormal();
        GlStateManager.translatef((float)x, (float)y, (float)z);
        GlStateManager.rotatef(MathHelper.lerp(partialTicks, entity.prevRotationYaw, entity.rotationYaw) - 90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotatef(MathHelper.lerp(partialTicks, entity.prevRotationPitch, entity.rotationPitch), 0.0F, 0.0F, 1.0F);


        {
            GlStateManager.shadeModel(GL11.GL_SMOOTH);

            GlStateManager.enableBlend();
            GlStateManager.disableAlphaTest();
            GlStateManager.disableCull();
            GlStateManager.blendFunc(GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
            //GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            if (!entity.isInvisible()) {
                GlStateManager.depthMask(false);
            } else {
                GlStateManager.depthMask(true);
            }


            int i = 61680;
            int j = i % 65536;
            int k = i / 65536;
            GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, (float) j, (float) k);
            GameRenderer gamerenderer = Minecraft.getInstance().gameRenderer;
            gamerenderer.setupFogColor(true);

            {
                if (this.renderOutlines) {
                    GlStateManager.enableColorMaterial();
                    GlStateManager.setupSolidRenderingTextureCombine( this.getTeamColor(entity));
                }

                WavefrontObject model = BladeModelManager.getInstance().getModel(modelLocation);

                {
                    int color = entity.getColor();
                    int lifetime = entity.getLifetime();

                    double deathTime = lifetime;
                    double baseAlpha = Math.sin(Math.PI * 0.5 * (Math.min(deathTime, (lifetime - (entity.ticksExisted) - partialTicks)) / deathTime));
                    int seed = entity.getSeed();


                    /*
                    float rotParTicks = 40.0f;
                    float rot = ((entity.ticksExisted % rotParTicks) / rotParTicks) * 360.f + partialTicks * (360.0f / rotParTicks);
                    GL11.glRotatef(rot, 0, 1, 0);
                    */

                    GL11.glRotatef(seed, 0, 1, 0);



                    float scale = 0.01f;
                    GL11.glScalef(scale, scale, scale);

                    Color col = new Color(color);
                    float[] hsb = Color.RGBtoHSB(col.getRed(),col.getGreen(),col.getBlue(), null);
                    col = Color.getHSBColor(0.5f + hsb[0],hsb[1], 0.2f/*hsb[2]*/);
                    float[] colf = col.getColorComponents(null);
                    GlStateManager.color4f(colf[0],colf[1],colf[2],(float)(20.4 * baseAlpha));

                    GlStateManager.blendFuncSeparate(
                            GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE
                            , GlStateManager.SourceFactor.ONE, GlStateManager.DestFactor.ONE);
                    GlStateManager.blendEquation(GL14.GL_FUNC_REVERSE_SUBTRACT);

                    GL11.glPushMatrix();
                    for(int l=0; l<5; l++){
                        GL11.glScaled(0.95, 0.95, 0.95);
                        model.renderPart("base");
                    }
                    GL11.glPopMatrix();


                    int loop = 3;
                    for(int l=0; l<loop; l++) {
                        GL11.glPushMatrix();
                        float ticks = 15;
                        float wave = (entity.ticksExisted + (ticks / (float)loop * l) + partialTicks) % ticks;
                        double waveScale = 1.0 + 0.03 * wave;
                        GL11.glScaled(waveScale, waveScale, waveScale);

                        GlStateManager.color4f(colf[0],colf[1],colf[2],(float)(0.55 * ((ticks - wave) / ticks)));
                        model.renderPart("base");
                        GL11.glPopMatrix();
                    }

                    GlStateManager.blendEquation(GL14.GL_FUNC_ADD);

                    col = new Color(color);
                    colf = col.getColorComponents(null);

                    int windCount = 5;
                    for(int l = 0; l < windCount; l++){
                        GL11.glPushMatrix();

                        GL11.glRotated((360.0 / windCount) * l, 1, 0, 0);
                        GL11.glRotated(30.0f , 0, 1, 0);

                        double rotWind = 360.0 / 20.0;

                        double offsetBase = 7;

                        double offset = l * offsetBase;

                        double motionLen = offsetBase * (windCount - 1);

                        double ticks = entity.ticksExisted + partialTicks + seed;
                        double offsetTicks = ticks + offset;
                        double progress = (offsetTicks % motionLen) / motionLen;

                        double rad = (Math.PI) * 2.0;
                        rad *= progress;

                        GlStateManager.color4f(colf[0],colf[1],colf[2],(float)Math.sin(rad));

                        double windScale = 0.4 + progress;
                        GL11.glScaled(windScale,windScale,windScale);

                        GL11.glRotated(rotWind * offsetTicks, 0, 0, 1);
                        model.renderPart("wind");

                        GL11.glPopMatrix();
                    }
                }

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
            GlStateManager.enableCull();
            GlStateManager.disableBlend();
            GlStateManager.enableAlphaTest();
            GlStateManager.shadeModel(GL11.GL_FLAT);
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