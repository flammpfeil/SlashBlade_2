package mods.flammpfeil.slashblade.client.renderer.util;

import com.mojang.blaze3d.platform.GLX;
import com.mojang.blaze3d.platform.GlStateManager;

public class LightLevelMaximizer implements AutoCloseable{

    float lastBrightnessX;
    float lastBrightnessY;
    static final float j = (float)(15728880 % 65536);
    static final float k = (float)(15728880 / 65536);

    static public LightLevelMaximizer maximize(){
        return new LightLevelMaximizer();
    }

    LightLevelMaximizer(){
        lastBrightnessX = GLX.lastBrightnessX;
        lastBrightnessY = GLX.lastBrightnessY;
        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, j, k);
    }

    @Override
    public void close() {

        GLX.glMultiTexCoord2f(GLX.GL_TEXTURE1, lastBrightnessX, lastBrightnessY);
    }
}
