package mods.flammpfeil.slashblade.event.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import io.netty.buffer.Unpooled;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.block.Blocks;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.AbstractGui;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.IGuiEventListener;
import net.minecraft.client.gui.advancements.AdvancementEntryGui;
import net.minecraft.client.gui.advancements.AdvancementTabGui;
import net.minecraft.client.gui.advancements.AdvancementsScreen;
import net.minecraft.client.gui.recipebook.GhostRecipe;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.*;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3i;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.GuiScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.*;

public class AdvancementsRecipeRenderer implements IRecipePlacer<Ingredient> {

    private static final ResourceLocation GUI_TEXTURE_CRAFTING_TABLE = new ResourceLocation("textures/gui/container/crafting_table.png");
    private static final ResourceLocation GUI_TEXTURE_FURNACE = new ResourceLocation("textures/gui/container/furnace.png");
    private static final ResourceLocation GUI_TEXTURE_BLAST_FURNACE = new ResourceLocation("textures/gui/container/blast_furnace.png");
    private static final ResourceLocation GUI_TEXTURE_SMOKER = new ResourceLocation("textures/gui/container/smoker.png");
    private static final ResourceLocation GUI_TEXTURE_SMITHING = new ResourceLocation("textures/gui/container/smithing.png");
    private static final ResourceLocation GUI_TEXTURE_ANVIL = new ResourceLocation("textures/gui/container/anvil.png");

    private static final class SingletonHolder {
        private static final AdvancementsRecipeRenderer instance = new AdvancementsRecipeRenderer();
    }
    public static AdvancementsRecipeRenderer getInstance() {
        return SingletonHolder.instance;
    }
    private AdvancementsRecipeRenderer(){}
    public void register(){
        MinecraftForge.EVENT_BUS.register(this);
    }


    static public ItemStack target = null;

    static public GhostRecipe gr = new GhostRecipe();

    static public ResourceLocation currentRecipe = null;

    static RecipeView currentView = null;
    static Map<IRecipeType, RecipeView> typeRecipeViewMap = createRecipeViewMap();


    static final IRecipeType dummy_anvilType = IRecipeType.register("sb_forgeing");
    static class DummyAnvilRecipe implements IRecipe<IInventory> {
        protected SmithingRecipe original;
        private final ItemStack result;
        private final ResourceLocation recipeId;

        NonNullList<Ingredient> nonnulllist = NonNullList.withSize(2, Ingredient.EMPTY);

        public DummyAnvilRecipe(SmithingRecipe recipe) {
            original = recipe;

            PacketBuffer pb = new PacketBuffer(Unpooled.buffer());
            SmithingRecipe.Serializer ss = new SmithingRecipe.Serializer();
            ss.write(pb,original);

            nonnulllist.set(0, Ingredient.read(pb));
            nonnulllist.set(1, Ingredient.read(pb));

            result = pb.readItemStack();

            this.recipeId = original.getId();
        }

        @Override
        public boolean matches(IInventory inv, World worldIn) {
            return false;
        }

        @Override
        public ItemStack getCraftingResult(IInventory inv) {
            return result.copy();
        }

        @Override
        public boolean canFit(int width, int height) {
            return false;
        }

        @Override
        public ItemStack getRecipeOutput() {
            return result;
        }

        @Override
        public NonNullList<Ingredient> getIngredients() {
            return nonnulllist;
        }

        @Override
        public ItemStack getIcon() {
            return new ItemStack(Blocks.ANVIL);
        }

        @Override
        public ResourceLocation getId() {
            return this.recipeId;
        }

        @Override
        public IRecipeSerializer<?> getSerializer() {
            return null;
        }

        @Override
        public IRecipeType<?> getType() {
            return dummy_anvilType;
        }
    }
    static class DummySmithingRecipe extends DummyAnvilRecipe{
        public DummySmithingRecipe(SmithingRecipe recipe) {
            super(recipe);
        }

        @Override
        public ItemStack getIcon() {
            return original.getIcon();
        }

        @Override
        public IRecipeType<?> getType() {
            return original.getType();
        }
    }

    static IRecipe overrideDummyRecipe(IRecipe original){

        if(!(original instanceof SmithingRecipe))
            return original;

        if(original.getId().getPath().startsWith("anvilcrafting")){
            return new DummyAnvilRecipe((SmithingRecipe)original);
        }else{
            return new DummySmithingRecipe((SmithingRecipe)original);
        }
    }

    static public class RecipeView{
        final IRecipeType recipeType;
        final ResourceLocation background;
        List<Vector3i> slots = Lists.newArrayList();
        final boolean isWideOutputSlot;

        public RecipeView(IRecipeType recipeType, ResourceLocation background, List<Vector3i> slots) {
            this(recipeType, background, slots, true);
        }
        public RecipeView(IRecipeType recipeType, ResourceLocation background, List<Vector3i> slots, boolean isWideOutputSlot) {
            this.recipeType = recipeType;
            this.background = background;
            this.slots = slots;
            this.isWideOutputSlot = isWideOutputSlot;
        }
    }
    static Map<IRecipeType, RecipeView> createRecipeViewMap(){
        Map<IRecipeType, RecipeView> map = Maps.newHashMap();

        {
            List<Vector3i> list = Lists.newArrayList();

            //output
            list.add(new Vector3i(124, 35, 0));

            //grid
            int SlotMargin = 18;
            int LeftMargin = 30;
            int TopMargin = 17;

            int RecipeGridX = 3;
            int RecipeGridY = 3;

            for(int i = 0; i < RecipeGridX; ++i) {
                for(int j = 0; j < RecipeGridY; ++j) {
                    list.add(new Vector3i(LeftMargin + j * SlotMargin, TopMargin + i * SlotMargin, 0));
                }
            }

            IRecipeType key = IRecipeType.CRAFTING;
            map.put(key, new RecipeView(key,
                    GUI_TEXTURE_CRAFTING_TABLE,
                    list));
        }

        {
            List<Vector3i> list = Lists.newArrayList();

            //output
            list.add(new Vector3i(116, 35,0));
            //input
            list.add(new Vector3i( 56, 17,0));
            //fuel
            list.add(new Vector3i( 56, 53,0));

            {
                IRecipeType key = IRecipeType.SMELTING;
                map.put(key, new RecipeView(key,
                        GUI_TEXTURE_FURNACE,
                        list));
            }
            {
                IRecipeType key = IRecipeType.BLASTING;
                map.put(key, new RecipeView(key,
                        GUI_TEXTURE_BLAST_FURNACE,
                        list));
            }
            {
                IRecipeType key = IRecipeType.SMOKING;
                map.put(key, new RecipeView(key,
                        GUI_TEXTURE_SMOKER,
                        list));
            }
        }

        {
            List<Vector3i> list = Lists.newArrayList();

            //output
            list.add(new Vector3i(134, 47,0));

            //input
            list.add(new Vector3i( 27, 47,0));
            //material
            list.add(new Vector3i( 76, 47,0));

            {
                IRecipeType key = IRecipeType.SMITHING;
                map.put(key, new RecipeView(key,
                        GUI_TEXTURE_SMITHING,
                        list, false));
            }

            {
                IRecipeType key = dummy_anvilType;
                map.put(key, new RecipeView(key,
                        GUI_TEXTURE_ANVIL,
                        list, false));
            }
        }

        return map;
    }


    @Override
    public void setSlotContents(Iterator<Ingredient> ingredients, int slotIn, int maxAmount, int y, int x) {
        Ingredient ingredient = ingredients.next();
        if (!ingredient.hasNoMatchingItems()) {
            if(slotIn < currentView.slots.size()){

                Vector3i slot = currentView.slots.get(slotIn);
                gr.addIngredient(ingredient, slot.getX(), slot.getY());
            }
        }
    }

    static void clearGhostRecipe(){
        target = null;
        gr.clear();
        currentRecipe = null;
        currentView = null;
    }

    static void setGhostRecipe(ItemStack icon){
        if(icon != null){
            if (icon.hasTag() && icon.getTag().contains("Crafting")) {
                getInstance().setGhostRecipe(new ResourceLocation(icon.getTag().getString("Crafting")));
            }
        }

        target = icon;
    }

    void setGhostRecipe(ResourceLocation loc){

        if(!Objects.equals(loc, currentRecipe)){
            currentRecipe = loc;

            Optional<? extends IRecipe<?>> recipe = Minecraft.getInstance().world.getRecipeManager().getRecipe(loc);
            if(recipe.isPresent()){
                gr.clear();

                IRecipe<?> iRecipe = recipe.get();
                iRecipe = overrideDummyRecipe(iRecipe);

                gr.setRecipe(iRecipe);

                currentView = typeRecipeViewMap.get(iRecipe.getType());

                if(currentView != null && 0 < currentView.slots.size()){
                    final int outputslotIndex = 0;
                    Vector3i outputSlot = currentView.slots.get(outputslotIndex);
                    gr.addIngredient(Ingredient.fromStacks(iRecipe.getRecipeOutput()), outputSlot.getX(), outputSlot.getY());

                    this.placeRecipe(3,3, outputslotIndex, iRecipe,  iRecipe.getIngredients().iterator(), 1);
                }
            }
            else
                gr.clear();
        }
    }


    void drawBackGround(MatrixStack matrixStack, int xCorner, int yCorner, int zOffset, int xSize, int ySize, int yClip){
        Minecraft.getInstance().getTextureManager().bindTexture(currentView.background);
        int bPadding = 5;
        AbstractGui.blit(matrixStack, xCorner, yCorner,zOffset, 0, 0, xSize, yClip-bPadding,256, 256);
        AbstractGui.blit(matrixStack, xCorner, yCorner + yClip - bPadding,zOffset, 0, ySize-bPadding, xSize, bPadding,256, 256);
    }

    void drawGhostRecipe(MatrixStack matrixStack, int xCorner, int yCorner, int zOffset, float partialTicks){
        try{
            RenderSystem.pushMatrix();
            RenderSystem.translatef(0,0,zOffset);

            ItemRenderer ir = Minecraft.getInstance().getItemRenderer();

            float tmp = ir.zLevel;
            ir.zLevel = zOffset - 125;

            int padding = 5;
            ir.renderItemAndEffectIntoGuiWithoutEntity(gr.getRecipe().getIcon(), xCorner + padding, yCorner + padding);

            boolean wideOutputSlot = currentView.isWideOutputSlot;

            gr.func_238922_a_(matrixStack, Minecraft.getInstance(), xCorner, yCorner, wideOutputSlot, partialTicks);
            ir.zLevel = tmp;

        }finally {
            RenderSystem.popMatrix();
        }
    }

    void drawTooltip(MatrixStack matrixStack, int xCorner, int yCorner, int zOffset, int mouseX, int mouseY , Screen gui){

        ItemStack itemStack = null;

        int slotSize = 16;

        for(int i = 0; i < gr.size(); ++i) {
            GhostRecipe.GhostIngredient ghostIngredient = gr.get(i);
            int j = ghostIngredient.getX() + xCorner;
            int k = ghostIngredient.getY() + yCorner;
            if (mouseX >= j && mouseY >= k && mouseX < j + slotSize && mouseY < k + slotSize) {
                itemStack = ghostIngredient.getItem();
            }
        }

        if(itemStack != null){

            FontRenderer font = Optional.ofNullable(itemStack.getItem().getFontRenderer(itemStack)).orElseGet(()->Minecraft.getInstance().fontRenderer);

            net.minecraftforge.fml.client.gui.GuiUtils.preItemToolTip(itemStack);
            gui.renderWrappedToolTip(matrixStack, gui.getTooltipFromItem(itemStack), mouseX, mouseY, font);
            net.minecraftforge.fml.client.gui.GuiUtils.postItemToolTip();
        }
    }


    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onDrawScreenPost(GuiScreenEvent.DrawScreenEvent.Post event){
        if(!(event.getGui() instanceof AdvancementsScreen)) return;
        if(AdvancementsRecipeRenderer.currentRecipe == null) return;

        AdvancementsScreen gui = (AdvancementsScreen) event.getGui();

        try {
            event.getMatrixStack().push();

            MatrixStack matrixStack = event.getMatrixStack();

            int zOffset = 425;
            int zStep = 75;
            matrixStack.translate(0, 0, zOffset);

            int xSize = 176;
            int ySize = 166;
            int yClip = 85;

            int xCorner = (gui.width - xSize) / 2;
            int yCorner = (gui.height - yClip) / 2;

            drawBackGround(matrixStack, xCorner, yCorner, zOffset, xSize, ySize, yClip);

            drawGhostRecipe(matrixStack, xCorner, yCorner, zOffset, event.getRenderPartialTicks());

            matrixStack.translate(0, 0, zStep);
            drawTooltip(matrixStack, xCorner, yCorner, zOffset, event.getMouseX(), event.getMouseY(), gui);

        }finally{
            event.getMatrixStack().pop();
        }
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public void onInitGuiPost(GuiScreenEvent.InitGuiEvent.Post event){
        if(!(event.getGui() instanceof AdvancementsScreen)) return;

        AdvancementsScreen gui = (AdvancementsScreen) event.getGui();

        ((List<IGuiEventListener>)gui.getEventListeners()).add(new AdvancementsExGuiEventListener(gui));
    }

    public static class AdvancementsExGuiEventListener implements IGuiEventListener {
        AdvancementsScreen screen;
        public AdvancementsExGuiEventListener(AdvancementsScreen screen){
            this.screen = screen;
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if(button == 1){
                clearGhostRecipe();
                return false;
            }

            int offsetX = (screen.width - 252) / 2;
            int offsetY = (screen.height - 140) / 2;

            ItemStack found = null;


            AdvancementTabGui selectedTab = screen.selectedTab;

            int mouseXX = (int)(mouseX - offsetX - 9);
            int mouseYY = (int)(mouseY - offsetY - 18);

            double scrollX = selectedTab.scrollX;
            double scrollY = selectedTab.scrollY;
            Map<Advancement, AdvancementEntryGui> guis = selectedTab.guis;

            int i = MathHelper.floor(scrollX);
            int j = MathHelper.floor(scrollY);
            if (mouseXX > 0 && mouseXX < 234 && mouseYY > 0 && mouseYY < 113) {
                for(AdvancementEntryGui advancemententrygui : guis.values()) {
                    if (advancemententrygui.isMouseOver(i, j, mouseXX, mouseYY)) {

                        DisplayInfo info = advancemententrygui.displayInfo;

                        found = info.getIcon();

                        break;
                    }
                }
            }

            setGhostRecipe(found);

            return false;
        }
    }
}
