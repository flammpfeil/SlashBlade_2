package mods.flammpfeil.slashblade.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.entity.PlacePreviewEntity;
import mods.flammpfeil.slashblade.init.SBItems;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockRendererDispatcher;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.FallingBlockEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.World;
import net.minecraftforge.client.ForgeHooksClient;

import java.util.EnumSet;
import java.util.Iterator;
import java.util.Random;

public class PlacePreviewEntityRenderer extends EntityRenderer<PlacePreviewEntity> {

    public PlacePreviewEntityRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn);
    }

    @Override
    public void render(PlacePreviewEntity entityIn, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        BlockState blockstate = entityIn.getBlockState();
        if (blockstate.getRenderType() == BlockRenderType.MODEL) {
            World world = entityIn.getEntityWorld();
            if (blockstate != world.getBlockState(entityIn.getPosition()) && blockstate.getRenderType() != BlockRenderType.INVISIBLE) {
                matrixStackIn.push();
                PlayerEntity player = Minecraft.getInstance().player;
                Vector3d pos = player.getLookVec().add(0,0.5,0).scale(3.0).align(EnumSet.of(Direction.Axis.X,Direction.Axis.Y,Direction.Axis.Z));

                Vector3d offset = entityIn.getPositionVec().subtract(player.getPositionVec()).scale(-1).align(EnumSet.of(Direction.Axis.X,Direction.Axis.Y,Direction.Axis.Z));
                matrixStackIn.translate(offset.x, offset.y, offset.z);
                matrixStackIn.translate(pos.x, pos.y+1.0f, pos.z);

                //BlockPos blockpos = new BlockPos(pos.getX(), pos.getY(), pos.getZ());
                BlockPos blockpos = new BlockPos(entityIn.getPosX(), entityIn.getBoundingBox().maxY, entityIn.getPosZ());
                matrixStackIn.translate(0.5D, 0.5D, 0.0D);
                BlockRendererDispatcher blockrendererdispatcher = Minecraft.getInstance().getBlockRendererDispatcher();
                Iterator var11 = RenderType.getBlockRenderTypes().iterator();
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

                    RenderType type = BladeRenderState.getPlacePreviewBlendLuminous(this.getEntityTexture(entityIn));
                    ForgeHooksClient.setRenderLayer(type);
                    blockrendererdispatcher.getBlockModelRenderer().renderModel(world, blockrendererdispatcher.getModelForState(blockstate), blockstate, blockpos, matrixStackIn, bufferIn.getBuffer(type), false, new Random(), blockstate.getPositionRandom(entityIn.getPosition()), OverlayTexture.NO_OVERLAY);
                }

                ForgeHooksClient.setRenderLayer((RenderType)null);
                matrixStackIn.pop();
                super.render(entityIn, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
            }
        }

    }

    public ResourceLocation getEntityTexture(PlacePreviewEntity entity) {
        return AtlasTexture.LOCATION_BLOCKS_TEXTURE;
    }

}
