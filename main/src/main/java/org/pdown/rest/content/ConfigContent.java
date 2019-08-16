package org.pdown.rest.content;

import com.fasterxml.jackson.core.type.TypeReference;
import org.pdown.gui.util.UserConfig;
import org.pdown.rest.base.content.PersistenceContent;
import org.pdown.rest.entity.ServerConfigInfo;

import java.io.File;

public class ConfigContent extends PersistenceContent<ServerConfigInfo, ConfigContent> {

    private static final ConfigContent INSTANCE = new ConfigContent();

    public static ConfigContent getInstance() {
        return INSTANCE;
    }

    @Override
    protected TypeReference type() {
        return new TypeReference<ServerConfigInfo>() {
        };
    }

    @Override
    public ServerConfigInfo get() {
        ServerConfigInfo serverConfigInfo = super.get();
        if (serverConfigInfo == null) return defaultValue();
        return serverConfigInfo;
    }

    @Override
    protected String savePath() {

        return UserConfig.getConfigFileNotCreate(".rest-server.cfg");
    }

    @Override
    protected ServerConfigInfo defaultValue() {
        ServerConfigInfo serverConfigInfo = new ServerConfigInfo();
        //Default values
        serverConfigInfo.setFilePath(System.getProperty("user.home") + File.separator + "Downloads");
        serverConfigInfo.setTaskLimit(3);
        serverConfigInfo.setConnections(64);
        serverConfigInfo.setPort(26339);
        serverConfigInfo.setTimeout(30);
        return serverConfigInfo;
    }
}
