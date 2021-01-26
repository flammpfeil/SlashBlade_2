package mods.flammpfeil.slashblade.capability.imputstate;

import mods.flammpfeil.slashblade.util.ImputCommand;
import net.minecraft.entity.LivingEntity;

import javax.swing.text.html.parser.Entity;
import java.util.EnumSet;

public interface IImputState {
    EnumSet<ImputCommand> getCommands();

    default EnumSet<ImputCommand> getCommands(LivingEntity owner){
        EnumSet<ImputCommand> commands = getCommands().clone();

        if(owner.isOnGround()) {
            commands.add(ImputCommand.ON_GROUND);
            //commands.remove(ImputCommand.ON_AIR);
        }else {
            commands.add(ImputCommand.ON_AIR);
            //commands.remove(ImputCommand.ON_GROUND);
        }
        return commands;
    }
}
