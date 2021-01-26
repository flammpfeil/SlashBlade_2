package mods.flammpfeil.slashblade.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileHelper;
import net.minecraft.util.math.*;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Predicate;

public class RayTraceHelper {

    public static Optional<RayTraceResult> rayTrace(World worldIn, Entity entityIn, Vector3d start, Vector3d dir, double blockReach, double entityReach, Predicate<Entity> selector){
        Vector3d end = start.add(dir.scale(blockReach));

        RayTraceResult raytraceresult = worldIn.rayTraceBlocks(new RayTraceContext(start, end, RayTraceContext.BlockMode.COLLIDER, RayTraceContext.FluidMode.NONE, entityIn));
        if (raytraceresult.getType() != RayTraceResult.Type.MISS) {
            end = raytraceresult.getHitVec();
            entityReach = start.distanceTo(end);
        }else{
            end = start.add(dir.scale(entityReach));
        }

        AxisAlignedBB area = entityIn.getBoundingBox().expand(dir.scale(entityReach)).grow(1.0D);
        EntityRayTraceResult entityraytraceresult =
                rayTrace(worldIn, entityIn, start, end, area, selector);

        if(entityraytraceresult != null){
            raytraceresult = entityraytraceresult;
        }

        return Optional.ofNullable(raytraceresult);
    }


    @Nullable
    public static EntityRayTraceResult rayTrace(World worldIn, Entity entityIn, Vector3d start, Vector3d end, AxisAlignedBB boundingBox, Predicate<Entity> selector){
        return rayTrace(worldIn, entityIn, start, end, boundingBox, selector, Double.MAX_VALUE);
    }

    @Nullable
    public static EntityRayTraceResult rayTrace(World worldIn, Entity entityIn, Vector3d start, Vector3d end, AxisAlignedBB boundingBox, Predicate<Entity> selector, double limitDist) {
        double currentDist = limitDist;
        Entity resultEntity = null;

        for(Entity foundEntity : worldIn.getEntitiesInAABBexcluding(entityIn, boundingBox, selector)) {
            AxisAlignedBB axisalignedbb = foundEntity.getBoundingBox().grow((double)0.3F);
            Optional<Vector3d> optional = axisalignedbb.rayTrace(start, end);
            if (optional.isPresent()) {
                double newDist = start.squareDistanceTo(optional.get());
                if (newDist < currentDist) {
                    resultEntity = foundEntity;
                    currentDist = newDist;
                }
            }
        }

        if (resultEntity == null) {
            return null;
        } else {
            return new EntityRayTraceResult(resultEntity);
        }
    }
}
