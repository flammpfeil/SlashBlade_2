package mods.flammpfeil.slashblade.capability.concentrationrank;

import mods.flammpfeil.slashblade.util.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.core.Direction;
import net.minecraftforge.common.capabilities.*;

import javax.annotation.Nullable;

public class CapabilityConcentrationRank {

    public static final Capability<IConcentrationRank> RANK_POINT = CapabilityManager.get(new CapabilityToken<>(){});

    public static void register(RegisterCapabilitiesEvent event)
    {
        event.register(IConcentrationRank.class);
    }
}
