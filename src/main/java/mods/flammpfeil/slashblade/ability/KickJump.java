package mods.flammpfeil.slashblade.ability;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.ComboState;
import mods.flammpfeil.slashblade.event.InputCommandEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.util.AdvancementHelper;
import mods.flammpfeil.slashblade.util.InputCommand;
import mods.flammpfeil.slashblade.util.VectorHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.EnumSet;
import java.util.List;

public class KickJump {
    private static final class SingletonHolder {
        private static final KickJump instance = new KickJump();
    }

    public static KickJump getInstance() {
        return KickJump.SingletonHolder.instance;
    }

    private KickJump() {
    }

    public void register() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    static final TargetingConditions tc = new TargetingConditions(false)
            .ignoreLineOfSight()
            .ignoreInvisibilityTesting();

    static public final ResourceLocation ADVANCEMENT_KICK_JUMP = new ResourceLocation(SlashBlade.modid, "abilities/kick_jump");

    static public final String KEY_KICKJUMP = "sb.kickjump";

    @SubscribeEvent
    public void onInputChange(InputCommandEvent event) {

        EnumSet<InputCommand> old = event.getOld();
        EnumSet<InputCommand> current = event.getCurrent();
        ServerPlayer sender = event.getEntity();
        Level worldIn = sender.level();

        if (sender.onGround()) return;
        if(old.contains(InputCommand.JUMP)) return;
        if(!current.contains(InputCommand.JUMP)) return;

        if(0 != sender.getPersistentData().getInt(KEY_KICKJUMP)) return;

        Iterable<VoxelShape> list = worldIn.getBlockCollisions(sender, sender.getBoundingBox().inflate(0.5,0,1));
        if(!list.iterator().hasNext()) return;

        //execute
        Untouchable.setUntouchable(sender, Untouchable.JUMP_TICKS);

        //set cooldown
        sender.getPersistentData().putInt(KEY_KICKJUMP, 2);

        Vec3 delta = sender.getDeltaMovement();
        Vec3 motion = new Vec3(delta.x, +0.8, delta.z);

        sender.move(MoverType.SELF, motion);

        sender.connection.send(new ClientboundSetEntityMotionPacket(sender.getId(), motion.scale(0.75f)));

        AdvancementHelper.grantCriterion(sender,ADVANCEMENT_KICK_JUMP);
        sender.playNotifySound(SoundEvents.PLAYER_SMALL_FALL, SoundSource.PLAYERS, 0.5f, 1.2f);

        sender.getMainHandItem().getCapability(ItemSlashBlade.BLADESTATE).ifPresent(s->{
            s.updateComboSeq(sender, ComboState.NONE);
        });

        if(worldIn instanceof ServerLevel){
            ((ServerLevel)worldIn).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.GLASS.defaultBlockState()), sender.getX(), sender.getY(), sender.getZ(), 20, 0.0D, 0.0D, 0.0D, (double)0.15F);
        }

    }
    @SubscribeEvent
    public void onTick(TickEvent.PlayerTickEvent event) {
        switch (event.phase) {
            case START -> {
                LivingEntity player = event.player;

                //cooldown
                if (event.player.onGround() && 0 < event.player.getPersistentData().getInt(KEY_KICKJUMP)) {

                    int count = event.player.getPersistentData().getInt(KEY_KICKJUMP);
                    count--;

                    if (count <= 0) {
                        event.player.getPersistentData().remove(KEY_KICKJUMP);
                    } else {
                        event.player.getPersistentData().putInt(KEY_KICKJUMP, count);
                    }
                }
            }
        }
    }
}
