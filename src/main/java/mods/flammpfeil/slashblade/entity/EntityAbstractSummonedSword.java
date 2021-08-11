package mods.flammpfeil.slashblade.entity;

import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.ability.StunManager;
import mods.flammpfeil.slashblade.util.CustomDamageSource;
import mods.flammpfeil.slashblade.util.EnumSetConverter;
import mods.flammpfeil.slashblade.util.NBTHelper;
import mods.flammpfeil.slashblade.util.TargetSelector;
import net.minecraft.block.BlockState;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.EndermanEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
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
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.fml.network.FMLPlayMessages;
import net.minecraftforge.fml.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class EntityAbstractSummonedSword extends ProjectileEntity implements IShootable {
    private static final DataParameter<Integer> COLOR = EntityDataManager.<Integer>createKey(EntityAbstractSummonedSword.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> FLAGS = EntityDataManager.<Integer>createKey(EntityAbstractSummonedSword.class, DataSerializers.VARINT);
    private static final DataParameter<Integer> HIT_ENTITY_ID = EntityDataManager.<Integer>createKey(EntityAbstractSummonedSword.class, DataSerializers.VARINT);
    private static final DataParameter<Float> OFFSET_YAW = EntityDataManager.<Float>createKey(EntityAbstractSummonedSword.class, DataSerializers.FLOAT);
    private static final DataParameter<Float> ROLL = EntityDataManager.<Float>createKey(EntityAbstractSummonedSword.class, DataSerializers.FLOAT);
    private static final DataParameter<Byte> PIERCE = EntityDataManager.createKey(EntityAbstractSummonedSword.class, DataSerializers.BYTE);
    private static final DataParameter<String> MODEL = EntityDataManager.createKey(EntityAbstractSummonedSword.class, DataSerializers.STRING);
    private static final DataParameter<Integer> DELAY = EntityDataManager.<Integer>createKey(EntityAbstractSummonedSword.class, DataSerializers.VARINT);

    private int ticksInGround;
    private boolean inGround;
    private BlockState inBlockState;
    private int ticksInAir;
    private double damage = 1.0D;
    
    private IntOpenHashSet alreadyHits;
    public UUID shootingEntity;

    private Entity hitEntity = null;

    static final int ON_GROUND_LIFE_TIME = 20*5;

    private SoundEvent hitEntitySound = SoundEvents.ITEM_TRIDENT_HIT;
    private SoundEvent hitEntityPlayerSound = SoundEvents.ITEM_TRIDENT_HIT;
    private SoundEvent hitGroundSound = SoundEvents.ITEM_TRIDENT_HIT_GROUND;
    protected SoundEvent getHitEntitySound() {
        return this.hitEntitySound;
    }
    protected SoundEvent getHitEntityPlayerSound() {
        return this.hitEntityPlayerSound;
    }
    protected SoundEvent getHitGroundSound() {
        return this.hitGroundSound;
    }

    public EntityAbstractSummonedSword(EntityType<? extends ProjectileEntity> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
        this.setNoGravity(true);
        //this.setGlowing(true);
    }

    public static EntityAbstractSummonedSword createInstance(FMLPlayMessages.SpawnEntity packet, World worldIn){
        return new EntityAbstractSummonedSword(SlashBlade.RegistryEvents.SummonedSword, worldIn);
    }

    @Override
    protected void registerData() {
        this.dataManager.register(COLOR, 0x3333FF);
        this.dataManager.register(FLAGS, 0);
        this.dataManager.register(HIT_ENTITY_ID, -1);
        this.dataManager.register(OFFSET_YAW, 0f);
        this.dataManager.register(ROLL, 0f);
        this.dataManager.register(PIERCE, (byte)0);
        this.dataManager.register(MODEL, "");
        this.dataManager.register(DELAY,10);
    }

    @Override
    public void writeAdditional(CompoundNBT compound) {

        NBTHelper.getNBTCoupler(compound)
                .put("Color", this.getColor())
                .put("life", (short)this.ticksInGround)
                .put("inBlockState", (this.inBlockState != null ? NBTUtil.writeBlockState(this.inBlockState) : null))
                .put("inGround", this.inGround)
                .put("damage", this.damage)
                .put("crit", this.getIsCritical())
                .put("clip", this.isNoClip())
                .put("PierceLevel", this.getPierce())
                .put("OwnerUUID", this.shootingEntity)
                .put("model", this.getModelName())
                .put("Delay", this.getDelay());
    }

    @Override
    public void readAdditional(CompoundNBT compound) {
        NBTHelper.getNBTCoupler(compound)
                .get("Color", this::setColor)
                .get("life",((Integer v)->this.ticksInGround = v))
                .get("inBlockState",((CompoundNBT v)->this.inBlockState = NBTUtil.readBlockState(v)))
                .get("inGround",  ((Boolean v)->this.inGround = v))
                .get("damage",  ((Double v)->this.damage = v), this.damage)
                .get("crit",this::setIsCritical)
                .get("clip",this::setNoClip)
                .get("PierceLevel",this::setPierce)
                .get("OwnerUUID",  ((UUID v)->this.shootingEntity = v), true)
                .get("model",this::setModelName)
                .get("Delay",this::setDelay);
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        Vector3d vec3d = (new Vector3d(x, y, z)).normalize().add(this.rand.nextGaussian() * (double)0.0075F * (double)inaccuracy, this.rand.nextGaussian() * (double)0.0075F * (double)inaccuracy, this.rand.nextGaussian() * (double)0.0075F * (double)inaccuracy).scale((double)velocity);
        this.setMotion(vec3d);
        float f = MathHelper.sqrt(horizontalMag(vec3d));
        this.rotationYaw = (float)(MathHelper.atan2(vec3d.x, vec3d.z) * (double)(180F / (float)Math.PI));
        this.rotationPitch = (float)(MathHelper.atan2(vec3d.y, (double)f) * (double)(180F / (float)Math.PI));
        this.prevRotationYaw = this.rotationYaw;
        this.prevRotationPitch = this.rotationPitch;
        this.ticksInGround = 0;
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
        this.setMotion(x, y, z);
        if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
            float f = MathHelper.sqrt(x * x + z * z);
            this.rotationPitch = (float)(MathHelper.atan2(y, (double)f) * (double)(180F / (float)Math.PI));
            this.rotationYaw = (float)(MathHelper.atan2(x, z) * (double)(180F / (float)Math.PI));
            this.prevRotationPitch = this.rotationPitch;
            this.prevRotationYaw = this.rotationYaw;
            this.setLocationAndAngles(this.getPosX(), this.getPosY(), this.getPosZ(), this.rotationYaw, this.rotationPitch);
            this.ticksInGround = 0;
        }

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

        if(getHitEntity() != null){
            Entity hits = getHitEntity();

            if(!hits.isAlive()){
                this.burst();
            }else{
                this.setPosition(hits.getPosX(),hits.getPosY() + hits.getEyeHeight() * 0.5f,hits.getPosZ());

                int delay = getDelay();
                delay--;
                setDelay(delay);

                if(!this.world.isRemote && delay < 0)
                    this.burst();
            }

            return;
        }

        boolean disallowedHitBlock = this.isNoClip();

        BlockPos blockpos = new BlockPos(this.getPosX(), this.getPosY(), this.getPosZ());
        BlockState blockstate = this.world.getBlockState(blockpos);
        if (!blockstate.isAir(this.world, blockpos) && !disallowedHitBlock) {
            VoxelShape voxelshape = blockstate.getCollisionShape(this.world, blockpos);
            if (!voxelshape.isEmpty()) {
                for(AxisAlignedBB axisalignedbb : voxelshape.toBoundingBoxList()) {
                    if (axisalignedbb.offset(blockpos).contains(new Vector3d(this.getPosX(), this.getPosY(), this.getPosZ()))) {
                        this.inGround = true;
                        break;
                    }
                }
            }
        }

        if (this.isWet()) {
            this.extinguish();
        }

        if (this.inGround && !disallowedHitBlock) {
            if (this.inBlockState != blockstate && this.world.hasNoCollisions(this.getBoundingBox().grow(0.06D))) {
                //block breaked
                this.burst();
            } else if (!this.world.isRemote) {
                //onBlock
                this.tryDespawn();
            }
        } else {
            //process pose
            Vector3d motionVec = this.getMotion();
            if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
                float f = MathHelper.sqrt(horizontalMag(motionVec));
                this.rotationYaw = (float)(MathHelper.atan2(motionVec.x, motionVec.z) * (double)(180F / (float)Math.PI));
                this.rotationPitch = (float)(MathHelper.atan2(motionVec.y, (double)f) * (double)(180F / (float)Math.PI));
                this.prevRotationYaw = this.rotationYaw;
                this.prevRotationPitch = this.rotationPitch;
            }

            //process inAir
            ++this.ticksInAir;
            Vector3d positionVec = this.getPositionVec();
            Vector3d movedVec = positionVec.add(motionVec);
            RayTraceResult raytraceresult = this.world.rayTraceBlocks(new RayTraceContext(positionVec, movedVec, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, this));
            if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
                movedVec = raytraceresult.getHitVec();
            }

            while(this.isAlive()) {
                //todo : replace TargetSelector
                EntityRayTraceResult entityraytraceresult = this.getRayTrace(positionVec, movedVec);
                if (entityraytraceresult != null) {
                    raytraceresult = entityraytraceresult;
                }

                if (raytraceresult != null && raytraceresult.getType() == RayTraceResult.Type.ENTITY) {
                    Entity entity = ((EntityRayTraceResult)raytraceresult).getEntity();
                    Entity entity1 = this.getShooter();
                    if (entity instanceof PlayerEntity && entity1 instanceof PlayerEntity && !((PlayerEntity)entity1).canAttackPlayer((PlayerEntity)entity)) {
                        raytraceresult = null;
                        entityraytraceresult = null;
                    }
                }

                if (raytraceresult != null && !disallowedHitBlock && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
                    this.onHit(raytraceresult);
                    this.isAirBorne = true;
                }

                if (entityraytraceresult == null || this.getPierce() <= 0) {
                    break;
                }

                raytraceresult = null;
            }

            motionVec = this.getMotion();
            double mx = motionVec.x;
            double my = motionVec.y;
            double mz = motionVec.z;
            if (this.getIsCritical()) {
                for(int i = 0; i < 4; ++i) {
                    this.world.addParticle(ParticleTypes.CRIT, this.getPosX() + mx * (double)i / 4.0D, this.getPosY() + my * (double)i / 4.0D, this.getPosZ() + mz * (double)i / 4.0D, -mx, -my + 0.2D, -mz);
                }
            }

            this.setPosition(this.getPosX()+ mx, this.getPosY()+my, this.getPosZ()+mz);
            float f4 = MathHelper.sqrt(horizontalMag(motionVec));
            if (disallowedHitBlock) {
                this.rotationYaw = (float)(MathHelper.atan2(-mx, -mz) * (double)(180F / (float)Math.PI));
            } else {
                this.rotationYaw = (float)(MathHelper.atan2(mx, mz) * (double)(180F / (float)Math.PI));
            }

            for(this.rotationPitch = (float)(MathHelper.atan2(my, (double)f4) * (double)(180F / (float)Math.PI)); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
                ;
            }

            while(this.rotationPitch - this.prevRotationPitch >= 180.0F) {
                this.prevRotationPitch += 360.0F;
            }

            while(this.rotationYaw - this.prevRotationYaw < -180.0F) {
                this.prevRotationYaw -= 360.0F;
            }

            while(this.rotationYaw - this.prevRotationYaw >= 180.0F) {
                this.prevRotationYaw += 360.0F;
            }

            this.rotationPitch = MathHelper.lerp(0.2F, this.prevRotationPitch, this.rotationPitch);
            this.rotationYaw = MathHelper.lerp(0.2F, this.prevRotationYaw, this.rotationYaw);
            float f1 = 0.99F;
            float f2 = 0.05F;
            if (this.isInWater()) {
                for(int j = 0; j < 4; ++j) {
                    float f3 = 0.25F;
                    this.world.addParticle(ParticleTypes.BUBBLE, this.getPosX() - mx * 0.25D, this.getPosY() - my * 0.25D, this.getPosZ() - mz * 0.25D, mx, my, mz);
                }
            }

            this.setMotion(motionVec.scale((double)f1));
            if (!this.hasNoGravity() && !disallowedHitBlock) {
                Vector3d vec3d3 = this.getMotion();
                this.setMotion(vec3d3.x, vec3d3.y - (double)0.05F, vec3d3.z);
            }

            //this.setPosition(this.getPosX(), this.getPosY(), this.getPosZ());
            this.doBlockCollisions();
        }


        if(!this.world.isRemote && ticksInGround <= 0 && 100 < this.ticksExisted)
            this.remove();

    }

    protected void tryDespawn() {
        ++this.ticksInGround;
        if (ON_GROUND_LIFE_TIME <= this.ticksInGround) {
            this.burst();
        }

    }

    protected void onHit(RayTraceResult raytraceResultIn) {
        RayTraceResult.Type type = raytraceResultIn.getType();
        switch (type){
            case ENTITY:
                this.onHitEntity((EntityRayTraceResult)raytraceResultIn);
                break;
            case BLOCK:
                this.onHitBlock((BlockRayTraceResult)raytraceResultIn);
                break;
        }
    }
    protected void onHitBlock(BlockRayTraceResult blockraytraceresult) {
        BlockState blockstate = this.world.getBlockState(blockraytraceresult.getPos());
        this.inBlockState = blockstate;
        Vector3d vec3d = blockraytraceresult.getHitVec().subtract(this.getPosX(), this.getPosY(), this.getPosZ());
        this.setMotion(vec3d);
        Vector3d vec3d1 = this.getPositionVec().subtract(vec3d.normalize().scale((double) 0.05F));
        this.setPosition(vec3d1.x, vec3d1.y, vec3d1.z);
        this.playSound(this.getHitGroundSound(), 1.0F, 2.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
        this.inGround = true;
        this.setIsCritical(false);
        this.setPierce((byte) 0);
        this.resetAlreadyHits();
        blockstate.onProjectileCollision(this.world, blockstate, blockraytraceresult, this);
    }

    public void doForceHitEntity(Entity target){
        onHitEntity(new EntityRayTraceResult(target));
    }

    protected void onHitEntity(EntityRayTraceResult p_213868_1_) {
        Entity targetEntity = p_213868_1_.getEntity();
        int i = MathHelper.ceil(this.getDamage());
        if (this.getPierce() > 0) {
            if (this.alreadyHits == null) {
                this.alreadyHits = new IntOpenHashSet(5);
            }

            if (this.alreadyHits.size() >= this.getPierce() + 1) {
                this.burst();
                return;
            }

            this.alreadyHits.add(targetEntity.getEntityId());
        }

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
                Entity hits = targetEntity;
                if(targetEntity instanceof PartEntity){
                    hits = ((PartEntity) targetEntity).getParent();
                }
                ((LivingEntity)shooter).setLastAttackedEntity(hits);
            }
        }

        int fireTime = targetEntity.getFireTimer();
        if (this.isBurning() && !(targetEntity instanceof EndermanEntity)) {
            targetEntity.setFire(5);
        }

        if (targetEntity.attackEntityFrom(damagesource, (float)i)) {
            if (targetEntity instanceof LivingEntity) {
                LivingEntity targetLivingEntity = (LivingEntity)targetEntity;

                StunManager.setStun(targetLivingEntity);

                if (!this.world.isRemote && this.getPierce() <= 0) {
                    Entity hits = targetEntity;
                    if(targetEntity instanceof PartEntity){
                        hits = ((PartEntity) targetEntity).getParent();
                    }
                    setHitEntity(hits);
                }

                if (!this.world.isRemote && shooter instanceof LivingEntity) {
                    EnchantmentHelper.applyThornEnchantments(targetLivingEntity, shooter);
                    EnchantmentHelper.applyArthropodEnchantments((LivingEntity)shooter, targetLivingEntity);
                }

                //this.arrowHit(targetLivingEntity);

                affectEntity(targetLivingEntity, getPotionEffects(), 1.0f);

                if (shooter != null && targetLivingEntity != shooter && targetLivingEntity instanceof PlayerEntity && shooter instanceof ServerPlayerEntity) {
                    ((ServerPlayerEntity) shooter).playSound(this.getHitEntityPlayerSound(), SoundCategory.PLAYERS, 0.18F, 0.45F);
                }
            }

            this.playSound(this.getHitEntitySound(), 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
            if (this.getPierce() <= 0 && (getHitEntity() == null || !getHitEntity().isAlive())) {
                this.burst();
            }
        } else {
            targetEntity.forceFireTicks(fireTime);
            //this.setMotion(this.getMotion().scale(-0.1D));
            this.rotationYaw += 180.0F;
            this.prevRotationYaw += 180.0F;
            this.ticksInAir = 0;
            if (!this.world.isRemote && this.getMotion().lengthSquared() < 1.0E-7D) {
                this.burst();
            }
        }

    }

    public int getColor(){
        return this.getDataManager().get(COLOR);
    }
    public void setColor(int value){
        this.getDataManager().set(COLOR,value);
    }

    public byte getPierce(){
        return this.getDataManager().get(PIERCE);
    }
    public void setPierce(byte value){
        this.getDataManager().set(PIERCE,(byte)value);
    }

    public int getDelay(){
        return this.getDataManager().get(DELAY);
    }
    public void setDelay(int value){
        this.getDataManager().set(DELAY,value);
    }

    @Nullable
    protected EntityRayTraceResult getRayTrace(Vector3d p_213866_1_, Vector3d p_213866_2_) {
        return ProjectileHelper.rayTraceEntities(this.world, this, p_213866_1_, p_213866_2_, this.getBoundingBox().expand(this.getMotion()).grow(1.0D), (p_213871_1_) -> {
            return !p_213871_1_.isSpectator() && p_213871_1_.isAlive() && p_213871_1_.canBeCollidedWith() && (p_213871_1_ != this.getShooter() || this.ticksInAir >= 5) && (this.alreadyHits == null || !this.alreadyHits.contains(p_213871_1_.getEntityId()));
        });
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
        this.playSound(SoundEvents.BLOCK_GLASS_BREAK, 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));

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

    private void resetAlreadyHits() {
        if(this.alreadyHits != null)
            alreadyHits.clear();
    }

    public void setHitEntity(Entity hitEntity) {
        if(hitEntity != this){
            this.dataManager.set(HIT_ENTITY_ID ,hitEntity.getEntityId());

            this.dataManager.set(OFFSET_YAW, this.rand.nextFloat() * 360);

            this.setDelay(20 * 5);
        }
    }

    @Nullable
    public Entity getHitEntity() {
        if(hitEntity == null){
            int id = this.dataManager.get(HIT_ENTITY_ID);
            if(0 <= id){
                hitEntity = this.world.getEntityByID(id);
            }
        }
        return hitEntity;
    }

    public float getOffsetYaw(){
        return this.dataManager.get(OFFSET_YAW);
    }

    public float getRoll(){
        return this.dataManager.get(ROLL);
    }
    public void setRoll(float value){
        this.dataManager.set(ROLL, value);
    }

    public void setDamage(double damageIn) {
        this.damage = damageIn;
    }

    @Override
    public double getDamage() {
        return this.damage;
    }


    private static final String defaultModelName = "slashblade:model/util/ss";

    public void setModelName(String name){
        this.dataManager.set(MODEL, Optional.ofNullable(name).orElse(defaultModelName));
    }

    public String getModelName(){
        String name = this.dataManager.get(MODEL);
        if(name == null || name.length() == 0){
            name = defaultModelName;
        }
        return name;
    }

    private static final ResourceLocation defaultModel =  new ResourceLocation(defaultModelName + ".obj");
    public LazyOptional<ResourceLocation> modelLoc = LazyOptional.of(() -> new ResourceLocation(getModelName() + ".obj"));
    private static final ResourceLocation defaultTexture =  new ResourceLocation(defaultModelName + ".png");
    public LazyOptional<ResourceLocation> textureLoc = LazyOptional.of(() -> new ResourceLocation(getModelName() + ".png"));

    public ResourceLocation getModelLoc() {
        return modelLoc.orElse(defaultModel);
    }
    public ResourceLocation getTextureLoc() {
        return textureLoc.orElse(defaultTexture);
    }


    @Override
    public void applyEntityCollision(Entity entityIn) {
        //Suppress velocity change due to collision
        //super.applyEntityCollision(entityIn);
    }
}
