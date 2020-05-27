package me.morpheus.lighteconomy.account;

import me.morpheus.lighteconomy.config.SimpleConfigService;
import me.morpheus.lighteconomy.currency.LECurrency;
import me.morpheus.lighteconomy.event.LEEconomyTransactionEvent;
import me.morpheus.lighteconomy.transaction.LETransactionResult;
import me.morpheus.lighteconomy.util.hacks.Reference2ObjectOpenHacksMap;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.event.cause.Cause;
import org.spongepowered.api.event.economy.EconomyTransactionEvent;
import org.spongepowered.api.service.context.Context;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.service.economy.transaction.TransactionType;
import org.spongepowered.api.service.economy.transaction.TransactionTypes;
import org.spongepowered.api.service.economy.transaction.TransferResult;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

public class LEAccount implements Account {

    @Nullable protected Text displayName;
    private final Map<Currency, BigDecimal> balances;
    private final String identifier;
    private boolean dirty = false;

    public LEAccount(LEAccount.AccountBuilder builder) {
        Objects.requireNonNull(builder.balances, "balances is null");
        Objects.requireNonNull(builder.identifier, "identifier is null");
        this.displayName = builder.displayName;
        this.balances = builder.balances;
        this.identifier = builder.identifier;
    }

    @Override
    public Text getDisplayName() {
        if (this.displayName == null) {
            this.displayName = Text.of(this.identifier);
        }
        return this.displayName;
    }

    @Override
    public final BigDecimal getDefaultBalance(Currency currency) {
        return ((LECurrency) currency).getDefaultBalance();
    }

    @Override
    public final boolean hasBalance(Currency currency, Set<Context> contexts) {
        return this.balances.containsKey(currency);
    }

    @Override
    public final BigDecimal getBalance(Currency currency, Set<Context> contexts) {
        final BigDecimal bal = this.balances.get(currency);
        if (bal == null) {
            return getDefaultBalance(currency);
        }
        return bal;
    }

    @Override
    public final Map<Currency, BigDecimal> getBalances(Set<Context> contexts) {
        return Collections.unmodifiableMap(this.balances);
    }

    @Override
    public final TransactionResult setBalance(Currency currency, BigDecimal amount, Cause cause, Set<Context> contexts) {
        requirePositive(amount);
        final BigDecimal max = ((LECurrency) currency).getMaxBalance();
        if (max.compareTo(amount) < 0) {
            final TransactionResult result = LETransactionResult.builder()
                    .account(this)
                    .amount(amount)
                    .contexts(contexts)
                    .currency(currency)
                    .result(ResultType.ACCOUNT_NO_SPACE)
                    .type(TransactionTypes.DEPOSIT)
                    .build();
            callTransactionEvent(cause, result);
            return result;
        }
        final BigDecimal previous = this.balances.put(currency, amount);
        setDirty(true);
        final TransactionType type = previous != null && amount.compareTo(previous) < 0 ? TransactionTypes.WITHDRAW : TransactionTypes.DEPOSIT;
        final TransactionResult result = LETransactionResult.builder()
                .account(this)
                .amount(amount)
                .contexts(contexts)
                .currency(currency)
                .result(ResultType.SUCCESS)
                .type(type)
                .build();
        callTransactionEvent(cause, result);
        return result;
    }

    @Override
    public final Map<Currency, TransactionResult> resetBalances(Cause cause, Set<Context> contexts) {
        final Map<Currency, TransactionResult> map = new IdentityHashMap<>(this.balances.size());
        this.balances.replaceAll((currency, current) -> {
            final BigDecimal amount = getDefaultBalance(currency);
            final TransactionType type = amount.compareTo(current) < 0 ? TransactionTypes.WITHDRAW : TransactionTypes.DEPOSIT;
            final TransactionResult result = LETransactionResult.builder()
                    .account(this)
                    .amount(amount)
                    .contexts(contexts)
                    .currency(currency)
                    .result(ResultType.SUCCESS)
                    .type(type)
                    .build();
            callTransactionEvent(cause, result);
            map.put(currency, result);
            return amount;
        });
        setDirty(true);
        return Collections.unmodifiableMap(map);
    }

    @Override
    public final TransactionResult resetBalance(Currency currency, Cause cause, Set<Context> contexts) {
        final BigDecimal amount = getDefaultBalance(currency);
        return setBalance(currency, amount, cause, contexts);
    }

    @Override
    public final TransactionResult deposit(Currency currency, BigDecimal amount, Cause cause, Set<Context> contexts) {
        requirePositive(amount);
        final boolean[] deposit = {true};
        this.balances.merge(currency, amount, (current, v) -> {
            final BigDecimal max = ((LECurrency) currency).getMaxBalance();
            final BigDecimal sum = current.add(v);
            if (max.compareTo(sum) < 0) {
                deposit[0] = false;
                return current;
            }
            return sum;
        });
        if (!deposit[0]) {
            final TransactionResult result = LETransactionResult.builder()
                    .account(this)
                    .amount(amount)
                    .contexts(contexts)
                    .currency(currency)
                    .result(ResultType.ACCOUNT_NO_SPACE)
                    .type(TransactionTypes.DEPOSIT)
                    .build();
            callTransactionEvent(cause, result);
            return result;
        }
        setDirty(true);
        final TransactionResult result = LETransactionResult.builder()
                .account(this)
                .amount(amount)
                .contexts(contexts)
                .currency(currency)
                .result(ResultType.SUCCESS)
                .type(TransactionTypes.DEPOSIT)
                .build();
        callTransactionEvent(cause, result);
        return result;
    }

    @Override
    public final TransactionResult withdraw(Currency currency, BigDecimal amount, Cause cause, Set<Context> contexts) {
        requirePositive(amount);
        final boolean[] withdraw = {true};
        final BigDecimal after = this.balances.computeIfPresent(currency, (k, old) -> {
            if (old.compareTo(amount) < 0) {
                withdraw[0] = false;
                return old;
            }
            return old.subtract(amount);
        });
        if (after == null || !withdraw[0]) {
            final TransactionResult result = LETransactionResult.builder()
                    .account(this)
                    .amount(amount)
                    .contexts(contexts)
                    .currency(currency)
                    .result(ResultType.ACCOUNT_NO_FUNDS)
                    .type(TransactionTypes.WITHDRAW)
                    .build();
            callTransactionEvent(cause, result);
            return result;
        }
        setDirty(true);
        final TransactionResult result = LETransactionResult.builder()
                .account(this)
                .amount(amount)
                .contexts(contexts)
                .currency(currency)
                .result(ResultType.SUCCESS)
                .type(TransactionTypes.WITHDRAW)
                .build();
        callTransactionEvent(cause, result);
        return result;
    }

    @Override
    public final TransferResult transfer(Account to, Currency currency, BigDecimal amount, Cause cause, Set<Context> contexts) {
        requirePositive(amount);
        final boolean[] withdraw = {true};
        final BigDecimal after = this.balances.computeIfPresent(currency, (k, old) -> {
            if (old.compareTo(amount) < 0) {
                withdraw[0] = false;
                return old;
            }
            return old.subtract(amount);
        });
        if (after == null || !withdraw[0]) {
            final TransferResult result = (TransferResult) LETransactionResult.builder()
                    .account(this)
                    .accountTo(to)
                    .amount(amount)
                    .contexts(contexts)
                    .currency(currency)
                    .result(ResultType.ACCOUNT_NO_FUNDS)
                    .type(TransactionTypes.TRANSFER)
                    .build();
            callTransactionEvent(cause, result);
            return result;
        }
        setDirty(true);
        final boolean[] deposit = {true};
        ((LEAccount) to).balances.merge(currency, amount, (current, v) -> {
            final BigDecimal max = ((LECurrency) currency).getMaxBalance();
            final BigDecimal sum = current.add(v);
            if (max.compareTo(sum) < 0) {
                deposit[0] = false;
                return current;
            }
            return sum;
        });
        if (!deposit[0]) {
            //revert withdraw
            this.balances.merge(currency, amount, BigDecimal::add);
            final TransferResult result = (TransferResult) LETransactionResult.builder()
                    .account(this)
                    .accountTo(to)
                    .amount(amount)
                    .contexts(contexts)
                    .currency(currency)
                    .result(ResultType.ACCOUNT_NO_SPACE)
                    .type(TransactionTypes.TRANSFER)
                    .build();
            callTransactionEvent(cause, result);
            return result;
        }
        ((LEAccount) to).setDirty(true);
        final TransferResult result = (TransferResult) LETransactionResult.builder()
                .account(this)
                .accountTo(to)
                .amount(amount)
                .contexts(contexts)
                .currency(currency)
                .result(ResultType.SUCCESS)
                .type(TransactionTypes.TRANSFER)
                .build();
        callTransactionEvent(cause, result);
        return result;
    }

    @Override
    public String getIdentifier() {
        return this.identifier;
    }

    @Override
    public Set<Context> getActiveContexts() {
        return Collections.emptySet();
    }

    private void callTransactionEvent(Cause cause, TransactionResult result) {
        final SimpleConfigService conf = Sponge.getServiceManager().provideUnchecked(SimpleConfigService.class);
        if (!conf.getGlobal().isEnableTransactionEvents()) {
            return;
        }
        final EconomyTransactionEvent event = new LEEconomyTransactionEvent(cause, result);
        Sponge.getEventManager().post(event);
    }

    private void requirePositive(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Expected a positive amount");
        }
    }

    public void setDirty(boolean dirty) {
        this.dirty = dirty;
    }

    public boolean isDirty() {
        return this.dirty;
    }

    public static LEAccount.AccountBuilder builder() {
        return new LEAccount.AccountBuilder();
    }

    public static LEAccount.AccountBuilder builder(int size) {
        if (size == 0) {
            throw new IllegalArgumentException("No");
        }
        return new LEAccount.AccountBuilder(size);
    }

    public static final class AccountBuilder {

        @Nullable private Text displayName;
        private final Map<Currency, BigDecimal> balances;
        @Nullable private String identifier;
        @Nullable UUID uniqueId;

        private AccountBuilder() {
            this.balances = new Reference2ObjectOpenHacksMap<>();
        }

        private AccountBuilder(int size) {
            this.balances = new Reference2ObjectOpenHacksMap<>(size);
        }

        public LEAccount.AccountBuilder displayName(Text displayName) {
            this.displayName = displayName;
            return this;
        }

        public LEAccount.AccountBuilder balances(Map<Currency, BigDecimal> balances) {
            this.balances.putAll(balances);
            return this;
        }

        public LEAccount.AccountBuilder balance(Currency currency, BigDecimal balance) {
            this.balances.put(currency, balance);
            return this;
        }

        public LEAccount.AccountBuilder identifier(String identifier) {
            this.identifier = identifier;
            return this;
        }

        public LEAccount.AccountBuilder uniqueId(@Nullable UUID uniqueId) {
            this.uniqueId = uniqueId;
            return this;
        }

        public void reset() {
            this.displayName = null;
            this.balances.clear();
            this.uniqueId = null;
            this.identifier = null;
        }

        public LEAccount build() {
            if (this.uniqueId != null) {
                return new LEUniqueAccount(this);
            }
            return new LEVirtualAccount(this);
        }
    }
}
