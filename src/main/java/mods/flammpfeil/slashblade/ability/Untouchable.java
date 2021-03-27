package mods.flammpfeil.slashblade.ability;

import mods.flammpfeil.slashblade.capability.mobeffect.CapabilityMobEffect;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Optional;

public class Untouchable {
    private static final class SingletonHolder {
        private static final Untouchable instance = new Untouchable();
    }

    public static Untouchable getInstance() {
        return Untouchable.SingletonHolder.instance;
    }

    private Untouchable() {
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static void setUntouchable(LivingEntity entity, int ticks){
        entity.getCapability(CapabilityMobEffect.MOB_EFFECT).ifPresent(ef->{
            ef.setManagedUntouchable(entity.world.getGameTime(), ticks);
            ef.storeEffects(entity.getActivePotionMap().keySet());
            ef.storeHealth(entity.getHealth());
        });
    }

    private boolean checkUntouchable(LivingEntity entity){
        Optional<Boolean> isUntouchable = entity.getCapability(CapabilityMobEffect.MOB_EFFECT)
                .map(ef->ef.isUntouchable(entity.getEntityWorld().getGameTime()));

        return isUntouchable.orElseGet(()->false);
    }

    private void doWitchTime(Entity entity){
        if(entity == null) return;

        if(!(entity instanceof LivingEntity)) return;

        StunManager.setStun((LivingEntity) entity);
    }

    @SubscribeEvent
    public void onLivingHurt(LivingHurtEvent event){
        if(checkUntouchable(event.getEntityLiving())) {
            event.setCanceled(true);
            doWitchTime(event.getSource().getTrueSource());
        }
    }

    @SubscribeEvent
    public void onLivingDamage(LivingDamageEvent event){
        if(checkUntouchable(event.getEntityLiving())) {
            event.setCanceled(true);
            doWitchTime(event.getSource().getTrueSource());
        }
    }

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event){
        if(checkUntouchable(event.getEntityLiving())) {
            event.setCanceled(true);
            doWitchTime(event.getSource().getTrueSource());
        }
    }

    @SubscribeEvent
    public void onLivingDeath(LivingDeathEvent event){
        if(checkUntouchable(event.getEntityLiving())) {
            event.setCanceled(true);
            doWitchTime(event.getSource().getTrueSource());

            LivingEntity entity = event.getEntityLiving();

            entity.getCapability(CapabilityMobEffect.MOB_EFFECT).ifPresent(ef->{
                if(ef.hasUntouchableWorked()) {
                    entity.getActivePotionMap().keySet().stream()
                            .filter(p -> !(ef.getEffectSet().contains(p) || p.isBeneficial()))
                            .forEach(p -> entity.removePotionEffect(p));

                    float storedHealth = ef.getStoredHealth();
                    if(ef.getStoredHealth() < storedHealth)
                        entity.setHealth(ef.getStoredHealth());
                }
            });
        }
    }

    @SubscribeEvent
    public void onLivingTicks(LivingEvent.LivingUpdateEvent event){
        LivingEntity entity = event.getEntityLiving();

        if(entity.world.isRemote) return;

        entity.getCapability(CapabilityMobEffect.MOB_EFFECT).ifPresent(ef->{
            if(ef.hasUntouchableWorked()) {
                ef.setUntouchableWorked(false);
                entity.getActivePotionMap().keySet().stream()
                        .filter(p -> !(ef.getEffectSet().contains(p) || p.isBeneficial()))
                        .forEach(p -> entity.removePotionEffect(p));

                float storedHealth = ef.getStoredHealth();
                if(ef.getStoredHealth() < storedHealth)
                    entity.setHealth(ef.getStoredHealth());
            }
        });
    }


    final static int JUMP_TICKS = 10;

    @SubscribeEvent
    public void onPlayerJump(LivingEvent.LivingJumpEvent event){
        if(!event.getEntityLiving().getHeldItemMainhand()
                .getCapability(ItemSlashBlade.BLADESTATE).isPresent())
            return;

        Untouchable.setUntouchable(event.getEntityLiving(), JUMP_TICKS);
    }
}
