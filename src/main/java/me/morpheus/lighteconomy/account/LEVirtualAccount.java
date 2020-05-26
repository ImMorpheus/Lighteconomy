package me.morpheus.lighteconomy.account;

import org.spongepowered.api.service.economy.account.VirtualAccount;

public final class LEVirtualAccount extends LEAccount implements VirtualAccount {

    LEVirtualAccount(LEAccount.AccountBuilder builder) {
        super(builder);
    }
}
