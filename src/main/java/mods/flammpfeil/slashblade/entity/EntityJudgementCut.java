package mods.flammpfeil.slashblade.entity;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.event.KnockBackHandler;
import mods.flammpfeil.slashblade.util.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.UUID;

public class EntityJudgementCut extends ProjectileEntity implements IShootable {
    private static final DataParameter<Integer> COLOR = EntityDataManager.<Integer>createKey(EntityJudgementCut.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> FLAGS = EntityDataManager.<Integer>createKey(EntityJudgementCut.class, DataSerializers.VARINT);

    private int lifetime = 10;
    private int seed = -1;

    private double damage = 1.0D;

    private boolean cycleHit = false;


    public int getSeed() {
        return seed;
    }

    public boolean doCycleHit() {
        return cycleHit;
    }

    public void setCycleHit(boolean cycleHit) {
        this.cycleHit = cycleHit;
    }

    public UUID shootingEntity;

    private SoundEvent livingEntitySound = SoundEvents.ENTITY_WITHER_HURT;
    protected SoundEvent getHitEntitySound() {
        return this.livingEntitySound;
    }
    public EntityJudgementCut(EntityType<? extends ProjectileEntity> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
        this.setNoGravity(true);
        //this.setGlowing(true);

        this.seed = this.rand.nextInt(360);
    }

    public static EntityJudgementCut createInstance(FMLPlayMessages.SpawnEntity packet, World worldIn){
        return new EntityJudgementCut(SlashBlade.RegistryEvents.JudgementCut, worldIn);
    }

    @Override
    protected void registerData() {
        this.dataManager.register(COLOR, 0x3333FF);
        this.dataManager.register(FLAGS, 0);
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {

        NBTHelper.getNBTCoupler(compound)
                .put("Color", this.getColor())
                .put("damage", this.damage)
                .put("crit", this.getIsCritical())
                .put("clip", this.isNoClip())
                .put("OwnerUUID", this.shootingEntity)
                .put("Lifetime", this.getLifetime());
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        NBTHelper.getNBTCoupler(compound)
                .get("Color", this::setColor)
                .get("damage",  ((Double v)->this.damage = v), this.damage)
                .get("crit",this::setIsCritical)
                .get("clip",this::setNoClip)
                .get("OwnerUUID",  ((UUID v)->this.shootingEntity = v), true)
                .get("Lifetime",this::setLifetime);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        this.setMotion(0,0,0);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public boolean isInRangeToRenderDist(double distance) {
        double d0 = this.getBoundingBox().getAverageEdgeLength() * 10.0D;
        if (Double.isNaN(d0)) {
            d0 = 1.0D;
        }

        d0 = d0 * 64.0D * getRenderDistanceWeight();
        return distance < d0 * d0;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setPositionAndRotationDirect(double x, double y, double z, float yaw, float pitch, int posRotationIncrements, boolean teleport) {
        this.setPosition(x, y, z);
        this.setRotation(yaw, pitch);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void setVelocity(double x, double y, double z) {
        this.setMotion(0, 0, 0);
    }

    enum FlagsState {
        Critical,
        NoClip,
    }

    EnumSet<FlagsState> flags = EnumSet.noneOf(FlagsState.class);
    int intFlags = 0;

    private void setFlags(FlagsState value) {
        this.flags.add(value);
        refreshFlags();
    }
    private void removeFlags(FlagsState value){
        this.flags.remove(value);
        refreshFlags();
    }

    private void refreshFlags(){
        if(this.world.isRemote){
            int newValue = this.dataManager.get(FLAGS).intValue();
            if(intFlags != newValue){
                intFlags = newValue;
                EnumSetConverter.convertToEnumSet(flags, FlagsState.values(), intFlags);
            }
        }else{
            int newValue = EnumSetConverter.convertToInt(this.flags);
            if(this.intFlags != newValue) {
                this.dataManager.set(FLAGS, newValue);
                this.intFlags = newValue;
            }
        }
    }


    public void setIsCritical(boolean value) {
        if(value)
            setFlags(FlagsState.Critical);
        else
            removeFlags(FlagsState.Critical);
    }
    public boolean getIsCritical() {
        refreshFlags();
        return flags.contains(FlagsState.Critical);
    }
    
    public void setNoClip(boolean value) {
        this.noClip = value;
        if(value)
            setFlags(FlagsState.NoClip);
        else
            removeFlags(FlagsState.NoClip);
    }
    //disallowedHitBlock
    public boolean isNoClip() {
        if (!this.world.isRemote) {
            return this.noClip;
        } else {
            refreshFlags();
            return flags.contains(FlagsState.NoClip);
        }
    }

    @Override
    public void tick() {
        super.tick();

        if(ticksExisted < 8 && ticksExisted % 2 == 0) {
            this.playSound(getHitEntitySound(), 0.2F, 0.5F + 0.25f * this.rand.nextFloat());
        }

        if(this.getShooter() != null) {
            AxisAlignedBB bb = this.getBoundingBox();

            //cyclehit
            if (this.ticksExisted % 2 == 0) {
                KnockBacks knockBackType = getIsCritical() ? KnockBacks.toss : KnockBacks.cancel;
                AttackManager.areaAttack(this, knockBackType.action,4.0, this.doCycleHit(),false);
            }

            final int count = 3;
            if(getIsCritical() && 0 < ticksExisted && ticksExisted <= count){
                EntitySlashEffect jc = new EntitySlashEffect(SlashBlade.RegistryEvents.SlashEffect, this.world);
                jc.setPositionAndRotation(
                        this.getPosX(), this.getPosY(), this.getPosZ(),
                        (360.0f / count) * ticksExisted + this.seed, 0);
                jc.setRotationRoll(30);

                jc.setShooter(this.getShooter());

                jc.setMute(false);
                jc.setIsCritical(true);

                jc.setDamage(1.0);

                jc.setColor(this.getColor());
                jc.setBaseSize(0.5f);

                jc.setKnockBack(KnockBacks.cancel);

                jc.setIndirect(true);

                this.world.addEntity(jc);
            }
        }

        tryDespawn();

    }

    protected void tryDespawn() {
        if(!this.world.isRemote){
            if (getLifetime() < this.ticksExisted) {
                this.burst();
            }
        }
    }

    /*
    protected void onHitEntity(EntityRayTraceResult p_213868_1_) {
        Entity targetEntity = p_213868_1_.getEntity();
        float f = (float)this.getMotion().length();
        int i = MathHelper.ceil(Math.max((double)f * this.damage, 0.0D));

        if (this.getIsCritical()) {
            i += this.rand.nextInt(i / 2 + 2);
        }

        Entity shooter = this.getShooter();
        DamageSource damagesource;
        if (shooter == null) {
            damagesource = CustomDamageSource.causeSummonedSwordDamage(this, this);
        } else {
            damagesource = CustomDamageSource.causeSummonedSwordDamage(this, shooter);
            if (shooter instanceof LivingEntity) {
                ((LivingEntity)shooter).setLastAttackedEntity(targetEntity);
            }
        }

        int fireTime = targetEntity.func_223314_ad();
        if (this.isBurning() && !(targetEntity instanceof EndermanEntity)) {
            targetEntity.setFire(5);
        }

        if (targetEntity.attackEntityFrom(damagesource, (float)i)) {
            if (targetEntity instanceof LivingEntity) {
                LivingEntity targetLivingEntity = (LivingEntity)targetEntity;

                if (!this.world.isRemote && shooter instanceof LivingEntity) {
                    EnchantmentHelper.applyThornEnchantments(targetLivingEntity, shooter);
                    EnchantmentHelper.applyArthropodEnchantments((LivingEntity)shooter, targetLivingEntity);
                }

                //this.arrowHit(targetLivingEntity);

                affectEntity(targetLivingEntity, getPotionEffects(), 1.0f);

                if (shooter != null && targetLivingEntity != shooter && targetLivingEntity instanceof PlayerEntity && shooter instanceof ServerPlayerEntity) {
                    ((ServerPlayerEntity) shooter).func_213823_a(this.getHitEntityPlayerSound(), SoundCategory.PLAYERS, 0.18F, 0.45F);
                }
            }

            this.playSound(this.getHitEntitySound(), 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
        } else {
            targetEntity.func_223308_g(fireTime);
            this.setMotion(this.getMotion().scale(-0.1D));
            this.rotationYaw += 180.0F;
            this.prevRotationYaw += 180.0F;
            if (!this.world.isRemote && this.getMotion().lengthSquared() < 1.0E-7D) {
                this.burst();
            }
        }

    }*/

    public int getColor(){
        return this.getDataManager().get(COLOR);
    }
    public void setColor(int value){
        this.getDataManager().set(COLOR,value);
    }

    public int getLifetime(){
        return Math.min(this.lifetime , 1000);
    }
    public void setLifetime(int value){
        this.lifetime = value;
    }

    @Nullable
    @Override
    public Entity getShooter() {
        return this.shootingEntity != null && this.world instanceof ServerWorld ? ((ServerWorld)this.world).getEntityByUuid(this.shootingEntity) : null;
    }

    @Override
    public void setShooter(Entity shooter) {
        this.shootingEntity = (shooter != null) ? shooter.getUniqueID() : null;
    }

    public List<EffectInstance> getPotionEffects(){
        List<EffectInstance> effects = PotionUtils.getEffectsFromTag(this.getPersistentData());

        if(effects.isEmpty())
            effects.add(new EffectInstance(Effects.POISON, 1, 1));

        return effects;
    }

    public void burst(){
        //this.playSound(SoundEvents.BLOCK_GLASS_BREAK, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));

        if(!this.world.isRemote){
            if(this.world instanceof ServerWorld)
                ((ServerWorld)this.world).spawnParticle(ParticleTypes.CRIT, this.getPosX(), this.getPosY(), this.getPosZ(), 16, 0.5, 0.5,0.5,0.25f);

            this.burst( getPotionEffects(), null);
        }

        super.remove();
    }


    public void burst(List<EffectInstance> effects, @Nullable Entity focusEntity) {
        AxisAlignedBB axisalignedbb = this.getBoundingBox().grow(4.0D, 2.0D, 4.0D);
        List<Entity> list = TargetSelector.getTargettableEntitiesWithinAABB(
                this.world,
                2,
                this);
        //this.world.getEntitiesWithinAABB(LivingEntity.class, axisalignedbb);

        list.stream()
                .filter(e -> e instanceof LivingEntity)
                .map(e -> (LivingEntity) e)
                .forEach(e -> {
                    double distanceSq = this.getDistanceSq(e);
                    if (distanceSq < 9.0D) {
                        double factor = 1.0D - Math.sqrt(distanceSq) / 4.0D;
                        if (e == focusEntity) {
                            factor = 1.0D;
                        }

                        affectEntity(e, effects, factor);
                    }
                });
    }

    public void affectEntity(LivingEntity focusEntity, List<EffectInstance> effects, double factor){
        for(EffectInstance effectinstance : getPotionEffects()) {
            Effect effect = effectinstance.getPotion();
            if (effect.isInstant()) {
                effect.affectEntity(this, this.getShooter(), focusEntity, effectinstance.getAmplifier(), factor);
            } else {
                int duration = (int)(factor * (double)effectinstance.getDuration() + 0.5D);
                if (duration > 0) {
                    focusEntity.addPotionEffect(new EffectInstance(effect, duration, effectinstance.getAmplifier(), effectinstance.isAmbient(), effectinstance.doesShowParticles()));
                }
            }
        }
    }

    public void setDamage(double damageIn) {
        this.damage = damageIn;
    }

    @Override
    public double getDamage() {
        return this.damage;
    }


    @Nullable
    public EntityRayTraceResult getRayTrace(Vector3d p_213866_1_, Vector3d p_213866_2_) {
        return ProjectileHelper.rayTraceEntities(this.world, this, p_213866_1_, p_213866_2_, this.getBoundingBox().expand(this.getMotion()).grow(1.0D), (p_213871_1_) -> {
            return !p_213871_1_.isSpectator() && p_213871_1_.isAlive() && p_213871_1_.canBeCollidedWith() && (p_213871_1_ != this.getShooter());
        });
    }
}
