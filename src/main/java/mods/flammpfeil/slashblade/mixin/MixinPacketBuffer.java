package mods.flammpfeil.slashblade.mixin;


import io.netty.buffer.ByteBuf;
import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;

@Mixin(PacketBuffer.class)
public class MixinPacketBuffer{

    @Inject(at = @At("HEAD")
            , method="writeItemStack(Lnet/minecraft/item/ItemStack;Z)Lnet/minecraft/network/PacketBuffer;"
            , cancellable = true
            , remap = false)
    public void writeItemStack(ItemStack stack, boolean limitedTag, CallbackInfoReturnable<PacketBuffer> callback) {
        if (stack.isEmpty()) {
            this.writeBoolean(false);
        } else {
            this.writeBoolean(true);
            Item item = stack.getItem();
            this.writeVarInt(Item.getIdFromItem(item));
            this.writeByte(stack.getCount());

            CompoundNBT compoundnbt = null;
            if (item.isDamageable(stack) || item.shouldSyncTag()) {
                compoundnbt = limitedTag ? stack.getShareTag() : stack.getTag();
            }

            this.writeCompoundTag(compoundnbt);

            //add
            CompoundNBT completeNbt =new CompoundNBT();
            stack.write(completeNbt);
            CompoundNBT caps = completeNbt.contains("ForgeCaps") ? completeNbt.getCompound("ForgeCaps") : null;
            this.writeCompoundTag(caps);
        }

        callback.setReturnValue(PacketBuffer.class.cast(this));
        callback.cancel();
    }

    @Shadow
    public ByteBuf writeBoolean(boolean b) {
        throw new IllegalStateException("Mixin failed to shadow getItem()");
    }
    @Shadow
    public PacketBuffer writeVarInt(int i) {
        throw new IllegalStateException("Mixin failed to shadow getItem()");
    }
    @Shadow
    public ByteBuf writeByte(int i) {
        throw new IllegalStateException("Mixin failed to shadow getItem()");
    }
    @Shadow
    public PacketBuffer writeCompoundTag(@Nullable CompoundNBT nbt) {
        throw new IllegalStateException("Mixin failed to shadow getItem()");
    }


    @Inject(at=@At("HEAD")
        , method = "readItemStack()Lnet/minecraft/item/ItemStack;"
        , cancellable = true)
    public void readItemStack(CallbackInfoReturnable<ItemStack> callback) {
        ItemStack result;
        if (!this.readBoolean()) {
            result = ItemStack.EMPTY;
        } else {
            int i = this.readVarInt();
            int j = this.readByte();

            CompoundNBT shareTag = this.readCompoundTag();
            CompoundNBT capsTag = this.readCompoundTag();

            result = new ItemStack(Item.getItemById(i), j, capsTag);
            result.readShareTag(shareTag);
        }

        callback.setReturnValue(result);
        callback.cancel();
    }

    @Shadow
    public boolean readBoolean() {
        throw new IllegalStateException("Mixin failed to shadow getItem()");
    }
    @Shadow
    public int readVarInt() {
        throw new IllegalStateException("Mixin failed to shadow getItem()");
    }
    @Shadow
    public byte readByte() {
        throw new IllegalStateException("Mixin failed to shadow getItem()");
    }
    @Shadow
    public CompoundNBT readCompoundTag() {
        throw new IllegalStateException("Mixin failed to shadow getItem()");
    }
}
