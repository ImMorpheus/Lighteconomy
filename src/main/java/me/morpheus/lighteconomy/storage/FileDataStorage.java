package me.morpheus.lighteconomy.storage;

import me.morpheus.lighteconomy.LELog;
import me.morpheus.lighteconomy.account.LEAccount;
import me.morpheus.lighteconomy.util.LETypeTokens;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;
import org.spongepowered.api.service.economy.account.Account;
import org.spongepowered.api.service.economy.account.UniqueAccount;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public final class FileDataStorage implements DataStorageService {

    private final Path data;
    private final Path unique;
    private final Path virtual;

    public FileDataStorage(Path root) {
        this.data = root.resolve("data");
        this.unique = this.data.resolve("unique");
        this.virtual = this.data.resolve("virtual");
    }

    @Override
    public Collection<Account> load() throws Exception {
        if (Files.notExists(this.data)) {
            return Collections.emptyList();
        }
        final List<Account> list = new ArrayList<>();
        if (Files.exists(this.virtual)) {
            populate(list, this.virtual);
        }
        if (Files.exists(this.unique)) {
            populate(list, this.unique);
        }
        return list;
    }

    public void populate(List<Account> list, Path dir) throws IOException, ObjectMappingException {
        try (final DirectoryStream<Path> accounts = Files.newDirectoryStream(dir)) {
            for (final Path acc : accounts) {
                final CommentedConfigurationNode node = HoconConfigurationLoader.builder()
                        .setPath(acc)
                        .build()
                        .load();

                final LEAccount account = node.getValue(LETypeTokens.ACCOUNT_TOKEN);
                if (account == null) {
                    throw new ObjectMappingException("Unable to read Account from " + acc);
                }
                list.add(account);
            }
        }
    }

    @Override
    public void save(Account account) throws Exception {
        final Path directory = getSaveDir(account);
        if (Files.notExists(directory)) {
            Files.createDirectories(directory);
        }
        final Path save = directory.resolve(account.getIdentifier() + ".conf");
        final ConfigurationNode n = SimpleCommentedConfigurationNode.root().setValue(LETypeTokens.ACCOUNT_TOKEN, (LEAccount) account);
        HoconConfigurationLoader.builder()
                .setPath(save)
                .build()
                .save(n);
        ((LEAccount) account).setDirty(false);
    }

    @Override
    public boolean delete(Account account) throws Exception {
        final Path directory = getSaveDir(account);
        final Path save = directory.resolve(account.getIdentifier() + ".conf");
        if (Files.notExists(save)) {
            return true;
        }
        Files.delete(save);
        return true;
    }

    private Path getSaveDir(Account account) {
        return account instanceof UniqueAccount ? this.unique : this.virtual;
    }
}
