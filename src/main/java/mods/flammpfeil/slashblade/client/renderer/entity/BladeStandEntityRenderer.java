package mods.flammpfeil.slashblade.client.renderer.entity;

import com.mojang.blaze3d.matrix.MatrixStack;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.entity.BladeStandEntity;
import mods.flammpfeil.slashblade.item.SBItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Vector3f;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.model.IBakedModel;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class BladeStandEntityRenderer extends ItemFrameRenderer {
    private final net.minecraft.client.renderer.ItemRenderer itemRenderer;

    public BladeStandEntityRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, Minecraft.getInstance().getItemRenderer());

        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }

    @Override
    public void render(ItemFrameEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        if (entity instanceof BladeStandEntity) {
            doRender((BladeStandEntity) entity, entityYaw, partialTicks, matrixStackIn, bufferIn, packedLightIn);
        }
    }

    public void doRender(BladeStandEntity entity, float entityYaw, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {

        if(entity.currentTypeStack.isEmpty()){
            if(entity.currentType == null || entity.currentType == Items.AIR){
                entity.currentTypeStack = new ItemStack(Items.ITEM_FRAME);
            }else{
                entity.currentTypeStack = new ItemStack(entity.currentType);
            }
            entity.currentTypeStack.setItemFrame(entity);
        }


        try(MSAutoCloser msac = MSAutoCloser.pushMatrix(matrixStackIn)){
            BlockPos blockpos = entity.getHangingPosition();
            Vec3d vec = new Vec3d(blockpos).subtract(entity.getPositionVec()).add(0.5,0.75,0.5);
            matrixStackIn.translate(vec.x, vec.y, vec.z);
            matrixStackIn.rotate(Vector3f.XP.rotationDegrees(entity.rotationPitch));
            matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180.0F - entity.rotationYaw));

            try(MSAutoCloser msacB = MSAutoCloser.pushMatrix(matrixStackIn)){
                int i = entity.getRotation();
                matrixStackIn.rotate(Vector3f.ZP.rotationDegrees((float)i * 360.0F / 8.0F));


                matrixStackIn.scale(2,2,2);
                Item type = entity.currentType;
                if(type == SBItems.bladestand_1) {
                    matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90f));
                }else if(type == SBItems.bladestand_2){
                    matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90f));
                }else if(type == SBItems.bladestand_v){
                    matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90f));
                }else if(type == SBItems.bladestand_s){
                    matrixStackIn.rotate(Vector3f.XP.rotationDegrees(-90f));
                }else if(type == SBItems.bladestand_1w){
                    matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180f));
                    matrixStackIn.translate(0,0,-0.15f);
                }else if(type == SBItems.bladestand_2w){
                    matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180f));
                    matrixStackIn.translate(0,0,-0.15f);
                }

                //stand render
                matrixStackIn.push();
                matrixStackIn.rotate(Vector3f.XP.rotationDegrees(90));
                matrixStackIn.scale(0.5f,0.5f,0.5f);
                matrixStackIn.translate(0,0,0.44);
                this.renderItem(entity, entity.currentTypeStack, matrixStackIn, bufferIn, packedLightIn);
                matrixStackIn.pop();

                if(entity.currentType == SBItems.bladestand_1w || type == SBItems.bladestand_2w){
                    matrixStackIn.translate(0,0,-0.19f);
                }else if(entity.currentType == SBItems.bladestand_1){
                }
                //blade render
                matrixStackIn.rotate(Vector3f.YP.rotationDegrees(-180f));
                this.renderItem(entity, entity.getDisplayedItem(), matrixStackIn, bufferIn, packedLightIn);

            }
        }

        net.minecraftforge.client.event.RenderNameplateEvent renderNameplateEvent = new net.minecraftforge.client.event.RenderNameplateEvent(entity, entity.getDisplayName().getFormattedText(), this, matrixStackIn, bufferIn, packedLightIn);
        net.minecraftforge.common.MinecraftForge.EVENT_BUS.post(renderNameplateEvent);
        if (renderNameplateEvent.getResult() != net.minecraftforge.eventbus.api.Event.Result.DENY && (renderNameplateEvent.getResult() == net.minecraftforge.eventbus.api.Event.Result.ALLOW || this.canRenderName(entity))) {
            this.renderName(entity, renderNameplateEvent.getContent(), matrixStackIn, bufferIn, packedLightIn);
        }
    }

    private void renderItem(BladeStandEntity entity, ItemStack itemstack, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, int packedLightIn) {
        if (!itemstack.isEmpty()) {
            IBakedModel ibakedmodel = this.itemRenderer.getItemModelWithOverrides(itemstack, entity.world, (LivingEntity)null);
            this.itemRenderer.renderItem(itemstack, ItemCameraTransforms.TransformType.FIXED, false, matrixStackIn, bufferIn, packedLightIn, OverlayTexture.NO_OVERLAY, ibakedmodel);
        }
    }

}
