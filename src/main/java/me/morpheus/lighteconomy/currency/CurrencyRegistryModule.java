package me.morpheus.lighteconomy.currency;

import org.spongepowered.api.registry.CatalogRegistryModule;
import org.spongepowered.api.service.economy.Currency;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public final class CurrencyRegistryModule implements CatalogRegistryModule<Currency> {

    private final Map<String, Currency> map = new HashMap<>();

    @Override
    public void registerDefaults() {
        final Collection<Currency> currencies = CurrencyLoader.getInstance().load();

        for (Currency currency : currencies) {
            register(currency);
        }
    }

    private void register(Currency currency) {
        this.map.put(currency.getId(), currency);
    }

    @Override
    public Optional<Currency> getById(String id) {
        return Optional.ofNullable(this.map.get(id));
    }

    @Override
    public Collection<Currency> getAll() {
        return Collections.unmodifiableCollection(this.map.values());
    }
}
