package me.morpheus.lighteconomy.storage;

import org.spongepowered.api.service.economy.account.Account;

import java.util.Collection;

public interface DataStorageService {

    Collection<Account> load() throws Exception;

    void save(Account account) throws Exception;

    boolean delete(Account account) throws Exception;
}
