package mods.flammpfeil.slashblade.util;

import com.google.common.collect.Maps;
import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ForgeRegistryEntry;
import net.minecraftforge.registries.IForgeRegistryEntry;

import java.util.Map;
import java.util.function.Supplier;

public abstract class RegistryBase<V extends IForgeRegistryEntry<V>>  extends ForgeRegistryEntry<V> {
    public static Map<ResourceLocation, Map<ResourceLocation, Object>> registries = Maps.newHashMap();

    private String name;

    static protected String BaseInstanceName = "none";

    public final ResourceLocation path;

    public RegistryBase(String name){
        this.name = name;
        path = new ResourceLocation(SlashBlade.modid, getPath() + "/"+ name /*+ ".json"*/);
        getRegistry().put(path, (V)this);
    }

    public Map<ResourceLocation, Object> getRegistry(){
        ResourceLocation key = this.delegate.name();

        if(!registries.containsKey(key)){
            registries.put(key, Maps.newHashMap());
        }

        return registries.get(key);
    }

    public abstract String getPath();

    public V valueOf(String name){
        Object result =getRegistry().get(new ResourceLocation(SlashBlade.modid, getPath() + "/" + name));

        return (V) result;
    }

    public String getName() {
        return name;
    }

    public V orNone(V src){
        if(src == null)
            return getNone();
        else
            return src;
    }

    public abstract V getNone();
}
