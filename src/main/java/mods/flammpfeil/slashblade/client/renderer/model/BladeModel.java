package mods.flammpfeil.slashblade.client.renderer.model;

import com.google.common.collect.ImmutableList;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverride;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Random;

/**
 * Created by Furia on 2016/02/07.
 */
public class BladeModel implements BakedModel {

    BakedModel original;
    ItemOverrides overrides;
    public BladeModel(BakedModel original, ModelBakery loader){
        this.original = original;
        this.overrides = new ItemOverrides(loader, null, null, ImmutableList.<ItemOverride>of()){
            @Nullable
            @Override
            public BakedModel resolve(BakedModel p_173465_, ItemStack p_173466_, @Nullable ClientLevel p_173467_, @Nullable LivingEntity p_173468_, int p_173469_) {
                user = p_173468_;
                return super.resolve(p_173465_, p_173466_, p_173467_, p_173468_, p_173469_);
            }
        };
    }

    public static LivingEntity user = null;

    //public static ItemCameraTransforms.TransformType type = ItemCameraTransforms.TransformType.NONE;


    @Override
    public ItemOverrides getOverrides() {
        return this.overrides;
    }

    @Override
    public List<BakedQuad> getQuads(@org.jetbrains.annotations.Nullable BlockState p_235039_, @org.jetbrains.annotations.Nullable Direction p_235040_, RandomSource p_235041_) {
        return original.getQuads(p_235039_, p_235040_, p_235041_);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return original.useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return original.isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return true;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return original.getParticleIcon();
        //return Minecraft.getInstance().getItemRenderer().getItemModelMesher().getParticleIcon(SlashBlade.proudSoul);
    }

    /*
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
    */

    /*
    @Override
    public IBakedModel handlePerspective(ItemCameraTransforms.TransformType cameraTransformType, MatrixStack mat) {
        this.type = cameraTransformType;
        return net.minecraftforge.client.ForgeHooksClient.handlePerspective(getBakedModel(), cameraTransformType, mat);
    }
    */
}
