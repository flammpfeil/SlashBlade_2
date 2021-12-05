package mods.flammpfeil.slashblade.mixin;


import io.netty.buffer.ByteBuf;
import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(FriendlyByteBuf.class)
public class MixinPacketBuffer{

    @Inject(at = @At("HEAD")
            , method="writeItemStack(Lnet/minecraft/world/item/ItemStack;Z)Lnet/minecraft/network/FriendlyByteBuf;"
            , cancellable = true
            , remap = false)
    public void writeItemStack(ItemStack stack, boolean limitedTag, CallbackInfoReturnable<FriendlyByteBuf> callback) {
        if (stack.isEmpty()) {
            this.writeBoolean(false);
        } else {
            this.writeBoolean(true);
            Item item = stack.getItem();
            this.writeVarInt(Item.getId(item));
            this.writeByte(stack.getCount());

            CompoundTag compoundnbt = null;
            if (item.isDamageable(stack) || item.shouldOverrideMultiplayerNbt()) {
                compoundnbt = limitedTag ? stack.getShareTag() : stack.getTag();
            }

            this.writeNbt(compoundnbt);

            //add
            CompoundTag completeNbt =new CompoundTag();
            stack.save(completeNbt);
            CompoundTag caps = completeNbt.contains("ForgeCaps") ? completeNbt.getCompound("ForgeCaps") : null;
            this.writeNbt(caps);
        }

        callback.setReturnValue(FriendlyByteBuf.class.cast(this));
        callback.cancel();
    }

    @Shadow
    public ByteBuf writeBoolean(boolean b) {
        throw new IllegalStateException("Mixin failed to shadow writeBoolean()");
    }
    @Shadow
    public FriendlyByteBuf writeVarInt(int i) {
        throw new IllegalStateException("Mixin failed to shadow writeVarInt()");
    }
    @Shadow
    public ByteBuf writeByte(int i) {
        throw new IllegalStateException("Mixin failed to shadow writeByte()");
    }
    @Shadow
    public FriendlyByteBuf writeNbt(@Nullable CompoundTag nbt) {
        throw new IllegalStateException("Mixin failed to shadow writeNbt()");
    }


    @Inject(at=@At("HEAD")
        , method = "readItem()Lnet/minecraft/world/item/ItemStack;"
        , cancellable = true
        , remap = true)
    public void readItemStack(CallbackInfoReturnable<ItemStack> callback) {
        ItemStack result;
        if (!this.readBoolean()) {
            result = ItemStack.EMPTY;
        } else {
            int i = this.readVarInt();
            int j = this.readByte();

            CompoundTag shareTag = this.readNbt();
            CompoundTag capsTag = this.readNbt();

            result = new ItemStack(Item.byId(i), j, capsTag);
            result.readShareTag(shareTag);
        }

        callback.setReturnValue(result);
        callback.cancel();
    }

    @Shadow
    public boolean readBoolean() {
        throw new IllegalStateException("Mixin failed to shadow readBoolean()");
    }
    @Shadow
    public int readVarInt() {
        throw new IllegalStateException("Mixin failed to shadow readVarInt()");
    }
    @Shadow
    public byte readByte() {
        throw new IllegalStateException("Mixin failed to shadow readByte()");
    }
    @Shadow
    public CompoundTag readNbt() {
        throw new IllegalStateException("Mixin failed to shadow readNbt()");
    }
}
