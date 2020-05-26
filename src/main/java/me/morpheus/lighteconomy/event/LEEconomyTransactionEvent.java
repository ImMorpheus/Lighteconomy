package me.morpheus.lighteconomy.event;

import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.economy.EconomyTransactionEvent;
import org.spongepowered.api.service.economy.transaction.TransactionResult;

public final class LEEconomyTransactionEvent implements EconomyTransactionEvent {

    private final Cause cause;
    private final TransactionResult result;

    public LEEconomyTransactionEvent(Cause cause, TransactionResult result) {
        this.cause = cause;
        this.result = result;
    }

    @Override
    public TransactionResult getTransactionResult() {
        return this.result;
    }

    @Override
    public Cause getCause() {
        return this.cause;
    }
}
