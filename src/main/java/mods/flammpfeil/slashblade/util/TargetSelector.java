package mods.flammpfeil.slashblade.util;

import com.google.common.collect.Lists;
import mods.flammpfeil.slashblade.ability.LockOnManager;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.boss.dragon.EnderDragonPartEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.DamagingProjectileEntity;
import net.minecraft.entity.projectile.ThrowableEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TargetSelector {
    static public final EntityPredicate lockon = (new EntityPredicate())
            .setDistance(12.0D)
            .setCustomPredicate(new AttackablePredicate());

    static final String AttackableTag = "RevengeAttacker";

    static public final EntityPredicate areaAttack = (new EntityPredicate(){
                @Override
                public boolean canTarget(@Nullable LivingEntity attacker, LivingEntity target) {
                    if(target.getRevengeTarget() != attacker){
                        target.addTag(AttackableTag);
                    }

                    return super.canTarget(attacker, target);
                }
            })
            .setDistance(12.0D)
            .setUseInvisibilityCheck()
            .setCustomPredicate(new AttackablePredicate());

    static public EntityPredicate getAreaAttackPredicate(double reach){
        return areaAttack.setDistance(reach);
    }

    static public class AttackablePredicate implements Predicate<LivingEntity> {
        public boolean test(LivingEntity livingentity) {
            if (livingentity instanceof ArmorStandEntity)
                if (((ArmorStandEntity) livingentity).hasMarker())
                    return true;
                else
                    return false;

            if (livingentity instanceof IMob)
                return true;

            if (livingentity.isGlowing())
                return true;

            if (livingentity instanceof WolfEntity)
                if (((WolfEntity) livingentity).isAngry())
                    return true;

            if (livingentity.getTags().contains(AttackableTag)){
                livingentity.removeTag(AttackableTag);
                return true;
            }

            return false;
        }
    }

    static public List<Entity> getReflectableEntitiesWithinAABB(World world, double reach, LivingEntity attacker) {
        AxisAlignedBB aabb = getResolvedAxisAligned(attacker, attacker.getLookVec(), reach);

        return Stream.of(
                world.getEntitiesWithinAABB(ThrowableEntity.class, aabb).stream()
                        .filter(e-> (e.getThrower() == null || e.getThrower() != attacker)),
                world.getEntitiesWithinAABB(DamagingProjectileEntity.class, aabb).stream()
                        .filter(e-> (e.shootingEntity == null || e.shootingEntity != attacker)),
                world.getEntitiesWithinAABB(AbstractArrowEntity.class, aabb).stream()
                        .filter(e->e.getShooter() == null || e.getShooter() != attacker))
                .flatMap(s->s)
                .filter(e-> (e.getDistanceSq(attacker) < (reach * reach)))
                .collect(Collectors.toList());
    }

    static public  List<Entity> getTargettableEntitiesWithinAABB(World world, double reach, LivingEntity attacker) {
        AxisAlignedBB aabb = getResolvedAxisAligned(attacker, attacker.getLookVec(), reach);

        List<Entity> list1 = Lists.newArrayList();

        list1.addAll(world.getEntitiesWithinAABB(EnderDragonEntity.class, aabb.grow(5)).stream()
                .flatMap(d -> Arrays.stream(d.func_213404_dT()))
                .filter(e-> (e.getDistanceSq(attacker) < (reach * reach)))
                .collect(Collectors.toList()));

        list1.addAll(getReflectableEntitiesWithinAABB(world,reach,attacker));

        EntityPredicate predicate = getAreaAttackPredicate(reach);

        list1.addAll(world.getEntitiesWithinAABB(LivingEntity.class, aabb, (Predicate<LivingEntity>)null).stream()
                .filter(t->predicate.canTarget(attacker,t))
                .collect(Collectors.toList()));

        return list1;
    }

    static public AxisAlignedBB getResolvedAxisAligned(LivingEntity user, Vec3d dir, double reach){
        final double padding = 1.0;

        AxisAlignedBB bb = user.getBoundingBox();

        if(dir == Vec3d.ZERO){
            bb = bb.grow(reach * 2);
        }else{
            bb = bb.offset(dir.scale(reach * 0.5)).grow(reach);
        }

        bb = bb.grow(padding);

        return bb;
    }

    static public double getResolvedReach(LivingEntity user){
        double reach = 4.0D; /* 4 block*/
        IAttributeInstance attrib = user.getAttribute(PlayerEntity.REACH_DISTANCE);
        if(attrib != null){
            reach = attrib.getValue() - 1;
        }
        return reach;
    }
}
