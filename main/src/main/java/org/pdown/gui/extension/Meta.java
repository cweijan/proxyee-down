package org.pdown.gui.extension;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.pdown.gui.util.UserConfig;
import org.pdown.rest.util.ContentUtil;

public class Meta {

  public transient static final String CONFIG_FILE = ".config.dat";

  private transient String path;
  private transient String fullPath;
  private boolean enabled = true;
  private String extensionName;
  private Map<String, Object> data;

  public String getPath() {
    return path;
  }

  public Meta setPath(String path) {
    this.path = path;
    return this;
  }

  public String getFullPath() {
    return fullPath;
  }

  public Meta setFullPath(String fullPath) {
    this.fullPath = fullPath;
    return this;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public Meta setEnabled(boolean enabled) {
    this.enabled = enabled;
    return this;
  }

  public Map<String, Object> getData() {
    return data;
  }

  public Meta setData(Map<String, Object> data) {
    this.data = data;
    return this;
  }

  public void save() {
    try {
      ContentUtil.save(this, UserConfig.getConfigFile("extension",extensionName), true);
    } catch (IOException e) {
    }
  }

  public static Meta load(String path,String extensionName) {
    Meta meta = null;
    try {
      meta = ContentUtil.get(UserConfig.getConfigFile("extension",extensionName), Meta.class);
    } catch (IOException ignored) {
    }
    if (meta == null) {
      meta = new Meta();
    }
    meta.setExtensionName(extensionName);
    meta.setPath("/" + new File(path).getName());
    meta.setFullPath(path);
    return meta;
  }

  public String getExtensionName() {
    return extensionName;
  }

  public void setExtensionName(String extensionName) {
    this.extensionName = extensionName;
  }
}
