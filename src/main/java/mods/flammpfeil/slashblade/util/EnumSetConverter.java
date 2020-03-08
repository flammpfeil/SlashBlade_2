package mods.flammpfeil.slashblade.util;

import java.util.*;
import java.util.stream.IntStream;

public class EnumSetConverter {
    public static <T extends Enum<T>> int convertToInt(EnumSet<T> enumSet) {
        return enumSet.stream().mapToInt((e) -> {
            int value = 1 << e.ordinal();
            return value;
        }).sum();
    }

    public static <T extends Enum<T>> EnumSet<T> convertToEnumSet(Class<T> tclass, T[] values, int ivalues) {
        EnumSet<T> set = EnumSet.noneOf(tclass);
        convertToEnumSet(set,values,ivalues);
        return set;
    }

    public static <T extends Enum<T>> void convertToEnumSet(EnumSet<T> set, T[] values, int ivalues) {
        set.clear();
        IntStream.range(0, Math.min(values.length, 32)).forEach(i -> {
            if ((ivalues & (1 << i)) != 0)
                set.add(values[i]);
        });
    }
}