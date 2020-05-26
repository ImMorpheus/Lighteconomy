package me.morpheus.lighteconomy;

import me.morpheus.lighteconomy.account.LEAccount;
import me.morpheus.lighteconomy.account.LEUniqueAccount;
import me.morpheus.lighteconomy.account.LEVirtualAccount;
import me.morpheus.lighteconomy.storage.DataStorageService;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.context.ContextCalculator;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.AccountDeletionResultType;
import org.spongepowered.api.service.economy.account.AccountDeletionResultTypes;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.account.VirtualAccount;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

public final class LightEconomyService implements EconomyService {

    @Nullable private Currency defaultCurrency;
    private int size;
    private final Map<UUID, UniqueAccount> uniqueAccountMap = new HashMap<>();
    private final Map<String, VirtualAccount> virtualAccountMap = new HashMap<>();
    private final DataStorageService storage;

    public LightEconomyService(DataStorageService storage) {
        this.storage = storage;
    }

    @Override
    public Currency getDefaultCurrency() {
        if (this.defaultCurrency == null) {
            // too early
            throw new IllegalStateException("default currency not initialised");
        }
        return this.defaultCurrency;
    }

    public void setDefaultCurrency(Currency defaultCurrency) {
        this.defaultCurrency = defaultCurrency;
    }

    public void setCurrencies(int size) {
        this.size = size;
    }

    @Override
    public Set<Currency> getCurrencies() {
        return new HashSet<>(Sponge.getRegistry().getAllOf(Currency.class));
    }

    @Override
    public boolean hasAccount(UUID uuid) {
        return this.uniqueAccountMap.containsKey(uuid);
    }

    @Override
    public boolean hasAccount(String identifier) {
        return this.virtualAccountMap.containsKey(identifier);
    }

    @Override
    public Optional<UniqueAccount> getOrCreateAccount(UUID uuid) {
        return Optional.of(
                this.uniqueAccountMap.computeIfAbsent(uuid,
                        id -> (LEUniqueAccount) LEAccount.builder(this.size)
                                .identifier(id.toString())
                                .uniqueId(id)
                                .build()
                )
        );
    }

    @Override
    public Optional<Account> getOrCreateAccount(String identifier) {
        return Optional.of(
                this.virtualAccountMap.computeIfAbsent(identifier,
                        id -> (LEVirtualAccount) LEAccount.builder(this.size)
                                .identifier(id)
                                .build()
                )
        );
    }

    @Override
    public AccountDeletionResultType deleteAccount(UUID uuid) {
        final UniqueAccount acc = this.uniqueAccountMap.remove(uuid);
        if (acc == null) {
            return AccountDeletionResultTypes.ABSENT;
        }
        final boolean success = this.storage.delete(acc);
        if (!success) {
            return AccountDeletionResultTypes.FAILED;
        }
        return AccountDeletionResultTypes.SUCCESS;
    }

    @Override
    public AccountDeletionResultType deleteAccount(String identifier) {
        final VirtualAccount acc = this.virtualAccountMap.remove(identifier);
        if (acc == null) {
            return AccountDeletionResultTypes.ABSENT;
        }
        final boolean success = this.storage.delete(acc);
        if (!success) {
            return AccountDeletionResultTypes.FAILED;
        }
        return AccountDeletionResultTypes.SUCCESS;
    }

    @Override
    public void registerContextCalculator(ContextCalculator<Account> calculator) {
        //TODO
    }

    public Stream<VirtualAccount> getVirtualAccounts() {
        return this.virtualAccountMap.values().stream();
    }

    public Stream<UniqueAccount> getUniqueAccounts() {
        return this.uniqueAccountMap.values().stream();
    }

    public DataStorageService getStorage() {
        return this.storage;
    }

    public void populate() {
        for (Account account : this.storage.load()) {
            if (account instanceof LEUniqueAccount) {
                this.uniqueAccountMap.put(((LEUniqueAccount) account).getUniqueId(), (LEUniqueAccount) account);
            } else if (account instanceof LEVirtualAccount) {
                this.virtualAccountMap.put(account.getIdentifier(), (LEVirtualAccount) account);
            } else {
                throw new IllegalArgumentException("Unknown account type");
            }
        }
    }
}
