package me.morpheus.lighteconomy.serializers;

import com.google.common.reflect.TypeToken;
import me.morpheus.lighteconomy.currency.LECurrency;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.service.economy.Currency;

public class CurrencySerializer implements TypeSerializer<Currency> {

    @Override
    public Currency deserialize(TypeToken<?> type, ConfigurationNode value) {
        return LECurrency.builder().id(value.getString()).build();
    }

    @Override
    public void serialize(TypeToken<?> type, Currency obj, ConfigurationNode value) {
        value.setValue(obj.getId());
    }
}
