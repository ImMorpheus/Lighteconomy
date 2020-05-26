package me.morpheus.lighteconomy.config;

import ninja.leaping.configurate.objectmapping.Setting;

public class Global {

    @Setting(value = "balance-top-limit")
    private int balanceTopLimit = 35;

    @Setting(value = "enable-transaction-events")
    private boolean enableTransactionEvents = true;

    public int getBalanceTopLimit() {
        return this.balanceTopLimit;
    }

    public boolean isEnableTransactionEvents() {
        return this.enableTransactionEvents;
    }
}
