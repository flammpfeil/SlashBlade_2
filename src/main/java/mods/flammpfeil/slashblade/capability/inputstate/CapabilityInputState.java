package mods.flammpfeil.slashblade.capability.inputstate;

import mods.flammpfeil.slashblade.util.EnumSetConverter;
import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class CapabilityInputState {

    @CapabilityInject(IInputState.class)
    public static Capability<IInputState> INPUT_STATE = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IInputState.class, new Capability.IStorage<IInputState>(){

            static final String KEY = "Command";

            @Nullable
            @Override
            public INBT writeNBT(Capability<IInputState> capability, IInputState instance, Direction side) {
                CompoundNBT nbt = new CompoundNBT();

                nbt.putInt(KEY, EnumSetConverter.convertToInt(instance.getCommands()));

                return nbt;
            }

            @Override
            public void readNBT(Capability<IInputState> capability, IInputState instance, Direction side, INBT nbt) {
                CompoundNBT tags = (CompoundNBT) nbt;

                instance.getCommands().addAll(
                        EnumSetConverter.convertToEnumSet(InputCommand.class, InputCommand.values(), tags.getInt(KEY)));
            }
        }, ()-> new InputState());
    }
}
