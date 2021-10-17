package mods.flammpfeil.slashblade.capability.inputstate;

import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumSet;

public interface IInputState {
    EnumSet<InputCommand> getCommands();

    default EnumSet<InputCommand> getCommands(LivingEntity owner){
        EnumSet<InputCommand> commands = getCommands().clone();

        if(owner.isOnGround()) {
            commands.add(InputCommand.ON_GROUND);
            //commands.remove(InputCommand.ON_AIR);
        }else {
            commands.add(InputCommand.ON_AIR);
            //commands.remove(InputCommand.ON_GROUND);
        }
        return commands;
    }
}
