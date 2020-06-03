package me.morpheus.lighteconomy;

import com.google.common.reflect.TypeToken;
import me.morpheus.lighteconomy.account.LEAccount;
import me.morpheus.lighteconomy.account.LEAccountSerializer;
import me.morpheus.lighteconomy.account.LEUniqueAccount;
import me.morpheus.lighteconomy.account.LEVirtualAccount;
import me.morpheus.lighteconomy.serializers.CurrencySerializer;
import me.morpheus.lighteconomy.util.LETypeTokens;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.objectmapping.serialize.TypeSerializers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.api.service.economy.Currency;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

final class AccountTests {

    private static final UUID DUMMY = UUID.fromString("00000000-0000-0000-0000-000000000000");

    @BeforeAll
    static void beforeAll() {
        TypeSerializers.getDefaultSerializers()
                .registerType(LETypeTokens.ACCOUNT_TOKEN, new LEAccountSerializer())
                .registerType(TypeToken.of(Currency.class), new CurrencySerializer());
    }

    @Test
    void createUnique() {
        final LEAccount unique = LEAccount.builder()
                .identifier(DUMMY.toString())
                .uniqueId(DUMMY)
                .build();
        assertSame(LEUniqueAccount.class, unique.getClass());
    }

    @Test
    void createVirtual() {
        final LEAccount unique = LEAccount.builder()
                .identifier(DUMMY.toString())
                .build();
        assertSame(LEVirtualAccount.class, unique.getClass());
    }

    @Test
    void serializer() throws Exception {
        final LEAccountSerializer serializer = new LEAccountSerializer();
        final LEAccount expected = LEAccount.builder()
                .identifier(DUMMY.toString())
                .uniqueId(DUMMY)
                .build();
        final SimpleCommentedConfigurationNode node = SimpleCommentedConfigurationNode.root();
        serializer.serialize(LETypeTokens.ACCOUNT_TOKEN, expected, node);
        final LEAccount actual = serializer.deserialize(LETypeTokens.ACCOUNT_TOKEN, node);

        assertEquals(expected.getIdentifier(), actual.getIdentifier());
        assertEquals(expected.hasDisplayName(), actual.hasDisplayName());
        assertEquals(expected.getFriendlyIdentifier(), actual.getFriendlyIdentifier());
    }

}
