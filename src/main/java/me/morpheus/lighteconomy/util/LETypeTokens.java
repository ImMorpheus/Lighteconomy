package me.morpheus.lighteconomy.util;

import com.google.common.reflect.TypeToken;
import me.morpheus.lighteconomy.account.LEAccount;
import me.morpheus.lighteconomy.currency.LECurrency;
import org.spongepowered.api.service.economy.Currency;

import java.util.Map;

public final class LETypeTokens {

    public static final TypeToken<LEAccount> ACCOUNT_TOKEN = TypeToken.of(LEAccount.class);

    public static final TypeToken<LECurrency> CURRENCY_TOKEN = TypeToken.of(LECurrency.class);

    public static final TypeToken<Map<Currency, Double>> MAP_CURRENCY_DOUBLE_TOKEN = new TypeToken<Map<Currency, Double>>() {private static final long serialVersionUID = -1L; };

    private LETypeTokens() {}
}
