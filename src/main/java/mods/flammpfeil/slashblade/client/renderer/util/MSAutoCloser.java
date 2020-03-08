package mods.flammpfeil.slashblade.client.renderer.util;

import com.mojang.blaze3d.platform.GlStateManager;

public class MSAutoCloser implements AutoCloseable{

    static public MSAutoCloser pushMatrix(){
        return new MSAutoCloser();
    }

    MSAutoCloser(){
        GlStateManager.pushMatrix();
    }

    @Override
    public void close() {
        GlStateManager.popMatrix();
    }
}
