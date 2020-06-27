package me.morpheus.lighteconomy.transaction;

import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.service.economy.transaction.TransactionType;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

public class LETransactionResult implements TransactionResult {

    private final Account account;
    private final Currency currency;
    private final BigDecimal amount;
    private final Set<Context> contexts;
    private final ResultType resultType;
    private final TransactionType transactionType;

    LETransactionResult(TransactionResultBuilder builder) {
        Objects.requireNonNull(builder.account, "account is null");
        Objects.requireNonNull(builder.currency, "currency is null");
        Objects.requireNonNull(builder.amount, "amount is null");
        if (builder.amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Expected a positive amount");
        }
        Objects.requireNonNull(builder.contexts, "contexts is null");
        Objects.requireNonNull(builder.resultType, "resultType is null");
        Objects.requireNonNull(builder.transactionType, "transactionType is null");
        this.account = builder.account;
        this.currency = builder.currency;
        this.amount = builder.amount;
        this.contexts = builder.contexts;
        this.resultType = builder.resultType;
        this.transactionType = builder.transactionType;
    }

    public static TransactionResultBuilder builder() {
        return new TransactionResultBuilder();
    }

    @Override
    public Account getAccount() {
        return this.account;
    }

    @Override
    public Currency getCurrency() {
        return this.currency;
    }

    @Override
    public BigDecimal getAmount() {
        return this.amount;
    }

    @Override
    public Set<Context> getContexts() {
        return Collections.unmodifiableSet(this.contexts);
    }

    @Override
    public ResultType getResult() {
        return this.resultType;
    }

    @Override
    public TransactionType getType() {
        return this.transactionType;
    }
}
