package mods.flammpfeil.slashblade.capability.inputstate;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class InputStateCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundNBT> {

    @CapabilityInject(IInputState.class)
    public static Capability<IInputState> INPUT_STATE = null;

    protected LazyOptional<IInputState> state = LazyOptional.of(INPUT_STATE::getDefaultInstance);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return INPUT_STATE.orEmpty(cap, state);
    }

    static final String tagState = "InputState";

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT baseTag = new CompoundNBT();

        state.ifPresent(state -> baseTag.put(tagState , INPUT_STATE.writeNBT(state, null)));

        return baseTag;
    }

    @Override
    public void deserializeNBT(CompoundNBT baseTag) {
        state.ifPresent(state -> INPUT_STATE.readNBT(state, null, baseTag.getCompound(tagState)));
    }
}
