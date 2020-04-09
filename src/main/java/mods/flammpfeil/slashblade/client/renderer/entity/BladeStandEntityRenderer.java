package mods.flammpfeil.slashblade.client.renderer.entity;

import com.mojang.blaze3d.platform.GlStateManager;
import mods.flammpfeil.slashblade.entity.BladeStandEntity;
import mods.flammpfeil.slashblade.item.SBItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.texture.AtlasTexture;
import net.minecraft.entity.item.ItemFrameEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.math.BlockPos;

public class BladeStandEntityRenderer extends ItemFrameRenderer {
    private final net.minecraft.client.renderer.ItemRenderer itemRenderer;

    public BladeStandEntityRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, Minecraft.getInstance().getItemRenderer());

        this.itemRenderer = Minecraft.getInstance().getItemRenderer();
    }
    @Override
    public void doRender(ItemFrameEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {
        if (entity instanceof BladeStandEntity) {
            doRender((BladeStandEntity) entity, x, y, z, entityYaw, partialTicks);
        }
    }

    public void doRender(BladeStandEntity entity, double x, double y, double z, float entityYaw, float partialTicks) {

        if(entity.currentTypeStack.isEmpty()){
            if(entity.currentType == null || entity.currentType == Items.AIR){
                entity.currentTypeStack = new ItemStack(Items.ITEM_FRAME);
            }else{
                entity.currentTypeStack = new ItemStack(entity.currentType);
            }
            entity.currentTypeStack.setItemFrame(entity);
        }


        GlStateManager.pushMatrix();
        {
            BlockPos blockpos = entity.getHangingPosition();
            double d0 = (double) blockpos.getX() - entity.posX + x;
            double d1 = (double) blockpos.getY() - entity.posY + y;
            double d2 = (double) blockpos.getZ() - entity.posZ + z;
            GlStateManager.translated(d0 + 0.5D, d1 + 0.5D, d2 + 0.5D);
            GlStateManager.rotatef(entity.rotationPitch, 1.0F, 0.0F, 0.0F);
            GlStateManager.rotatef(180.0F - entity.rotationYaw, 0.0F, 1.0F, 0.0F);
            this.renderManager.textureManager.bindTexture(AtlasTexture.LOCATION_BLOCKS_TEXTURE);

            GlStateManager.enableLighting();

            if (this.renderOutlines) {
                GlStateManager.enableColorMaterial();
                GlStateManager.setupSolidRenderingTextureCombine(this.getTeamColor(entity));
            }

            GlStateManager.pushMatrix();
            {
                int i = entity.getRotation();
                GlStateManager.rotatef((float)i * 360.0F / 8.0F, 0.0F, 0.0F, 1.0F);


                GlStateManager.scaled(2,2,2);
                Item type = entity.currentType;
                if(type == SBItems.bladestand_1) {
                    GlStateManager.rotatef(-90,1,0,0);
                }else if(type == SBItems.bladestand_2){
                    GlStateManager.rotatef(-90,1,0,0);
                }else if(type == SBItems.bladestand_v){
                    GlStateManager.rotatef(-90,1,0,0);
                }else if(type == SBItems.bladestand_s){
                    GlStateManager.rotatef(-90,1,0,0);
                }else if(type == SBItems.bladestand_1w){
                    GlStateManager.rotatef(180,0,1,0);
                }else if(type == SBItems.bladestand_2w){
                    GlStateManager.rotatef(180,0,1,0);
                }

                //stand render
                renderItem(entity.currentTypeStack);

                if(entity.currentType == SBItems.bladestand_1w || type == SBItems.bladestand_2w){
                    GlStateManager.translatef(0,0,-0.19f);
                }else if(entity.currentType == SBItems.bladestand_1){
                }
                //blade render
                GlStateManager.rotatef(-180,0,1,0);
                this.renderItem(entity.getDisplayedItem());

            }GlStateManager.popMatrix();


            if (this.renderOutlines) {
                GlStateManager.tearDownSolidRenderingTextureCombine();
                GlStateManager.disableColorMaterial();
            }

            GlStateManager.enableLighting();
        }GlStateManager.popMatrix();

        this.renderName(entity, x + (double)((float)entity.getHorizontalFacing().getXOffset() * 0.3F), y - 0.25D, z + (double)((float)entity.getHorizontalFacing().getZOffset() * 0.3F));




        //super.doRender(entity, x, y, z, entityYaw, partialTicks);
    }

    private void renderItem(ItemStack itemstack) {
        if (!itemstack.isEmpty()) {
            this.itemRenderer.renderItem(itemstack, ItemCameraTransforms.TransformType.FIXED);
        }
    }

}
