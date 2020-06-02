package me.morpheus.lighteconomy.config;

import me.morpheus.lighteconomy.LELog;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.commented.SimpleCommentedConfigurationNode;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMapper;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.IOException;
import java.nio.file.Files;
import java.util.concurrent.CompletionException;

public final class SimpleConfigService {

    private final ConfigurationLoader<CommentedConfigurationNode> loader;
    private final ObjectMapper<Global>.BoundInstance mapper;

    public SimpleConfigService() {
        this.loader = HoconConfigurationLoader.builder()
                .setPath(ConfigUtil.CONF)
                .build();

        try {
            this.mapper = ObjectMapper.forClass(Global.class).bindToNew();
        } catch (ObjectMappingException e) {
            LELog.getLogger().error("Failed to populate configuration");
            throw new RuntimeException(e);
        }
    }

    public Global getGlobal() {
        return this.mapper.getInstance();
    }

    public void reload() {
        if (Files.notExists(ConfigUtil.CONF)) {
            return;
        }
        try {
            CommentedConfigurationNode node = this.loader.load();
            this.mapper.populate(node);
        } catch (Exception e) {
            throw new CompletionException(e);
        }
    }

    public void populate() throws ObjectMappingException, IOException {
        if (Files.notExists(ConfigUtil.CONF)) {
            return;
        }
        CommentedConfigurationNode node = this.loader.load();
        this.mapper.populate(node);
    }

    public void save() {
        SimpleCommentedConfigurationNode node = SimpleCommentedConfigurationNode.root();
        try {
            if (Files.notExists(ConfigUtil.CONF)) {
                Files.createFile(ConfigUtil.CONF);
            }
            this.mapper.serialize(node);
            this.loader.save(node);
        } catch (Exception e) {
            throw new CompletionException(e);
        }
    }
}
