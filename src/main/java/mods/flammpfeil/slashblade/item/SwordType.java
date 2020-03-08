package mods.flammpfeil.slashblade.item;

import net.minecraft.item.ItemStack;

import java.util.EnumSet;

public enum SwordType{
    None,
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

        itemStackIn.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s->{
            if(s.isBroken())
                types.add(Broken);

            if(s.isNoScabbard())
                types.add(NoScabbard);

            if(s.isSealed())
                types.add(Cursed);

            if(!s.isSealed() && itemStackIn.isEnchanted() && (itemStackIn.hasDisplayName() || s.isDefaultBewitched()))
                types.add(Bewitched);
        });

        if(itemStackIn.isEnchanted())
            types.add(Enchanted);

        return types;
    }
}
