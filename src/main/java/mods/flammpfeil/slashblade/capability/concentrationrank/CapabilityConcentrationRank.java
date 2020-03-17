package mods.flammpfeil.slashblade.capability.concentrationrank;

import mods.flammpfeil.slashblade.util.NBTHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.INBT;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityInject;
import net.minecraftforge.common.capabilities.CapabilityManager;

import javax.annotation.Nullable;

public class CapabilityConcentrationRank {

    @CapabilityInject(IConcentrationRank.class)
    public static Capability<IConcentrationRank> RANK_POINT = null;

    public static void register()
    {
        CapabilityManager.INSTANCE.register(IConcentrationRank.class, new Capability.IStorage<IConcentrationRank>(){

            @Nullable
            @Override
            public INBT writeNBT(Capability<IConcentrationRank> capability, IConcentrationRank instance, Direction side) {
                return NBTHelper.getNBTCoupler(new CompoundNBT())
                        .put("rawPoint", instance.getRawRankPoint())
                        .put("lastupdate", instance.getLastUpdate())
                        .getRawCompound();
            }

            @Override
            public void readNBT(Capability<IConcentrationRank> capability, IConcentrationRank instance, Direction side, INBT nbt) {

                NBTHelper.getNBTCoupler((CompoundNBT) nbt)
                        .get("rawPoint", instance::setRawRankPoint)
                        .get("lastupdate", instance::setLastUpdte)
                        ;

            }
        }, ()-> new ConcentrationRank());
    }
}
