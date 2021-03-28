package mods.flammpfeil.slashblade.util;

import com.google.common.collect.Lists;
import mods.flammpfeil.slashblade.entity.IShootable;
import mods.flammpfeil.slashblade.event.InputCommandEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.attributes.ModifiableAttributeInstance;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.entity.monster.IMob;
import net.minecraft.entity.passive.WolfEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TargetSelector {
    static public final EntityPredicate lockon = (new EntityPredicate())
            .setDistance(12.0D)
            .setCustomPredicate(new AttackablePredicate());

    static public final EntityPredicate lockon_focus = (new EntityPredicate())
            .setDistance(12.0D);

    static final String AttackableTag = "RevengeAttacker";

    static boolean isAttackable(Entity revengeTarget, Entity attacker){
        return revengeTarget != null && attacker != null && (revengeTarget == attacker || revengeTarget.isOnSameTeam(attacker));
    }

    static public final EntityPredicate areaAttack = (new EntityPredicate(){
                @Override
                public boolean canTarget(@Nullable LivingEntity attacker, LivingEntity target) {
                    boolean isAttackable = false;

                    isAttackable |= isAttackable(target.getRevengeTarget(), attacker);

                    if(!isAttackable && target instanceof MobEntity)
                        isAttackable |= isAttackable(((MobEntity) target).getAttackTarget(), attacker);

                    if(isAttackable)
                        target.addTag(AttackableTag);

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
                if (((WolfEntity) livingentity).func_233678_J__()/*isAngry()*/)
                    return true;

            if (livingentity.getTags().contains(AttackableTag)){
                livingentity.removeTag(AttackableTag);
                return true;
            }

            if(livingentity.getTeam() != null)
                return true;

            return false;
        }
    }

    static public List<Entity> getReflectableEntitiesWithinAABB(LivingEntity attacker) {
        double reach = TargetSelector.getResolvedReach(attacker);

        AxisAlignedBB aabb = getResolvedAxisAligned(attacker.getBoundingBox(), attacker.getLookVec(), reach);
        World world = attacker.world;
        return Stream.of(
                world.getEntitiesWithinAABB(ProjectileEntity.class, aabb).stream()
                        .filter(e-> ((e.func_234616_v_()/*getThrower()*/ == null || e.func_234616_v_()/*getThrower()*/ != attacker) && (e instanceof IShootable ? ((IShootable)e).getShooter() != attacker : true))))
                /*
                world.getEntitiesWithinAABB(DamagingProjectileEntity.class, aabb).stream()
                        .filter(e-> (e.shootingEntity == null || e.shootingEntity != attacker)),
                world.getEntitiesWithinAABB(AbstractArrowEntity.class, aabb).stream()
                        .filter(e->e.getShooter() == null || e.getShooter() != attacker))
                */
                .flatMap(s->s)
                .filter(e-> (e.getDistanceSq(attacker) < (reach * reach)))
                .collect(Collectors.toList());
    }

    static public List<Entity> getExtinguishableEntitiesWithinAABB(LivingEntity attacker) {
        double reach = TargetSelector.getResolvedReach(attacker);

        AxisAlignedBB aabb = getResolvedAxisAligned(attacker.getBoundingBox(), attacker.getLookVec(), reach);
        World world = attacker.world;
        return world.getEntitiesWithinAABB(TNTEntity.class, aabb).stream()
                .filter(e-> (e.getDistanceSq(attacker) < (reach * reach)))
                .collect(Collectors.toList());
    }

    static public  List<Entity> getTargettableEntitiesWithinAABB(World world, LivingEntity attacker) {
        double reach = TargetSelector.getResolvedReach(attacker);

        List<Entity> list1 = Lists.newArrayList();

        AxisAlignedBB aabb = getResolvedAxisAligned(attacker.getBoundingBox(), attacker.getLookVec(), reach);

        list1.addAll(world.getEntitiesWithinAABB(EnderDragonEntity.class, aabb.grow(5)).stream()
                .flatMap(d -> Arrays.stream(d.getDragonParts()))
                .filter(e-> (e.getDistanceSq(attacker) < (reach * reach)))
                .collect(Collectors.toList()));

        list1.addAll(getReflectableEntitiesWithinAABB(attacker));
        list1.addAll(getExtinguishableEntitiesWithinAABB(attacker));

        EntityPredicate predicate = getAreaAttackPredicate(reach);

        list1.addAll(world.getEntitiesWithinAABB(LivingEntity.class, aabb, (Predicate<LivingEntity>)null).stream()
                .filter(t->predicate.canTarget(attacker,t))
                .collect(Collectors.toList()));

        return list1;
    }

    static public <E extends Entity & IShootable> List<Entity> getTargettableEntitiesWithinAABB(World world, double reach, E owner) {
        AxisAlignedBB aabb = owner.getBoundingBox().grow(reach);

        List<Entity> list1 = Lists.newArrayList();

        list1.addAll(world.getEntitiesWithinAABB(EnderDragonEntity.class, aabb.grow(5)).stream()
                .flatMap(d -> Arrays.stream(d.getDragonParts()))
                .filter(e -> (e.getDistanceSq(owner) < (reach * reach)))
                .collect(Collectors.toList()));


        LivingEntity user;
        if (owner.getShooter() instanceof LivingEntity)
            user = (LivingEntity) owner.getShooter();
        else
            user = null;

        list1.addAll(getReflectableEntitiesWithinAABB(world, reach, owner));

        EntityPredicate predicate = getAreaAttackPredicate(0); //reach check has already been completed

        list1.addAll(world.getEntitiesWithinAABB(LivingEntity.class, aabb, (Predicate<LivingEntity>) null).stream()
                .filter(t -> predicate.canTarget(user, t))
                .collect(Collectors.toList()));

        return list1;
    }

    static public <E extends Entity & IShootable> List<Entity> getReflectableEntitiesWithinAABB(World world, double reach, E owner) {
        AxisAlignedBB aabb = owner.getBoundingBox().grow(reach);

        return Stream.of(
                world.getEntitiesWithinAABB(ProjectileEntity.class, aabb).stream()
                        .filter(e-> (e.func_234616_v_()/*getThrower()*/ == null || e.func_234616_v_()/*getThrower()*/ != owner.getShooter())))
                /*
                world.getEntitiesWithinAABB(DamagingProjectileEntity.class, aabb).stream()
                        .filter(e-> (e.shootingEntity == null || e.shootingEntity != owner.getShooter())),
                world.getEntitiesWithinAABB(AbstractArrowEntity.class, aabb).stream()
                        .filter(e->e.getShooter() == null || e.getShooter() != owner.getShooter()))
                 */
                .flatMap(s->s)
                .filter(e-> (e.getDistanceSq(owner) < (reach * reach)) && e != owner)
                .collect(Collectors.toList());
    }

    static public AxisAlignedBB getResolvedAxisAligned(AxisAlignedBB bb, Vector3d dir, double reach){
        final double padding = 1.0;

        if(dir == Vector3d.ZERO){
            bb = bb.grow(reach * 2);
        }else{
            bb = bb.offset(dir.scale(reach * 0.5)).grow(reach);
        }

        bb = bb.grow(padding);

        return bb;
    }

    static public double getResolvedReach(LivingEntity user){
        double reach = 4.0D; /* 4 block*/
        ModifiableAttributeInstance attrib = user.getAttribute(ForgeMod.REACH_DISTANCE.get());
        if(attrib != null){
            reach = attrib.getValue() - 1;
        }
        return reach;
    }

    @SubscribeEvent
    public static void onInputChange(InputCommandEvent event) {

        EnumSet<InputCommand> old = event.getOld();
        EnumSet<InputCommand> current = event.getCurrent();
        ServerPlayerEntity sender = event.getPlayer();

        //SneakHold & Middle Click
        if (!(!old.contains(InputCommand.M_DOWN) && current.contains(InputCommand.M_DOWN) && current.contains(InputCommand.SNEAK))) return;

        ItemStack stack = sender.getHeldItemMainhand();
        if (stack.isEmpty()) return;
        if (!(stack.getItem() instanceof ItemSlashBlade)) return;

        stack.getCapability(ItemSlashBlade.BLADESTATE)
                .ifPresent(s->{
                    Entity tmp = s.getTargetEntity(sender.world);
                    if (tmp == null) return;
                    if (!(tmp instanceof LivingEntity)) return;

                    LivingEntity target = (LivingEntity) tmp;

                    if(target.getRevengeTarget() == sender) return;

                    target.setRevengeTarget(sender);

                    if(target.world instanceof ServerWorld){
                        ServerWorld sw = (ServerWorld)target.world;

                        sw.spawnParticle(sender, ParticleTypes.ANGRY_VILLAGER, false,
                                target.getPosX(), target.getPosY() + target.getEyeHeight(), target.getPosZ(),
                                5,
                                target.getWidth() * 1.5,
                                target.getHeight(),
                                target.getWidth() * 1.5,
                                0.02D);
                    }
                });
    }
}
