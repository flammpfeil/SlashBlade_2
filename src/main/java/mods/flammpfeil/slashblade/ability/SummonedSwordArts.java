package mods.flammpfeil.slashblade.ability;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.entity.EntityAbstractSummonedSword;
import mods.flammpfeil.slashblade.entity.EntityJudgementCut;
import mods.flammpfeil.slashblade.event.InputCommandEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.*;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stats;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.EnumSet;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SummonedSwordArts {
    private static final class SingletonHolder {
        private static final SummonedSwordArts instance = new SummonedSwordArts();
    }

    public static SummonedSwordArts getInstance() {
        return SummonedSwordArts.SingletonHolder.instance;
    }

    private SummonedSwordArts() {
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onInputChange(InputCommandEvent event) {

        EnumSet<InputCommand> old = event.getOld();
        EnumSet<InputCommand> current = event.getCurrent();
        ServerPlayer sender = event.getPlayer();

        boolean onDown = !old.contains(InputCommand.M_DOWN) && current.contains(InputCommand.M_DOWN);
        boolean onUp = old.contains(InputCommand.M_DOWN) && !current.contains(InputCommand.M_DOWN);


        if(onDown){
            Level worldIn = sender.level;

            sender.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state)->{

                if(sender.experienceLevel <= 0)
                    return;

                sender.giveExperiencePoints(-1);

                Optional<Entity> foundTarget = Stream.of(Optional.ofNullable(state.getTargetEntity(sender.level))
                            , RayTraceHelper.rayTrace(sender.level, sender, sender.getEyePosition(1.0f) , sender.getLookAngle(), 12,12, (e)->true)
                                    .filter(r->r.getType() == HitResult.Type.ENTITY)
                                    .filter(r->{
                                        EntityHitResult er = (EntityHitResult)r;
                                        Entity target = ((EntityHitResult) r).getEntity();

                                        boolean isMatch = true;
                                        if(target instanceof LivingEntity)
                                            isMatch = TargetSelector.lockon_focus.test(sender, (LivingEntity)target);

                                        return isMatch;
                                    }).map(r->((EntityHitResult) r).getEntity()))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst();

                Vec3 targetPos = foundTarget.map((e)->new Vec3(e.getX(), e.getY() + e.getEyeHeight() * 0.5, e.getZ()))
                        .orElseGet(()->{
                            Vec3 start = sender.getEyePosition(1.0f);
                            Vec3 end = start.add(sender.getLookAngle().scale(40));
                            HitResult result = worldIn.clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, sender));
                            return result.getLocation();
                        });

                int counter = StatHelper.increase(sender, SlashBlade.RegistryEvents.SWORD_SUMMONED, 1);
                boolean sided = counter % 2 == 0;


                EntityAbstractSummonedSword ss = new EntityAbstractSummonedSword(SlashBlade.RegistryEvents.SummonedSword, worldIn);

                Vec3 pos = sender.getEyePosition(1.0f)
                        .add(VectorHelper.getVectorForRotation( 0.0f, sender.getViewYRot(0) + 90).scale(sided ? 1 : -1));
                ss.setPos(pos.x, pos.y, pos.z);

                Vec3 dir = targetPos.subtract(pos).normalize();
                ss.shoot(dir.x,dir.y,dir.z, 3.0f, 0.0f);


                ss.setOwner(sender);
                ss.setColor(state.getColorCode());
                ss.setRoll(sender.getRandom().nextFloat() * 360.0f);

                worldIn.addFreshEntity(ss);

                sender.playNotifySound(SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.2F, 1.45F);
            });
        }
    }
}
