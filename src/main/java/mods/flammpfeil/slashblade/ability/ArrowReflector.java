package mods.flammpfeil.slashblade.ability;

import mods.flammpfeil.slashblade.capability.slashblade.ComboState;
import mods.flammpfeil.slashblade.entity.IShootable;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.TargetSelector;
import mods.flammpfeil.slashblade.util.TimeValueHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class ArrowReflector {

    static public boolean isMatch(Entity arrow, Entity attacker){
        if(arrow == null) return false;
        if(!(arrow instanceof Projectile)) return false;

        return true;
    }

    static public void doReflect(Entity arrow, Entity attacker){
        if(!isMatch(arrow, attacker)) return;

        arrow.hurtMarked = true;
        if (attacker != null) {
            Vec3 dir = attacker.getLookAngle();

            do{
                if(attacker instanceof LivingEntity) break;

                ItemStack stack = ((LivingEntity) attacker).getMainHandItem();

                if(stack.isEmpty()) break;
                if(!(stack.getItem() instanceof ItemSlashBlade)) break;

                Entity target = stack.getCapability(ItemSlashBlade.BLADESTATE).map(s->s.getTargetEntity(attacker.level())).orElse(null);
                if(target != null){
                    dir = arrow.position().subtract(target.getEyePosition(1.0f)).normalize();
                }else{
                    dir = arrow.position().subtract(attacker.getLookAngle().scale(10).add(attacker.getEyePosition(1.0f))).normalize();
                }

            }while(false);


            arrow.setDeltaMovement(dir);

            ((Projectile) arrow).shoot(dir.x, dir.y, dir.z, 1.1f, 0.5f);

            arrow.setNoGravity(true);

            if(arrow instanceof AbstractArrow)
                ((AbstractArrow) arrow).setCritArrow(true);

        }
    }

    static public void doTicks(LivingEntity attacker){

        ItemStack stack = attacker.getMainHandItem();

        if(stack.isEmpty()) return;
        if(!(stack.getItem() instanceof ItemSlashBlade)) return;

        stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s->{
            int ticks = attacker.getTicksUsingItem();

            if(ticks == 0) return;

            ComboState old = s.getComboSeq();
            ComboState current = s.resolvCurrentComboState(attacker);
            if(old != current){
                ticks -= TimeValueHelper.getTicksFromMSec(old.getTimeoutMS());
            }

            double period = TimeValueHelper.getTicksFromFrames(current.getEndFrame() - current.getStartFrame()) * (1.0f / current.getSpeed());

            if(ticks < period){
                List<Entity> founds = TargetSelector.getReflectableEntitiesWithinAABB(attacker);

                founds.stream()
                        .filter(e-> (e instanceof Projectile) && ((Projectile) e).getOwner() != attacker)
                        .forEach(e->doReflect(e, attacker));
            }
        });

    }


}
