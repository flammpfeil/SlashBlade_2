package mods.flammpfeil.slashblade.capability.slashblade;

import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.util.NonNullFunction;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Created by Furia on 2017/01/10.
 */
public class BladeStateCapabilityProvider implements ICapabilityProvider, INBTSerializable<CompoundNBT> {

    @CapabilityInject(ISlashBladeState.class)
    public static Capability<ISlashBladeState> CAP = null;

    @CapabilityInject(IEnergyStorage.class)
    public static Capability<IEnergyStorage> ENERGY = null;

    protected LazyOptional<ISlashBladeState> state = LazyOptional.of(CAP::getDefaultInstance);


    protected LazyOptional<IEnergyStorage> storage = LazyOptional.of(()-> new EnergyStorage(defaultCapacity));
    static final int defaultCapacity = 1000000;


    private final String tagState = "State";
    private final String tagEnergy = "Energy";


    public BladeStateCapabilityProvider(){
    }


    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if(cap == ENERGY)
            return state.filter(iSlashBladeState -> iSlashBladeState.hasEnergy()).isPresent()
                    ? storage.cast()
                    : LazyOptional.empty();

        return CAP.orEmpty(cap, state);
    }

    @Override
    public CompoundNBT serializeNBT() {
        CompoundNBT baseTag = new CompoundNBT();

        state.ifPresent(iSlashBladeState -> baseTag.put(tagState ,CAP.writeNBT(iSlashBladeState, null)));
        storage.ifPresent(iEnergyStorage -> baseTag.put(tagEnergy, ENERGY.writeNBT(iEnergyStorage, null)));

        return baseTag;
    }

    @Override
    public void deserializeNBT(CompoundNBT baseTag) {
        state.ifPresent(iSlashBladeState -> CAP.readNBT(iSlashBladeState, null, baseTag.get(tagState)));
        storage.ifPresent(iEnergyStorage -> ENERGY.readNBT(iEnergyStorage, null, baseTag.get(tagEnergy)));
    }
}
