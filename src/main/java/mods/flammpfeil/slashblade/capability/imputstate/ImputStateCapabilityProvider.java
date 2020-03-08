package mods.flammpfeil.slashblade.capability.imputstate;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ImputStateCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundNBT> {

    @CapabilityInject(IImputState.class)
    public static Capability<IImputState> IMPUT_STATE = null;

    protected LazyOptional<IImputState> state = LazyOptional.of(IMPUT_STATE::getDefaultInstance);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return IMPUT_STATE.orEmpty(cap, state);
    }

    static final String tagState = "MobEffect";

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT baseTag = new CompoundNBT();

        state.ifPresent(state -> baseTag.put(tagState , IMPUT_STATE.writeNBT(state, null)));

        return baseTag;
    }

    @Override
    public void deserializeNBT(CompoundNBT baseTag) {
        state.ifPresent(state -> IMPUT_STATE.readNBT(state, null, baseTag.get(tagState)));
    }
}
