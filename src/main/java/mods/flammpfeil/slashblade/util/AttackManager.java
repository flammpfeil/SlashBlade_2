package mods.flammpfeil.slashblade.util;

import mods.flammpfeil.slashblade.entity.IShootable;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.CreatureAttribute;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
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

        AxisAlignedBB bb = playerIn.getBoundingBox();
        bb = bb.offset(playerIn.getLookVec().mul(3.5, 3.5, 3.5));
        bb = bb.grow(3.0D, 3D, 3.0D);

        double reach = 4.0D; /* 4 block*/
        IAttributeInstance attrib = playerIn.getAttribute(PlayerEntity.REACH_DISTANCE);
        if(attrib != null){
            reach = attrib.getValue() - 1;
        }

        float modifiedRatio = (1.0F + EnchantmentHelper.getSweepingDamageRatio(playerIn) * 0.5f) * ratio;
        AttributeModifier am = new AttributeModifier("SweepingDamageRatio", modifiedRatio, AttributeModifier.Operation.MULTIPLY_BASE);

        if (!playerIn.world.isRemote()) {
            try {
                playerIn.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).applyModifier(am);

                List<LivingEntity> founds = playerIn.world.getTargettableEntitiesWithinAABB(
                        LivingEntity.class,
                        TargetSelector.getAreaAttackPredicate(reach),
                        playerIn,
                        bb);

                for (LivingEntity livingentity : founds) {
                    if(forceHit)
                        livingentity.hurtResistantTime = 0;

                    //todo : attack method nize
                    beforeHit.accept(livingentity);

                    if (playerIn instanceof PlayerEntity) {

                        playerIn.getHeldItemMainhand().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state) -> {
                            state.setOnClick(true);
                            ((PlayerEntity) playerIn).attackTargetEntityWithCurrentItem(livingentity);
                            state.setOnClick(false);
                        });
                    } else {
                        float baseAmount = (float) playerIn.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).getValue();
                        livingentity.attackEntityFrom(DamageSource.causeMobDamage(playerIn), baseAmount);
                    }

                    if(resetHit)
                        livingentity.hurtResistantTime = 0;
                }

            } finally {
                playerIn.getAttribute(SharedMonsterAttributes.ATTACK_DAMAGE).removeModifier(am);
            }
        }

        if(!mute)
            playerIn.world.playSound((PlayerEntity)null, playerIn.posX, playerIn.posY, playerIn.posZ, SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, SoundCategory.PLAYERS, 0.5F, 0.4F / (playerIn.getRNG().nextFloat() * 0.4F + 0.8F));

    }

    static public <E extends Entity & IShootable> void areaAttack(E owner, Consumer<LivingEntity> beforeHit, float ratio, boolean forceHit, boolean resetHit , boolean mute) {

        AxisAlignedBB bb = owner.getBoundingBox();
        //bb = bb.grow(3.0D, 3D, 3.0D);

        if (!owner.world.isRemote()) {

            for (LivingEntity livingentity : owner.world.getEntitiesWithinAABB(LivingEntity.class, bb)) {
                if (livingentity != owner
                        && !owner.isOnSameTeam(livingentity)
                        && (!(livingentity instanceof ArmorStandEntity) || !((ArmorStandEntity) livingentity).hasMarker())
                        && owner.getDistanceSq(livingentity) < 16.0D /*4^2=16*/) {

                    if(forceHit)
                        livingentity.hurtResistantTime = 0;

                    beforeHit.accept(livingentity);

                    float baseAmount = (float) owner.getDamage();
                    livingentity.attackEntityFrom(DamageSource.causeIndirectMagicDamage(owner, owner.getShooter()), baseAmount);

                    if(resetHit)
                        livingentity.hurtResistantTime = 0;
                }
            }


        }
    }
}
