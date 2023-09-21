package mods.flammpfeil.slashblade.ability;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.concentrationrank.CapabilityConcentrationRank;
import mods.flammpfeil.slashblade.capability.concentrationrank.IConcentrationRank;
import mods.flammpfeil.slashblade.capability.inputstate.CapabilityInputState;
import mods.flammpfeil.slashblade.capability.inputstate.InputStateCapabilityProvider;
import mods.flammpfeil.slashblade.entity.*;
import mods.flammpfeil.slashblade.event.InputCommandEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.*;
import net.minecraft.client.player.Input;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.VecDeltaCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.timers.TimerCallback;
import net.minecraft.world.level.timers.TimerQueue;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.EnumSet;
import java.util.List;
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


    static public final ResourceLocation ADVANCEMENT_SUMMONEDSWORDS = new ResourceLocation(SlashBlade.modid, "arts/shooting/summonedswords");
    static public final ResourceLocation ADVANCEMENT_SPIRAL_SWORDS = new ResourceLocation(SlashBlade.modid, "arts/shooting/spiral_swords");
    static public final ResourceLocation ADVANCEMENT_STORM_SWORDS = new ResourceLocation(SlashBlade.modid, "arts/shooting/storm_swords");
    static public final ResourceLocation ADVANCEMENT_BLISTERING_SWORDS = new ResourceLocation(SlashBlade.modid, "arts/shooting/blistering_swords");
    static public final ResourceLocation ADVANCEMENT_HEAVY_RAIN_SWORDS = new ResourceLocation(SlashBlade.modid, "arts/shooting/heavy_rain_swords");

    @SubscribeEvent
    public void onInputChange(InputCommandEvent event) {

        EnumSet<InputCommand> old = event.getOld();
        EnumSet<InputCommand> current = event.getCurrent();
        ServerPlayer sender = event.getEntity();

        InputCommand targetCommnad = InputCommand.M_DOWN;

        boolean onDown = !old.contains(targetCommnad) && current.contains(targetCommnad);
        boolean onPress = current.contains(targetCommnad);
        boolean onUp = old.contains(targetCommnad) && !current.contains(targetCommnad);

        final Long pressTime = event.getState().getLastPressTime(targetCommnad);

        //basic summoned swords
        if(onDown){

            sender.getCapability(CapabilityInputState.INPUT_STATE).ifPresent(input-> {
                //SpiralSwords command
                input.getScheduler().schedule("SpiralSwords", pressTime + 10, new TimerCallback<LivingEntity>() {

                    @Override
                    public void handle(LivingEntity rawEntity, TimerQueue<LivingEntity> queue, long now) {
                        if (!(rawEntity instanceof ServerPlayer)) return;
                        ServerPlayer entity = (ServerPlayer) rawEntity;

                        InputCommand targetCommnad = InputCommand.M_DOWN;
                        boolean inputSucceed = entity.getCapability(CapabilityInputState.INPUT_STATE).filter(input ->
                                input.getCommands().contains(targetCommnad)
                                        && (!InputCommand.anyMatch(input.getCommands(), InputCommand.move) || !input.getCommands().contains(InputCommand.SNEAK))
                                        && input.getLastPressTime(targetCommnad) == pressTime).isPresent();
                        if (!inputSucceed) return;


                        //spiralSwords
                        boolean alreadySummoned = entity.getPassengers().stream().anyMatch(e -> e instanceof EntitySpiralSwords);

                        if (alreadySummoned) {
                            //fire
                            List<Entity> list = entity.getPassengers().stream().filter(e -> e instanceof EntitySpiralSwords).toList();

                            list.stream().forEach(e -> {
                                ((EntitySpiralSwords) e).doFire();
                            });
                        } else {
                            //summon
                            entity.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state) -> {

                                if (entity.experienceLevel <= 0) return;

                                entity.giveExperiencePoints(-5);


                                AdvancementHelper.grantCriterion(entity, ADVANCEMENT_SPIRAL_SWORDS);

                                Level worldIn = entity.level();

                                int rank = entity.getCapability(CapabilityConcentrationRank.RANK_POINT)
                                        .map(r->r.getRank(worldIn.getGameTime()).level)
                                        .orElse(0);

                                int count = 6;

                                if(IConcentrationRank.ConcentrationRanks.S.level <= rank){
                                    count = 8;
                                }

                                for (int i = 0; i < count; i++) {
                                    EntitySpiralSwords ss = new EntitySpiralSwords(SlashBlade.RegistryEvents.SpiralSwords, worldIn);

                                    worldIn.addFreshEntity(ss);

                                    ss.setOwner(entity);
                                    ss.setColor(state.getColorCode());
                                    ss.setRoll(0);

                                    //force riding
                                    ss.startRiding(entity, true);

                                    ss.setDelay(360 / count * i);

                                    entity.playNotifySound(SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.2F, 1.45F);
                                }
                            });
                        }
                    }
                });


                //StormSwords command
                input.getScheduler().schedule("StormSwords", pressTime + 10, new TimerCallback<LivingEntity>() {

                    @Override
                    public void handle(LivingEntity rawEntity, TimerQueue<LivingEntity> queue, long now) {
                        if (!(rawEntity instanceof ServerPlayer)) return;
                        ServerPlayer entity = (ServerPlayer) rawEntity;

                        InputCommand targetCommnad = InputCommand.M_DOWN;
                        boolean inputSucceed = entity.getCapability(CapabilityInputState.INPUT_STATE).filter(input ->
                                input.getCommands().contains(targetCommnad)
                                        && input.getCommands().contains(InputCommand.SNEAK)
                                        && input.getCommands().contains(InputCommand.BACK)
                                        && !input.getCommands().contains(InputCommand.FORWARD)
                                        && input.getLastPressTime(targetCommnad) == pressTime).isPresent();
                        if (!inputSucceed) return;


                        //summon
                        entity.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state) -> {

                            Level worldIn = entity.level();
                            Entity target = state.getTargetEntity(worldIn);

                            if(target == null) return;

                            if (entity.experienceLevel <= 0) return;

                            entity.giveExperiencePoints(-5);

                            AdvancementHelper.grantCriterion(entity, ADVANCEMENT_STORM_SWORDS);

                            int rank = entity.getCapability(CapabilityConcentrationRank.RANK_POINT)
                                    .map(r->r.getRank(worldIn.getGameTime()).level)
                                    .orElse(0);

                            int count = 6;

                            if(IConcentrationRank.ConcentrationRanks.S.level <= rank){
                                count = 8;
                            }

                            for (int i = 0; i < count; i++) {
                                EntityStormSwords ss = new EntityStormSwords(SlashBlade.RegistryEvents.StormSwords, worldIn);

                                worldIn.addFreshEntity(ss);

                                ss.setOwner(entity);
                                ss.setColor(state.getColorCode());
                                ss.setRoll(0);

                                //force riding
                                ss.startRiding(target, true);

                                ss.setDelay(360 / count * i);

                                entity.playNotifySound(SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.2F, 1.45F);
                            }
                        });
                    }
                });

                //BlisteringSwords command
                input.getScheduler().schedule("BlisteringSwords", pressTime + 10, new TimerCallback<LivingEntity>() {

                    @Override
                    public void handle(LivingEntity rawEntity, TimerQueue<LivingEntity> queue, long now) {
                        if (!(rawEntity instanceof ServerPlayer)) return;
                        ServerPlayer entity = (ServerPlayer) rawEntity;

                        InputCommand targetCommnad = InputCommand.M_DOWN;
                        boolean inputSucceed = entity.getCapability(CapabilityInputState.INPUT_STATE).filter(input ->
                                input.getCommands().contains(targetCommnad)
                                        && input.getCommands().contains(InputCommand.SNEAK)
                                        && input.getCommands().contains(InputCommand.FORWARD)
                                        && input.getLastPressTime(InputCommand.BACK) + 20 < now
                                        && input.getLastPressTime(targetCommnad) == pressTime).isPresent();
                        if (!inputSucceed) return;


                        //summon
                        entity.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state) -> {

                            Level worldIn = entity.level();

                            if (entity.experienceLevel <= 0) return;

                            entity.giveExperiencePoints(-5);

                            AdvancementHelper.grantCriterion(entity, ADVANCEMENT_BLISTERING_SWORDS);

                            int rank = entity.getCapability(CapabilityConcentrationRank.RANK_POINT)
                                    .map(r->r.getRank(worldIn.getGameTime()).level)
                                    .orElse(0);

                            int count = 6;

                            if(IConcentrationRank.ConcentrationRanks.S.level <= rank){
                                count = 8;
                            }

                            for (int i = 0; i < count; i++) {
                                EntityBlisteringSwords ss = new EntityBlisteringSwords(SlashBlade.RegistryEvents.BlisteringSwords, worldIn);

                                worldIn.addFreshEntity(ss);

                                ss.setOwner(entity);
                                ss.setColor(state.getColorCode());
                                ss.setRoll(0);

                                //force riding
                                ss.startRiding(entity, true);

                                ss.setDelay(i);

                                entity.playNotifySound(SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.2F, 1.45F);
                            }
                        });
                    }
                });

                //BlisteringSwords command
                input.getScheduler().schedule("HeavyRainSwords", pressTime + 10, new TimerCallback<LivingEntity>() {

                    @Override
                    public void handle(LivingEntity rawEntity, TimerQueue<LivingEntity> queue, long now) {
                        if (!(rawEntity instanceof ServerPlayer)) return;
                        ServerPlayer entity = (ServerPlayer) rawEntity;

                        InputCommand targetCommnad = InputCommand.M_DOWN;
                        boolean inputSucceed = entity.getCapability(CapabilityInputState.INPUT_STATE).filter(input ->
                                input.getCommands().contains(targetCommnad)
                                        && input.getCommands().contains(InputCommand.SNEAK)
                                        && input.getCommands().contains(InputCommand.FORWARD)
                                        && input.getLastPressTime(InputCommand.BACK) + 30 > now
                                        && input.getLastPressTime(targetCommnad) == pressTime).isPresent();
                        if (!inputSucceed) return;


                        //summon
                        entity.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state) -> {

                            Level worldIn = entity.level();
                            Entity target = state.getTargetEntity(worldIn);

                            if (entity.experienceLevel <= 0) return;

                            entity.giveExperiencePoints(-5);

                            AdvancementHelper.grantCriterion(entity, ADVANCEMENT_HEAVY_RAIN_SWORDS);

                            int rank = entity.getCapability(CapabilityConcentrationRank.RANK_POINT)
                                    .map(r->r.getRank(worldIn.getGameTime()).level)
                                    .orElse(0);


                            Vec3 basePos;

                            if(target != null){
                                basePos = target.position();
                            }else{
                                Vec3 forwardDir = calculateViewVector(0, entity.getYRot());
                                basePos = entity.getPosition(0).add(forwardDir.scale(5));
                            }

                            float yOffset = 7;
                            basePos = basePos.add(0,yOffset, 0);


                            {//no random pos
                                EntityHeavyRainSwords ss = new EntityHeavyRainSwords(SlashBlade.RegistryEvents.HeavyRainSwords, worldIn);

                                worldIn.addFreshEntity(ss);

                                ss.setOwner(entity);
                                ss.setColor(state.getColorCode());
                                ss.setRoll(0);

                                //force riding
                                ss.startRiding(entity, true);

                                ss.setDelay(0);

                                ss.setPos(basePos);

                                ss.setXRot(-90);
                            }


                            int count = 9;
                            int multiplier = 2;
                            for (int i = 0; i < count; i++)
                            for (int l = 0; l < multiplier; l++){
                                EntityHeavyRainSwords ss = new EntityHeavyRainSwords(SlashBlade.RegistryEvents.HeavyRainSwords, worldIn);

                                worldIn.addFreshEntity(ss);

                                ss.setOwner(entity);
                                ss.setColor(state.getColorCode());
                                ss.setRoll(0);

                                //force riding
                                ss.startRiding(entity, true);

                                ss.setDelay(i);

                                ss.setSpread(basePos);

                                ss.setXRot(-90);

                                entity.playNotifySound(SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.2F, 1.45F);
                            }
                        });
                    }
                });

            });

            sender.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state)->{
                if(sender.experienceLevel <= 0)
                    return;

                sender.giveExperiencePoints(-1);

                AdvancementHelper.grantCriterion(sender, ADVANCEMENT_SUMMONEDSWORDS);

                Optional<Entity> foundTarget = Stream.of(Optional.ofNullable(state.getTargetEntity(sender.level()))
                            , RayTraceHelper.rayTrace(sender.level(), sender, sender.getEyePosition(1.0f) , sender.getLookAngle(), 12,12, (e)->true)
                                    .filter(r->r.getType() == HitResult.Type.ENTITY)
                                    .filter(r->{
                                        EntityHitResult er = (EntityHitResult)r;
                                        Entity target = ((EntityHitResult) r).getEntity();

                                        boolean isMatch = true;
                                        if(target instanceof LivingEntity)
                                            isMatch = TargetSelector.lockon_focus.test(sender, (LivingEntity)target);

                                        if(target instanceof IShootable)
                                            isMatch = ((IShootable) target).getShooter() != sender;

                                        return isMatch;
                                    }).map(r->((EntityHitResult) r).getEntity()))
                        .filter(Optional::isPresent)
                        .map(Optional::get)
                        .findFirst();

                Level worldIn = sender.level();
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

                worldIn.addFreshEntity(ss);

                Vec3 pos = sender.getEyePosition(1.0f)
                        .add(VectorHelper.getVectorForRotation( 0.0f, sender.getViewYRot(0) + 90).scale(sided ? 1 : -1));
                ss.setPos(pos.x, pos.y, pos.z);

                Vec3 dir = targetPos.subtract(pos).normalize();
                ss.shoot(dir.x,dir.y,dir.z, 3.0f, 0.0f);


                ss.setOwner(sender);
                ss.setColor(state.getColorCode());
                ss.setRoll(sender.getRandom().nextFloat() * 360.0f);

                sender.playNotifySound(SoundEvents.CHORUS_FRUIT_TELEPORT, SoundSource.PLAYERS, 0.2F, 1.45F);
            });
        }
    }


    Vec3 calculateViewVector(float x, float y) {
        float f = x * ((float)Math.PI / 180F);
        float f1 = -y * ((float)Math.PI / 180F);
        float f2 = Mth.cos(f1);
        float f3 = Mth.sin(f1);
        float f4 = Mth.cos(f);
        float f5 = Mth.sin(f);
        return new Vec3((double)(f3 * f4), (double)(-f5), (double)(f2 * f4));
    }
}
