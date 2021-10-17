package mods.flammpfeil.slashblade.capability.inputstate;

import mods.flammpfeil.slashblade.capability.mobeffect.IMobEffectState;
import mods.flammpfeil.slashblade.util.EnumSetConverter;
import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.*;

import javax.annotation.Nullable;

public class CapabilityInputState {

    public static final Capability<IInputState> INPUT_STATE = CapabilityManager.get(new CapabilityToken<>(){});

    public static void register(RegisterCapabilitiesEvent event)
    {
        event.register(IInputState.class);
    }
}
