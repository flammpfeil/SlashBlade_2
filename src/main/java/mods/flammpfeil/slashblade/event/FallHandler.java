package mods.flammpfeil.slashblade.event;

import mods.flammpfeil.slashblade.capability.slashblade.ComboState;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.ai.attributes.IAttributeInstance;
import net.minecraft.particles.BlockParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.player.PlayerFlyableFallEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class FallHandler {
    private static final class SingletonHolder {
        private static final FallHandler instance = new FallHandler();
    }
    public static FallHandler getInstance() {
        return FallHandler.SingletonHolder.instance;
    }
    private FallHandler(){}
    public void register(){
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onFall(LivingFallEvent event){
        resetState(event.getEntityLiving());
    }

    @SubscribeEvent
    public void onFlyableFall(PlayerFlyableFallEvent event){
        resetState(event.getEntityLiving());
    }

    public static void resetState(LivingEntity user){
        user.getHeldItemMainhand().getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state)->{
            state.setFallDecreaseRate(0);

            ComboState combo = state.getComboSeq();
            if(combo.isAerial()){
                state.setComboSeq(combo.getNextOfTimeout());
            }
        });

    }

    public static void spawnLandingParticle(LivingEntity user , float fallFactor){
        if (!user.world.isRemote) {
            int x = MathHelper.floor(user.posX);
            int y = MathHelper.floor(user.posY - (double)0.5F);
            int z = MathHelper.floor(user.posZ);
            BlockPos pos = new BlockPos(x, y, z);
            BlockState state = user.world.getBlockState(pos);

            float f = (float) MathHelper.ceil(fallFactor);
            if (!state.isAir(user.world, pos)) {
                double d0 = Math.min((double)(0.2F + f / 15.0F), 2.5D);
                int i = (int)(150.0D * d0);
                if (!state.addLandingEffects((ServerWorld)user.world, pos, state, user, i))
                    ((ServerWorld)user.world).spawnParticle(new BlockParticleData(ParticleTypes.BLOCK, state), user.posX, user.posY, user.posZ, i, 0.0D, 0.0D, 0.0D, (double)0.15F);
            }
        }
    }

    public static void fallDecrease(LivingEntity user){
        if(!user.hasNoGravity() && !user.onGround){
            user.fallDistance = 1;

            float currentRatio = user.getHeldItemMainhand().getCapability(ItemSlashBlade.BLADESTATE).map((state)->
            {
                float decRatio = state.getFallDecreaseRate();

                float newDecRatio = decRatio + 0.05f;
                newDecRatio = Math.min(1.0f, newDecRatio);
                state.setFallDecreaseRate(newDecRatio);

                return decRatio;
            }).orElseGet(()->1.0f);

            IAttributeInstance gravity = user.getAttribute(LivingEntity.ENTITY_GRAVITY);
            double g = gravity.getValue() * 0.9;

            Vec3d motion = user.getMotion();
            if(motion.y < 0)
                user.setMotion(motion.x, (motion.y + g) * currentRatio, motion.z);
        }
    }

    public static void fallResist(LivingEntity user){
        if(!user.hasNoGravity() && !user.onGround){
            user.fallDistance = 1;

            Vec3d motion = user.getMotion();
            IAttributeInstance gravity = user.getAttribute(LivingEntity.ENTITY_GRAVITY);
            double g = gravity.getValue();
            if(motion.y < 0)
                user.setMotion(motion.x, (motion.y + g + 0.002f), motion.z);
        }
    }
}
