package mods.flammpfeil.slashblade.capability.imputstate;

import mods.flammpfeil.slashblade.util.EnumSetConverter;
import mods.flammpfeil.slashblade.util.ImputCommand;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class CapabilityImputState {

    @CapabilityInject(IImputState.class)
    public static Capability<IImputState> IMPUT_STATE = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IImputState.class, new Capability.IStorage<IImputState>(){

            static final String KEY = "Command";

            @Nullable
            @Override
            public INBT writeNBT(Capability<IImputState> capability, IImputState instance, Direction side) {
                CompoundNBT nbt = new CompoundNBT();

                nbt.putInt(KEY, EnumSetConverter.convertToInt(instance.getCommands()));

                return nbt;
            }

            @Override
            public void readNBT(Capability<IImputState> capability, IImputState instance, Direction side, INBT nbt) {
                CompoundNBT tags = (CompoundNBT) nbt;

                instance.getCommands().addAll(
                        EnumSetConverter.convertToEnumSet(ImputCommand.class, ImputCommand.values(), tags.getInt(KEY)));
            }
        }, ()-> new ImputState());
    }
}
