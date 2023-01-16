package mods.flammpfeil.slashblade.entity;

import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.minecraftforge.network.PlayMessages;

public class EntitySpiralSwords extends EntityAbstractSummonedSword{
    private static final EntityDataAccessor<Boolean> IT_FIRED = SynchedEntityData.defineId(EntitySpiralSwords.class, EntityDataSerializers.BOOLEAN);

    public EntitySpiralSwords(EntityType<? extends Projectile> entityTypeIn, Level worldIn) {
        super(entityTypeIn, worldIn);

        this.setPierce((byte)5);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();

        this.entityData.define(IT_FIRED, false);
    }

    public void doFire(){
        this.getEntityData().set(IT_FIRED,true);
    }
    public boolean itFired(){
        return this.getEntityData().get(IT_FIRED);
    }

    public static EntitySpiralSwords createInstance(PlayMessages.SpawnEntity packet, Level worldIn){
        return new EntitySpiralSwords(SlashBlade.RegistryEvents.SpiralSwords, worldIn);
    }

    @Override
    public void rideTick(){
        if(itFired()){
            faceEntityStandby();
            this.stopRiding();

            this.tickCount = 0;
            Vec3 dir = this.getViewVector(1.0f);
            ((EntitySpiralSwords)this).shoot(dir.x,dir.y,dir.z, 3.0f, 1.0f);
            return;
        }

        //this.startRiding()
        this.setDeltaMovement(Vec3.ZERO);
        if (canUpdate())
            this.baseTick();

        faceEntityStandby();
        //this.getVehicle().positionRider(this);

        //todo: add lifetime

        if(!getLevel().isClientSide())
            hitCheck();
    }

    private void hitCheck(){
        Vec3 positionVec = this.position();
        Vec3 dirVec = this.getViewVector(1.0f);
        EntityHitResult raytraceresult = null;


        //todo : replace TargetSelector
        EntityHitResult entityraytraceresult = this.getRayTrace(positionVec, dirVec);
        if (entityraytraceresult != null) {
            raytraceresult = entityraytraceresult;
        }

        if (raytraceresult != null && raytraceresult.getType() == HitResult.Type.ENTITY) {
            Entity entity = ((EntityHitResult)raytraceresult).getEntity();
            Entity entity1 = this.getShooter();
            if (entity instanceof Player && entity1 instanceof Player && !((Player)entity1).canHarmPlayer((Player)entity)) {
                raytraceresult = null;
                entityraytraceresult = null;
            }
        }

        if (raytraceresult != null && raytraceresult.getType() == HitResult.Type.ENTITY && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, raytraceresult)) {
            this.onHit(raytraceresult);
            this.resetAlreadyHits();
            this.hasImpulse = true;
        }
    }

    private void faceEntityStandby() {

        long cycle = 30;
        long tickOffset = 0;
        if(this.getLevel().isClientSide)
            tickOffset = 1;
        int ticks = (int)((this.getLevel().getGameTime() + tickOffset) % cycle);
        /*
        if ((getInterval() - waitTime) < ticks) {
            ticks = getInterval() - waitTime;
        }
        */


        double rotParTick = 360.0 / (double)cycle;
        double offset = getDelay();
        double degYaw = (ticks * rotParTick + offset) % 360.0;
        double yaw = Math.toRadians(degYaw);

        Vec3 dir = new Vec3(0,0,1);

        //yaw
        dir = dir.yRot((float)-yaw);
        dir = dir.normalize().scale(2);

        if (this.getVehicle() != null) {
            dir = dir.add(this.getVehicle().position());
            dir = dir.add(0, this.getVehicle().getEyeHeight() / 2.0, 0);
        }

        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();

        //■初期位置・初期角度等の設定
        setPos(dir);

        final double pitchFactor = 7.5;

        //Vec2 rot = rotate(rotMat.last().pose());
        //setRot((float)Math.toDegrees(rot.y), (float)Math.toDegrees(rot.x));

        //setRot((float)degYaw,0);
        //setRot((float) (-degYaw),(float)(-(pitchFactor) * Math.sin(yaw))/**/);
        setRot((float) (-degYaw),0);




    }

}
