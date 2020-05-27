package me.morpheus.lighteconomy.currency;

import com.google.common.reflect.TypeToken;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.TypeTokens;

import javax.annotation.Nullable;
import java.math.RoundingMode;

public final class LECurrencySerializer implements TypeSerializer<LECurrency> {

    @Override
    public LECurrency deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        final Text displayName = value.getNode("displayName").getValue(TypeTokens.TEXT_TOKEN);
        final Text pluralDisplayName = value.getNode("pluralDisplayName").getValue(TypeTokens.TEXT_TOKEN);
        final Text symbol = value.getNode("symbol").getValue(TypeTokens.TEXT_TOKEN);
        final double maxBalance = value.getNode("maxBalance").getDouble();
        final double defaultBalance = value.getNode("defaultBalance").getDouble();
        final int defaultFractionDigits = value.getNode("defaultFractionDigits").getInt();
        final boolean isDefault = value.getNode("isDefault").getBoolean();
        final String id = value.getNode("id").getString();
        final String name = value.getNode("name").getString();
        return LECurrency.builder()
                .displayName(displayName)
                .pluralDisplayName(pluralDisplayName)
                .symbol(symbol)
                .maxBalance(maxBalance)
                .defaultBalance(defaultBalance)
                .defaultFractionDigits(defaultFractionDigits)
                .isDefault(isDefault)
                .id(id)
                .name(name)
                .build();
    }

    @Override
    public void serialize(TypeToken<?> type, @Nullable LECurrency obj, ConfigurationNode value) throws ObjectMappingException {
        value.getNode("displayName").setValue(TypeTokens.TEXT_TOKEN, obj.getDisplayName());
        value.getNode("pluralDisplayName").setValue(TypeTokens.TEXT_TOKEN, obj.getPluralDisplayName());
        value.getNode("symbol").setValue(TypeTokens.TEXT_TOKEN, obj.getSymbol());
        if (obj.getMaxBalance() == null) {
            value.getNode("maxBalance").setValue(-1);
        } else {
            value.getNode("maxBalance").setValue(obj.getMaxBalance().setScale(obj.getDefaultFractionDigits(), RoundingMode.HALF_UP).doubleValue());
        }
        value.getNode("defaultBalance").setValue(obj.getDefaultBalance().setScale(obj.getDefaultFractionDigits(), RoundingMode.HALF_UP).doubleValue());
        value.getNode("defaultFractionDigits").setValue(obj.getDefaultFractionDigits());
        value.getNode("isDefault").setValue(obj.isDefault());
        value.getNode("id").setValue(obj.getId());
        value.getNode("name").setValue(obj.getName());
    }
}
