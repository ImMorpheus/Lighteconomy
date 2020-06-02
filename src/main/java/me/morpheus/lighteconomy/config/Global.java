package me.morpheus.lighteconomy.config;

import ninja.leaping.configurate.objectmapping.Setting;

public final class Global {

    @Setting(value = "balance-top-limit")
    private short balanceTopLimit = 35;

    @Setting(value = "enable-transaction-events")
    private boolean enableTransactionEvents = true;

    public short getBalanceTopLimit() {
        return this.balanceTopLimit;
    }

    public boolean isEnableTransactionEvents() {
        return this.enableTransactionEvents;
    }
}
