package mods.flammpfeil.slashblade.event.client;

import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class SneakingMotionCanceller {
    private static final class SingletonHolder {
        private static final SneakingMotionCanceller instance = new SneakingMotionCanceller();
    }
    public static SneakingMotionCanceller getInstance() {
        return SingletonHolder.instance;
    }
    private SneakingMotionCanceller(){}
    public void register(){
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onRenderPlayerEventPre(RenderPlayerEvent.Pre event){
        ItemStack stack = event.getPlayer().getHeldItemMainhand();

        if(stack.isEmpty()) return;
        if(!(stack.getItem() instanceof ItemSlashBlade)) return;

        if(!event.getPlayer().isCrouching()) return;

        if(event.getPlayer().abilities.isFlying) return;

        event.getPlayer().getPersistentData().putBoolean("CancelSneak",true);
        event.getPlayer().abilities.isFlying = true;
    }

    @SubscribeEvent
    public void onRenderPlayerEventPost(RenderPlayerEvent.Post event){
        if(event.getPlayer().getPersistentData().contains("CancelSneak")){
            event.getPlayer().getPersistentData().remove("CancelSneak");

            event.getPlayer().abilities.isFlying = false;
        }
    }
}
