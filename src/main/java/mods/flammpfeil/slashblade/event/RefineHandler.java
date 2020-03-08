package mods.flammpfeil.slashblade.event;

import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.event.AnvilUpdateEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class RefineHandler {
    private static final class SingletonHolder {
        private static final RefineHandler instance = new RefineHandler();
    }
    public static RefineHandler getInstance() {
        return RefineHandler.SingletonHolder.instance;
    }
    private RefineHandler(){}
    public void register(){
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onAnvilUpdateEvent(AnvilUpdateEvent event){
        ItemStack base = event.getLeft();
        ItemStack material = event.getRight();

        if(base.isEmpty()) return;
        if(!(base.getItem() instanceof ItemSlashBlade)) return;
        if(material.isEmpty()) return;

        boolean isRepairable = base.getItem().getIsRepairable(base,material);

        if(!isRepairable) return;

        int level = material.getHarvestLevel(ToolType.get("proudsoul"), null, null);

        if(level < 0) return;

        ItemStack result = base.copy();

        int refineLimit = 6 <= level ? Integer.MAX_VALUE : Math.max(10, 50*(level-1));

        int cost = 0;
        while(cost < material.getCount()){
            cost ++;

            float damage = result.getCapability(ItemSlashBlade.BLADESTATE).map(s->{
                s.setDamage(s.getDamage() - (0.2f + 0.05f * level));
                if(s.getRefine() < refineLimit)
                    s.setRefine(s.getRefine() + 1);
                return s.getDamage();
            }).orElse(0f);

            if(damage <= 0f) break;
        }

        event.setMaterialCost(cost);
        int levelCostBase = Math.max(1 , 2 * (level - 1));
        event.setCost(levelCostBase * cost);
        event.setOutput(result);
    }

    static private final ResourceLocation REFINE = new ResourceLocation(SlashBlade.modid, "tips/refine");

    @SubscribeEvent
    public void onAnvilRepairEvent(AnvilRepairEvent event){

        if(!(event.getPlayer() instanceof ServerPlayerEntity)) return;

        ItemStack material = event.getIngredientInput();
        ItemStack base = event.getItemInput();

        if(base.isEmpty()) return;
        if(!(base.getItem() instanceof ItemSlashBlade)) return;
        if(material.isEmpty()) return;

        boolean isRepairable = base.getItem().getIsRepairable(base,material);

        if(!isRepairable) return;

        int level = material.getHarvestLevel(ToolType.get("proudsoul"), null, null);

        if(level < 0) return;

        grantCriterion((ServerPlayerEntity) event.getPlayer(), REFINE);
    }

    private static void grantCriterion(ServerPlayerEntity player, ResourceLocation resourcelocation){
        Advancement adv = player.getServer().getAdvancementManager().getAdvancement(resourcelocation);
        if(adv == null) return;

        AdvancementProgress advancementprogress = player.getAdvancements().getProgress(adv);
        if (advancementprogress.isDone()) return;

        for(String s : advancementprogress.getRemaningCriteria()) {
            player.getAdvancements().grantCriterion(adv, s);
        }
    }

}
