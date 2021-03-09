package mods.flammpfeil.slashblade.event.client;

import mods.flammpfeil.slashblade.item.ItemSlashBlade;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Pose;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;

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

        if(!event.getRenderer().getEntityModel().isSneak) return;

        event.getRenderer().getEntityModel().isSneak = false;

        Vector3d offset = event.getRenderer()
                .getRenderOffset((AbstractClientPlayerEntity) event.getPlayer(), event.getPartialRenderTick())
                .scale(-1);

        event.getMatrixStack().translate(offset.x, offset.y, offset.z);
    }
}
