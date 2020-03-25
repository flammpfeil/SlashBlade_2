package mods.flammpfeil.slashblade.entity;

import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.DamageSource;
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
        if(onGround && ticksExisted % 40 == 0){
            ticksExisted ++;
        }
        super.tick();

        if(!this.isInWater() && !onGround && ticksExisted % 6 == 0){
            this.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 0.5F, 2.5F);
        }
    }

    @Override
    public boolean isBurning() {
        return super.isBurning();
    }

    @Override
    public void fall(float distance, float damageMultiplier) {
        super.fall(distance, damageMultiplier);

        int i = MathHelper.ceil(distance);
        if (i > 0) {
            this.playSound(SoundEvents.ENTITY_GENERIC_BIG_FALL, 1.0F, 1.0F);
            this.attackEntityFrom(DamageSource.FALL, (float)i);
            int j = MathHelper.floor(this.posX);
            int k = MathHelper.floor(this.posY - (double)0.2F);
            int l = MathHelper.floor(this.posZ);
            BlockState blockstate = this.world.getBlockState(new BlockPos(j, k, l));
            if (!blockstate.isAir()) {
                SoundType soundtype = blockstate.getSoundType(world, new BlockPos(j, k, l), this);
                this.playSound(soundtype.getFallSound(), soundtype.getVolume() * 0.5F, soundtype.getPitch() * 0.75F);
            }
        }
    }
}
