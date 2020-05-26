package me.morpheus.lighteconomy.storage;

import org.spongepowered.api.service.economy.account.Account;

import java.util.Collection;

public interface DataStorageService {

    Collection<Account> load();

    void save(Account account);

    boolean delete(Account account);
}
