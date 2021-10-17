package mods.flammpfeil.slashblade.item;

import mods.flammpfeil.slashblade.capability.slashblade.ISlashBladeState;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.LazyOptional;

import java.util.EnumSet;

public enum SwordType{
    None,
    EdgeFragment,
    Broken,
    Perfect,
    Enchanted,
    Bewitched,
    SoulEeater,
    FiercerEdge,
    NoScabbard,
    Sealed,
    Cursed,
    ;

    static public EnumSet<SwordType> from(ItemStack itemStackIn){
        EnumSet<SwordType> types = EnumSet.noneOf(SwordType.class);

        LazyOptional<ISlashBladeState> state = itemStackIn.getCapability(ItemSlashBlade.BLADESTATE);

        if(state.isPresent()){
            itemStackIn.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s->{
                if(s.isBroken())
                    types.add(Broken);

                if(s.isNoScabbard())
                    types.add(NoScabbard);

                if(s.isSealed())
                    types.add(Cursed);

                if(!s.isSealed() && itemStackIn.isEnchanted() && (itemStackIn.hasCustomHoverName() || s.isDefaultBewitched()))
                    types.add(Bewitched);
            });
        }else{
            types.add(NoScabbard);
            types.add(EdgeFragment);
        }


        if(itemStackIn.isEnchanted())
            types.add(Enchanted);

        return types;
    }
}
