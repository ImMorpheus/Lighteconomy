package me.morpheus.lighteconomy.util;

import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.format.TextStyles;

public final class TextUtil {

    private static final Text PLUGIN = Text.of(TextColors.GOLD, "[LE] ");

    public static Text watermark() {
        return PLUGIN;
    }

    public static Text watermark(Object object) {
        return PLUGIN.concat(Text.of(object));
    }

    public static Text watermark(Object... objects) {
        return PLUGIN.concat(Text.of(objects));
    }

    public static Text reset(Object... objects) {
        return Text.of(TextColors.RESET, TextStyles.RESET, Text.of(objects));
    }

    private TextUtil() {}
}
