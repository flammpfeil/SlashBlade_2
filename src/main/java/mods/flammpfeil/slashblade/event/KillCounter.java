package mods.flammpfeil.slashblade.event;

import mods.flammpfeil.slashblade.client.renderer.model.BladeModelManager;
import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class KillCounter {
    private static final class SingletonHolder {
        private static final KillCounter instance = new KillCounter();
    }
    public static KillCounter getInstance() {
        return SingletonHolder.instance;
    }
    private KillCounter(){}
    public void register(){
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public void onLivingDeathEvent(LivingDeathEvent event) {
        Entity trueSource = event.getSource().getEntity();

        if (!(trueSource instanceof LivingEntity)) return;

        ItemStack stack = ((LivingEntity) trueSource).getMainHandItem();
        if(stack.isEmpty()) return;
        if(!(stack.getItem() instanceof ItemSlashBlade)) return;

        stack.getCapability(ItemSlashBlade.BLADESTATE).ifPresent(state->{
            state.setKillCount(state.getKillCount() + 1);
        });
    }
}
