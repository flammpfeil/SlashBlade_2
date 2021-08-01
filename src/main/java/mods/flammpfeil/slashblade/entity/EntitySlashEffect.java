package mods.flammpfeil.slashblade.entity;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.matrix.MatrixStack;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.event.FallHandler;
import mods.flammpfeil.slashblade.util.AttackManager;
import mods.flammpfeil.slashblade.util.EnumSetConverter;
import mods.flammpfeil.slashblade.util.KnockBacks;
import mods.flammpfeil.slashblade.util.NBTHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.potion.PotionUtils;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
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

public class EntitySlashEffect extends ProjectileEntity implements IShootable {
    private static final DataParameter<Integer> COLOR = EntityDataManager.<Integer>createKey(EntitySlashEffect.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> FLAGS = EntityDataManager.<Integer>createKey(EntitySlashEffect.class, DataSerializers.VARINT);
    private static final DataParameter<Float> ROTATION_OFFSET = EntityDataManager.<Float>createKey(EntitySlashEffect.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> ROTATION_ROLL = EntityDataManager.<Float>createKey(EntitySlashEffect.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> BASESIZE = EntityDataManager.<Float>createKey(EntitySlashEffect.class, DataSerializers.FLOAT);

    private int lifetime = 10;
    private KnockBacks action = KnockBacks.cancel;

    private double damage = 1.0D;

    private boolean cycleHit = false;

    private List<Entity> alreadyHits = Lists.newArrayList();


    public KnockBacks getKnockBack() {
        return action;
    }
    public void setKnockBack(KnockBacks action){
        this.action = action;
    }
    public void setKnockBackOrdinal(int ordinal){
        if(0 <= ordinal && ordinal < KnockBacks.values().length)
            this.action = KnockBacks.values()[ordinal];
        else
            this.action = KnockBacks.cancel;
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
    public EntitySlashEffect(EntityType<? extends ProjectileEntity> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
        this.setNoGravity(true);
        //this.setGlowing(true);
    }

    public static EntitySlashEffect createInstance(FMLPlayMessages.SpawnEntity packet, World worldIn){
        return new EntitySlashEffect(SlashBlade.RegistryEvents.SlashEffect, worldIn);
    }

    @Override
    protected void registerData() {
        this.dataManager.register(COLOR, 0x3333FF);
        this.dataManager.register(FLAGS, 0);

        this.dataManager.register(ROTATION_OFFSET, 0.0f);
        this.dataManager.register(ROTATION_ROLL, 0.0f);
        this.dataManager.register(BASESIZE, 1.0f);
    }

    @Override
    protected void writeAdditional(CompoundNBT compound) {

        NBTHelper.getNBTCoupler(compound)
                .put("RotationOffset", this.getRotationOffset())
                .put("RotationRoll", this.getRotationRoll())
                .put("BaseSize", this.getBaseSize())
                .put("Color", this.getColor())
                .put("damage", this.damage)
                .put("crit", this.getIsCritical())
                .put("clip", this.isNoClip())
                .put("OwnerUUID", this.shootingEntity)
                .put("Lifetime", this.getLifetime())
                .put("Knockback", this.getKnockBack().ordinal());
    }

    @Override
    protected void readAdditional(CompoundNBT compound) {
        NBTHelper.getNBTCoupler(compound)
                .get("RotationOffset", this::setRotationOffset)
                .get("RotationRoll", this::setRotationRoll)
                .get("BaseSize", this::setBaseSize)
                .get("Color", this::setColor)
                .get("damage",  ((Double v)->this.damage = v), this.damage)
                .get("crit",this::setIsCritical)
                .get("clip",this::setNoClip)
                .get("OwnerUUID",  ((UUID v)->this.shootingEntity = v), true)
                .get("Lifetime",this::setLifetime)
                .get("Knockback", this::setKnockBackOrdinal);
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
        Mute,
        Indirect,
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

    public void setIndirect(boolean value) {
        if(value)
            setFlags(FlagsState.Indirect);
        else
            removeFlags(FlagsState.Indirect);
    }
    public boolean getIndirect() {
        refreshFlags();
        return flags.contains(FlagsState.Indirect);
    }

    public void setMute(boolean value) {
        if(value)
            setFlags(FlagsState.Mute);
        else
            removeFlags(FlagsState.Mute);
    }
    public boolean getMute() {
        refreshFlags();
        return flags.contains(FlagsState.Mute);
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

        if (ticksExisted == 2){

            if (!getMute())
                this.playSound(SoundEvents.ITEM_TRIDENT_THROW, 0.80F, 0.625F + 0.1f * this.rand.nextFloat());
            else
                this.playSound(SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP, 0.5F, 0.4F / (this.rand.nextFloat() * 0.4F + 0.8F));

            if(getIsCritical())
                this.playSound(getHitEntitySound(), 0.2F, 0.4F + 0.25f * this.rand.nextFloat());
        }

        if(getShooter() != null && !getShooter().isWet() && ticksExisted < (getLifetime() * 0.75)){
            Vector3d start = this.getPositionVec();
            Vector4f normal = new Vector4f(1,0,0,1);

            float progress = this.ticksExisted / (float)lifetime;

            normal.transform(new Quaternion(Vector3f.YP,-this.rotationYaw -90, true));
            normal.transform(new Quaternion(Vector3f.ZP,this.rotationPitch, true));
            normal.transform(new Quaternion(Vector3f.XP,this.getRotationRoll(), true));
            normal.transform(new Quaternion(Vector3f.YP,140 + this.getRotationOffset() -200.0F * progress, true));

            Vector3d normal3d = new Vector3d(normal.getX(), normal.getY(), normal.getZ());

            BlockRayTraceResult rayResult = this.getEntityWorld().rayTraceBlocks(
                    new RayTraceContext(
                            start.add(normal3d.scale(1.5)),
                            start.add(normal3d.scale(3)),
                            RayTraceContext.BlockMode.COLLIDER,
                            RayTraceContext.FluidMode.ANY,
                            null));

            if(rayResult.getType() == RayTraceResult.Type.BLOCK){
                FallHandler.spawnLandingParticle(this, rayResult.getHitVec(), normal3d , 3);
            }
        }

        if(this.getShooter() != null) {
            AxisAlignedBB bb = this.getBoundingBox();

            //no cyclehit
            if (this.ticksExisted % 2 == 0) {
                boolean forceHit = true;

                //todo: isCritical = hp direct attack & magic damage & melee damage & armor piercing & event override force hit

                //this::onHitEntity ro KnockBackHandler::setCancel
                List<Entity> hits;
                if(!getIndirect() && getShooter() instanceof LivingEntity) {
                    LivingEntity shooter = (LivingEntity) getShooter();
                    float ratio = (float)damage * (getIsCritical() ? 1.1f : 1.0f);
                    hits = AttackManager.areaAttack(shooter, this.action.action, ratio, forceHit,false, true, alreadyHits);
                }else{
                    hits = AttackManager.areaAttack(this, this.action.action,4.0, forceHit,false, alreadyHits);
                }

                if(!this.doCycleHit())
                    alreadyHits.addAll(hits);
            }
        }

        tryDespawn();

    }

    protected void tryDespawn() {
        if(!this.world.isRemote){
            if (getLifetime() < this.ticksExisted)
                this.remove();
        }
    }

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

    public float getRotationOffset(){
        return this.getDataManager().get(ROTATION_OFFSET);
    }
    public void setRotationOffset(float value){
        this.getDataManager().set(ROTATION_OFFSET, value);
    }
    public float getRotationRoll(){
        return this.getDataManager().get(ROTATION_ROLL);
    }
    public void setRotationRoll(float value){
        this.getDataManager().set(ROTATION_ROLL, value);
    }public float getBaseSize(){
        return this.getDataManager().get(BASESIZE);
    }
    public void setBaseSize(float value){
        this.getDataManager().set(BASESIZE, value);
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
