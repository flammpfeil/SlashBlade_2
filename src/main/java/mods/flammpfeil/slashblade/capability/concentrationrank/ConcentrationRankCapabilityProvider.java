package mods.flammpfeil.slashblade.capability.concentrationrank;

import mods.flammpfeil.slashblade.util.NBTHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.Direction;
import net.minecraft.nbt.Tag;
import net.minecraftforge.common.capabilities.*;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConcentrationRankCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundTag> {

    public static final Capability<IConcentrationRank> RANK_POINT = CapabilityManager.get(new CapabilityToken<>(){});

    protected LazyOptional<IConcentrationRank> state = LazyOptional.of(()->new ConcentrationRank());

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return RANK_POINT.orEmpty(cap, state);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag baseTag = new CompoundTag();

        state.ifPresent(instance -> {
            NBTHelper.getNBTCoupler(baseTag)
                    .put("rawPoint", instance.getRawRankPoint())
                    .put("lastupdate", instance.getLastUpdate());
        });

        return baseTag;
    }

    @Override
    public void deserializeNBT(CompoundTag baseTag) {
        state.ifPresent(instance -> NBTHelper.getNBTCoupler((CompoundTag) baseTag)
                .get("rawPoint", instance::setRawRankPoint)
                .get("lastupdate", instance::setLastUpdte));
        ;
    }
}
