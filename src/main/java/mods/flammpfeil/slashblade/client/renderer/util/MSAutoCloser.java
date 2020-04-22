package mods.flammpfeil.slashblade.client.renderer.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;

public class MSAutoCloser implements AutoCloseable{

    static public MSAutoCloser pushMatrix(MatrixStack ms){
        return new MSAutoCloser(ms);
    }

    MatrixStack ms;

    MSAutoCloser(MatrixStack ms){
        this.ms = ms;
        this.ms.push();
    }

    @Override
    public void close() {
        this.ms.pop();
    }
}
