package mods.flammpfeil.slashblade.network;

import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class ActiveStateSyncMessage {
    public CompoundTag activeTag;
    public int id;


    public ActiveStateSyncMessage(){}

    static public ActiveStateSyncMessage decode(FriendlyByteBuf buf) {
        ActiveStateSyncMessage msg = new ActiveStateSyncMessage();
        msg.id = buf.readInt();
        msg.activeTag = buf.readNbt();
        return msg;
    }

    static public void encode(ActiveStateSyncMessage msg, FriendlyByteBuf buf) {
        buf.writeInt(msg.id);
        buf.writeNbt(msg.activeTag);
    }

    static public void handle(ActiveStateSyncMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {

            if(!msg.activeTag.hasUUID("BladeUniqueId")) return;

            // Work that needs to be threadsafe (most work)
            ServerPlayer sender = ctx.get().getSender(); // the client that sent this packet

            // do stuff
            Entity target = Minecraft.getInstance().level.getEntity(msg.id);

            if(target instanceof LivingEntity){
                ItemStack stack = ((LivingEntity)target).getItemInHand(InteractionHand.MAIN_HAND);
                if (stack.isEmpty()) return;
                if (!(stack.getItem() instanceof ItemSlashBlade)) return;

                stack.getCapability(ItemSlashBlade.BLADESTATE)
                        .filter((state)->state.getUniqueId().equals(msg.activeTag.getUUID("BladeUniqueId")))
                        .ifPresent((state)->state.setActiveState(msg.activeTag));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}