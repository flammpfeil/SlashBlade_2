package mods.flammpfeil.slashblade.client.renderer.model;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import mods.flammpfeil.slashblade.client.renderer.model.obj.WavefrontObject;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.stream.Stream;

/**
 * Created by Furia on 2016/02/06.
 */
@OnlyIn(Dist.CLIENT)
public class BladeModelManager {

    private static final class SingletonHolder {
        private static final BladeModelManager instance = new BladeModelManager();
    }

    public static BladeModelManager getInstance() {
        return SingletonHolder.instance;
    }

    WavefrontObject defaultModel;
    public static final ResourceLocation resourceDefaultModel = new ResourceLocation("slashblade","model/blade.obj");
    public static final ResourceLocation resourceDefaultTexture = new ResourceLocation("slashblade","model/blade.png");

    public static final ResourceLocation resourceDurabilityModel = new ResourceLocation("slashblade","model/util/durability.obj");
    public static final ResourceLocation resourceDurabilityTexture = new ResourceLocation("slashblade","model/util/durability.png");

    LoadingCache<ResourceLocation, WavefrontObject> cache;

    private BladeModelManager() {
        defaultModel = new WavefrontObject(resourceDefaultModel);

        cache = CacheBuilder.newBuilder()
                .build(
                CacheLoader.asyncReloading(new CacheLoader<ResourceLocation, WavefrontObject>() {
                    @Override
                    public WavefrontObject load(ResourceLocation key) throws Exception {
                        try{
                            return new WavefrontObject(key);
                        }catch(Exception e){
                            return defaultModel;
                        }
                    }

                }, Executors.newCachedThreadPool())
        );
    }

    @SubscribeEvent
    public void reload(TextureStitchEvent.Post event){
        cache.invalidateAll();

        defaultModel = new WavefrontObject(resourceDefaultModel);
    }

    public WavefrontObject getModel(ResourceLocation loc) {
        if(loc != null){
            try {
                return cache.get(loc);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return defaultModel;
    }

}
