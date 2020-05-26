package me.morpheus.lighteconomy.transaction;

import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionType;
import org.spongepowered.api.service.economy.transaction.TransactionTypes;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Set;

public final class TransactionResultBuilder {

    @Nullable Account account;
    @Nullable Account accountTo;
    @Nullable Currency currency;
    @Nullable BigDecimal amount;
    Set<Context> contexts = Collections.emptySet();
    @Nullable ResultType resultType;
    @Nullable TransactionType transactionType;

    TransactionResultBuilder() {}

    public TransactionResultBuilder account(Account account) {
        this.account = account;
        return this;
    }

    public TransactionResultBuilder accountTo(Account accountTo) {
        this.accountTo = accountTo;
        return this;
    }

    public TransactionResultBuilder currency(Currency currency) {
        this.currency = currency;
        return this;
    }

    public TransactionResultBuilder amount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public TransactionResultBuilder contexts(Set<Context> contexts) {
        this.contexts = contexts;
        return this;
    }

    public TransactionResultBuilder result(ResultType result) {
        this.resultType = result;
        return this;
    }

    public TransactionResultBuilder type(TransactionType type) {
        this.transactionType = type;
        return this;
    }

    public LETransactionResult build() {
        if (this.transactionType == TransactionTypes.TRANSFER) {
            return new LETransferResult(this);
        }
        return new LETransactionResult(this);
    }
}
