package me.morpheus.lighteconomy.transaction;

import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.TransactionType;
import org.spongepowered.api.service.economy.transaction.TransactionTypes;
import org.spongepowered.api.service.economy.transaction.TransferResult;

import java.util.Objects;

public final class LETransferResult extends LETransactionResult implements TransferResult {

    private final Account accountTo;

    LETransferResult(TransactionResultBuilder builder) {
        super(builder);
        Objects.requireNonNull(builder.accountTo, "AccountTo is null");
        final TransactionType type = getType();
        if (type != TransactionTypes.TRANSFER) {
            throw new IllegalArgumentException("Expected TransactionTypes#TRANSFER but got " + type.getName());
        }
        this.accountTo = builder.accountTo;
    }

    @Override
    public Account getAccountTo() {
        return this.accountTo;
    }
}
