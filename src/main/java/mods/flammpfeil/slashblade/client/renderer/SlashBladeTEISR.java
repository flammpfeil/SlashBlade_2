package mods.flammpfeil.slashblade.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import mods.flammpfeil.slashblade.client.renderer.model.BladeFirstPersonRender;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModel;
import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import mods.flammpfeil.slashblade.client.renderer.util.MSAutoCloser;
import mods.flammpfeil.slashblade.client.renderer.util.BladeRenderState;
import mods.flammpfeil.slashblade.entity.BladeStandEntity;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import mods.flammpfeil.slashblade.init.SBItems;
import mods.flammpfeil.slashblade.item.SwordType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import com.mojang.math.Axis;

import java.awt.*;
import java.util.EnumSet;

public class SlashBladeTEISR extends BlockEntityWithoutLevelRenderer {

    public SlashBladeTEISR(BlockEntityRenderDispatcher p_172550_, EntityModelSet p_172551_) {
        super(p_172550_, p_172551_);
    }

    @Override
    public void renderByItem(ItemStack itemStackIn, ItemDisplayContext type, PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn) {
    //public void render(ItemStack itemStackIn, MatrixStack matrixStack, IRenderTypeBuffer bufferIn, int combinedLightIn, int combinedOverlayIn) {
        if(!(itemStackIn.getItem() instanceof ItemSlashBlade)) return;
        ItemSlashBlade item = (ItemSlashBlade)itemStackIn.getItem();

        if(itemStackIn.hasTag() && itemStackIn.getTag().contains(ItemSlashBlade.ICON_TAG_KEY)){
            itemStackIn.readShareTag(itemStackIn.getTag());
            itemStackIn.removeTagKey(ItemSlashBlade.ICON_TAG_KEY);
        }

        renderBlade(itemStackIn, type, matrixStack, bufferIn, combinedLightIn, combinedOverlayIn);
    }

    boolean checkRenderNaked(){
        ItemStack mainHand = BladeModel.user.getMainHandItem();
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

    private boolean renderBlade(ItemStack stack, ItemDisplayContext transformType , PoseStack matrixStack, MultiBufferSource bufferIn, int combinedLightIn, int combinedOverlayIn){

        if(transformType == ItemDisplayContext.THIRD_PERSON_LEFT_HAND
                || transformType == ItemDisplayContext.THIRD_PERSON_RIGHT_HAND
                || transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND
                || transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND
                || transformType == ItemDisplayContext.NONE) {

            if(BladeModel.user == null)
                BladeModel.user = Minecraft.getInstance().player;

            EnumSet<SwordType> types = SwordType.from( stack);

            boolean handle = false;

            if(!types.contains(SwordType.NoScabbard)) {
                handle = BladeModel.user.getMainArm() == HumanoidArm.RIGHT ?
                        transformType == ItemDisplayContext.FIRST_PERSON_RIGHT_HAND :
                        transformType == ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
            }

            if(handle){
                BladeFirstPersonRender.getInstance().render(matrixStack, bufferIn, combinedLightIn);
            }

            /*
            if(transformType == ItemCameraTransforms.TransformType.NONE) {
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



        try(MSAutoCloser msacA = MSAutoCloser.pushMatrix(matrixStack)) {

            matrixStack.translate(0.5f, 0.5f, 0.5f);

            if (transformType == ItemDisplayContext.GROUND) {
                matrixStack.translate(0, 0.15f, 0);
                renderIcon(stack, matrixStack, bufferIn, combinedLightIn,0.005f);
            } else if (transformType == ItemDisplayContext.GUI) {
                renderIcon(stack, matrixStack, bufferIn, combinedLightIn,0.008f, true);
            } else if (transformType == ItemDisplayContext.FIXED) {
                if (stack.isFramed() && stack.getFrame() instanceof BladeStandEntity) {
                    renderModel(stack, matrixStack, bufferIn, combinedLightIn);
                } else {
                    matrixStack.mulPose(Axis.YP.rotationDegrees(180.0f));
                    renderIcon(stack, matrixStack, bufferIn, combinedLightIn,0.0095f);
                }
            }else{
                renderIcon(stack, matrixStack, bufferIn, combinedLightIn,0.0095f);
            }
        }

        return true;
    }

    private void renderIcon(ItemStack stack, PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn, float scale){
        renderIcon(stack, matrixStack, bufferIn, lightIn, scale, false);
    }
    private void renderIcon(ItemStack stack, PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn, float scale, boolean renderDurability){

        matrixStack.scale(scale, scale, scale);

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

        BladeRenderState.renderOverrided(stack, model, renderTarget, textureLocation, matrixStack, bufferIn, lightIn);
        BladeRenderState.renderOverridedLuminous(stack, model, renderTarget + "_luminous", textureLocation, matrixStack, bufferIn, lightIn);

        if(renderDurability){

            WavefrontObject durabilityModel = BladeModelManager.getInstance().getModel(BladeModelManager.resourceDurabilityModel);

            float durability = stack.getCapability(ItemSlashBlade.BLADESTATE).map(s->s.getDurabilityForDisplay()).orElse(0.0f);
            matrixStack.translate(0.0F, 0.0F, 0.1f);

            if(BladeModel.user != null && BladeModel.user.getMainHandItem() == stack){

                BladeRenderState.setCol(new Color(0xEEEEEE));
                BladeRenderState.renderOverrided(stack, durabilityModel, "base", BladeModelManager.resourceDurabilityTexture, matrixStack, bufferIn, lightIn);
                matrixStack.translate(0.0F, 0.0F, 0.1f);
                BladeRenderState.setCol(Color.black);
                BladeRenderState.renderOverrided(stack, durabilityModel, "color_r", BladeModelManager.resourceDurabilityTexture, matrixStack, bufferIn, lightIn);
            }else{
                Color aCol = new Color(0.25f,0.25f,0.25f,1.0f);
                Color bCol = new Color(0xA52C63);
                int r = 0xFF & (int)Mth.lerp(aCol.getRed(), bCol.getRed(),durability);
                int g = 0xFF & (int)Mth.lerp(aCol.getGreen(), bCol.getGreen(),durability);
                int b = 0xFF & (int)Mth.lerp(aCol.getBlue(), bCol.getBlue(),durability);

                BladeRenderState.setCol(new Color(r,g,b));
                BladeRenderState.renderOverrided(stack, durabilityModel, "base", BladeModelManager.resourceDurabilityTexture, matrixStack, bufferIn, lightIn);


                boolean isBroken = types.contains(SwordType.Broken);
                matrixStack.translate(0.0F, 0.0F, -2.0f * durability);
                BladeRenderState.renderOverrided(stack, durabilityModel, isBroken ? "color_r" : "color", BladeModelManager.resourceDurabilityTexture, matrixStack, bufferIn, lightIn);
            }
        }
    }

    private void renderModel(ItemStack stack, PoseStack matrixStack, MultiBufferSource bufferIn, int lightIn){

        float scale = 0.003125f;
        matrixStack.scale(scale, scale, scale);
        float defaultOffset = 130;
        matrixStack.translate(defaultOffset, 0, 0);

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

        Vec3 bladeOffset = Vec3.ZERO;
        float bladeOffsetRot =0;
        float bladeOffsetBaseRot = -3;
        Vec3 sheathOffset = Vec3.ZERO;
        float sheathOffsetRot =0;
        float sheathOffsetBaseRot = -3;
        boolean vFlip = false;
        boolean hFlip = false;
        boolean hasScabbard = !types.contains(SwordType.NoScabbard);

        if(stack.isFramed()){
            if(stack.getFrame() instanceof BladeStandEntity){
                BladeStandEntity stand = (BladeStandEntity) stack.getFrame();
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
                     bladeOffset = Vec3.ZERO;
                    sheathOffset = Vec3.ZERO;
                }else if(type == SBItems.bladestand_2){
                    bladeOffset = new Vec3(0,21.5f,0);
                    if(hFlip){
                        sheathOffset = new Vec3(-40,-27,0);
                    }else{
                        sheathOffset = new Vec3(40,-27,0);
                    }
                    sheathOffsetBaseRot = -4;
                }else if(type == SBItems.bladestand_v){
                    bladeOffset = new Vec3(-100,230,0);
                    sheathOffset = new Vec3(-100,230,0);
                    bladeOffsetRot = 80;
                    sheathOffsetRot = 80;
                }else if(type == SBItems.bladestand_s){
                    if(hFlip){
                        bladeOffset = new Vec3(60,-25,0);
                        sheathOffset = new Vec3(60,-25,0);
                    }else{
                        bladeOffset = new Vec3(-60,-25,0);
                        sheathOffset = new Vec3(-60,-25,0);
                    }
                }else if(type == SBItems.bladestand_1w){
                    bladeOffset = Vec3.ZERO;
                    sheathOffset = Vec3.ZERO;
                }else if(type == SBItems.bladestand_2w){
                    bladeOffset = new Vec3(0,21.5f,0);
                    if(hFlip){
                        sheathOffset = new Vec3(-40,-27,0);
                    }else{
                        sheathOffset = new Vec3(40,-27,0);
                    }
                    sheathOffsetBaseRot = -4;
                }
            }
        }

        try(MSAutoCloser msac = MSAutoCloser.pushMatrix(matrixStack)) {
            String renderTarget;
            if(types.contains(SwordType.Broken))
                renderTarget = "blade_damaged";
            else
                renderTarget = "blade";

            matrixStack.translate(bladeOffset.x, bladeOffset.y, bladeOffset.z);
            matrixStack.mulPose(Axis.ZP.rotationDegrees(bladeOffsetRot));


            if(vFlip) {
                matrixStack.mulPose(Axis.XP.rotationDegrees(180.0f));
                matrixStack.translate(0, -15,0);

                matrixStack.translate(0, 5, 0);
            }

            if (hFlip) {
                double offset = defaultOffset;
                matrixStack.translate(-offset, 0,0);
                matrixStack.mulPose(Axis.YP.rotationDegrees(180.0f));
                matrixStack.translate(offset, 0,0);
            }

            matrixStack.mulPose(Axis.ZP.rotationDegrees(bladeOffsetBaseRot));


            BladeRenderState.renderOverrided(stack, model, renderTarget, textureLocation, matrixStack, bufferIn, lightIn);
            BladeRenderState.renderOverridedLuminous(stack, model, renderTarget + "_luminous", textureLocation, matrixStack, bufferIn, lightIn);
        }

        if(hasScabbard){
            try(MSAutoCloser msac = MSAutoCloser.pushMatrix(matrixStack)) {
                String renderTarget = "sheath";

                matrixStack.translate(sheathOffset.x, sheathOffset.y, sheathOffset.z);
                matrixStack.mulPose(Axis.ZP.rotationDegrees(sheathOffsetRot));


                if(vFlip) {
                    matrixStack.mulPose(Axis.XP.rotationDegrees(180.0f));
                    matrixStack.translate(0, -15,0);

                    matrixStack.translate(0, 5, 0);
                }

                if (hFlip) {
                    double offset = defaultOffset;
                    matrixStack.translate(-offset, 0,0);
                    matrixStack.mulPose(Axis.YP.rotationDegrees(180.0f));
                    matrixStack.translate(offset, 0,0);
                }

                matrixStack.mulPose(Axis.ZP.rotationDegrees(sheathOffsetBaseRot));

                BladeRenderState.renderOverrided(stack, model, renderTarget, textureLocation, matrixStack, bufferIn, lightIn);
                BladeRenderState.renderOverridedLuminous(stack, model, renderTarget + "_luminous", textureLocation, matrixStack, bufferIn, lightIn);
            }
        }

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

            matrixStack.pushMatrix();

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

                matrixStack.disableLighting();
                try(LightSetup ls = LightSetup.setupAdd()){
                    for(String renderTarget : renderTargets)
                        model.renderPart(renderTarget + "_luminous");
                }

                matrixStack.enableLighting();
            }

            matrixStack.popMatrix();
        }
    }*/

}
