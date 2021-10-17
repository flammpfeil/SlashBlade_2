package mods.flammpfeil.slashblade.mixin;

import mods.flammpfeil.slashblade.client.renderer.layers.LayerMainBlade;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public class MixinPlayerRenderer {


    @Inject(at = @At("TAIL")
            , method="<init>(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;Z)V")
    public void extraLayers(EntityRendererProvider.Context p_174557_, boolean p_174558_, CallbackInfo callback) {
        ((PlayerRenderer)(Object)this).addLayer(new LayerMainBlade(((PlayerRenderer)(Object)this)));
    }

    PlayerRenderer self(){
        return ((PlayerRenderer)(Object)this);
    }
}
