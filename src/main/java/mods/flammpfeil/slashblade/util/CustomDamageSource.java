package mods.flammpfeil.slashblade.util;

import mods.flammpfeil.slashblade.entity.EntityAbstractSummonedSword;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;

import javax.annotation.Nullable;

public class CustomDamageSource {

    public static DamageSource causeSummonedSwordDamage(EntityAbstractSummonedSword sword, @Nullable Entity indirectEntityIn) {
        return (new IndirectEntityDamageSource("slashblade_summonedsword", sword, indirectEntityIn)).setProjectile();
    }
}
