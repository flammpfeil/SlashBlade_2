package mods.flammpfeil.slashblade.ability;

import mods.flammpfeil.slashblade.capability.inputstate.CapabilityInputState;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.InputCommand;
import mods.flammpfeil.slashblade.util.RayTraceHelper;
import mods.flammpfeil.slashblade.util.TargetSelector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;

public class LockOnManager {
    private static final class SingletonHolder {
        private static final LockOnManager instance = new LockOnManager();
    }

    public static LockOnManager getInstance() {
        return SingletonHolder.instance;
    }

    private LockOnManager() {
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static void onInputChange(EnumSet<InputCommand> old, EnumSet<InputCommand> current, ServerPlayerEntity player) {
        if(old.contains(InputCommand.SNEAK) == current.contains(InputCommand.SNEAK)) return;

        //set target
        ItemStack stack = player.getHeldItemMainhand();
        if (stack.isEmpty()) return;
        if (!(stack.getItem() instanceof ItemSlashBlade)) return;

        Entity targetEntity;

        if((old.contains(InputCommand.SNEAK) && !current.contains(InputCommand.SNEAK))){
            //remove target
            targetEntity = null;
        }else{
            //find target

            Optional<RayTraceResult> result = RayTraceHelper.rayTrace(player.world, player, player.getEyePosition(0) , player.getLookVec(), 12,12, null);
            Optional<Entity> foundEntity = result
                    .filter(r->r.getType() == RayTraceResult.Type.ENTITY)
                    .filter(r->{
                        EntityRayTraceResult er = (EntityRayTraceResult)r;
                        Entity target = ((EntityRayTraceResult) r).getEntity();

                        boolean isMatch = true;
                        if(target instanceof LivingEntity)
                            isMatch = TargetSelector.lockon_focus.canTarget(player, (LivingEntity)target);

                        return isMatch;
                    }).map(r->((EntityRayTraceResult) r).getEntity());

            if(!foundEntity.isPresent()){
                List<LivingEntity> entities = player.world.getTargettableEntitiesWithinAABB(
                        LivingEntity.class,
                        TargetSelector.lockon,
                        player,
                        player.getBoundingBox().grow(12.0D, 6.0D, 12.0D));

                foundEntity = entities.stream().map(s->(Entity)s).min(Comparator.comparingDouble(e -> e.getDistanceSq(player)));
            }

            targetEntity = foundEntity.orElse(null);
        }

        stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
            s.setTargetEntityId(targetEntity);
        });

    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onEntityUpdate(TickEvent.RenderTickEvent event) {
        if(event.phase != TickEvent.Phase.START) return;

        //if(Minecraft.getInstance().player != event.player) return;

        if(Minecraft.getInstance().player == null) return;

        ClientPlayerEntity player = Minecraft.getInstance().player;

        ItemStack stack = player.getHeldItemMainhand();
        if (stack.isEmpty()) return;
        if (!(stack.getItem() instanceof ItemSlashBlade)) return;

        stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {

            Entity target = s.getTargetEntity(player.world);

            if (target == null) return;
            if(!target.isAlive()) return;

            LivingEntity entity = player;

            if(!entity.world.isRemote) return;
            if(!entity.getCapability(CapabilityInputState.INPUT_STATE).filter(input->input.getCommands().contains(InputCommand.SNEAK)).isPresent()) return;


            float partialTicks = Minecraft.getInstance().getRenderPartialTicks();

            float oldYawHead = entity.rotationYawHead;
            float oldYawOffset = entity.renderYawOffset;
            float oldPitch = entity.rotationPitch;
            float oldYaw = entity.rotationYaw;

            float prevYawHead = entity.prevRotationYawHead;
            float prevYawOffset = entity.prevRenderYawOffset;
            float prevYaw = entity.prevRotationYaw;
            float prevPitch = entity.prevRotationPitch;

            entity.lookAt(EntityAnchorArgument.Type.EYES, target.getPositionVec().add(0,target.getEyeHeight() / 2.0,0));

            float step = 0.125f * partialTicks;

            step *= Math.min(1.0f ,Math.abs(MathHelper.wrapDegrees(oldYaw - entity.rotationYawHead) * 0.5));

            //entity.renderYawOffset = entity.rotationYawHead;
            //entity.prevRenderYawOffset = entity.renderYawOffset;

            entity.rotationPitch = MathHelper.interpolateAngle(step,oldPitch ,entity.rotationPitch);
            entity.rotationYaw = MathHelper.interpolateAngle(step, oldYaw , entity.rotationYaw);
            entity.rotationYawHead = MathHelper.interpolateAngle(step, oldYawHead , entity.rotationYawHead);

            entity.renderYawOffset = oldYawOffset;

            entity.prevRenderYawOffset = prevYawOffset;
            entity.prevRotationYawHead = prevYawHead;
            entity.prevRotationYaw = prevYaw;
            entity.prevRotationPitch = prevPitch;
        });
    }

}
