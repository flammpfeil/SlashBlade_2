package mods.flammpfeil.slashblade.ability;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.item.TNTEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class TNTExtinguisher {
    public static void doExtinguishing(Entity target, LivingEntity attacker) {
        if(!(target instanceof TNTEntity)) return;

        if(attacker.world.isRemote) return;

        target.remove();

        ServerWorld world = (ServerWorld) attacker.world;

        world.spawnParticle(ParticleTypes.SMOKE,
                target.getPosX(), target.getPosY() + target.getHeight() * 0.5, target.getPosZ(),
                5,
                target.getWidth() * 1.5,
                target.getHeight(),
                target.getWidth() * 1.5,
                0.02D);

        if(target.getType() == EntityType.TNT){
            if(world.getGameRules().getBoolean(GameRules.DO_MOB_LOOT)){
                ItemEntity itementity = new ItemEntity(world, target.getPosX(), target.getPosY() + target.getHeight(), target.getPosZ(),
                        new ItemStack(Items.TNT));
                itementity.setDefaultPickupDelay();

                world.addEntity(itementity);
            }
        }
    }
}
