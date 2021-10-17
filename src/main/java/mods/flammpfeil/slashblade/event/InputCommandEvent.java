package mods.flammpfeil.slashblade.event;

import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import java.util.EnumSet;

public class InputCommandEvent extends Event {

    public InputCommandEvent(ServerPlayer player, EnumSet<InputCommand> old, EnumSet<InputCommand> current) {
        this.player = player;
        this.old = old;
        this.current = current;
    }

    public ServerPlayer getPlayer() {
        return player;
    }
    public EnumSet<InputCommand> getOld() {
        return old;
    }
    public EnumSet<InputCommand> getCurrent() {
        return current;
    }

    ServerPlayer player;
    EnumSet<InputCommand> old;
    EnumSet<InputCommand> current;


    public static InputCommandEvent onInputChange(ServerPlayer player, EnumSet<InputCommand> old, EnumSet<InputCommand> current)
    {
        InputCommandEvent event = new InputCommandEvent(player, old, current);
        MinecraftForge.EVENT_BUS.post(event);
        return event;
    }
}
