package mods.flammpfeil.slashblade.util;

import mods.flammpfeil.slashblade.entity.EntityAbstractSummonedSword;
import net.minecraft.entity.Entity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IndirectEntityDamageSource;

import javax.annotation.Nullable;

public class CustomDamageSource {

    public static DamageSource causeSummonedSwordDamage(EntityAbstractSummonedSword sword, @Nullable Entity indirectEntityIn) {
        return (new IndirectEntityDamageSource("slashblade_summonedsword", sword, indirectEntityIn)).setProjectile();
    }
}
