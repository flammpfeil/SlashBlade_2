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

        VmdAnimation animation = this.animation.get(event.getCombo().getName());
        if(animation != null) {
            animationStack.removeLayer(0);
            animation.play();
            animationStack.addAnimLayer(0, animation);

            //IAnimation aa = new KeyframeAnimationPlayer(PlayerAnimationRegistry.getAnimation(new ResourceLocation(SlashBlade.modid , "waving")));
            //animationStack.addAnimLayer(0,aa);
        }

    }


    private Map<String, VmdAnimation> initAnimations() {
        Map<String, VmdAnimation> map = Maps.newHashMap();

        //guard
        map.put(Extra.EX_COMBO_A1_END2.getName(), new VmdAnimation(MotionLocation, 21,41,false));

        map.put(Extra.EX_COMBO_A1.getName(), new VmdAnimation(MotionLocation, 1,41,false));
        map.put(Extra.EX_COMBO_A2.getName(), new VmdAnimation(MotionLocation, 100,151,false));
        map.put(Extra.EX_COMBO_C.getName(), new VmdAnimation(MotionLocation, 400,488,false));
        map.put(Extra.EX_COMBO_A3.getName(), new VmdAnimation(MotionLocation, 200,314,false));
        map.put(Extra.EX_COMBO_A4.getName(), new VmdAnimation(MotionLocation, 500,608,false));

        map.put(Extra.EX_COMBO_A4EX.getName(), new VmdAnimation(MotionLocation, 800,894,false));
        map.put(Extra.EX_COMBO_A5EX.getName(), new VmdAnimation(MotionLocation, 900,1061,false));

        map.put(Extra.EX_COMBO_B1.getName(), new VmdAnimation(MotionLocation, 700,787,false));
        map.put(Extra.EX_COMBO_B2.getName(), new VmdAnimation(MotionLocation, 710,787,false));
        map.put(Extra.EX_COMBO_B3.getName(), new VmdAnimation(MotionLocation, 710,787,false));
        map.put(Extra.EX_COMBO_B4.getName(), new VmdAnimation(MotionLocation, 710,787,false));
        map.put(Extra.EX_COMBO_B5.getName(), new VmdAnimation(MotionLocation, 710,787,false));
        map.put(Extra.EX_COMBO_B6.getName(), new VmdAnimation(MotionLocation, 710,787,false));
        map.put(Extra.EX_COMBO_B7.getName(), new VmdAnimation(MotionLocation, 710,787,false));

        map.put(Extra.EX_AERIAL_RAVE_A1.getName(), new VmdAnimation(MotionLocation, 1100,1132,false).setBlendLegs(false));
        map.put(Extra.EX_AERIAL_RAVE_A2.getName(), new VmdAnimation(MotionLocation, 1200,1241,false).setBlendLegs(false));
        map.put(Extra.EX_AERIAL_RAVE_A3.getName(), new VmdAnimation(MotionLocation, 1300,1338,false).setBlendLegs(false));

        map.put(Extra.EX_AERIAL_RAVE_B3.getName(), new VmdAnimation(MotionLocation, 1400,1443,false).setBlendLegs(false));
        map.put(Extra.EX_AERIAL_RAVE_B4.getName(), new VmdAnimation(MotionLocation, 1500,1547,false).setBlendLegs(false));

        map.put(Extra.EX_UPPERSLASH.getName(), new VmdAnimation(MotionLocation, 1600,1693,false));
        map.put(Extra.EX_UPPERSLASH_JUMP.getName(), new VmdAnimation(MotionLocation, 1700,1717,false).setBlendLegs(false));

        map.put(Extra.EX_AERIAL_CLEAVE.getName(), new VmdAnimation(MotionLocation, 1800,1817,false).setBlendLegs(false));
        map.put(Extra.EX_AERIAL_CLEAVE_LOOP.getName(), new VmdAnimation(MotionLocation, 1812,1817,true).setBlendLegs(false));
        map.put(Extra.EX_AERIAL_CLEAVE_LANDING.getName(), new VmdAnimation(MotionLocation, 1816,1886,false));

        map.put(Extra.EX_RAPID_SLASH.getName(), new VmdAnimation(MotionLocation, 2000,2073,false).setBlendLegs(false));
        map.put(Extra.EX_RAPID_SLASH_QUICK.getName(), new VmdAnimation(MotionLocation, 2000,2073,false).setBlendLegs(false));
        map.put(Extra.EX_RISING_STAR.getName(), new VmdAnimation(MotionLocation, 2100,2147,false).setBlendLegs(false));

        map.put(Extra.EX_JUDGEMENT_CUT.getName(), new VmdAnimation(MotionLocation, 1900,1963,false).setBlendLegs(false));
        map.put(Extra.EX_JUDGEMENT_CUT_SLASH_AIR.getName(), new VmdAnimation(MotionLocation, 1923,1963,false).setBlendLegs(false));
        map.put(Extra.EX_JUDGEMENT_CUT_SLASH_JUST.getName(), new VmdAnimation(MotionLocation, 1923,1963,false).setBlendLegs(false));

        map.put(Extra.EX_VOID_SLASH.getName(), new VmdAnimation(MotionLocation, 2200,2299,false).setBlendLegs(false));


        return map;
    }


}
