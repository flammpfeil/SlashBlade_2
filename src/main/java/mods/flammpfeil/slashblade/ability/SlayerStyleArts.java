package mods.flammpfeil.slashblade.ability;

import com.google.common.collect.Sets;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.mobeffect.CapabilityMobEffect;
import mods.flammpfeil.slashblade.capability.slashblade.ComboState;
import mods.flammpfeil.slashblade.entity.EntityAbstractSummonedSword;
import mods.flammpfeil.slashblade.event.InputCommandEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SPlayerPositionLookPacket;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.server.TicketType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public class SlayerStyleArts {
    private static final class SingletonHolder {
        private static final SlayerStyleArts instance = new SlayerStyleArts();
    }

    public static SlayerStyleArts getInstance() {
        return SlayerStyleArts.SingletonHolder.instance;
    }

    private SlayerStyleArts() {
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    final static EnumSet<InputCommand> fowerd_sprint_sneak = EnumSet.of(InputCommand.FORWARD, InputCommand.SPRINT, InputCommand.SNEAK);
    final static EnumSet<InputCommand> move = EnumSet.of(InputCommand.FORWARD, InputCommand.BACK, InputCommand.LEFT, InputCommand.RIGHT);

    @SubscribeEvent
    public void onInputChange(InputCommandEvent event) {

        EnumSet<InputCommand> old = event.getOld();
        EnumSet<InputCommand> current = event.getCurrent();
        ServerPlayerEntity sender = event.getPlayer();
        World worldIn = sender.world;

        if(!old.contains(InputCommand.SPRINT)){

            boolean isHandled = false;

            if(current.containsAll(fowerd_sprint_sneak)){
                //air trick
                isHandled = sender.getHeldItemMainhand().getCapability(ItemSlashBlade.BLADESTATE).map(state->{
                    Entity target = state.getTargetEntity(worldIn);

                    if(target == null) return false;

                    if(target == sender.getLastAttackedEntity() && sender.ticksExisted < sender.getLastAttackedEntityTime() + 100){
                        LivingEntity hitEntity = sender.getLastAttackedEntity();
                        if(hitEntity != null){
                            SlayerStyleArts.doTeleport(sender, hitEntity);
                        }
                    }else{
                        EntityAbstractSummonedSword ss = new EntityAbstractSummonedSword(SlashBlade.RegistryEvents.SummonedSword, worldIn){
                            @Override
                            protected void onHitEntity(EntityRayTraceResult p_213868_1_) {
                                super.onHitEntity(p_213868_1_);

                                LivingEntity target = sender.getLastAttackedEntity();
                                if(target != null && this.getHitEntity() == target){
                                    SlayerStyleArts.doTeleport(sender, target);
                                }
                            }
                        };

                        Vector3d lastPos = sender.getEyePosition(1.0f);
                        ss.lastTickPosX = lastPos.x;
                        ss.lastTickPosY = lastPos.y;
                        ss.lastTickPosZ = lastPos.z;

                        Vector3d targetPos = target.getPositionVec().add(0, target.getHeight() / 2.0, 0).add(sender.getLookVec().scale(-2.0));
                        ss.setPosition(targetPos.x, targetPos.y, targetPos.z);

                        Vector3d dir = sender.getLookVec();
                        ss.shoot(dir.x, dir.y, dir.z, 0.5f, 0);

                        ss.setShooter(sender);

                        ss.setDamage(0.01f);

                        ss.setColor(state.getColorCode());

                        worldIn.addEntity(ss);
                        sender.playSound(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT, SoundCategory.PLAYERS, 0.2F, 1.45F);

                        ss.doForceHitEntity(target);
                    }

                    return true;
                }).orElse(false);
            }

            if(!isHandled && sender.isOnGround() && current.contains(InputCommand.SPRINT) && current.stream().anyMatch(cc->move.contains(cc))){
                //quick avoid ground

                int count = sender.getCapability(CapabilityMobEffect.MOB_EFFECT)
                        .map(ef->ef.doAvoid(sender.world.getGameTime()))
                        .orElse(0);

                if(0 < count){
                    Untouchable.setUntouchable(sender, 10);

                    float moveForward = current.contains(InputCommand.FORWARD) == current.contains(InputCommand.BACK) ? 0.0F : (current.contains(InputCommand.FORWARD) ? 1.0F : -1.0F);
                    float moveStrafe = current.contains(InputCommand.LEFT) == current.contains(InputCommand.RIGHT) ? 0.0F : (current.contains(InputCommand.LEFT) ? 1.0F : -1.0F);
                    Vector3d input = new Vector3d(moveStrafe,0,moveForward);

                    sender.moveRelative(3.0f, input);

                    Vector3d motion = this.maybeBackOffFromEdge(sender.getMotion(), sender);

                    sender.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.5f, 1.2f);

                    sender.move(MoverType.SELF, motion);

                    sender.moveForced(sender.getPositionVec());

                    sender.getHeldItemMainhand().getCapability(ItemSlashBlade.BLADESTATE)
                            .ifPresent(state->state.updateComboSeq(sender, state.getComboRootAir()));
                }

                isHandled = true;
            }
            //slow avoid ground
            //move double tap

            /**
             //relativeList : pos -> convertflag -> motion
             sender.connection.setPlayerLocation(sender.getPosX(), sender.getPosY(), sender.getPosZ()
             , sender.getYaw(1.0f), sender.getPitch(1.0f)
             , Sets.newHashSet(SPlayerPositionLookPacket.Flags.X,SPlayerPositionLookPacket.Flags.Z));
             */
        }

    }

    private static void doTeleport(Entity entityIn, LivingEntity target) {
        if(!(entityIn.world instanceof ServerWorld)) return;

        if(entityIn instanceof PlayerEntity) {
            PlayerEntity player = ((PlayerEntity) entityIn);
            player.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.75F, 1.25F);

            player.getHeldItemMainhand().getCapability(ItemSlashBlade.BLADESTATE)
                    .ifPresent(state -> state.updateComboSeq(player, state.getComboRootAir()));

            Untouchable.setUntouchable(player, 10);
        }

        ServerWorld worldIn = (ServerWorld) entityIn.world;

        Vector3d tereportPos = target.getPositionVec().add(0,target.getHeight() / 2.0, 0).add(entityIn.getLookVec().scale(-2.0));

        double x = tereportPos.x;
        double y = tereportPos.y;
        double z = tereportPos.z;
        float yaw = entityIn.rotationYaw;
        float pitch = entityIn.rotationPitch;

        Set<SPlayerPositionLookPacket.Flags> relativeList = Collections.emptySet();
        BlockPos blockpos = new BlockPos(x, y, z);
        if (!World.isInvalidPosition(blockpos)) {
            return;
        } else {
            if (entityIn instanceof ServerPlayerEntity) {
                ChunkPos chunkpos = new ChunkPos(new BlockPos(x, y, z));
                worldIn.getChunkProvider().registerTicket(TicketType.POST_TELEPORT, chunkpos, 1, entityIn.getEntityId());
                entityIn.stopRiding();
                if (((ServerPlayerEntity)entityIn).isSleeping()) {
                    ((ServerPlayerEntity)entityIn).stopSleepInBed(true, true);
                }

                if (worldIn == entityIn.world) {
                    ((ServerPlayerEntity)entityIn).connection.setPlayerLocation(x, y, z, yaw, pitch, relativeList);
                } else {
                    ((ServerPlayerEntity)entityIn).teleport(worldIn, x, y, z, yaw, pitch);
                }

                entityIn.setRotationYawHead(yaw);
            } else {
                float f1 = MathHelper.wrapDegrees(yaw);
                float f = MathHelper.wrapDegrees(pitch);
                f = MathHelper.clamp(f, -90.0F, 90.0F);
                if (worldIn == entityIn.world) {
                    entityIn.setLocationAndAngles(x, y, z, f1, f);
                    entityIn.setRotationYawHead(f1);
                } else {
                    entityIn.detach();
                    Entity entity = entityIn;
                    entityIn = entityIn.getType().create(worldIn);
                    if (entityIn == null) {
                        return;
                    }

                    entityIn.copyDataFromOld(entity);
                    entityIn.setLocationAndAngles(x, y, z, f1, f);
                    entityIn.setRotationYawHead(f1);
                    worldIn.addFromAnotherDimension(entityIn);
                }
            }

            if (!(entityIn instanceof LivingEntity) || !((LivingEntity)entityIn).isElytraFlying()) {
                entityIn.setMotion(entityIn.getMotion().mul(1.0D, 0.0D, 1.0D));
                entityIn.setOnGround(false);
            }

            if (entityIn instanceof CreatureEntity) {
                ((CreatureEntity)entityIn).getNavigator().clearPath();
            }

        }
    }

    protected Vector3d maybeBackOffFromEdge(Vector3d vec, LivingEntity mover) {
        double d0 = vec.x;
        double d1 = vec.z;
        double d2 = 0.05D;

        while(d0 != 0.0D && mover.world.hasNoCollisions(mover, mover.getBoundingBox().offset(d0, (double)(-mover.stepHeight), 0.0D))) {
            if (d0 < 0.05D && d0 >= -0.05D) {
                d0 = 0.0D;
            } else if (d0 > 0.0D) {
                d0 -= 0.05D;
            } else {
                d0 += 0.05D;
            }
        }

        while(d1 != 0.0D && mover.world.hasNoCollisions(mover, mover.getBoundingBox().offset(0.0D, (double)(-mover.stepHeight), d1))) {
            if (d1 < 0.05D && d1 >= -0.05D) {
                d1 = 0.0D;
            } else if (d1 > 0.0D) {
                d1 -= 0.05D;
            } else {
                d1 += 0.05D;
            }
        }

        while(d0 != 0.0D && d1 != 0.0D && mover.world.hasNoCollisions(mover, mover.getBoundingBox().offset(d0, (double)(-mover.stepHeight), d1))) {
            if (d0 < 0.05D && d0 >= -0.05D) {
                d0 = 0.0D;
            } else if (d0 > 0.0D) {
                d0 -= 0.05D;
            } else {
                d0 += 0.05D;
            }

            if (d1 < 0.05D && d1 >= -0.05D) {
                d1 = 0.0D;
            } else if (d1 > 0.0D) {
                d1 -= 0.05D;
            } else {
                d1 += 0.05D;
            }
        }

        vec = new Vector3d(d0, vec.y, d1);

        return vec;
    }
}
