package mods.flammpfeil.slashblade.client.renderer.util;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.platform.GlStateManager;

public class MSAutoCloser implements AutoCloseable{

    static public MSAutoCloser pushMatrix(PoseStack ms){
        return new MSAutoCloser(ms);
    }

    PoseStack ms;

    MSAutoCloser(PoseStack ms){
        this.ms = ms;
        this.ms.pushPose();
    }

    @Override
    public void close() {
        this.ms.popPose();
    }
}
