package me.morpheus.lighteconomy.currency;

import me.morpheus.lighteconomy.LELog;
import me.morpheus.lighteconomy.config.ConfigUtil;
import me.morpheus.lighteconomy.util.LETypeTokens;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializerCollection;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.text.Text;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class CurrencyLoader {

    private static final CurrencyLoader INSTANCE = new CurrencyLoader();
    private static final Path CURRENCY = ConfigUtil.ROOT.resolve("currency");

    public static CurrencyLoader getInstance() {
        return INSTANCE;
    }

    private CurrencyLoader() {}

    public Collection<Currency> load() {
        if (Files.exists(CurrencyLoader.CURRENCY)) {
            final List<Currency> currencies = new ArrayList<>();
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(CurrencyLoader.CURRENCY)) {
                for (Path file : stream) {
                    LELog.getLogger().info("Loading currency from {}", file.getFileName());
                    currencies.add(load(file));
                }
                if (!currencies.isEmpty()) {
                    return currencies;
                }
            } catch (Exception e) {
                LELog.getLogger().error("Error while reading currency", e);
                return Collections.emptyList();
            }
        }

        return Collections.singleton(
                LECurrency.builder()
                        .displayName(Text.of("Coin"))
                        .pluralDisplayName(Text.of("Coins"))
                        .symbol(Text.of("c"))
                        .defaultBalance(0.0)
                        .defaultFractionDigits(2)
                        .isDefault(true)
                        .id("coin")
                        .name("Coin")
                        .build()
        );
    }

    public Currency load(Path path) throws IOException, ObjectMappingException {
        TypeSerializerCollection serializers = TypeSerializers.getDefaultSerializers().newChild()
                .registerType(LETypeTokens.CURRENCY_TOKEN, new LECurrencySerializer());
        ConfigurationOptions options = ConfigurationOptions.defaults().setSerializers(serializers);
        CommentedConfigurationNode node = HoconConfigurationLoader.builder()
                .setPath(path)
                .setDefaultOptions(options)
                .build()
                .load();

        final LECurrency currency = node.getValue(LETypeTokens.CURRENCY_TOKEN);
        if (currency == null) {
            throw new IllegalStateException("Unable to read Currency from " + path);
        }
        return currency;
    }

    public void save() {
        if (Files.notExists(CurrencyLoader.CURRENCY)) {
            try {
                Files.createDirectories(CurrencyLoader.CURRENCY);
            } catch (IOException e) {
                LELog.getLogger().error("Error while creating folder", e);
                return;
            }
        }
        final TypeSerializerCollection serializers = TypeSerializers.getDefaultSerializers().newChild()
                .registerType(LETypeTokens.CURRENCY_TOKEN, new LECurrencySerializer());
        final ConfigurationOptions options = ConfigurationOptions.defaults().setSerializers(serializers);

        final Collection<Currency> currencies = Sponge.getRegistry().getAllOf(Currency.class);
        for (final Currency currency : currencies) {
            final Path save = CurrencyLoader.CURRENCY.resolve(currency.getId() + ".conf");
            try {
                if (Files.notExists(save)) {
                    Files.createFile(save);
                }

                final ConfigurationNode n = SimpleCommentedConfigurationNode.root(options).setValue(LETypeTokens.CURRENCY_TOKEN, (LECurrency) currency);
                HoconConfigurationLoader.builder()
                        .setDefaultOptions(options)
                        .setPath(save)
                        .build()
                        .save(n);
            } catch (IOException | ObjectMappingException e) {
                LELog.getLogger().error("Error while saving currency {} {}", currency.getClass(), currency.getId());
                LELog.getLogger().error("Exception:", e);
            }
        }
    }
}
