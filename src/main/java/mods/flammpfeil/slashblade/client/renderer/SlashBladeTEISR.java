package mods.flammpfeil.slashblade.client.renderer;

import com.mojang.blaze3d.platform.GlStateManager;
import mods.flammpfeil.slashblade.client.renderer.model.BladeFirstPersonRender;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModel;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.client.renderer.util.RenderHandler;
import mods.flammpfeil.slashblade.entity.BladeStandEntity;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.item.SBItems;
import mods.flammpfeil.slashblade.item.SwordType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.model.ItemCameraTransforms;
import net.minecraft.client.renderer.tileentity.ItemStackTileEntityRenderer;
import net.minecraft.entity.Pose;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.HandSide;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

import javax.vecmath.Color4f;
import java.awt.*;
import java.util.EnumSet;

public class SlashBladeTEISR extends ItemStackTileEntityRenderer {

    private void bindTexture(ResourceLocation res){
        Minecraft.getInstance().getTextureManager().bindTexture(res);
    }

    @Override
    public void renderByItem(ItemStack itemStackIn) {
        if(!(itemStackIn.getItem() instanceof ItemSlashBlade)) return;
        ItemSlashBlade item = (ItemSlashBlade)itemStackIn.getItem();

        CompoundNBT capsTag = itemStackIn.getChildTag(ItemSlashBlade.ICON_TAG_KEY);
        if(capsTag != null){
            itemStackIn.readShareTag(capsTag);
            itemStackIn.removeChildTag(ItemSlashBlade.ICON_TAG_KEY);
        }

        render(itemStackIn);

        if(itemStackIn.hasEffect()){
            renderEffect(()->this.render(itemStackIn));
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

    private boolean render(ItemStack stack){

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

            EnumSet<SwordType> types = SwordType.from( stack);

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

        try(MSAutoCloser msac = MSAutoCloser.pushMatrix()) {

            GL11.glTranslatef(0.5f, 0.5f, 0.5f);

            if (BladeModel.type == ItemCameraTransforms.TransformType.GROUND) {
                GlStateManager.translatef(0, 0.15f, 0);
                renderIcon(stack,0.005f);
            } else if (BladeModel.type == ItemCameraTransforms.TransformType.GUI) {
                renderIcon(stack,0.008f, true);
            } else if (BladeModel.type == ItemCameraTransforms.TransformType.FIXED) {
                if (stack.isOnItemFrame()) {
                    renderModel(stack);
                } else {
                    GlStateManager.rotatef(180.0f, 0, 1, 0);
                    renderIcon(stack,0.0095f);
                }
            }else{
                renderIcon(stack,0.0095f);
            }
        }

        GlStateManager.popAttributes();

        if(!depthState)
            GlStateManager.disableDepthTest();

        return true;
    }

    private void renderIcon(ItemStack stack, float scale){
        renderIcon(stack, scale, false);
    }
    private void renderIcon(ItemStack stack, float scale, boolean renderDurability){

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);
        GlStateManager.scalef(scale, scale, scale);

        EnumSet<SwordType> types = SwordType.from(stack);

        ResourceLocation modelLocation = stack.getCapability(ItemSlashBlade.BLADESTATE)
                .filter(s->s.getModel().isPresent())
                .map(s->s.getModel().get())
                .orElseGet(()-> BladeModelManager.resourceDefaultModel);
        WavefrontObject model = BladeModelManager.getInstance().getModel(modelLocation);
        ResourceLocation textureLocation = stack.getCapability(ItemSlashBlade.BLADESTATE)
                .filter(s->s.getTexture().isPresent())
                .map(s->s.getTexture().get())
                .orElseGet(()->BladeModelManager.resourceDefaultTexture);

        String renderTarget;
        if(types.contains(SwordType.Broken))
            renderTarget = "item_damaged";
        else if(!types.contains(SwordType.NoScabbard)){
            renderTarget = "item_blade";
        }else{
            renderTarget = "item_bladens";
        }

        RenderHandler.renderOverrided(stack, model, renderTarget, textureLocation);

        GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
        RenderHandler.renderOverrided(stack, model, renderTarget + "_luminous", textureLocation);
        GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);

        if(renderDurability){

            WavefrontObject durabilityModel = BladeModelManager.getInstance().getModel(BladeModelManager.resourceDurabilityModel);
            bindTexture(BladeModelManager.resourceDurabilityTexture);

            float durability = stack.getCapability(ItemSlashBlade.BLADESTATE).map(s->s.getDurabilityForDisplay()).orElse(0.0f);
            GlStateManager.translatef(0.0F, 0.0F, 0.1f);

            if(BladeModel.user != null && BladeModel.user.getHeldItemMainhand() == stack){
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

    private void renderModel(ItemStack stack){

        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GL11.glEnable(GL11.GL_BLEND);

        float scale = 0.003125f;
        GlStateManager.scalef(scale, scale, scale);
        float defaultOffset = 130;
        GlStateManager.translatef(defaultOffset, 0, 0);

        EnumSet<SwordType> types = SwordType.from(stack);
        //BladeModel.itemBlade.getModelLocation(itemStackIn)

        ResourceLocation modelLocation = stack.getCapability(ItemSlashBlade.BLADESTATE)
                .filter(s->s.getModel().isPresent())
                .map(s->s.getModel().get())
                .orElseGet(()-> BladeModelManager.resourceDefaultModel);
        WavefrontObject model = BladeModelManager.getInstance().getModel(modelLocation);
        ResourceLocation textureLocation = stack.getCapability(ItemSlashBlade.BLADESTATE)
                .filter(s->s.getTexture().isPresent())
                .map(s->s.getTexture().get())
                .orElseGet(()->BladeModelManager.resourceDefaultTexture);

        Vec3d bladeOffset = Vec3d.ZERO;
        float bladeOffsetRot =0;
        float bladeOffsetBaseRot = -3;
        Vec3d sheathOffset = Vec3d.ZERO;
        float sheathOffsetRot =0;
        float sheathOffsetBaseRot = -3;
        boolean vFlip = false;
        boolean hFlip = false;
        boolean hasScabbard = !types.contains(SwordType.NoScabbard);

        if(stack.isOnItemFrame()){
            if(stack.getItemFrame() instanceof BladeStandEntity){
                BladeStandEntity stand = (BladeStandEntity) stack.getItemFrame();
                Item type = stand.currentType;

                Pose pose = stand.getPose();
                switch (pose.ordinal()){
                    case 0:
                        vFlip = false;
                        hFlip = false;
                        break;
                    case 1:
                        vFlip = true;
                        hFlip = false;
                        break;
                    case 2:
                        vFlip = true;
                        hFlip = true;
                        break;
                    case 3:
                        vFlip = false;
                        hFlip = true;
                        break;
                    case 4:
                        vFlip = false;
                        hFlip = false;
                        hasScabbard = false;
                        break;
                    case 5:
                        vFlip = false;
                        hFlip = true;
                        hasScabbard = false;
                        break;
                }

                if(type == SBItems.bladestand_1) {
                     bladeOffset = Vec3d.ZERO;
                    sheathOffset = Vec3d.ZERO;
                }else if(type == SBItems.bladestand_2){
                    bladeOffset = new Vec3d(0,21.5f,0);
                    if(hFlip){
                        sheathOffset = new Vec3d(-40,-27,0);
                    }else{
                        sheathOffset = new Vec3d(40,-27,0);
                    }
                    sheathOffsetBaseRot = -4;
                }else if(type == SBItems.bladestand_v){
                    bladeOffset = new Vec3d(-100,230,0);
                    sheathOffset = new Vec3d(-100,230,0);
                    bladeOffsetRot = 80;
                    sheathOffsetRot = 80;
                }else if(type == SBItems.bladestand_s){
                    if(hFlip){
                        bladeOffset = new Vec3d(60,-25,0);
                        sheathOffset = new Vec3d(60,-25,0);
                    }else{
                        bladeOffset = new Vec3d(-60,-25,0);
                        sheathOffset = new Vec3d(-60,-25,0);
                    }
                }else if(type == SBItems.bladestand_1w){
                    bladeOffset = Vec3d.ZERO;
                    sheathOffset = Vec3d.ZERO;
                }else if(type == SBItems.bladestand_2w){
                    bladeOffset = new Vec3d(0,21.5f,0);
                    if(hFlip){
                        sheathOffset = new Vec3d(-40,-27,0);
                    }else{
                        sheathOffset = new Vec3d(40,-27,0);
                    }
                    sheathOffsetBaseRot = -4;
                }
            }
        }

        try(MSAutoCloser msac = MSAutoCloser.pushMatrix()) {
            String renderTarget;
            if(types.contains(SwordType.Broken))
                renderTarget = "blade_damaged";
            else
                renderTarget = "blade";

            GlStateManager.translated(bladeOffset.x, bladeOffset.y, bladeOffset.z);
            GlStateManager.rotatef(bladeOffsetRot, 0,0,1);


            if(vFlip) {
                GlStateManager.rotatef(180, 1, 0, 0);
                GlStateManager.translated(0, -15,0);

                GlStateManager.translated(0, 5, 0);
            }

            if (hFlip) {
                double offset = defaultOffset;
                GlStateManager.translated(-offset, 0,0);
                GlStateManager.rotatef(180, 0, 1, 0);
                GlStateManager.translated(offset, 0,0);
            }

            GlStateManager.rotatef(bladeOffsetBaseRot, 0,0,1);


            RenderHandler.renderOverrided(stack, model, renderTarget, textureLocation);

            GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
            RenderHandler.renderOverrided(stack, model, renderTarget + "_luminous", textureLocation);
            GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
        }

        if(hasScabbard){
            try(MSAutoCloser msac = MSAutoCloser.pushMatrix()) {
                String renderTarget = "sheath";

                GlStateManager.translated(sheathOffset.x, sheathOffset.y, sheathOffset.z);
                GlStateManager.rotatef(sheathOffsetRot, 0,0,1);


                if(vFlip) {
                    GlStateManager.rotatef(180, 1, 0, 0);
                    GlStateManager.translated(0, -15,0);

                    GlStateManager.translated(0, 5, 0);
                }

                if (hFlip) {
                    double offset = defaultOffset;
                    GlStateManager.translated(-offset, 0,0);
                    GlStateManager.rotatef(180, 0, 1, 0);
                    GlStateManager.translated(offset, 0,0);
                }

                GlStateManager.rotatef(sheathOffsetBaseRot, 0,0,1);

                RenderHandler.renderOverrided(stack, model, renderTarget, textureLocation);

                GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ONE, GL11.GL_ZERO);
                RenderHandler.renderOverrided(stack, model, renderTarget + "_luminous", textureLocation);
                GlStateManager.blendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, GL11.GL_ONE, GL11.GL_ZERO);
            }
        }

        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GlStateManager.enableLighting();
        GL11.glEnable(GL11.GL_CULL_FACE);

        GlStateManager.shadeModel(GL11.GL_FLAT);
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

}
