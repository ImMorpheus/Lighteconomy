package me.morpheus.lighteconomy.util;

import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.text.Text;

public final class NameUtil {

    public static Text getDisplayName(User user) {
        return user.get(Keys.DISPLAY_NAME).orElse(Text.of(user.getName()));
    }

    private NameUtil() {}
}
