package mods.flammpfeil.slashblade.capability.mobeffect;

import mods.flammpfeil.slashblade.util.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.*;

import javax.annotation.Nullable;

public class CapabilityMobEffect {

    public static final Capability<IMobEffectState> MOB_EFFECT = CapabilityManager.get(new CapabilityToken<>(){});

    public static void register(RegisterCapabilitiesEvent event)
    {
        event.register(IMobEffectState.class);
    }
}
