package mods.flammpfeil.slashblade.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mods.flammpfeil.slashblade.event.AnvilCraftingRecipe;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(GuiGraphics.class)
public class MixinItemRenderer {

    @Inject(at = @At("RETURN")
            , method="renderItem(Lnet/minecraft/world/entity/LivingEntity;Lnet/minecraft/world/level/Level;Lnet/minecraft/world/item/ItemStack;IIII)V"
            , cancellable = false
            , remap = true)
    public void renderItem(@Nullable LivingEntity p_282619_, @Nullable Level p_281754_, ItemStack stack, int p_281271_, int p_282210_, int p_283260_, int p_281995_, CallbackInfo callback) {
        AnvilCraftingRecipe recipe = AnvilCraftingRecipe.getRecipe(stack);
        if(recipe == null) return;

        ItemStack result = recipe.getResult();
        BakedModel bakedmodel = Minecraft.getInstance().getItemRenderer().getModel(result, p_281754_, p_282619_, p_283260_);

        pose().pushPose();

        int offsetX = -4;
        int offsetY = 5;

        pose().translate((float)(p_281271_ + offsetX + 8), (float)(p_282210_ + offsetY + 8), (float)(150 + (bakedmodel.isGui3d() ? p_281995_ : 0)));

        pose().scale(0.375f,0.375f,0.375f);
        pose().translate(0,0,200);

        pose().translate(-(float)(p_281271_ + offsetX + 8), -(float)(p_282210_ + offsetY + 8), -(float)(150 + (bakedmodel.isGui3d() ? p_281995_ : 0)));

        renderItem(result,p_281271_ + offsetX,p_282210_ + offsetY);

        pose().popPose();
    }

    @Shadow
    public void renderItem(ItemStack p_281978_, int p_282647_, int p_281944_)  {
        throw new IllegalStateException("Mixin failed to shadow readBoolean()");
    }

    @Shadow
    public PoseStack pose() {
        throw new IllegalStateException("Mixin failed to shadow readBoolean()");
    }
}
