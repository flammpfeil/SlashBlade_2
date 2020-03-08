package mods.flammpfeil.slashblade.capability.mobeffect;

import mods.flammpfeil.slashblade.util.NBTHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class CapabilityMobEffect {

    @CapabilityInject(IMobEffectState.class)
    public static Capability<IMobEffectState> MOB_EFFECT = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IMobEffectState.class, new Capability.IStorage<IMobEffectState>(){

            @Nullable
            @Override
            public INBT writeNBT(Capability<IMobEffectState> capability, IMobEffectState instance, Direction side) {
                CompoundNBT nbt = new CompoundNBT();

                NBTHelper.getNBTCoupler(nbt)
                        .put("StunTimeout", instance.getStunTimeOut())
                        .put("FreezeTimeout", instance.getFreezeTimeOut());

                return nbt;
            }

            @Override
            public void readNBT(Capability<IMobEffectState> capability, IMobEffectState instance, Direction side, INBT nbt) {

                NBTHelper.getNBTCoupler((CompoundNBT)nbt)
                        .get("StunTimeout", instance::setStunTimeOut)
                        .get("FreezeTimeout", instance::setFreezeTimeOut);

            }
        }, ()-> new MobEffectState());
    }
}
