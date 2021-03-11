package mods.flammpfeil.slashblade.client.renderer.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import jp.nyatla.nymmd.MmdException;
import jp.nyatla.nymmd.MmdVmdMotionMc;
import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.io.IOException;
import java.util.concurrent.Executors;

/**
 * Created by Furia on 2016/02/06.
 */
public class BladeMotionManager {

    private static final class SingletonHolder {
        private static final BladeMotionManager instance = new BladeMotionManager();
    }

    public static BladeMotionManager getInstance() {
        return SingletonHolder.instance;
    }

    MmdVmdMotionMc defaultMotion;
    public static final ResourceLocation resourceDefaultMotion = new ResourceLocation(SlashBlade.modid,"combostate/motion_old.vmd");


    LoadingCache<ResourceLocation, MmdVmdMotionMc> cache;

    private BladeMotionManager() {
        try {
            defaultMotion = new MmdVmdMotionMc(resourceDefaultMotion);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MmdException e) {
            e.printStackTrace();
        }

        cache = CacheBuilder.newBuilder()
                .build(
                CacheLoader.asyncReloading(new CacheLoader<ResourceLocation, MmdVmdMotionMc>() {
                    @Override
                    public MmdVmdMotionMc load(ResourceLocation key) throws Exception {
                        try{
                            return new MmdVmdMotionMc(key);
                        }catch(Exception e){
                            e.printStackTrace();
                            return defaultMotion;
                        }
                    }

                }, Executors.newCachedThreadPool())
        );
    }

    @SubscribeEvent
    public void reload(TextureStitchEvent.Pre event){
        cache.invalidateAll();

        try {
            defaultMotion = new MmdVmdMotionMc(resourceDefaultMotion);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (MmdException e) {
            e.printStackTrace();
        }
    }

    public MmdVmdMotionMc getMotion(ResourceLocation loc) {
        if(loc != null){
            try {
                return cache.get(loc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return defaultMotion;
    }

}
