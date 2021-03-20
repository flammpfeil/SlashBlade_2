package mods.flammpfeil.slashblade.ability;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.entity.EntityAbstractSummonedSword;
import mods.flammpfeil.slashblade.entity.EntityJudgementCut;
import mods.flammpfeil.slashblade.event.InputCommandEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.stats.Stats;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.EnumSet;
import java.util.Optional;
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
        ServerPlayerEntity sender = event.getPlayer();

        boolean onDown = !old.contains(InputCommand.M_DOWN) && current.contains(InputCommand.M_DOWN);
        boolean onUp = old.contains(InputCommand.M_DOWN) && !current.contains(InputCommand.M_DOWN);


        if(onDown){
            World worldIn = sender.world;

            sender.getHeldItemMainhand().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state)->{

                if(sender.experienceLevel <= 0)
                    return;

                sender.giveExperiencePoints(-1);

                Optional<Entity> foundTarget = Stream.of(Optional.ofNullable(state.getTargetEntity(sender.world))
                            , RayTraceHelper.rayTrace(sender.world, sender, sender.getEyePosition(1.0f) , sender.getLookVec(), 12,12, null)
                                    .filter(r->r.getType() == RayTraceResult.Type.ENTITY)
                                    .filter(r->{
                                        EntityRayTraceResult er = (EntityRayTraceResult)r;
                                        Entity target = ((EntityRayTraceResult) r).getEntity();

                                        boolean isMatch = true;
                                        if(target instanceof LivingEntity)
                                            isMatch = TargetSelector.lockon_focus.canTarget(sender, (LivingEntity)target);

                                        return isMatch;
                                    }).map(r->((EntityRayTraceResult) r).getEntity()))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst();

                Vector3d targetPos = foundTarget.map((e)->new Vector3d(e.getPosX(), e.getPosY() + e.getEyeHeight() * 0.5, e.getPosZ()))
                        .orElseGet(()->{
                            Vector3d start = sender.getEyePosition(1.0f);
                            Vector3d end = start.add(sender.getLookVec().scale(40));
                            RayTraceResult result = worldIn.rayTraceBlocks(new RayTraceContext(start, end, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, sender));
                            return result.getHitVec();
                        });

                int counter = StatHelper.increase(sender, SlashBlade.RegistryEvents.SWORD_SUMMONED, 1);
                boolean sided = counter % 2 == 0;


                EntityAbstractSummonedSword ss = new EntityAbstractSummonedSword(SlashBlade.RegistryEvents.SummonedSword, worldIn);

                Vector3d pos = sender.getEyePosition(1.0f)
                        .add(VectorHelper.getVectorForRotation( 0.0f, sender.getYaw(0) + 90).scale(sided ? 1 : -1));
                ss.setPosition(pos.x, pos.y, pos.z);

                Vector3d dir = targetPos.subtract(pos).normalize();
                ss.shoot(dir.x,dir.y,dir.z, 3.0f, 0.0f);


                ss.setShooter(sender);
                ss.setColor(state.getColorCode());
                ss.setRoll(sender.getRNG().nextFloat() * 360.0f);

                worldIn.addEntity(ss);

                sender.playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 0.2F, 1.45F);
            });
        }
    }
}
