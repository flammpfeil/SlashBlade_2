package mods.flammpfeil.slashblade.capability.inputstate;

import com.google.common.collect.Maps;
import mods.flammpfeil.slashblade.event.Scheduler;
import mods.flammpfeil.slashblade.util.InputCommand;

import java.util.EnumMap;
import java.util.EnumSet;

public class InputState implements IInputState {

    EnumSet<InputCommand> commands = EnumSet.noneOf(InputCommand.class);
    Scheduler scheduler = new Scheduler();
    EnumMap<InputCommand,Long> lastPressTimes = Maps.newEnumMap(InputCommand.class);

    @Override
    public EnumSet<InputCommand> getCommands() {
        return commands;
    }

    @Override
    public Scheduler getScheduler() {
        return scheduler;
    }

    @Override
    public EnumMap<InputCommand, Long> getLastPressTimes() {
        return lastPressTimes;
    }


}
