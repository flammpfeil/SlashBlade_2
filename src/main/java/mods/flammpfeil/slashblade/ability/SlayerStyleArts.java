package mods.flammpfeil.slashblade.ability;

import com.google.common.collect.Sets;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.ComboState;
import mods.flammpfeil.slashblade.entity.EntityAbstractSummonedSword;
import mods.flammpfeil.slashblade.event.InputCommandEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.InputCommand;
import net.minecraft.entity.CreatureEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
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

    final static EnumSet<InputCommand> fowerd_sprint = EnumSet.of(InputCommand.FORWARD, InputCommand.SPRINT);

    @SubscribeEvent
    public void onInputChange(InputCommandEvent event) {

        EnumSet<InputCommand> old = event.getOld();
        EnumSet<InputCommand> current = event.getCurrent();
        ServerPlayerEntity sender = event.getPlayer();
        World worldIn = sender.world;

        if(!old.contains(InputCommand.SPRINT) && current.containsAll(fowerd_sprint)){
            sender.getHeldItemMainhand().getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state->{
                Entity target = state.getTargetEntity(worldIn);

                if(target == null) return;

                if(target == sender.getLastAttackedEntity() && sender.ticksExisted < sender.getLastAttackedEntityTime() + 100){
                    SlayerStyleArts.doTeleport(sender, sender.getLastAttackedEntity());
                }else{
                    EntityAbstractSummonedSword ss = new EntityAbstractSummonedSword(SlashBlade.RegistryEvents.SummonedSword, worldIn){
                        @Override
                        protected void onHitEntity(EntityRayTraceResult p_213868_1_) {
                            super.onHitEntity(p_213868_1_);

                            if(this.getHitEntity() == sender.getLastAttackedEntity()){
                                SlayerStyleArts.doTeleport(sender, sender.getLastAttackedEntity());
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
                }

            });
        }
    }

    private static void doTeleport(Entity entityIn, LivingEntity target) {
        if(!(entityIn.world instanceof ServerWorld)) return;

        if(entityIn instanceof PlayerEntity){
            PlayerEntity player = ((PlayerEntity)entityIn);
            player.playSound(SoundEvents.ENTITY_ENDERMAN_TELEPORT, SoundCategory.PLAYERS, 0.75F, 1.25F);

            player.getHeldItemMainhand().getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state->{
                state.updateComboSeq(player, state.getComboRootAir());
                //todo: invincible time
            });
        }

        ServerWorld worldIn = (ServerWorld) entityIn.world;

        Vector3d tereportPos = target.getPositionVec().add(0,target.getHeight() / 2.0, 0).add(entityIn.getLookVec().scale(-2.0));

        double x = tereportPos.x;
        double y = tereportPos.y;
        double z = tereportPos.z;
        float yaw = entityIn.rotationYaw;
        float pitch = entityIn.rotationPitch;

        Set<SPlayerPositionLookPacket.Flags> relativeList = Sets.newIdentityHashSet();
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

}
