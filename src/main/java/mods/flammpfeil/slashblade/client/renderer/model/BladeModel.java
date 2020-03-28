package mods.flammpfeil.slashblade.client.renderer.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.block.BlockState;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Direction;
import net.minecraft.world.World;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.client.model.PerspectiveMapWrapper;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import javax.vecmath.Matrix4f;
import java.util.List;
import java.util.Random;

/**
 * Created by Furia on 2016/02/07.
 */
public class BladeModel implements IBakedModel {

    IBakedModel original;
    ItemOverrideList overrides;
    public BladeModel(IBakedModel original, ModelLoader loader){
        this.original = original;
        this.overrides = new ItemOverrideList(loader, null, null, ImmutableList.<ItemOverride>of()){
            @Override
            public IBakedModel getModelWithOverrides(IBakedModel originalModel, ItemStack stack, World world, LivingEntity entity) {
                user = entity;
                return super.getModelWithOverrides(originalModel, stack, world, entity);
            }

        };
    }

    public static LivingEntity user = null;

    public static ItemCameraTransforms.TransformType type = ItemCameraTransforms.TransformType.NONE;


    @Override
    public ItemOverrideList getOverrides() {
        return this.overrides;
    }


    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, Random rand) {
        return original.getQuads(state, side, rand);
    }

    @Override
    public boolean isAmbientOcclusion() {
        return original.isAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return original.isGui3d();
    }

    @Override
    public boolean isBuiltInRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleTexture() {
        return original.getParticleTexture();
        //return Minecraft.getInstance().getItemRenderer().getItemModelMesher().getParticleIcon(SlashBlade.proudSoul);
    }

    ItemCameraTransforms tf = new ItemCameraTransforms(ItemCameraTransforms.DEFAULT){
        @Override
        public ItemTransformVec3f getTransform(TransformType srctype) {
            type = srctype;
            return super.getTransform(srctype);
        }
    } ;
    @Override
    public ItemCameraTransforms getItemCameraTransforms() {
        return tf;
    }

    @Override
    public Pair<? extends IBakedModel, Matrix4f> handlePerspective(ItemCameraTransforms.TransformType cameraTransformType) {
        this.type = cameraTransformType;

        return PerspectiveMapWrapper.handlePerspective(this, ModelRotation.X0_Y0, cameraTransformType);
    }
}
