package mods.flammpfeil.slashblade.capability.inputstate;

import mods.flammpfeil.slashblade.event.Scheduler;
import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.world.entity.LivingEntity;

import java.util.EnumMap;
import java.util.EnumSet;

public interface IInputState {
    EnumSet<InputCommand> getCommands();

    Scheduler getScheduler();

    EnumMap<InputCommand,Long> getLastPressTimes();

    default long getLastPressTime(InputCommand command){
        if(this.getLastPressTimes().containsKey(command)){
            return this.getLastPressTimes().get(command);
        }else{
            return -1;
        }
    }

    default EnumSet<InputCommand> getCommands(LivingEntity owner){
        EnumSet<InputCommand> commands = getCommands().clone();

        if(owner.onGround()) {
            commands.add(InputCommand.ON_GROUND);
            //commands.remove(InputCommand.ON_AIR);
        }else {
            commands.add(InputCommand.ON_AIR);
            //commands.remove(InputCommand.ON_GROUND);
        }
        return commands;
    }
}
