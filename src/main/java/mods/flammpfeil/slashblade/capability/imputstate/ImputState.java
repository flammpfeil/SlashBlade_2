package mods.flammpfeil.slashblade.capability.imputstate;

import mods.flammpfeil.slashblade.util.ImputCommand;

import java.util.EnumSet;

public class ImputState implements IImputState {

    EnumSet<ImputCommand> commands = EnumSet.noneOf(ImputCommand.class);

    @Override
    public EnumSet<ImputCommand> getCommands() {
        return commands;
    }
}
