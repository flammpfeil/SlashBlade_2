package mods.flammpfeil.slashblade.ability;

import mods.flammpfeil.slashblade.capability.imputstate.CapabilityImputState;
import mods.flammpfeil.slashblade.event.AnvilCrafting;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.ImputCommand;
import mods.flammpfeil.slashblade.util.TargetSelector;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.NativeUtil;
import net.minecraft.command.arguments.EntityAnchorArgument;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ArmorStandEntity;
import net.minecraft.entity.monster.MonsterEntity;
import net.minecraft.entity.passive.ChickenEntity;
import net.minecraft.entity.passive.FoxEntity;
import net.minecraft.entity.passive.RabbitEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class LockOnManager {
    private static final class SingletonHolder {
        private static final LockOnManager instance = new LockOnManager();
    }

    public static LockOnManager getInstance() {
        return LockOnManager.SingletonHolder.instance;
    }

    private LockOnManager() {
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    public static void onImputChange(EnumSet<ImputCommand> old, EnumSet<ImputCommand> current, ServerPlayerEntity player) {

        if (!(!old.contains(ImputCommand.SNEAK) && current.contains(ImputCommand.SNEAK))) return;

        ItemStack stack = player.getHeldItemMainhand();
        if (stack.isEmpty()) return;
        if (!(stack.getItem() instanceof ItemSlashBlade)) return;

        //todo :rayTrace entity hit is Force lockon
        List<LivingEntity> entities = player.world.getTargettableEntitiesWithinAABB(
                LivingEntity.class,
                TargetSelector.lockon,
                player,
                player.getBoundingBox().grow(12.0D, 6.0D, 12.0D));

        Optional<LivingEntity> foundEntity = entities.stream().min(Comparator.comparingDouble(e -> e.getDistanceSq(player)));

        foundEntity.ifPresent(e -> {
            stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {
                s.setTargetEntityId(e);
            });
        });

    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onEntityUpdate(TickEvent.PlayerTickEvent event) {
        if(event.phase != TickEvent.Phase.START) return;

        if(Minecraft.getInstance().player != event.player) return;

        ItemStack stack = event.player.getHeldItemMainhand();
        if (stack.isEmpty()) return;
        if (!(stack.getItem() instanceof ItemSlashBlade)) return;

        stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s -> {

            Entity target = s.getTargetEntity(event.player.world);

            if (target == null) return;
            if(!target.isAlive()) return;

            LivingEntity entity = event.player;

            if(!entity.world.isRemote) return;
            if(!entity.getCapability(CapabilityImputState.IMPUT_STATE).filter(imput->imput.getCommands().contains(ImputCommand.SNEAK)).isPresent()) return;


            float partialTicks = Minecraft.getInstance().getRenderPartialTicks();

            float oldYawHead = entity.rotationYawHead;
            float oldPitch = entity.rotationPitch;
            float oldYaw = entity.rotationYaw;

            entity.lookAt(EntityAnchorArgument.Type.EYES, target.getEyePosition(partialTicks));

            float step = 0.125f;

            step *= Math.min(1.0f ,Math.abs(Math.tan(Math.toRadians(oldYaw - entity.rotationYawHead)) ));

            entity.rotationYawHead = MathHelper.func_219805_h(step, oldYawHead, entity.rotationYawHead);
            entity.renderYawOffset = entity.rotationYawHead;
            entity.prevRenderYawOffset = entity.renderYawOffset;

            entity.rotationPitch = MathHelper.func_219805_h(step,oldPitch ,entity.rotationPitch);
            entity.rotationYaw = MathHelper.func_219805_h(step, oldYaw , entity.rotationYaw);
        });
    }

}
