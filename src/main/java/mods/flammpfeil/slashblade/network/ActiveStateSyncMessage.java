package mods.flammpfeil.slashblade.network;

import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.EnumSetConverter;
import mods.flammpfeil.slashblade.util.ImputCommand;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class ActiveStateSyncMessage {
    public CompoundNBT activeTag;
    public int id;


    public ActiveStateSyncMessage(){}

    static public ActiveStateSyncMessage decode(PacketBuffer buf) {
        ActiveStateSyncMessage msg = new ActiveStateSyncMessage();
        msg.id = buf.readInt();
        msg.activeTag = buf.readCompoundTag();
        return msg;
    }

    static public void encode(ActiveStateSyncMessage msg, PacketBuffer buf) {
        buf.writeInt(msg.id);
        buf.writeCompoundTag(msg.activeTag);
    }

    static public void handle(ActiveStateSyncMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {

            if(!msg.activeTag.hasUniqueId("BladeUniqueId")) return;

            // Work that needs to be threadsafe (most work)
            ServerPlayerEntity sender = ctx.get().getSender(); // the client that sent this packet

            // do stuff
            Entity target = Minecraft.getInstance().world.getEntityByID(msg.id);

            if(target instanceof LivingEntity){
                ItemStack stack = ((LivingEntity)target).getHeldItem(Hand.MAIN_HAND);
                if (stack.isEmpty()) return;
                if (!(stack.getItem() instanceof ItemSlashBlade)) return;

                stack.getCapability(ItemSlashBlade.BLADESTATE)
                        .filter((state)->state.getUniqueId().equals(msg.activeTag.getUniqueId("BladeUniqueId")))
                        .ifPresent((state)->state.setActiveState(msg.activeTag));
            }
        });
        ctx.get().setPacketHandled(true);
    }
}