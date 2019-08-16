package org.pdown.rest.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.pdown.core.boot.HttpDownBootstrap;
import org.pdown.core.dispatch.HttpDownCallback;
import org.pdown.core.util.OsUtil;
import org.pdown.gui.DownApplication;
import org.pdown.gui.content.PDownConfigContent;
import org.pdown.gui.entity.PDownConfigInfo;
import org.pdown.gui.extension.ExtensionContent;
import org.pdown.gui.extension.mitm.server.PDownProxyServer;
import org.pdown.gui.extension.mitm.util.ExtensionCertUtil;
import org.pdown.gui.extension.mitm.util.ExtensionProxyUtil;
import org.pdown.gui.extension.util.ExtensionUtil;
import org.pdown.gui.util.AppUtil;
import org.pdown.gui.util.ConfigUtil;
import org.pdown.gui.util.ExecUtil;
import org.pdown.rest.util.PathUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.Map;

@RequestMapping("/native")
@RestController
public class NativeController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NativeController.class);

    //启动的时候检查一次
    private boolean checkFlag = true;
    private static final long WEEK = 7 * 24 * 60 * 60 * 1000L;

    @RequestMapping("getInitConfig")
    public Object getInitConfig() {
        HashMap<String, Object> data = new HashMap<>();
        PDownConfigInfo configInfo = PDownConfigContent.getInstance().get();
        //语言
        data.put("locale", configInfo.getLocale());
        //后台管理API请求地址
        data.put("adminServer", ConfigUtil.getString("adminServer"));
        //是否要检查更新
        boolean needCheckUpdate = false;
        if (checkFlag) {
            int rate = configInfo.getUpdateCheckRate();
            if (rate == 2
                    || (rate == 1 && (System.currentTimeMillis() - configInfo.getLastUpdateCheck()) > WEEK)) {
                needCheckUpdate = true;
                checkFlag = false;
                configInfo.setLastUpdateCheck(System.currentTimeMillis());
                PDownConfigContent.getInstance().save();
            }
        }
        data.put("needCheckUpdate", needCheckUpdate);
        //扩展下载服务器列表
        data.put("extFileServers", configInfo.getExtFileServers());
        //软件版本
        data.put("version", ConfigUtil.getString("version"));
        return data;
    }

    @RequestMapping("getConfig")
    public Object getConfig() {
        return PDownConfigContent.getInstance().get();
    }

    @RequestMapping("setConfig")
    public void setConfig(@RequestBody PDownConfigInfo configInfo) {
        PDownConfigInfo beforeConfigInfo = PDownConfigContent.getInstance().get();
        boolean proxyChange = (beforeConfigInfo.getProxyConfig() != null && configInfo.getProxyConfig() == null) ||
                (configInfo.getProxyConfig() != null && beforeConfigInfo.getProxyConfig() == null) ||
                (beforeConfigInfo.getProxyConfig() != null && !beforeConfigInfo.getProxyConfig().equals(configInfo.getProxyConfig())) ||
                (configInfo.getProxyConfig() != null && !configInfo.getProxyConfig().equals(beforeConfigInfo.getProxyConfig()));
        boolean localeChange = !configInfo.getLocale().equals(beforeConfigInfo.getLocale());
        BeanUtils.copyProperties(configInfo, beforeConfigInfo);
        if (localeChange) {
            DownApplication.INSTANCE.loadPopupMenu();
        }
        //检查到前置代理有变动重启MITM代理服务器
        if (proxyChange && PDownProxyServer.isStart) {
            new Thread(() -> {
                PDownProxyServer.close();
                PDownProxyServer.start(DownApplication.INSTANCE.PROXY_PORT);
            }).start();
        }
        PDownConfigContent.getInstance().save();

    }

    @RequestMapping("runFile")
    public void runFile(@RequestBody Map<String, String> data) throws Exception {
        String path = data.get("path");
        if (!StringUtils.isEmpty(path)) {
            File file = new File(path);
            if (!file.exists() || OsUtil.isUnix()) {
                // TODO: 2019/8/16
                Desktop.getDesktop().open(file);
            } else if (OsUtil.isWindows()) {
                ExecUtil.execBlock("cmd", "/c", "start", file.getPath());
            } else if (OsUtil.isMac()) {
                ExecUtil.execBlock("open", "-R", file.getPath());
            }
        }

    }

    @RequestMapping("showFile")
    public void showFile(@RequestBody Map<String, String> data) throws Exception {
        String path = data.get("path");
        if (!StringUtils.isEmpty(path)) {
            File file = new File(path);
            if (!file.exists() || OsUtil.isUnix()) {
                // TODO: 2019/8/16
                Desktop.getDesktop().open(file.getParentFile());
            } else if (OsUtil.isWindows()) {
                ExecUtil.execBlock("explorer.exe", "/select,", file.getPath());
            } else if (OsUtil.isMac()) {
                ExecUtil.execBlock("open", "-R", file.getPath());
            }
        }

    }

    @RequestMapping("openUrl")
    public void openUrl(String url, HttpServletResponse response) throws Exception {
        response.sendRedirect(URLDecoder.decode(url, "UTF-8"));
    }

    private static volatile HttpDownBootstrap updateBootstrap;

    @RequestMapping("doUpdate")
    public void doUpdate(@RequestParam(name = "path") String url) throws Exception {
        String path = PathUtil.ROOT_PATH + File.separator + "proxyee-down-main.jar.tmp";
        try {
            File updateTmpJar = new File(path);
            if (updateTmpJar.exists()) {
                updateTmpJar.delete();
            }
            updateBootstrap = AppUtil.fastDownload(url, updateTmpJar, new HttpDownCallback() {
                @Override
                public void onDone(HttpDownBootstrap httpDownBootstrap) {
                    File updateBakJar = new File(updateTmpJar.getParent() + File.separator + "proxyee-down-main.jar.bak");
                    updateTmpJar.renameTo(updateBakJar);
                }

                @Override
                public void onError(HttpDownBootstrap httpDownBootstrap) {
                    File file = new File(path);
                    if (file.exists()) {
                        file.delete();
                    }
                    httpDownBootstrap.close();
                }
            });
        } catch (Exception e) {
            throw e;
        }

    }

    @RequestMapping("getUpdateProgress")
    public Object getUpdateProgress() throws Exception {
        Map<String, Object> data = new HashMap<>();
        if (updateBootstrap != null) {
            data.put("status", updateBootstrap.getTaskInfo().getStatus());
            data.put("totalSize", updateBootstrap.getResponse().getTotalSize());
            data.put("downSize", updateBootstrap.getTaskInfo().getDownSize());
            data.put("speed", updateBootstrap.getTaskInfo().getSpeed());
        } else {
            data.put("status", 0);
        }
        return data;
    }

    @RequestMapping("doRestart")
    public void doRestart() {
        System.out.println("proxyee-down-restart");

    }

    /**
     * 获取已安装的插件列表
     */
    @RequestMapping("getExtensions")
    public void getExtensions(HttpServletResponse response) throws Exception {
        //刷新扩展信息
        ExtensionContent.load();
        ObjectMapper objectMapper = new ObjectMapper();
        response.setHeader("content-type", "application/json;charset=utf8");
        response.getWriter().write(objectMapper.writeValueAsString(ExtensionContent.get()));

    }

    /**
     * 安装扩展
     */
    @RequestMapping("installExtension")
    public void installExtension(HttpServletRequest request) throws Exception {
        extensionCommon(request, false);
    }

    /**
     * 更新扩展
     */
    @RequestMapping("updateExtension")
    public void updateExtension(HttpServletRequest request) throws Exception {
        extensionCommon(request, true);
    }

    private void extensionCommon(HttpServletRequest request, boolean isUpdate) throws Exception {
        String server = request.getParameter("server");
        String path = request.getParameter("path");
        String files = request.getParameter("files");
        if (isUpdate) {
            ExtensionUtil.update(server, path, files);
        } else {
            ExtensionUtil.install(server, path, files);
        }
        //刷新扩展content
        ExtensionContent.refresh(path);
        //刷新系统pac代理
        AppUtil.refreshPAC();

    }

    /**
     * 启用或禁用插件
     */
    @RequestMapping("toggleExtension")
    public void toggleExtension(@RequestBody Map<String, String> data) throws Exception {
        String path = data.get("path");
        boolean enabled = Boolean.parseBoolean(data.get("enabled"));
        ExtensionContent.get()
                .stream()
                .filter(extensionInfo -> extensionInfo.getMeta().getPath().equals(path))
                .findFirst()
                .get()
                .getMeta()
                .setEnabled(enabled)
                .save();
        //刷新pac
        ExtensionContent.refresh();
        AppUtil.refreshPAC();

    }

    @RequestMapping("getProxyMode")
    public Object getProxyMode() {
        Map<String, Object> data = new HashMap<>();
        data.put("mode", PDownConfigContent.getInstance().get().getProxyMode());
        return data;
    }

    @RequestMapping("changeProxyMode")
    public void changeProxyMode(Integer mode) throws Exception {
        PDownConfigContent.getInstance().get().setProxyMode(mode);
//    修改系统代理
        if (mode == 1) {
            AppUtil.refreshPAC();
        } else {
            ExtensionProxyUtil.disabledProxy();
        }
        PDownConfigContent.getInstance().save();

    }

    @RequestMapping("checkCert")
    public Object checkCert() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("status", AppUtil.checkIsInstalledCert());
        return data;
    }

    @RequestMapping("installCert")
    public Object installCert() throws Exception {
        Map<String, Object> data = new HashMap<>();
        boolean status;
        if (OsUtil.isUnix()) {
            if (!AppUtil.checkIsInstalledCert()) {
                ExtensionCertUtil.buildCert(AppUtil.SSL_PATH, AppUtil.SUBJECT);
            }
            File file = new File(AppUtil.CERT_PATH);
            // TODO: 2019/8/16
//      Desktop.getDesktop().open(file);
            status = true;
        } else {
            //再检测一次，确保不重复安装
            if (!AppUtil.checkIsInstalledCert()) {
                if (ExtensionCertUtil.existsCert(AppUtil.SUBJECT)) {
                    //存在无用证书需要卸载
                    ExtensionCertUtil.uninstallCert(AppUtil.SUBJECT);
                }
                //生成新的证书
                ExtensionCertUtil.buildCert(AppUtil.SSL_PATH, AppUtil.SUBJECT);
                //安装
                ExtensionCertUtil.installCert(new File(AppUtil.CERT_PATH));
                //检测是否安装成功，可能点了取消就没安装成功
                status = AppUtil.checkIsInstalledCert();
            } else {
                status = true;
            }
        }
        data.put("status", status);
        if (status && !PDownProxyServer.isStart) {
            new Thread(() -> {
                try {
                    AppUtil.startProxyServer();
                } catch (IOException e) {
                    LOGGER.error("Start proxy server error", e);
                }
            }).start();
        }
        return data;
    }

    @RequestMapping("copy")
    public void copy(@RequestBody Map<String, String> param) {
        String type = param.get("type");
        String data = param.get("data");
        Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
        Transferable selection = null;
        if ("text".equalsIgnoreCase(type)) {
            selection = new StringSelection(data);
        }
        clipboard.setContents(selection, null);

    }


}
