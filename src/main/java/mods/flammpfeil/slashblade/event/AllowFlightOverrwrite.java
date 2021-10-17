package mods.flammpfeil.slashblade.event;

import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fmlserverevents.FMLServerAboutToStartEvent;

public class AllowFlightOverrwrite {

    private static final class SingletonHolder {
        private static final AllowFlightOverrwrite instance = new AllowFlightOverrwrite();
    }
    public static AllowFlightOverrwrite getInstance() {
        return AllowFlightOverrwrite.SingletonHolder.instance;
    }
    private AllowFlightOverrwrite(){}
    public void register(){
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void onFMLServerAboutToStartEvent(FMLServerAboutToStartEvent event){
        event.getServer().setFlightAllowed(true);
    }
}
