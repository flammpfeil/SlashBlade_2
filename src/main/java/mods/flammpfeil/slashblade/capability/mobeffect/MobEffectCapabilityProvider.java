package mods.flammpfeil.slashblade.capability.mobeffect;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MobEffectCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundNBT> {

    @CapabilityInject(IMobEffectState.class)
    public static Capability<IMobEffectState> MOB_EFFECT = null;

    protected LazyOptional<IMobEffectState> state = LazyOptional.of(MOB_EFFECT::getDefaultInstance);

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return MOB_EFFECT.orEmpty(cap, state);
    }

    static final String tagState = "MobEffect";

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT baseTag = new CompoundNBT();

        state.ifPresent(state -> baseTag.put(tagState ,MOB_EFFECT.writeNBT(state, null)));

        return baseTag;
    }

    @Override
    public void deserializeNBT(CompoundNBT baseTag) {
        state.ifPresent(state -> MOB_EFFECT.readNBT(state, null, baseTag.get(tagState)));
    }
}
