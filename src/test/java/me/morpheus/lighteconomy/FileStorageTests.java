package me.morpheus.lighteconomy;

import com.google.common.reflect.TypeToken;
import me.morpheus.lighteconomy.account.LEAccount;
import me.morpheus.lighteconomy.account.LEAccountSerializer;
import me.morpheus.lighteconomy.account.LEUniqueAccount;
import me.morpheus.lighteconomy.serializers.CurrencySerializer;
import me.morpheus.lighteconomy.storage.FileDataStorage;
import me.morpheus.lighteconomy.util.LETypeTokens;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.api.service.economy.Currency;
import org.spongepowered.api.service.economy.account.Account;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.Comparator;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class FileStorageTests {

    private static final UUID DUMMY = UUID.fromString("00000000-0000-0000-0000-000000000000");
    private static final Path ROOT = Paths.get("tests");

    @BeforeAll
    static void beforeAll() throws IOException {
        TypeSerializers.getDefaultSerializers()
                .registerType(LETypeTokens.ACCOUNT_TOKEN, new LEAccountSerializer())
                .registerType(TypeToken.of(Currency.class), new CurrencySerializer());
        Files.createDirectories(ROOT);
    }

    @Test
    void save() throws Exception {
        final LEAccount expected = LEAccount.builder()
                .identifier(DUMMY.toString())
                .uniqueId(DUMMY)
                .build();
        final FileDataStorage file = new FileDataStorage(ROOT);
        file.save(expected);
        final Collection<Account> loaded = file.load();
        assertEquals(1, loaded.size());

        final Account actual = loaded.iterator().next();
        assertSame(LEUniqueAccount.class, actual.getClass());
        assertEquals(expected.getIdentifier(), actual.getIdentifier());
        assertEquals(expected.hasDisplayName(), ((LEUniqueAccount) actual).hasDisplayName());
        assertEquals(expected.getFriendlyIdentifier(), actual.getFriendlyIdentifier());
    }

    @Test
    void delete() throws Exception {
        final LEAccount expected = LEAccount.builder()
                .identifier(DUMMY.toString())
                .uniqueId(DUMMY)
                .build();
        final FileDataStorage file = new FileDataStorage(ROOT);
        file.save(expected);

        final Collection<Account> loaded = file.load();
        assertEquals(1, loaded.size());
        final Account actual = loaded.iterator().next();

        file.delete(actual);
        assertTrue(file.load().isEmpty());
    }

    @AfterAll
    static void afterAll() throws IOException {
        Files.walk(ROOT)
                .sorted(Comparator.reverseOrder())
                .map(Path::toFile)
                .forEach(File::delete);
    }
}
