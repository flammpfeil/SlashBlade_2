package mods.flammpfeil.slashblade.util;

import mods.flammpfeil.slashblade.ability.ArrowReflector;
import mods.flammpfeil.slashblade.entity.IShootable;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.AxisAlignedBB;

import java.util.List;
import java.util.function.Consumer;

public class AttackManager {
    static public void areaAttack(LivingEntity playerIn, Consumer<LivingEntity> beforeHit){
        areaAttack(playerIn, beforeHit, 1.0f, true, true, false);
    }

    static public void areaAttack(LivingEntity playerIn, Consumer<LivingEntity> beforeHit, float ratio, boolean forceHit, boolean resetHit , boolean mute) {


        float modifiedRatio = (1.0F + EnchantmentHelper.getSweepingDamageRatio(playerIn) * 0.5f) * ratio;
        AttributeModifier am = new AttributeModifier("SweepingDamageRatio", modifiedRatio, AttributeModifier.Operation.MULTIPLY_BASE);

        if (!playerIn.world.isRemote()) {
            try {
                playerIn.getAttribute(Attributes.ATTACK_DAMAGE).applyNonPersistentModifier(am);

                List<Entity> founds = TargetSelector.getTargettableEntitiesWithinAABB(playerIn.world,
                        TargetSelector.getResolvedReach(playerIn),
                        playerIn);

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

    }

    static public <E extends Entity & IShootable> void areaAttack(E owner, Consumer<LivingEntity> beforeHit, double reach, boolean forceHit, boolean resetHit) {

        AxisAlignedBB bb = owner.getBoundingBox();
        //bb = bb.grow(3.0D, 3D, 3.0D);

        if (!owner.world.isRemote()) {

            List<Entity> founds = TargetSelector.getTargettableEntitiesWithinAABB(owner.world,
                    reach,
                    owner);

            for (Entity entity : founds) {

                if(entity instanceof LivingEntity)
                    beforeHit.accept((LivingEntity)entity);

                float baseAmount = (float) owner.getDamage();
                doAttackWith(DamageSource.causeIndirectMagicDamage(owner, owner.getShooter()), baseAmount,entity, forceHit, resetHit);
            }
        }
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
                    state.setOnClick(true);
                    ((PlayerEntity) attacker).attackTargetEntityWithCurrentItem(t);
                    state.setOnClick(false);
                });
            },target, forceHit, resetHit);
        }else{
            float baseAmount = (float) attacker.getAttribute(Attributes.ATTACK_DAMAGE).getValue();
            doAttackWith(DamageSource.causeMobDamage(attacker), baseAmount, target, forceHit, resetHit);
        }

        ArrowReflector.doReflect(target, attacker);
    }
}
