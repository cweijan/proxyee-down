package org.pdown.gui.util;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;

/**
 * @author cweijan
 * @version 2019/8/13 15:15
 */
public class UserConfig {

    public static String getConfigPath(String... pathList) {
        StringBuilder path = new StringBuilder(System.getProperty("user.home") + File.separator + ".proxyee-config" + File.separator);
        for (String tempPath : pathList) {
            path.append(tempPath).append(File.separator);
        }
        File file = new File(path.toString());
        if (!file.exists()) {
            file.mkdirs();
        }
        return path.toString();
    }

    public static String getConfigFile(String... pathList) {
        StringBuilder path = new StringBuilder(System.getProperty("user.home") + File.separator + ".proxyee-config");
        for (String tempPath : pathList) {
            path.append(File.separator).append(tempPath);
        }
        File file = new File(path.toString());
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return path.toString();
    }

    public static String getConfigFileNotCreate(String... pathList) {
        StringBuilder path = new StringBuilder(System.getProperty("user.home") + File.separator + ".proxyee-config");
        for (String tempPath : pathList) {
            path.append(File.separator).append(tempPath);
        }
        File file = new File(path.toString());
        if (!file.exists()) {
            file.getParentFile().mkdirs();
        }
        return path.toString();
    }

}
