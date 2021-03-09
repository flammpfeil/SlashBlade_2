package mods.flammpfeil.slashblade.ability;

import mods.flammpfeil.slashblade.capability.mobeffect.CapabilityMobEffect;
import mods.flammpfeil.slashblade.entity.ai.StunGoal;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraft.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingEvent;

/**
 * Created by Furia on 15/06/20.
 */
public class StunManager {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onEntityJoinWorldEvent(EntityJoinWorldEvent event){
        if(!(event.getEntity() instanceof CreatureEntity)) return;
        CreatureEntity entity = (CreatureEntity) event.getEntity();

        entity.goalSelector.addGoal(-1,new StunGoal(entity));
    }

    @SubscribeEvent
    public void onEntityLivingUpdate(LivingEvent.LivingUpdateEvent event){
        LivingEntity target = event.getEntityLiving();
        if(!(target instanceof CreatureEntity)) return;
        if(target == null) return;
        if(target.world == null) return;

        boolean onStun = target.getCapability(CapabilityMobEffect.MOB_EFFECT)
                .filter((state)->state.isStun(target.world.getGameTime()))
                .isPresent();

        if(onStun){
            Vector3d motion = target.getMotion();
            if(5 < target.fallDistance)
                target.setMotion(motion.x, motion.y - 2.0f, motion.z);
            else if(motion.y < 0)
                target.setMotion(motion.x, motion.y * 0.25f, motion.z);
        }

    }

    public static void setStun(LivingEntity target, LivingEntity attacker){
        setStun(target);
    }
    public static void setStun(LivingEntity target){
        setStun(target, 10);
    }
    public static void setStun(LivingEntity target, long duration){
        if(!(target instanceof CreatureEntity)) return;
        if(target.world == null) return;

        target.getCapability(CapabilityMobEffect.MOB_EFFECT).ifPresent((state)->{
            state.setManagedStun(target.world.getGameTime() , duration);
        });
    }

    public static void removeStun(LivingEntity target){
        if(target.world == null) return;
        if(!(target instanceof LivingEntity)) return;


        target.getCapability(CapabilityMobEffect.MOB_EFFECT).ifPresent((state)->{
            state.clearStunTimeOut();
            state.clearFreezeTimeOut();
        });
    }

    @SubscribeEvent
    public void onEntityCanUpdate(EntityEvent.CanUpdate event){
        if(event.isCanceled()) return;
        Entity target = event.getEntity();
        if(target == null) return;
        if(target.world == null) return;


        boolean onFreeze = target.getCapability(CapabilityMobEffect.MOB_EFFECT)
                .filter((state)->state.isFreeze(target.world.getGameTime()))
                .isPresent();

        if(onFreeze)
            event.setCanUpdate(false);

    }

    public static void setFreeze(LivingEntity target, long duration){
        if(target.world == null) return;
        if(!(target instanceof LivingEntity)) return;

        target.getCapability(CapabilityMobEffect.MOB_EFFECT).ifPresent((state)->{
            state.setManagedFreeze(target.world.getGameTime(),duration);
        });
    }
}
