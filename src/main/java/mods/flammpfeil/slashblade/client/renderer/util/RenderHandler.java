package mods.flammpfeil.slashblade.client.renderer.util;

import com.mojang.blaze3d.platform.GlStateManager;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.event.client.RenderOverrideEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.util.LazyOptional;
import org.lwjgl.opengl.GL11;

public class RenderHandler {


    static public void renderOverrided(ItemStack stack, WavefrontObject model, String target, ResourceLocation texture){

        try {
            GlStateManager.pushMatrix();
            GlStateManager.pushLightingAttributes();

            RenderOverrideEvent event
                    = RenderOverrideEvent.onRenderOverride(stack, model, target, texture);

            if(event.isCanceled()) return;

            bindTexture(event.getTexture());

            GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

            event.getModel().renderOnly(event.getTarget());

            GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST );
            GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST );

        }finally{
            GlStateManager.popAttributes();
            GlStateManager.popMatrix();
        }
    }

    //private static final ResourceLocation empty = new ResourceLocation(SlashBlade.modid,"slashblade.png");
    static public void bindTexture(ResourceLocation loc){
        TextureManager tm = Minecraft.getInstance().getRenderManager().textureManager;
        tm.bindTexture(TextureManager.RESOURCE_LOCATION_EMPTY);
        tm.bindTexture(loc);
    }
}
