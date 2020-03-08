package mods.flammpfeil.slashblade.entity.ai;

import mods.flammpfeil.slashblade.capability.mobeffect.CapabilityMobEffect;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.PrioritizedGoal;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.pathfinding.GroundPathNavigator;

import java.util.EnumSet;

public class StunGoal extends Goal {
    private final CreatureEntity entity;

    public StunGoal(CreatureEntity creature) {
        this.entity = creature;
        this.setMutexFlags(EnumSet.of(Flag.MOVE,Flag.JUMP,Flag.LOOK,Flag.TARGET));
    }

    /**
     * Returns whether the EntityAIBase should begin execution.
     */
    public boolean shouldExecute() {
        boolean onStun = this.entity.getCapability(CapabilityMobEffect.MOB_EFFECT)
                .filter((state)->state.isStun(this.entity.world.getGameTime()))
                .isPresent();

        return onStun;
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by another one
     */
    public void resetTask() {
        this.entity.getCapability(CapabilityMobEffect.MOB_EFFECT)
                .ifPresent((state)->{
                    state.clearStunTimeOut();
                });
    }
}
