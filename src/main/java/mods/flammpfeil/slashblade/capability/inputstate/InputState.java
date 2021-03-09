package mods.flammpfeil.slashblade.capability.inputstate;

import mods.flammpfeil.slashblade.util.InputCommand;

import java.util.EnumSet;

public class InputState implements IInputState {

    EnumSet<InputCommand> commands = EnumSet.noneOf(InputCommand.class);

    @Override
    public EnumSet<InputCommand> getCommands() {
        return commands;
    }
}
