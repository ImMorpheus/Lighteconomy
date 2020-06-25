package me.morpheus.lighteconomy.account;

import com.google.common.reflect.TypeToken;
import me.morpheus.lighteconomy.util.LETypeTokens;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializer;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.util.TypeTokens;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.UUID;

public final class LEAccountSerializer implements TypeSerializer<LEAccount> {

    private int size;

    public void setSize(int size) {
        this.size = size;
    }

    @Override
    public LEAccount deserialize(TypeToken<?> type, ConfigurationNode value) throws ObjectMappingException {
        final LEAccount.AccountBuilder builder = this.size == 0 ? LEAccount.builder() : LEAccount.builder(this.size);
        final Text displayName = value.getNode("displayName").getValue(TypeTokens.TEXT_TOKEN);
        if (displayName != null) {
            builder.displayName(displayName);
        }
        final Map<Currency, Double> map = value.getNode("balances").getValue(LETypeTokens.MAP_CURRENCY_DOUBLE_TOKEN);
        if (map != null && !map.isEmpty()) {
            for (Map.Entry<Currency, Double> entry : map.entrySet()){
                builder.balance(entry.getKey(), BigDecimal.valueOf(entry.getValue()));
            }
        }
        final String identifier = value.getNode("identifier").getString();
        if (identifier == null) {
            throw new ObjectMappingException("Missing identifier");
        }
        final UUID uuid = value.getNode("uuid").getValue(TypeTokens.UUID_TOKEN);
        return builder
                .identifier(identifier)
                .uniqueId(uuid)
                .build();
    }

    @Override
    public void serialize(TypeToken<?> type, @Nullable LEAccount obj, ConfigurationNode value) throws ObjectMappingException {
        if (obj == null) {
            return;
        }
        if (obj.hasDisplayName()) {
            value.getNode("displayName").setValue(TypeTokens.TEXT_TOKEN, obj.getDisplayName());
        }
        final Map<Currency, Double> balances = new IdentityHashMap<>();
        for (Map.Entry<Currency, BigDecimal> entry : obj.getBalances().entrySet()) {
            balances.put(entry.getKey(), entry.getValue().setScale(entry.getKey().getDefaultFractionDigits(), RoundingMode.HALF_UP).doubleValue());
        }
        value.getNode("balances").setValue(LETypeTokens.MAP_CURRENCY_DOUBLE_TOKEN, balances);
        value.getNode("identifier").setValue(obj.getIdentifier());
        if (obj instanceof UniqueAccount) {
            value.getNode("uuid").setValue(TypeTokens.UUID_TOKEN, ((UniqueAccount) obj).getUniqueId());
        }
    }
}
