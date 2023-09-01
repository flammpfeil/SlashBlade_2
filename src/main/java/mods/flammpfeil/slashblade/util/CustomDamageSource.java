package mods.flammpfeil.slashblade.util;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.entity.EntityAbstractSummonedSword;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.damagesource.DamageSource;

import javax.annotation.Nullable;

public class CustomDamageSource {
    public static final ResourceKey<DamageType> SUMMONED_SWORD = ResourceKey.create(Registries.DAMAGE_TYPE, new ResourceLocation(SlashBlade.modid,"summonedsword"));

}
