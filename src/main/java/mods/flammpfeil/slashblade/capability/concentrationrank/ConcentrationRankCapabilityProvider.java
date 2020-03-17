package mods.flammpfeil.slashblade.capability.concentrationrank;

import mods.flammpfeil.slashblade.capability.imputstate.IImputState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ConcentrationRankCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundNBT> {

    @CapabilityInject(IConcentrationRank.class)
    public static Capability<IConcentrationRank> RANK_POINT = null;

    protected LazyOptional<IConcentrationRank> state = LazyOptional.of(RANK_POINT::getDefaultInstance);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return RANK_POINT.orEmpty(cap, state);
    }

    static final String tagState = "rawPoint";

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT baseTag = new CompoundNBT();

        state.ifPresent(state -> baseTag.put(tagState , RANK_POINT.writeNBT(state, null)));

        return baseTag;
    }

    @Override
    public void deserializeNBT(CompoundNBT baseTag) {
        state.ifPresent(state -> RANK_POINT.readNBT(state, null, baseTag.getCompound(tagState)));
    }
}
