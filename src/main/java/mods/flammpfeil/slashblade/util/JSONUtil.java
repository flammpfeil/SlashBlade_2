package mods.flammpfeil.slashblade.util;

import com.google.common.collect.Lists;
import net.minecraft.nbt.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class JSONUtil {
    private static final Pattern SIMPLE_VALUE = Pattern.compile("[A-Za-z0-9._+-]+");

    static String handleEscape(String str) {
        return SIMPLE_VALUE.matcher(str).matches() ? str : StringTag.quoteAndEscape(str);
    }

    static public String NBTtoJsonString(Tag childTag){
        int i = childTag.getId();

        String str;
        if(childTag instanceof CompoundTag){
            str = NBTtoJsonString((CompoundTag)childTag);
        }else if(childTag instanceof ListTag){
            str = NBTtoJsonString((ListTag)childTag);
        }else if(i == 1 || i == 2 || i == 3 || i == 4 || i == 5 || i == 6){
            str = childTag.toString();
            str = str.replaceAll("[fdbsL]","");
        }else{
            str = childTag.toString();
        }

        return str;
    }

    static public String NBTtoJsonString(ListTag list){
        StringBuilder stringbuilder = new StringBuilder("[");

        for(int i = 0; i < list.size(); ++i) {
            if (i != 0) {
                stringbuilder.append(',');
            }

            stringbuilder.append(NBTtoJsonString(list.get(i)));
        }

        return stringbuilder.append(']').toString();
    }

    static public String NBTtoJsonString(CompoundTag tag) {
        StringBuilder stringbuilder = new StringBuilder("{");
        Collection<String> collection = tag.getAllKeys();

        for(String s : collection) {
            if (stringbuilder.length() != 1) {
                stringbuilder.append(',');
            }

            stringbuilder.append(handleEscape(s)).append(':').append(NBTtoJsonString(tag.get(s)));
        }

        return stringbuilder.append('}').toString();
    }
}
