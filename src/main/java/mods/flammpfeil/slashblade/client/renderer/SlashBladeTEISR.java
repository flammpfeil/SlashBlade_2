package mods.flammpfeil.slashblade.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import mods.flammpfeil.slashblade.client.renderer.model.BladeFirstPersonRender;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModel;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.Face;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.event.client.RenderOverrideEvent;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.SwordType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Color4f;
import java.awt.*;
import java.util.EnumSet;

public class SlashBladeTEISR extends ItemStackTileEntityRenderer {

    private void bindTexture(ResourceLocation res){
        Minecraft.getInstance().getTextureManager().bindTexture(res);
    }

    ItemStack current = null;

    @Override
    public void renderByItem(ItemStack itemStackIn) {
        if(!(itemStackIn.getItem() instanceof ItemSlashBlade)) return;
        ItemSlashBlade item = (ItemSlashBlade)itemStackIn.getItem();

        CompoundNBT capsTag = itemStackIn.getChildTag(ItemSlashBlade.ICON_TAG_KEY);
        if(capsTag != null){
            itemStackIn.readShareTag(capsTag);
            itemStackIn.removeChildTag(ItemSlashBlade.ICON_TAG_KEY);
        }

        current = itemStackIn;

        render();

        if(itemStackIn.hasEffect()){
            renderEffect(this::render);
        }

    }

    private void renderEffect(Runnable renderModelFunction) {
        GlStateManager.color3f(0.5019608F, 0.2509804F, 0.8F);
        Minecraft.getInstance().getTextureManager().bindTexture(ItemRenderer.RES_ITEM_GLINT);
        ItemRenderer.renderEffect(Minecraft.getInstance().getTextureManager(), renderModelFunction, 1);
    }

    boolean checkRenderNaked(){
        ItemStack mainHand = BladeModel.user.getHeldItemMainhand();
        if(!(mainHand.getItem() instanceof ItemSlashBlade))
            return true;
/*
        if(ItemSlashBlade.hasScabbardInOffhand(BladeModel.user))
            return true;

        EnumSet<SwordType> type = SwordType.from(mainHand);
        if(type.contains(SwordType.NoScabbard))
            return true;
*/
        
        return false;
    }

    private boolean render(){

        boolean depthState = GL11.glIsEnabled(GL11.GL_DEPTH_TEST);
        if(!depthState)
            GlStateManager.enableDepthTest();


        if(BladeModel.type == ItemCameraTransforms.TransformType.THIRD_PERSON_LEFT_HAND
                || BladeModel.type == ItemCameraTransforms.TransformType.THIRD_PERSON_RIGHT_HAND
                || BladeModel.type == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND
                || BladeModel.type == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND
                || BladeModel.type == ItemCameraTransforms.TransformType.NONE) {

            if(BladeModel.user == null)
                return false;

            EnumSet<SwordType> types = SwordType.from( this.current);

            boolean handle = false;

            if(!types.contains(SwordType.NoScabbard)) {
                handle = BladeModel.user.getPrimaryHand() == HandSide.RIGHT ?
                        BladeModel.type == ItemCameraTransforms.TransformType.FIRST_PERSON_RIGHT_HAND :
                        BladeModel.type == ItemCameraTransforms.TransformType.FIRST_PERSON_LEFT_HAND;
            }

            if(handle){
                BladeFirstPersonRender.getInstance().render();
            }

            /*
            if(BladeModel.type == ItemCameraTransforms.TransformType.NONE) {
                if(checkRenderNaked()){
                    renderNaked(true);
                }
                else if(itemStackIn == BladeModel.user.getHeldItemMainhand()){
                    BladeFirstPersonRender.getInstance().renderVR();
                }
            }else {
                if(checkRenderNaked()){
                    renderNaked();
                }else if(itemStackIn == BladeModel.user.getHeldItemMainhand()){
                    BladeFirstPersonRender.getInstance().render();
                }
            }*/

            return false;
        }


        GlStateManager.pushTextureAttributes();
        BladeModel.renderPath = 1;
/*
        if(BladeModel.renderPath++ >= 1) {
            Face.setColor(0xFF8040CC);
            GL11.glMatrixMode(GL11.GL_TEXTURE);
            GlStateManager.scalef(0.1F, 0.1F, 0.1F);
            GL11.glMatrixMode(GL11.GL_MODELVIEW);
        }else{
            Face.resetColor();

            GL11.glEnable(GL11.GL_BLEND);
            GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
            GL11.glDisable(GL11.GL_CULL_FACE);


            GlStateManager.disableLighting(); //Forge: Make sure that render states are reset, ad renderEffect can derp them up.
            GL11.glEnable(GL11.GL_ALPHA_TEST);

            GL11.glAlphaFunc(GL11.GL_GEQUAL, 0.05f);
        }
        */

        try(MSAutoCloser msac = MSAutoCloser.pushMatrix()){
            GlStateManager.shadeModel(GL11.GL_SMOOTH);

            GL11.glTranslatef(0.5f, 0.5f, 0.5f);

            float scale = 0.0095f;

            if(BladeModel.type == ItemCameraTransforms.TransformType.GROUND) {
                scale = 0.005f;
                GlStateManager.translatef(0,0.15f,0);
            }

            if(BladeModel.type == ItemCameraTransforms.TransformType.GUI)
                scale = 0.008f;

            if(BladeModel.type == ItemCameraTransforms.TransformType.FIXED){
                scale = 0.0095f;
                GlStateManager.rotatef(180.0f,0,1,0);
            }

            GlStateManager.scalef(scale, scale, scale);


            EnumSet<SwordType> types = SwordType.from(current);
            //BladeModel.itemBlade.getModelLocation(itemStackIn)

            String renderTarget;
            if(types.contains(SwordType.Broken))
                renderTarget = "item_damaged";
            else if(!types.contains(SwordType.NoScabbard)){
                renderTarget = "item_blade";
            }else{
                renderTarget = "item_bladens";
            }

            this.current.getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state)->{
                ResourceLocation modelLocation = state.getModel().orElseGet(()->BladeModelManager.resourceDefaultModel);
                WavefrontObject model = BladeModelManager.getInstance().getModel(modelLocation);
                ResourceLocation textureLocation = state.getTexture().orElseGet(()->BladeModelManager.resourceDefaultTexture);

                //if(!(BladeModel.type == ItemCameraTransforms.TransformType.GUI && BladeModel.user.getHeldItemMainhand() == current))
                    renderOverrided(current, model, renderTarget, textureLocation);
            });


            GL11.glEnable(GL11.GL_BLEND);
            GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);

            //RenderHelper.enableStandardItemLighting();
            //try(LightSetup ls = LightSetup.setup()){
            this.current.getCapability(ItemSlashBlade.BLADESTATE).ifPresent((state)->{
                ResourceLocation modelLocation = state.getModel().orElseGet(()->BladeModelManager.resourceDefaultModel);
                WavefrontObject model = BladeModelManager.getInstance().getModel(modelLocation);
                ResourceLocation textureLocation = state.getTexture().orElseGet(()->BladeModelManager.resourceDefaultTexture);

                //if(!(BladeModel.type == ItemCameraTransforms.TransformType.GUI && BladeModel.user.getHeldItemMainhand() == current))
                    renderOverrided(current, model, renderTarget + "_luminous", textureLocation);
            });
            //}

            GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

            if(BladeModel.renderPath == 1 && BladeModel.type == ItemCameraTransforms.TransformType.GUI){


                WavefrontObject durabilityModel = BladeModelManager.getInstance().getModel(BladeModelManager.resourceDurabilityModel);
                bindTexture(BladeModelManager.resourceDurabilityTexture);

                float durability = this.current.getCapability(ItemSlashBlade.BLADESTATE).map(s->s.getDurabilityForDisplay()).orElse(0.0f);
                GlStateManager.translatef(0.0F, 0.0F, 0.1f);


                if(BladeModel.user != null && BladeModel.user.getHeldItemMainhand() == this.current){
                    Color4f aCol = new Color4f(new Color(0xEEEEEE));
                    GlStateManager.color4f(aCol.x,aCol.y, aCol.z, aCol.w);
                    durabilityModel.renderPart("base");
                    GlStateManager.color4f(0,0,0,1);
                    durabilityModel.renderPart("color");
                }else{

                    Color4f aCol = new Color4f(new Color(0.25f,0.25f,0.25f,1.0f));
                    Color4f bCol = new Color4f(new Color(0xA52C63));
                    aCol.interpolate(bCol,(float)durability);

                    GlStateManager.color4f(aCol.x,aCol.y, aCol.z, aCol.w);
                    durabilityModel.renderPart("base");

                    GlStateManager.color4f(1,1,1,1);

                    boolean isBroken = types.contains(SwordType.Broken);
                    GlStateManager.translated(0.0F, 0.0F, -2.0f * durability);
                    durabilityModel.renderPart(isBroken ? "color_r" : "color");
                }

            }

            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GlStateManager.enableLighting();
            GL11.glEnable(GL11.GL_CULL_FACE);

            GlStateManager.shadeModel(GL11.GL_FLAT);
        }

        GlStateManager.popAttributes();

        //Face.resetColor();

        if(!depthState)
            GlStateManager.disableDepthTest();


        return true;
    }
/*
    private void renderNaked(){
        renderNaked(false);
    }
    private void renderNaked(boolean isVR){
        LivingEntity LivingEntityIn = BladeModel.user ;
        ItemStack itemstack = itemStackIn;
        ItemSlashBlade itemBlade = BladeModel.itemBlade;


        if (!itemstack.isEmpty())
        {

            Item item = itemstack.getItem();

            boolean isScabbard = (item instanceof ItemSlashBladeWrapper && !ItemSlashBladeWrapper.hasWrapedItem(itemstack));

            if(isScabbard) {
                ItemStack mainHnad = LivingEntityIn.getHeldItemMainhand();
                if (mainHnad.getItem() instanceof ItemSlashBlade) {
                    EnumSet<SwordType> mainhandtypes = ((ItemSlashBlade) (mainHnad.getItem())).getSwordType(mainHnad);
                    if (!mainhandtypes.contains(SwordType.NoScabbard)) {
                        itemstack = mainHnad;
                    }else{
                        return;
                    }
                }
            }

            GlStateManager.pushMatrix();

            EnumSet<SwordType> swordType = itemBlade.getSwordType(itemstack);

            {
                WavefrontObject model = BladeModelManager.getInstance().getModel(itemBlade.getModelLocation(itemstack));
                ResourceLocation resourceTexture = itemBlade.getModelTexture(itemstack);
                bindTexture(resourceTexture);

                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
                GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
                GL11.glAlphaFunc(GL11.GL_GEQUAL, 0.05f);

                if(isVR) {
                    GL11.glTranslatef(-0.4f, -0.1f, -0.05f);
                }

                GL11.glTranslatef(0.5f, 0.3f, 0.55f);
                float scale = 0.008f;
                GL11.glScalef(scale,scale,scale);
                GL11.glTranslatef(0.0f, 0.15f, 0.0f);

                if(isVR) {
                    GL11.glRotatef(-90, 0, 1, 0);
                }

                GL11.glRotatef(90, 0, 1, 0);
                GL11.glRotatef(-90, 0, 0, 1);

                if(isVR) {
                    GL11.glRotatef(-43, 0, 0, 1);
                }

                if(isScabbard){
                    //GL11.glRotatef(180, 0, 0, 1);
                    GL11.glRotatef(180, 0, 1, 0);
                    GL11.glTranslatef(75.0f, 0.0f, 0.0f);
                }

                String renderTargets[];

                if(isScabbard){
                    renderTargets = new String[]{"sheath"};
                }else if(swordType.contains(SwordType.Cursed)){
                    renderTargets = new String[]{"sheath", "blade"};
                }else{
                    if(swordType.contains(SwordType.Broken)){
                        renderTargets = new String[]{"blade_damaged"};
                    }else{
                        renderTargets = new String[]{"blade"};
                    }
                }

                model.renderOnly(renderTargets);

                GlStateManager.disableLighting();
                try(LightSetup ls = LightSetup.setupAdd()){
                    for(String renderTarget : renderTargets)
                        model.renderPart(renderTarget + "_luminous");
                }

                GlStateManager.enableLighting();
            }

            GlStateManager.popMatrix();
        }
    }*/


    private void renderOverrided(ItemStack stack, WavefrontObject model, String target, ResourceLocation texture){

        try {
            GlStateManager.pushMatrix();
            GlStateManager.pushLightingAttributes();
            GlStateManager.pushTextureAttributes();

            RenderOverrideEvent event
                    = RenderOverrideEvent.onRenderOverride(stack, model, target, texture);

            if(event.isCanceled()) return;

            bindTexture(event.getTexture());

            GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
            GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);

            event.getModel().renderOnly(event.getTarget());

            GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST );
            GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST );

        }finally{
            GlStateManager.popAttributes();
            GlStateManager.popAttributes();
            GlStateManager.popMatrix();
        }
    }
}
