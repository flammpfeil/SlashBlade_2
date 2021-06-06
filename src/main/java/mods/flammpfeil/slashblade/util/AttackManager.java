package mods.flammpfeil.slashblade.util;

import com.google.common.collect.Lists;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.ability.ArrowReflector;
import mods.flammpfeil.slashblade.ability.TNTExtinguisher;
import mods.flammpfeil.slashblade.capability.concentrationrank.ConcentrationRank;
import mods.flammpfeil.slashblade.capability.concentrationrank.ConcentrationRankCapabilityProvider;
import mods.flammpfeil.slashblade.capability.concentrationrank.IConcentrationRank;
import mods.flammpfeil.slashblade.entity.EntitySlashEffect;
import mods.flammpfeil.slashblade.entity.IShootable;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.vector.Vector3d;

import java.util.List;
import java.util.function.Consumer;

public class AttackManager {
    static public void areaAttack(LivingEntity playerIn, Consumer<LivingEntity> beforeHit){
        areaAttack(playerIn, beforeHit, 1.0f, true, true, false);
    }

    static public EntitySlashEffect doSlash(LivingEntity playerIn, float roll) {
        return doSlash(playerIn,roll, false);
    }
    static public EntitySlashEffect doSlash(LivingEntity playerIn, float roll, boolean mute) {
        return doSlash(playerIn,roll, mute, false);
    }
    static public EntitySlashEffect doSlash(LivingEntity playerIn, float roll, boolean mute, boolean critical) {
        return doSlash(playerIn,roll,  mute, critical, 1.0);
    }
    static public EntitySlashEffect doSlash(LivingEntity playerIn, float roll, boolean mute, boolean critical, double damage) {
        return doSlash(playerIn,roll, Vector3d.ZERO, mute, critical, damage);
    }
    static public EntitySlashEffect doSlash(LivingEntity playerIn, float roll, Vector3d centerOffset, boolean mute, boolean critical, double damage) {
        return doSlash(playerIn,roll, centerOffset, mute, critical, damage, KnockBacks.cancel);
    }
    static public EntitySlashEffect doSlash(LivingEntity playerIn, float roll, Vector3d centerOffset, boolean mute, boolean critical, double damage, KnockBacks knockback) {

        int colorCode = playerIn.getHeldItemMainhand().getCapability(ItemSlashBlade.BLADESTATE)
                .map(state->state.getColorCode())
                .orElseGet(()->0xFFFFFF);

        return doSlash(playerIn,roll,colorCode, centerOffset, mute, critical, damage, knockback);
    }
    static public EntitySlashEffect doSlash(LivingEntity playerIn, float roll, int colorCode, Vector3d centerOffset, boolean mute, boolean critical, double damage, KnockBacks knockback) {

        if(playerIn.world.isRemote) return null;

        Vector3d pos = playerIn.getPositionVec()
                .add(0.0D, (double)playerIn.getEyeHeight() * 0.75D, 0.0D)
                .add(playerIn.getLookVec().scale(0.3f));

        pos = pos.add(VectorHelper.getVectorForRotation( -90.0F, playerIn.getYaw(0)).scale(centerOffset.y))
                .add(VectorHelper.getVectorForRotation( 0, playerIn.getYaw(0) + 90).scale(centerOffset.z))
                .add(playerIn.getLookVec().scale(centerOffset.z));

        EntitySlashEffect jc = new EntitySlashEffect(SlashBlade.RegistryEvents.SlashEffect, playerIn.world);
        jc.setPosition(pos.x ,pos.y, pos.z);
        jc.setShooter(playerIn);

        jc.setRotationRoll(roll);
        jc.rotationYaw = playerIn.rotationYaw;
        jc.rotationPitch = 0;

        jc.setColor(colorCode);

        jc.setMute(mute);
        jc.setIsCritical(critical);

        jc.setDamage(damage);

        jc.setKnockBack(knockback);

        playerIn.world.addEntity(jc);

        return jc;
    }

    static public List<Entity> areaAttack(LivingEntity playerIn, Consumer<LivingEntity> beforeHit, float ratio, boolean forceHit, boolean resetHit , boolean mute) {
        return areaAttack(playerIn, beforeHit, ratio, forceHit, resetHit, mute,null);
    }
    static public List<Entity> areaAttack(LivingEntity playerIn, Consumer<LivingEntity> beforeHit, float ratio, boolean forceHit, boolean resetHit , boolean mute, List<Entity> exclude) {
        List<Entity> founds = Lists.newArrayList();
        float modifiedRatio = (1.0F + EnchantmentHelper.getSweepingDamageRatio(playerIn) * 0.5f) * ratio;
        AttributeModifier am = new AttributeModifier("SweepingDamageRatio", modifiedRatio, AttributeModifier.Operation.MULTIPLY_BASE);

        if (!playerIn.world.isRemote()) {
            try {
                playerIn.getAttribute(Attributes.ATTACK_DAMAGE).applyNonPersistentModifier(am);

                founds = TargetSelector.getTargettableEntitiesWithinAABB(playerIn.world,playerIn);

                if(exclude != null)
                    founds.removeAll(exclude);

                for (Entity entity : founds) {
                    if(entity instanceof LivingEntity)
                        beforeHit.accept((LivingEntity)entity);

                    doMeleeAttack(playerIn, entity, forceHit, resetHit);
                }

            } finally {
                playerIn.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(am);
            }
        }

        if(!mute)
            playerIn.world.playSound((PlayerEntity)null, playerIn.getPosX(), playerIn.getPosY(), playerIn.getPosZ(), SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 0.5F, 0.4F / (playerIn.getRNG().nextFloat() * 0.4F + 0.8F));

        return founds;
    }

    static public <E extends Entity & IShootable> List<Entity> areaAttack(E owner, Consumer<LivingEntity> beforeHit, double reach, boolean forceHit, boolean resetHit) {
        return areaAttack(owner, beforeHit, reach, forceHit, resetHit, null);
    }
    static public <E extends Entity & IShootable> List<Entity> areaAttack(E owner, Consumer<LivingEntity> beforeHit, double reach, boolean forceHit, boolean resetHit, List<Entity> exclude) {
        List<Entity> founds = Lists.newArrayList();

        AxisAlignedBB bb = owner.getBoundingBox();
        //bb = bb.grow(3.0D, 3D, 3.0D);

        if (!owner.world.isRemote()) {

            founds = TargetSelector.getTargettableEntitiesWithinAABB(owner.world,
                    reach,
                    owner);

            if(exclude != null)
                founds.removeAll(exclude);

            for (Entity entity : founds) {

                if(entity instanceof LivingEntity)
                    beforeHit.accept((LivingEntity)entity);

                float baseAmount = (float) owner.getDamage();
                doAttackWith(DamageSource.causeIndirectMagicDamage(owner, owner.getShooter()), baseAmount,entity, forceHit, resetHit);
            }
        }

        return founds;
    }

    static public void doManagedAttack(Consumer<Entity> attack, Entity target, boolean forceHit, boolean resetHit){
        if(forceHit)
            target.hurtResistantTime = 0;

        attack.accept(target);

        if(resetHit)
            target.hurtResistantTime = 0;
    }

    static public void doAttackWith(DamageSource src, float amount , Entity target, boolean forceHit, boolean resetHit){
        doManagedAttack((t)->{
            t.attackEntityFrom(src, amount);
        },target, forceHit, resetHit);
    }

    static public void doMeleeAttack(LivingEntity attacker, Entity target, boolean forceHit, boolean resetHit){
        if (attacker instanceof PlayerEntity) {
            doManagedAttack((t)->{
                attacker.getHeldItemMainhand().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state) -> {

                    IConcentrationRank.ConcentrationRanks rankBonus = attacker.getCapability(ConcentrationRankCapabilityProvider.RANK_POINT)
                            .map(rp->rp.getRank(attacker.getEntityWorld().getGameTime())).orElse(IConcentrationRank.ConcentrationRanks.NONE);

                    float modifiedRatio = rankBonus.level / 2.0f;
                    if(attacker instanceof PlayerEntity && IConcentrationRank.ConcentrationRanks.S.level <= rankBonus.level){
                        int level = ((PlayerEntity) attacker).experienceLevel;
                        modifiedRatio = Math.max(modifiedRatio, Math.min(level, state.getRefine()));
                    }

                    AttributeModifier am = new AttributeModifier("RankDamageBonus", modifiedRatio, AttributeModifier.Operation.ADDITION);
                    try {
                        state.setOnClick(true);
                        attacker.getAttribute(Attributes.ATTACK_DAMAGE).applyNonPersistentModifier(am);

                        ((PlayerEntity) attacker).attackTargetEntityWithCurrentItem(t);

                    }finally {
                        attacker.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(am);
                        state.setOnClick(false);
                    }
                });
            },target, forceHit, resetHit);
        }else{
            float baseAmount = (float) attacker.getAttribute(Attributes.ATTACK_DAMAGE).getValue();
            doAttackWith(DamageSource.causeMobDamage(attacker), baseAmount, target, forceHit, resetHit);
        }

        ArrowReflector.doReflect(target, attacker);
        TNTExtinguisher.doExtinguishing(target,attacker);
    }
}
