package mods.flammpfeil.slashblade.entity;

import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageSources;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.network.PlayMessages;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.network.PlayMessages;

public class BladeItemEntity extends ItemEntity {
    public BladeItemEntity(EntityType<? extends BladeItemEntity> p_i50217_1_, Level p_i50217_2_) {
        super(p_i50217_1_, p_i50217_2_);
    }

    public void init(){
        this.setInvulnerable(true);

        CompoundTag compoundnbt = this.saveWithoutId(new CompoundTag());
        compoundnbt.remove("Dimension");
        compoundnbt.putShort("Health", (short)100);
        compoundnbt.putShort("Age", Short.MIN_VALUE);
        this.load(compoundnbt);
    }

    public static BladeItemEntity createInstanceFromPacket(PlayMessages.SpawnEntity packet, Level worldIn){
        return new BladeItemEntity(SlashBlade.RegistryEvents.BladeItem, worldIn);
    }

    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        if(onGround() && tickCount % 40 == 0){
            tickCount ++;
        }
        super.tick();

        if(!this.isInWater() && !onGround() && tickCount % 6 == 0){
            this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 0.5F, 2.5F);
        }


        if(this.level().isClientSide){
            if (random.nextInt(5) == 0 && getAirSupply() < 0) {
                Direction direction = Direction.UP;
                double d0 = (double)this.getX() - (double)(random.nextFloat() * 0.1F);
                double d1 = (double)this.getY() - (double)(random.nextFloat() * 0.1F);
                double d2 = (double)this.getZ() - (double)(random.nextFloat() * 0.1F);
                double d3 = (double)(0.4F - (random.nextFloat() + random.nextFloat()) * 0.4F);
                this.level().addParticle(ParticleTypes.PORTAL, d0 + (double)direction.getStepX() * d3, d1 + 2 + (double)direction.getStepY() * d3, d2 + (double)direction.getStepZ() * d3, random.nextGaussian() * 0.005D, -2, random.nextGaussian() * 0.005D);
            }

            if (!this.onGround() && !this.isInWater() && random.nextInt(3) == 0) {
                Direction direction = Direction.UP;
                double d0 = (double)this.getX() - (double)(random.nextFloat() * 0.1F);
                double d1 = (double)this.getY() - (double)(random.nextFloat() * 0.1F);
                double d2 = (double)this.getZ() - (double)(random.nextFloat() * 0.1F);
                double d3 = (double)(0.4F - (random.nextFloat() + random.nextFloat()) * 0.4F);
                this.level().addParticle(ParticleTypes.END_ROD, d0 + (double)direction.getStepX() * d3, d1 + (double)direction.getStepY() * d3, d2 + (double)direction.getStepZ() * d3, random.nextGaussian() * 0.005D, random.nextGaussian() * 0.005D, random.nextGaussian() * 0.005D);
            }
        }
    }

    @Override
    public boolean isOnFire() {
        return super.isOnFire();
    }

    @Override
    public boolean causeFallDamage(float distance, float damageMultiplier, DamageSource ds) {
        super.causeFallDamage(distance, damageMultiplier, ds);

        int i = Mth.ceil(distance);
        if (i > 0) {
            this.playSound(SoundEvents.GENERIC_BIG_FALL, 1.0F, 1.0F);
            this.hurt(this.level().damageSources().fall(), (float)i);
            int j = Mth.floor(this.getX());
            int k = Mth.floor(this.getY() - (double)0.2F);
            int l = Mth.floor(this.getZ());
            BlockState blockstate = this.level().getBlockState(new BlockPos(j, k, l));
            if (!blockstate.isAir()) {
                SoundType soundtype = blockstate.getSoundType(level(), new BlockPos(j, k, l), this);
                this.playSound(soundtype.getFallSound(), soundtype.getVolume() * 0.5F, soundtype.getPitch() * 0.75F);
            }

            if(this.isCurrentlyGlowing() && getAirSupply() < 0){
                this.setGlowingTag(false);
            }
        }

        return false;
    }

    @Override
    public float getLightLevelDependentMagicValue() {
        if(getAirSupply() < 0)
            return 15728880;
        return super.getLightLevelDependentMagicValue();
    }
}
