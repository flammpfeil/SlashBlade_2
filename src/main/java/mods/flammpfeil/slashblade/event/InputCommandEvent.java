package mods.flammpfeil.slashblade.event;

import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import java.util.EnumSet;

public class InputCommandEvent extends Event {

    public InputCommandEvent(ServerPlayerEntity player, EnumSet<InputCommand> old, EnumSet<InputCommand> current) {
        this.player = player;
        this.old = old;
        this.current = current;
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }
    public EnumSet<InputCommand> getOld() {
        return old;
    }
    public EnumSet<InputCommand> getCurrent() {
        return current;
    }

    ServerPlayerEntity player;
    EnumSet<InputCommand> old;
    EnumSet<InputCommand> current;


    public static InputCommandEvent onInputChange(ServerPlayerEntity player, EnumSet<InputCommand> old, EnumSet<InputCommand> current)
    {
        InputCommandEvent event = new InputCommandEvent(player, old, current);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }
}
