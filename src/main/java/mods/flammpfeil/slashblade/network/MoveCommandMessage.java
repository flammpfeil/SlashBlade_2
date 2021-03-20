package mods.flammpfeil.slashblade.network;

import mods.flammpfeil.slashblade.ability.LockOnManager;
import mods.flammpfeil.slashblade.ability.SummonedSwordArts;
import mods.flammpfeil.slashblade.capability.inputstate.CapabilityInputState;
import mods.flammpfeil.slashblade.event.InputCommandEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.EnumSetConverter;
import mods.flammpfeil.slashblade.util.InputCommand;
import mods.flammpfeil.slashblade.util.TargetSelector;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.EnumSet;
import java.util.function.Supplier;

public class MoveCommandMessage {
    public int command;


    public MoveCommandMessage(){}

    static public MoveCommandMessage decode(PacketBuffer buf) {
        MoveCommandMessage msg = new MoveCommandMessage();
        msg.command = buf.readInt();
        return msg;
    }

    static public void encode(MoveCommandMessage msg, PacketBuffer buf) {
        buf.writeInt(msg.command);
    }

    static public void handle(MoveCommandMessage msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // Work that needs to be threadsafe (most work)
            ServerPlayerEntity sender = ctx.get().getSender(); // the client that sent this packet
            // do stuff
            ItemStack stack = sender.getHeldItem(Hand.MAIN_HAND);
            if (stack.isEmpty()) return;
            if (!(stack.getItem() instanceof ItemSlashBlade)) return;

            sender.getCapability(CapabilityInputState.INPUT_STATE).ifPresent((state)->{
                EnumSet<InputCommand> old = state.getCommands().clone();

                state.getCommands().clear();
                state.getCommands().addAll(
                        EnumSetConverter.convertToEnumSet(InputCommand.class, InputCommand.values(),msg.command));

                EnumSet<InputCommand> current = state.getCommands().clone();

                InputCommandEvent.onInputChange(sender, old, current);
                //todo: quick turnも実装したい
            });
        });
        ctx.get().setPacketHandled(true);
    }
}