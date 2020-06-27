package me.morpheus.lighteconomy.currency;

import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.text.Text;

import javax.annotation.Nullable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public final class LECurrency implements Currency {

    private final Text displayName;
    private final Text pluralDisplayName;
    private final Text symbol;
    @Nullable private final BigDecimal maxBalance;
    private final BigDecimal defaultBalance;
    private final int defaultFractionDigits;
    private final boolean isDefault;
    private final String id;
    private final String name;

    private LECurrency(LECurrency.CurrencyBuilder builder) {
        Objects.requireNonNull(builder.displayName, "displayname is null");
        Objects.requireNonNull(builder.pluralDisplayName, "pluralDisplayName is null");
        Objects.requireNonNull(builder.symbol, "symbol is null");
        Objects.requireNonNull(builder.id, "id is null");
        Objects.requireNonNull(builder.name, "name is null");
        if (builder.id.equals(builder.name)) {
            throw new IllegalArgumentException("id's and names should never be the same");
        }
        this.displayName = builder.displayName;
        this.pluralDisplayName = builder.pluralDisplayName;
        this.symbol = builder.symbol;
        this.maxBalance = builder.maxBalance < 0 ? null : BigDecimal.valueOf(builder.maxBalance);
        this.defaultBalance = BigDecimal.valueOf(builder.defaultBalance);
        this.defaultFractionDigits = builder.defaultFractionDigits;
        this.isDefault = builder.isDefault;
        this.id = builder.id;
        this.name = builder.name;
    }

    @Override
    public Text getDisplayName() {
        return this.displayName;
    }

    @Override
    public Text getPluralDisplayName() {
        return this.pluralDisplayName;
    }

    @Override
    public Text getSymbol() {
        return this.symbol;
    }

    @Override
    public Text format(BigDecimal amount, int numFractionDigits) {
        return Text.of(amount.setScale(numFractionDigits, RoundingMode.HALF_UP), this.symbol);
    }

    public BigDecimal getDefaultBalance() {
        return this.defaultBalance;
    }

    @Nullable
    public BigDecimal getMaxBalance() {
        return this.maxBalance;
    }

    @Override
    public int getDefaultFractionDigits() {
        return this.defaultFractionDigits;
    }

    @Override
    public boolean isDefault() {
        return this.isDefault;
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public String getName() {
        return this.name;
    }

    public static LECurrency.CurrencyBuilder builder() {
        return new LECurrency.CurrencyBuilder();
    }

    public static final class CurrencyBuilder {

        @Nullable private Text displayName;
        @Nullable private Text pluralDisplayName;
        @Nullable private Text symbol;
        private double maxBalance = -1.0;
        private double defaultBalance;
        private int defaultFractionDigits;
        private boolean isDefault;
        @Nullable private String id;
        @Nullable private String name;

        private CurrencyBuilder() {}

        public LECurrency.CurrencyBuilder displayName(Text displayName) {
            this.displayName = displayName;
            return this;
        }

        public LECurrency.CurrencyBuilder pluralDisplayName(Text pluralDisplayName) {
            this.pluralDisplayName = pluralDisplayName;
            return this;
        }

        public LECurrency.CurrencyBuilder symbol(Text symbol) {
            this.symbol = symbol;
            return this;
        }

        public LECurrency.CurrencyBuilder maxBalance(double maxBalance) {
            this.maxBalance = maxBalance;
            return this;
        }

        public LECurrency.CurrencyBuilder defaultBalance(double defaultBalance) {
            this.defaultBalance = defaultBalance;
            return this;
        }

        public LECurrency.CurrencyBuilder defaultFractionDigits(int defaultFractionDigits) {
            this.defaultFractionDigits = defaultFractionDigits;
            return this;
        }

        public LECurrency.CurrencyBuilder isDefault(boolean isDefault) {
            this.isDefault = isDefault;
            return this;
        }

        public LECurrency.CurrencyBuilder id(String id) {
            this.id = id;
            return this;
        }

        public LECurrency.CurrencyBuilder name(String name) {
            this.name = name;
            return this;
        }

        public LECurrency build() {
            return new LECurrency(this);
        }
    }
}
