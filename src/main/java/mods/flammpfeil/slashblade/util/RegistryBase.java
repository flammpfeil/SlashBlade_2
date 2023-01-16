package mods.flammpfeil.slashblade.util;

import com.google.common.collect.Maps;
import mods.flammpfeil.slashblade.SlashBlade;
import net.minecraft.resources.ResourceLocation;

import java.util.Map;

public abstract class RegistryBase<V> {
    private String name;

    static protected String BaseInstanceName = "none";

    public final ResourceLocation path;

    public RegistryBase(String name){
        this.name = name;
        path = new ResourceLocation(SlashBlade.modid, getPath() + "/"+ name /*+ ".json"*/);
        getRegistry().put(path, (V)this);
    }

    public abstract Map<ResourceLocation, V> getRegistry();

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
