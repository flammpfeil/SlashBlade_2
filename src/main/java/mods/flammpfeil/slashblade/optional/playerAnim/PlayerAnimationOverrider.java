package mods.flammpfeil.slashblade.optional.playerAnim;

import com.google.common.collect.Maps;
import dev.kosmx.playerAnim.api.layered.AnimationStack;
import dev.kosmx.playerAnim.api.layered.IAnimation;
import dev.kosmx.playerAnim.api.layered.KeyframeAnimationPlayer;
import dev.kosmx.playerAnim.api.layered.ModifierLayer;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationAccess;
import dev.kosmx.playerAnim.minecraftApi.PlayerAnimationRegistry;
import mods.flammpfeil.slashblade.SlashBlade;
import mods.flammpfeil.slashblade.capability.slashblade.ComboState;
import mods.flammpfeil.slashblade.capability.slashblade.combo.Extra;
import mods.flammpfeil.slashblade.event.BladeMotionEvent;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.Map;

public class PlayerAnimationOverrider {
    private static final class SingletonHolder {
        private static final PlayerAnimationOverrider instance = new PlayerAnimationOverrider();
    }
    public static PlayerAnimationOverrider getInstance() {
        return PlayerAnimationOverrider.SingletonHolder.instance;
    }
    private PlayerAnimationOverrider(){}
    public void register(){
        MinecraftForge.EVENT_BUS.register(this);
    }

    static final ResourceLocation MotionLocation = new ResourceLocation(SlashBlade.modid, "model/pa/player_motion.vmd");

    Map<String,VmdAnimation> animation = initAnimations();

    @SubscribeEvent
    public void onBladeAnimationStart(BladeMotionEvent event){
        if(!(event.getEntity() instanceof AbstractClientPlayer)) return;
        AbstractClientPlayer player = (AbstractClientPlayer) event.getEntity();

        AnimationStack animationStack = PlayerAnimationAccess.getPlayerAnimLayer(player);

        animationStack.removeLayer(0);
        VmdAnimation animation = this.animation.get(event.getCombo().getName());
        if(animation != null) {
            animation.play();
            animationStack.addAnimLayer(0, animation);

            //IAnimation aa = new KeyframeAnimationPlayer(PlayerAnimationRegistry.getAnimation(new ResourceLocation(SlashBlade.modid , "waving")));
            //animationStack.addAnimLayer(0,aa);
        }

    }


    private Map<String, VmdAnimation> initAnimations() {
        Map<String, VmdAnimation> map = Maps.newHashMap();

        map.put(Extra.EX_COMBO_A1.getName(), new VmdAnimation(MotionLocation, 1,41,false));
        map.put(Extra.EX_COMBO_A2.getName(), new VmdAnimation(MotionLocation, 100,400,false));

        return map;
    }


}
