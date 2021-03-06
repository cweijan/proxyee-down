package org.pdown.gui;

import org.pdown.core.util.OsUtil;
import org.pdown.gui.content.PDownConfigContent;
import org.pdown.gui.extension.ExtensionContent;
import org.pdown.gui.extension.mitm.util.ExtensionProxyUtil;
import org.pdown.gui.util.*;
import org.pdown.rest.DownRestServer;
import org.pdown.rest.content.ConfigContent;
import org.pdown.rest.content.RestWebServerFactoryCustomizer;
import org.pdown.rest.entity.ServerConfigInfo;
import org.pdown.rest.util.PathUtil;
import org.pdown.shell.task.DownloadHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.util.StringUtils;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;

public class DownApplication {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownApplication.class);

    private static final String OS = OsUtil.isWindows() ? "windows"
            : (OsUtil.isMac() ? "mac" : "linux");
    private static final String ICON_NAME = OS + "/logo.png";

    public static DownApplication INSTANCE;

    private TrayIcon trayIcon;

    //前端页面http服务器端口
    public int FRONT_PORT;
    //代理服务器端口
    public int PROXY_PORT;

    public void start() throws Exception {
        INSTANCE = this;
        //load config
        initConfig();
        //load pdown-rest
        initRest();
        initMacMITMTool();
        initExtension();
        initTray();
        loadUri(null);
    }


    private void initConfig() {
        PDownConfigContent.getInstance().load();
        FRONT_PORT = ConfigUtil.getInt("front.port");
        if ("prd".equals(ConfigUtil.getString("spring.profiles.active"))) {
            if (FRONT_PORT == -1) {
                FRONT_PORT = REST_PORT;
            }

        }
    }

    private void initRest() {
        //init rest server config
        RestWebServerFactoryCustomizer.init();
        ServerConfigInfo serverConfigInfo = ConfigContent.getInstance().get();
        serverConfigInfo.setPort(REST_PORT);
        if (StringUtils.isEmpty(serverConfigInfo.getFilePath())) {
            serverConfigInfo.setFilePath(System.getProperty("user.home") + File.separator + "Downloads");
        }
        new SpringApplicationBuilder(DownRestServer.class).headless(false).build().run();
    }

    //读取扩展信息和启动代理服务器
    private void initExtension() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            //退出时把系统代理还原
            if (PDownConfigContent.getInstance().get().getProxyMode() == 1) {
                try {
                    ExtensionProxyUtil.disabledProxy();
                } catch (IOException e) {
                }
            }
        }));
        new Thread(() -> {
            //检查是否安装了证书
            try {
                if (AppUtil.checkIsInstalledCert()) {
                    AppUtil.startProxyServer();
                }
            } catch (Exception e) {
                LOGGER.error("Init extension error", e);
            }
        }).start();
        //根据扩展生成pac文件并切换系统代理
        try {
            ExtensionContent.load();
            AppUtil.refreshPAC();
        } catch (IOException e) {
            LOGGER.error("Extension content load error", e);
        }
    }

    public static int macToolPort;

    //加载mac tool
    private void initMacMITMTool() {
        if (OsUtil.isMac()) {
            new Thread(() -> {
                String toolUri = "mac/mitm-tool.bin";
                Path toolPath = Paths.get(PathUtil.ROOT_PATH + File.separator + toolUri);
                try {
                    if (!toolPath.toFile().exists()) {
                        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
                        URL url = classLoader.getResource(toolUri);
                        URLConnection connection = url.openConnection();
                        if (connection instanceof JarURLConnection) {
                            if (!toolPath.getParent().toFile().exists()) {
                                Files.createDirectories(toolPath.getParent());
                            }
                            Files.copy(classLoader.getResourceAsStream(toolUri), toolPath);
                            Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxrw-rw-");
                            Files.setPosixFilePermissions(toolPath, perms);
                        }
                    }
                    //取一个空闲端口来运行mac tool
                    macToolPort = OsUtil.getFreePort();
                    //程序退出监听
                    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                        try {
                            ExecUtil.httpGet("http://127.0.0.1:" + macToolPort + "/quit");
                        } catch (IOException e) {
                        }
                    }));
                    ExecUtil.execBlockWithAdmin("'" + toolPath.toFile().getPath() + "' " + macToolPort);
                } catch (Exception e) {
                    LOGGER.error("initMacMITMTool error", e);
                    alertAndExit("Init mitm-tool error：" + e.getMessage());
                }
                System.exit(0);
            }).start();
        }
    }

    //加载托盘
    private void initTray() throws AWTException {
        if (OsUtil.isWindows() && SystemTray.isSupported()) {
            // 获得系统托盘对象
            SystemTray systemTray = SystemTray.getSystemTray();
            // 获取图片所在的URL
            URL url = Thread.currentThread().getContextClassLoader().getResource(ICON_NAME);
            // 为系统托盘加托盘图标
            Image trayImage = Toolkit.getDefaultToolkit().getImage(url);
            Dimension trayIconSize = systemTray.getTrayIconSize();
            trayImage = trayImage.getScaledInstance(trayIconSize.width, trayIconSize.height, Image.SCALE_SMOOTH);
            trayIcon = new TrayIcon(trayImage, "Proxyee Down");
            systemTray.add(trayIcon);
            loadPopupMenu();
            //双击事件监听
            trayIcon.addActionListener(event -> loadUri(null));
        }
    }

    public void loadPopupMenu() {
        //添加右键菜单
        PopupMenu popupMenu = new PopupMenu();
        MenuItem showItem = new MenuItem(I18nUtil.getMessage("gui.tray.show"));
        showItem.addActionListener(event -> loadUri(""));
        MenuItem setItem = new MenuItem(I18nUtil.getMessage("gui.tray.set"));
        setItem.addActionListener(event -> loadUri("/#/setting"));
        MenuItem aboutItem = new MenuItem(I18nUtil.getMessage("gui.tray.about"));
        aboutItem.addActionListener(event -> loadUri("/#/about"));
        MenuItem supportItem = new MenuItem(I18nUtil.getMessage("gui.tray.support"));
        supportItem.addActionListener(event -> loadUri("/#/support"));
        MenuItem closeItem = new MenuItem(I18nUtil.getMessage("gui.tray.exit"));
        closeItem.addActionListener(event -> {
            System.exit(0);
        });
        popupMenu.add(showItem);
        popupMenu.addSeparator();
        popupMenu.add(setItem);
        popupMenu.add(aboutItem);
        popupMenu.add(supportItem);
        popupMenu.addSeparator();
        popupMenu.add(closeItem);
        trayIcon.setPopupMenu(popupMenu);
    }

    public void loadUri(String uri) {
        String url = "http://127.0.0.1:" + FRONT_PORT + (uri == null ? "" : uri);
        try {
            if (!OsUtil.isUnix())
                Desktop.getDesktop().browse(URI.create(url));
        } catch (IOException e) {
            LOGGER.error("Open browse error", e);
        }
    }

    //提示并退出程序
    private void alertAndExit(String msg) {
        JOptionPane.showMessageDialog(null, msg, I18nUtil.getMessage("gui.warning"), JOptionPane.WARNING_MESSAGE);
        System.exit(0);
    }

    static {
        //设置日志存放路径
        System.setProperty("ROOT_PATH", UserConfig.getConfigPath("log"));
        //webView允许跨域访问
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

        //处理MAC dock图标
        if (OsUtil.isMac()) {
            try {
                Class<?> appClass = Class.forName("com.apple.eawt.Application");
                Method getApplication = appClass.getMethod("getApplication");
                Object application = getApplication.invoke(appClass);
                Method setDockIconImage = appClass.getMethod("setDockIconImage", Image.class);
                URL url = Thread.currentThread().getContextClassLoader().getResource("mac/dock_logo.png");
                Image image = Toolkit.getDefaultToolkit().getImage(url);
                setDockIconImage.invoke(application, image);
            } catch (Exception e) {
                LOGGER.error("handle mac dock icon error", e);
            }
        }
    }

    public static final int REST_PORT = 26339;

    private static void doCheck() {
        if (OsUtil.isBusyPort(REST_PORT)) {
            JOptionPane.showMessageDialog(
                    null,
                    I18nUtil.getMessage("gui.alert.startError", I18nUtil.getMessage("gui.alert.restPortBusy")),
                    I18nUtil.getMessage("gui.warning"),
                    JOptionPane.WARNING_MESSAGE);
            System.exit(0);
        }
    }

    public static void main(String[] args) throws Exception {

        if (args.length > 0) {
            DownloadHelper.createDownloadTask("http://8dx.pc6.com/lc6/sftpmsi902690.zip");
        } else {
            doCheck();
            new DownApplication().start();
        }

    }


}
