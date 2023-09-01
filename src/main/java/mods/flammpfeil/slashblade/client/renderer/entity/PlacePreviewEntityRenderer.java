package mods.flammpfeil.slashblade.client.renderer.entity;

import com.mojang.blaze3d.vertex.PoseStack;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.entity.PlacePreviewEntity;
import mods.flammpfeil.slashblade.init.SBItems;
import mods.flammpfeil.slashblade.util.VectorHelper;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Random;

public class PlacePreviewEntityRenderer extends EntityRenderer<PlacePreviewEntity> {

    public PlacePreviewEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(PlacePreviewEntity entityIn, float entityYaw, float partialTicks, PoseStack matrixStackIn, MultiBufferSource bufferIn, int packedLightIn) {
        BlockState blockstate = entityIn.getBlockState();
        if (blockstate.getRenderShape() == RenderShape.MODEL) {
            Level world = entityIn.getCommandSenderWorld();
            if (blockstate != world.getBlockState(entityIn.blockPosition()) && blockstate.getRenderShape() != RenderShape.INVISIBLE) {
                matrixStackIn.pushPose();
                Player player = Minecraft.getInstance().player;
                Vec3 pos = player.getLookAngle().add(0,0.5,0).scale(3.0).align(EnumSet.of(Direction.Axis.X,Direction.Axis.Y,Direction.Axis.Z));

                Vec3 offset = entityIn.position().subtract(player.position()).scale(-1).align(EnumSet.of(Direction.Axis.X,Direction.Axis.Y,Direction.Axis.Z));
                matrixStackIn.translate(offset.x, offset.y, offset.z);
                matrixStackIn.translate(pos.x, pos.y+1.0f, pos.z);

                //BlockPos blockpos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
                BlockPos blockpos = new BlockPos(VectorHelper.f2i(entityIn.getX(), entityIn.getBoundingBox().maxY, entityIn.getZ()));
                matrixStackIn.translate(0.5D, 0.5D, 0.0D);
                BlockRenderDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRenderer();
                Iterator var11 = RenderType.chunkBufferLayers().iterator();
                /*
                while(var11.hasNext()) {
                    RenderType type = (RenderType)var11.next();
                    if (RenderTypeLookup.canRenderInLayer(blockstate, type)) {
                        ForgeHooksClient.setRenderLayer(type);
                        blockrendererdispatcher.getBlockModelRenderer().renderModel(world, blockrendererdispatcher.getModelForState(blockstate), blockstate, blockpos, matrixStackIn, bufferIn.getBuffer(type), false, new Random(), blockstate.getPositionRandom(entityIn.getPosition()), OverlayTexture.NO_OVERLAY);
                    }
                }
                */
                {

                    RenderType type = BladeRenderState.getPlacePreviewBlendLuminous(this.getTextureLocation(entityIn));
                    //ForgeHooksClient.setRenderLayer(type);
                    blockrendererdispatcher.getModelRenderer().tesselateBlock(world, blockrendererdispatcher.getBlockModel(blockstate), blockstate, blockpos, matrixStackIn, bufferIn.getBuffer(type), false, RandomSource.create(), blockstate.getSeed(entityIn.blockPosition()), OverlayTexture.NO_OVERLAY, net.minecraftforge.client.model.data.ModelData.EMPTY, null);
                }

                //ForgeHooksClient.setRenderLayer((RenderType)null);
                matrixStackIn.popPose();
                super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
            }
        }

    }

    public ResourceLocation getTextureLocation(PlacePreviewEntity entity) {
        return TextureAtlas.LOCATION_BLOCKS;
    }

}
