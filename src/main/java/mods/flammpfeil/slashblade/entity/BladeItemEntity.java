package mods.flammpfeil.slashblade.entity;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

public class BladeItemEntity extends ItemEntity {
    public BladeItemEntity(EntityType<? extends BladeItemEntity> p_i50217_1_, World p_i50217_2_) {
        super(p_i50217_1_, p_i50217_2_);
    }

    public void init(){
        this.setInvulnerable(true);

        CompoundNBT compoundnbt = this.writeWithoutTypeId(new CompoundNBT());
        compoundnbt.remove("Dimension");
        compoundnbt.putShort("Health", (short)100);
        compoundnbt.putShort("Age", Short.MIN_VALUE);
        this.read(compoundnbt);
    }

    public static BladeItemEntity createInstanceFromPacket(FMLPlayMessages.SpawnEntity packet, World worldIn){
        return new BladeItemEntity(SlashBlade.RegistryEvents.BladeItem, worldIn);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void tick() {
        if(isOnGround() && ticksExisted % 40 == 0){
            ticksExisted ++;
        }
        super.tick();

        if(!this.isInWater() && !isOnGround() && ticksExisted % 6 == 0){
            this.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 0.5F, 2.5F);
        }


        if(this.world.isRemote){
            if (rand.nextInt(5) == 0 && getAir() < 0) {
                Direction direction = Direction.UP;
                double d0 = (double)this.getPosX() - (double)(rand.nextFloat() * 0.1F);
                double d1 = (double)this.getPosY() - (double)(rand.nextFloat() * 0.1F);
                double d2 = (double)this.getPosZ() - (double)(rand.nextFloat() * 0.1F);
                double d3 = (double)(0.4F - (rand.nextFloat() + rand.nextFloat()) * 0.4F);
                this.world.addParticle(ParticleTypes.PORTAL, d0 + (double)direction.getXOffset() * d3, d1 + 2 + (double)direction.getYOffset() * d3, d2 + (double)direction.getZOffset() * d3, rand.nextGaussian() * 0.005D, -2, rand.nextGaussian() * 0.005D);
            }

            if (!this.isOnGround() && !this.isInWater() && rand.nextInt(3) == 0) {
                Direction direction = Direction.UP;
                double d0 = (double)this.getPosX() - (double)(rand.nextFloat() * 0.1F);
                double d1 = (double)this.getPosY() - (double)(rand.nextFloat() * 0.1F);
                double d2 = (double)this.getPosZ() - (double)(rand.nextFloat() * 0.1F);
                double d3 = (double)(0.4F - (rand.nextFloat() + rand.nextFloat()) * 0.4F);
                this.world.addParticle(ParticleTypes.END_ROD, d0 + (double)direction.getXOffset() * d3, d1 + (double)direction.getYOffset() * d3, d2 + (double)direction.getZOffset() * d3, rand.nextGaussian() * 0.005D, rand.nextGaussian() * 0.005D, rand.nextGaussian() * 0.005D);
            }
        }
    }

    @Override
    public boolean isBurning() {
        return super.isBurning();
    }

    @Override
    public boolean onLivingFall(float distance, float damageMultiplier) {
        super.onLivingFall(distance, damageMultiplier);

        int i = MathHelper.ceil(distance);
        if (i > 0) {
            this.playSound(SoundEvents.ENTITY_GENERIC_BIG_FALL, 1.0F, 1.0F);
            this.attackEntityFrom(DamageSource.FALL, (float)i);
            int j = MathHelper.floor(this.getPosX());
            int k = MathHelper.floor(this.getPosY() - (double)0.2F);
            int l = MathHelper.floor(this.getPosZ());
            BlockState blockstate = this.world.getBlockState(new BlockPos(j, k, l));
            if (!blockstate.isAir()) {
                SoundType soundtype = blockstate.getSoundType(world, new BlockPos(j, k, l), this);
                this.playSound(soundtype.getFallSound(), soundtype.getVolume() * 0.5F, soundtype.getPitch() * 0.75F);
            }

            if(this.isGlowing() && getAir() < 0){
                this.setGlowing(false);
            }
        }

        return false;
    }

    @Override
    public float getBrightness() {
        if(getAir() < 0)
            return 15728880;
        return super.getBrightness();
    }
}
