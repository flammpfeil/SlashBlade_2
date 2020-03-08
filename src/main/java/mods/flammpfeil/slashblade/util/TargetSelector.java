package mods.flammpfeil.slashblade.util;

import mods.flammpfeil.slashblade.ability.LockOnManager;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;

import java.util.function.Predicate;

public class TargetSelector {
    static public final EntityPredicate lockon = (new EntityPredicate())
            .setDistance(12.0D)
            .setLineOfSiteRequired()
            .setCustomPredicate(new AttackablePredicate());

    static public final EntityPredicate areaAttack = (new EntityPredicate())
            .setDistance(12.0D)
            .setLineOfSiteRequired()
            .setUseInvisibilityCheck()
            .setCustomPredicate(new AttackablePredicate());

    static public EntityPredicate getAreaAttackPredicate(double reach){
        return areaAttack.setDistance(reach);
    }

    static public class AttackablePredicate implements Predicate<LivingEntity> {
        public boolean test(LivingEntity livingentity) {
            return (!(livingentity instanceof ArmorStandEntity) || !((ArmorStandEntity) livingentity).hasMarker());
        }
    }
}
