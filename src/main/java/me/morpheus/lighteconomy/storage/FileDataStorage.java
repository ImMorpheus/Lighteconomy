package me.morpheus.lighteconomy.storage;

import me.morpheus.lighteconomy.LELog;
import me.morpheus.lighteconomy.account.LEAccount;
import me.morpheus.lighteconomy.config.ConfigUtil;
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

    private static final Path DATA = ConfigUtil.ROOT.resolve("data");
    private static final Path UNIQUE = DATA.resolve("unique");
    private static final Path VIRTUAL = DATA.resolve("virtual");

    @Override
    public Collection<Account> load() {
        if (Files.notExists(FileDataStorage.DATA)) {
            return Collections.emptyList();
        }
        final List<Account> list = new ArrayList<>();
        if (Files.exists(FileDataStorage.VIRTUAL)) {
            populate(list, FileDataStorage.VIRTUAL);
        }
        if (Files.exists(FileDataStorage.UNIQUE)) {
            populate(list, FileDataStorage.UNIQUE);
        }
        return list;
    }

    public void populate(List<Account> list, Path dir) {
        try (final DirectoryStream<Path> accounts = Files.newDirectoryStream(dir)) {
            for (final Path acc : accounts) {
                final CommentedConfigurationNode node = HoconConfigurationLoader.builder()
                        .setPath(acc)
                        .build()
                        .load();

                final LEAccount account = node.getValue(LETypeTokens.ACCOUNT_TOKEN);
                if (account == null) {
                    LELog.getLogger().error("Unable to read Account from {}", acc);
                } else {
                    list.add(account);
                }
            }
        } catch (Exception e) {
            LELog.getLogger().error("Error while reading accounts from {}", dir);
            LELog.getLogger().error("Exception:", e);
        }
    }

    @Override
    public void save(Account account) {
        final Path directory = getSaveDir(account);
        if (Files.notExists(directory)) {
            try {
                Files.createDirectories(directory);
            } catch (IOException e) {
                LELog.getLogger().error("Error while creating directory {}", directory);
                LELog.getLogger().error("Exception:", e);
                return;
            }
        }
        final Path save = directory.resolve(account.getIdentifier() + ".conf");
        try {
            if (Files.notExists(save)) {
                Files.createFile(save);
            }
            final ConfigurationNode n = SimpleCommentedConfigurationNode.root().setValue(LETypeTokens.ACCOUNT_TOKEN, (LEAccount) account);
            HoconConfigurationLoader.builder()
                    .setPath(save)
                    .build()
                    .save(n);
            ((LEAccount) account).setDirty(false);
        } catch (IOException | ObjectMappingException e) {
            LELog.getLogger().error("Error while saving account {} {}", account.getClass(), account.getIdentifier());
            LELog.getLogger().error("Exception:", e);
        }
    }

    @Override
    public boolean delete(Account account) {
        final Path directory = getSaveDir(account);
        final Path save = directory.resolve(account.getIdentifier() + ".conf");
        if (Files.notExists(save)) {
            return true;
        }
        try {
            Files.delete(save);
            return true;
        } catch (IOException e) {
            LELog.getLogger().error("Error while deleting account {} {}", account.getClass(), account.getIdentifier());
            LELog.getLogger().error("Exception:", e);
            return false;
        }
    }

    private Path getSaveDir(Account account) {
        return account instanceof UniqueAccount ? FileDataStorage.UNIQUE : FileDataStorage.VIRTUAL;
    }
}
