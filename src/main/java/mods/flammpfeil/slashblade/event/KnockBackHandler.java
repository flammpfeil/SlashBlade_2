package mods.flammpfeil.slashblade.event;

import mods.flammpfeil.slashblade.util.NBTHelper;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KnockBackHandler {

    static final String NBT_KEY = "knockback_factor";

    static public void setCancel(LivingEntity target){
        setFactor(target, 0, 0, 0);
    }
    static public void setBoost(LivingEntity target, double horizontalFactor){
        setFactor(target, horizontalFactor, 0, 0);
    }
    static public void setSmash(LivingEntity target, double verticalFactor){
        setFactor(target, 0, verticalFactor, -verticalFactor);
    }
    static public void setFactor(LivingEntity target, double horizontalFactor, double verticalFactor, double addFallDistance){
        NBTHelper.putVector3d(target.getPersistentData(),
                NBT_KEY,
                new Vector3d(horizontalFactor,verticalFactor,addFallDistance));
    }

    @SubscribeEvent
    public static void onLivingKnockBack(LivingKnockBackEvent event){
        LivingEntity target = event.getEntityLiving();


        CompoundNBT nbt = target.getPersistentData();

        if(!nbt.contains(NBT_KEY))
            return;

        Vector3d factor = NBTHelper.getVector3d(nbt, NBT_KEY);
        nbt.remove(NBT_KEY);

        //z = falldistance factor
        target.fallDistance += factor.z;

        //movement factor is resistable
        if ((target.getRNG().nextDouble() < target.getAttribute(Attributes.KNOCKBACK_RESISTANCE).getValue()))
            return;

        target.isAirBorne = true;

        //x = strength multiplier
        if(factor.x == 0)
            event.setCanceled(true);
        else
            event.setStrength((float)(event.getStrength() * factor.x));

        //y = vertical factor

        if(0 < factor.y){
            target.setOnGround(false);
            Vector3d motion = target.getMotion();
            event.getEntityLiving().setMotion(motion.x, Math.max(motion.y, factor.y), motion.z);
        }else if(factor.y < 0){
            Vector3d motion = target.getMotion();
            event.getEntityLiving().setMotion(motion.x, Math.min(motion.y, factor.y), motion.z);
        }
    }
}
