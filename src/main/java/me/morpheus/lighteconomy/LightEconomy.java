package me.morpheus.lighteconomy;

import com.google.inject.Inject;
import me.morpheus.lighteconomy.account.LEAccount;
import me.morpheus.lighteconomy.account.LEAccountSerializer;
import me.morpheus.lighteconomy.config.ConfigUtil;
import me.morpheus.lighteconomy.config.Global;
import me.morpheus.lighteconomy.config.SimpleConfigService;
import me.morpheus.lighteconomy.currency.CurrencyLoader;
import me.morpheus.lighteconomy.currency.CurrencyRegistryModule;
import me.morpheus.lighteconomy.storage.DataStorageService;
import me.morpheus.lighteconomy.storage.FileDataStorage;
import me.morpheus.lighteconomy.util.LETypeTokens;
import me.morpheus.lighteconomy.util.NameUtil;
import me.morpheus.lighteconomy.util.TextUtil;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.entity.living.player.User;
import org.spongepowered.api.event.CauseStackManager;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.cause.EventContextKeys;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameConstructionEvent;
import org.spongepowered.api.event.game.state.GameInitializationEvent;
import org.spongepowered.api.event.game.state.GamePostInitializationEvent;
import org.spongepowered.api.event.game.state.GameStoppingServerEvent;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.EconomyService;
import org.spongepowered.api.service.economy.account.AccountDeletionResultType;
import org.spongepowered.api.service.economy.account.UniqueAccount;
import org.spongepowered.api.service.economy.transaction.ResultType;
import org.spongepowered.api.service.economy.transaction.TransactionResult;
import org.spongepowered.api.service.pagination.PaginationList;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Plugin(id = LightEconomy.ID, name = LightEconomy.NAME, version = LightEconomy.VERSION, description = LightEconomy.DESCRIPTION)
public class LightEconomy {

    public static final String ID = "lighteconomy";
    public static final String NAME = "Lighteconomy";
    public static final String VERSION = "0.0.1";
    public static final String DESCRIPTION = "A lightweighted economy service";
    private static final String PERM = LightEconomy.ID + "commands";
    private boolean errored = false;

    @Inject public PluginContainer container;

    @Listener
    public void onConstruct(GameConstructionEvent event) {
        TypeSerializers.getDefaultSerializers().registerType(LETypeTokens.ACCOUNT_TOKEN, new LEAccountSerializer());

        Sponge.getRegistry().registerModule(Currency.class, new CurrencyRegistryModule());
        Sponge.getServiceManager().setProvider(this.container, DataStorageService.class, new FileDataStorage(ConfigUtil.ROOT));
        final DataStorageService dss = Sponge.getServiceManager().provideUnchecked(DataStorageService.class);
        Sponge.getServiceManager().setProvider(this.container, EconomyService.class, new LightEconomyService(dss));
        try {
            registerConfigService();
        } catch (IOException | ObjectMappingException e) {
            Sponge.getServer().shutdown();
            this.errored = true;
            LELog.getLogger().error("Config failed to load");
            LELog.getLogger().error("Error", e);
        }
    }

    @Listener
    public void onInit(GameInitializationEvent event) {
        if (this.errored) {
            return;
        }
        registerCommands();
        LELog.getLogger().info("Loading EconomyService");
        try {
            initEconomyService();
        } catch (IllegalStateException e) {
            Sponge.getServer().shutdown();
            this.errored = true;
        }
    }

    @Listener
    public void onPostInit(GamePostInitializationEvent event) {
        if (this.errored) {
            return;
        }
        LELog.getLogger().info("Loading config");
        final EconomyService es = Sponge.getServiceManager().provideUnchecked(EconomyService.class);
        try {
            ((LightEconomyService) es).populate();
        } catch (Exception e) {
            Sponge.getServer().shutdown();
            this.errored = true;
            LELog.getLogger().error("Failed to populate the economy service");
            LELog.getLogger().error("Error", e);
        }
    }

    @Listener
    public void onServerStopping(GameStoppingServerEvent event) {
        if (this.errored) {
            return;
        }
        LELog.getLogger().info("Saving...");
        CurrencyLoader.getInstance().save();
        final LightEconomyService es = (LightEconomyService) Sponge.getServiceManager().provideUnchecked(EconomyService.class);
        es.getVirtualAccounts().forEach(acc -> save(es, (LEAccount) acc));
        es.getUniqueAccounts().forEach(acc -> save(es, (LEAccount) acc));
        Sponge.getServiceManager().provideUnchecked(SimpleConfigService.class).save();
    }

    private void save(LightEconomyService es, LEAccount account) {
        if (account.isDirty()) {
            try {
                es.getStorage().save(account);
            } catch (Exception e) {
                LELog.getLogger().error("Failed to save account {}", account.getIdentifier());
                LELog.getLogger().error("Error", e);
            }
        }
    }
    @Listener
    public void onReload(GameReloadEvent event) {
        final SimpleConfigService cs = Sponge.getServiceManager().provideUnchecked(SimpleConfigService.class);
        cs.reload();
        LELog.getLogger().info("Config reloaded");
    }

    private void initEconomyService() {
        final Collection<Currency> currencies = Sponge.getRegistry().getAllOf(Currency.class);
        Currency def = null;
        for (Currency currency : currencies) {
            if (currency.isDefault()) {
                if (def != null) {
                    throw new IllegalStateException("Multiple default currencies: (" + def.getId() + ") and (" + currency.getId() + ")");
                }
                def = currency;
            }
        }
        if (def == null) {
            throw new IllegalStateException("Missing default currency");
        }
        final EconomyService es = Sponge.getServiceManager().provideUnchecked(EconomyService.class);
        ((LightEconomyService) es).setDefaultCurrency(def);
        ((LightEconomyService) es).setCurrencies(currencies.size());
        ((LEAccountSerializer) TypeSerializers.getDefaultSerializers().get(LETypeTokens.ACCOUNT_TOKEN)).setSize(currencies.size());
    }

    private void registerConfigService() throws IOException, ObjectMappingException {
        final SimpleConfigService cs = new SimpleConfigService();
        cs.populate();
        Sponge.getServiceManager().setProvider(this.container, SimpleConfigService.class, cs);
    }

    private void registerCommands() {
        Sponge.getCommandManager().register(this.container, bal(), "bal");
        Sponge.getCommandManager().register(this.container, baltop(), "baltop");
        Sponge.getCommandManager().register(this.container, pay(), "pay");
        Sponge.getCommandManager().register(this.container, setbal(), "setbal");
        Sponge.getCommandManager().register(this.container, delbal(), "delbal");
    }

    private CommandSpec bal() {
        return CommandSpec.builder()
                .arguments(GenericArguments.onlyOne(GenericArguments.userOrSource(Text.of("user"))))
                .permission(LightEconomy.PERM + ".balance.base")
                .executor((src, args) -> {
                    final User user = args.requireOne("user");
                    final EconomyService es = Sponge.getServiceManager().provideUnchecked(EconomyService.class);
                    if (!es.hasAccount(user.getUniqueId())) {
                        src.sendMessage(TextUtil.watermark(TextColors.RED, NameUtil.getDisplayName(user), " doesn't have an account"));
                        return CommandResult.empty();
                    }
                    final UniqueAccount acc = es.getOrCreateAccount(user.getUniqueId()).get();
                    final Map<Currency, BigDecimal> balances = acc.getBalances();
                    if (balances.size() == 1) {
                        final Map.Entry<Currency, BigDecimal> balance = balances.entrySet().iterator().next();
                        src.sendMessage(TextUtil.watermark(TextColors.GREEN, "Balance: ", balance.getKey().format(balance.getValue())));
                        return CommandResult.success();
                    }
                    final List<Text> contents = balances.entrySet().stream()
                            .map(entry -> entry.getKey().format(entry.getValue()))
                            .collect(Collectors.toList());

                    PaginationList.builder()
                            .title(Text.of(TextColors.GOLD, "[", TextColors.YELLOW, "Balance", TextColors.GOLD, "]"))
                            .contents(Text.of(Text.joinWith(Text.NEW_LINE, contents)))
                            .padding(Text.of(TextColors.GOLD, "-"))
                            .sendTo(src);
                    return CommandResult.success();
                })
                .build();
    }

    private CommandSpec baltop() {
        return CommandSpec.builder()
                .arguments(
                        GenericArguments.optional(
                                GenericArguments.onlyOne(GenericArguments.catalogedElement(Text.of("currency"), Currency.class))
                        )
                )
                .permission(LightEconomy.PERM + ".balancetop.base")
                .executor((src, args) -> {
                    final LightEconomyService es = (LightEconomyService) Sponge.getServiceManager().provideUnchecked(EconomyService.class);
                    final Currency currency = args.<Currency>getOne("currency").orElse(es.getDefaultCurrency());
                    final Global global = Sponge.getServiceManager().provideUnchecked(SimpleConfigService.class).getGlobal();
                    final List<Text> list = es.getUniqueAccounts()
                            .limit(global.getBalanceTopLimit())
                            .sorted(Comparator.<UniqueAccount, BigDecimal>comparing(acc -> acc.getBalance(currency)).reversed())
                            .map(uniqueAccount -> uniqueAccount.getDisplayName().concat(Text.of(": ")).concat(currency.format(uniqueAccount.getBalance(currency))))
                            .collect(Collectors.toList());

                    PaginationList.builder()
                            .title(Text.of(TextColors.GOLD, "[ ", TextColors.YELLOW, "Balance Top", TextColors.GOLD, " ]"))
                            .contents(list)
                            .padding(Text.of(TextColors.GOLD, "-"))
                            .sendTo(src);
                    return CommandResult.success();
                })
                .build();
    }

    private CommandSpec pay() {
        return CommandSpec.builder()
                .arguments(
                        GenericArguments.bigDecimal(Text.of("amount")),
                        GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.catalogedElement(Text.of("currency"), Currency.class))),
                        GenericArguments.onlyOne(GenericArguments.user(Text.of("user")))
                )
                .permission(LightEconomy.PERM + ".pay.base")
                .executor((src, args) -> {
                    if (!(src instanceof Player)) {
                        src.sendMessage(Text.of("no"));
                        return CommandResult.empty();
                    }
                    final EconomyService es = Sponge.getServiceManager().provideUnchecked(EconomyService.class);
                    final BigDecimal amount = args.requireOne("amount");
                    final Currency currency = args.<Currency>getOne("currency").orElse(es.getDefaultCurrency());
                    final User target = args.requireOne("user");
                    if (amount.compareTo(BigDecimal.ZERO) < 0) {
                        src.sendMessage(TextUtil.watermark(TextColors.RED, "Expected a positive number, but input ", amount, " was not"));
                        return CommandResult.empty();
                    }
                    final Optional<UniqueAccount> accOpt = es.getOrCreateAccount(((Player) src).getUniqueId());
                    if (!accOpt.isPresent()) {
                        src.sendMessage(TextUtil.watermark(TextColors.RED, "Unable to create account"));
                        return CommandResult.empty();
                    }
                    final Optional<UniqueAccount> targetAccOpt = es.getOrCreateAccount(target.getUniqueId());
                    if (!targetAccOpt.isPresent()) {
                        src.sendMessage(TextUtil.watermark(TextColors.RED, "Unable to create target account"));
                        return CommandResult.empty();
                    }
                    try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                        frame.addContext(EventContextKeys.PLUGIN, this.container);
                        final TransactionResult result = accOpt.get().transfer(targetAccOpt.get(), currency, amount, frame.getCurrentCause());
                        if (result.getResult() != ResultType.SUCCESS) {
                            src.sendMessage(TextUtil.watermark(TextColors.RED, "Transaction was not successful (", result.getResult(), ")"));
                            return CommandResult.empty();
                        }
                    }
                    final Text formatted = currency.format(amount);
                    src.sendMessage(TextUtil.watermark(TextColors.GREEN, formatted, " were sent to ", targetAccOpt.get().getDisplayName()));
                    target.getPlayer().ifPresent(p -> p.sendMessage(TextUtil.watermark(TextColors.GREEN, src.getName(), " sent you ", formatted)));
                    return CommandResult.success();
                })
                .build();
    }

    private CommandSpec setbal() {
        return CommandSpec.builder()
                .arguments(
                        GenericArguments.bigDecimal(Text.of("amount")),
                        GenericArguments.optionalWeak(GenericArguments.onlyOne(GenericArguments.catalogedElement(Text.of("currency"), Currency.class))),
                        GenericArguments.onlyOne(GenericArguments.user(Text.of("user")))
                )
                .permission(LightEconomy.PERM + ".setbalance.base")
                .executor((src, args) -> {
                    final EconomyService es = Sponge.getServiceManager().provideUnchecked(EconomyService.class);
                    final BigDecimal amount = args.requireOne("amount");
                    final Currency currency = args.<Currency>getOne("currency").orElse(es.getDefaultCurrency());
                    final User target = args.requireOne("user");
                    if (amount.compareTo(BigDecimal.ZERO) < 0) {
                        src.sendMessage(TextUtil.watermark(TextColors.RED, "Expected a positive number, but input ", amount, " was not"));
                        return CommandResult.empty();
                    }
                    final Optional<UniqueAccount> accOpt = es.getOrCreateAccount(target.getUniqueId());
                    if (!accOpt.isPresent()) {
                        src.sendMessage(TextUtil.watermark(TextColors.RED, "Unable to create account"));
                        return CommandResult.empty();
                    }
                    try (final CauseStackManager.StackFrame frame = Sponge.getCauseStackManager().pushCauseFrame()) {
                        frame.addContext(EventContextKeys.PLUGIN, this.container);
                        final TransactionResult result = accOpt.get().setBalance(currency, amount, frame.getCurrentCause());
                        if (result.getResult() != ResultType.SUCCESS) {
                            src.sendMessage(TextUtil.watermark(TextColors.RED, "Transaction was not successful (", result.getResult(), ")"));
                            return CommandResult.empty();
                        }
                    }
                    final Text formatted = currency.format(amount);
                    src.sendMessage(TextUtil.watermark(TextColors.GREEN, NameUtil.getDisplayName(target), "'s balance set to ", formatted));
                    target.getPlayer().ifPresent(p -> p.sendMessage(TextUtil.watermark(TextColors.GREEN, "Your balance has been set to ", formatted)));
                    return CommandResult.success();
                })
                .build();
    }

    private CommandSpec delbal() {
        return CommandSpec.builder()
                .arguments(GenericArguments.onlyOne(GenericArguments.user(Text.of("user"))))
                .permission(LightEconomy.PERM + ".delbalance.base")
                .executor((src, args) -> {
                    final EconomyService es = Sponge.getServiceManager().provideUnchecked(EconomyService.class);
                    final User target = args.requireOne("user");
                    final AccountDeletionResultType result = es.deleteAccount(target.getUniqueId());
                    if (!result.isSuccess()) {
                        src.sendMessage(TextUtil.watermark(TextColors.RED, "Unable to delete account ", result.getName()));
                        return CommandResult.empty();
                    }
                    src.sendMessage(TextUtil.watermark(TextColors.GREEN, NameUtil.getDisplayName(target), "'s account has been deleted"));
                    target.getPlayer().ifPresent(p -> p.sendMessage(TextUtil.watermark(TextColors.GREEN, "Your balance has been deleted ")));
                    return CommandResult.success();
                })
                .build();
    }
}
