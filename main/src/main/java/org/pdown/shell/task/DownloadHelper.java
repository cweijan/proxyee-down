package org.pdown.shell.task;

import io.netty.channel.nio.NioEventLoopGroup;
import org.pdown.core.boot.HttpDownBootstrap;
import org.pdown.core.boot.HttpDownBootstrapBuilder;
import org.pdown.core.entity.HttpDownConfigInfo;
import org.pdown.core.entity.HttpRequestInfo;
import org.pdown.core.entity.HttpResponseInfo;
import org.pdown.core.util.HttpDownUtil;
import org.pdown.rest.content.ConfigContent;

import java.util.HashMap;

/**
 * @author cweijan
 * @version 2019/8/16 15:00
 */
public class DownloadHelper {

    public static void createDownloadTask(String url) throws Exception {
        HttpDownBootstrapBuilder bootstrapBuilder;
        HashMap<String, String> heads = new HashMap<>();
        HttpRequestInfo httpRequestInfo = HttpDownUtil.buildRequest("GET", url, heads, null);
        bootstrapBuilder = HttpDownBootstrap.builder().request(httpRequestInfo);
        HttpDownBootstrap httpDownBootstrap = bootstrapBuilder
                .response(resolve(httpRequestInfo))
                .downConfig(getConfig())
                .callback(HttpDownShellCallback.getCallback())
                .proxyConfig(ConfigContent.getInstance().get().getProxyConfig())
//                .taskInfo(new TaskInfo().setStatus(HttpDownStatus.WAIT).setStartTime(System.currentTimeMillis()))
                .build();
        httpDownBootstrap.start();
    }

    private static HttpResponseInfo resolve(HttpRequestInfo requestInfo) throws Exception {
        return HttpDownUtil.getHttpResponseInfo(requestInfo, null, ConfigContent.getInstance().get().getProxyConfig(), new NioEventLoopGroup(1));
    }

    private static HttpDownConfigInfo getConfig() {
        HttpDownConfigInfo httpDownConfigInfo = new HttpDownConfigInfo();
        httpDownConfigInfo.setSpeedLimit(0);
        httpDownConfigInfo.setRetryCount(0);
        httpDownConfigInfo.setTimeout(30);
        httpDownConfigInfo.setConnections(20);
        httpDownConfigInfo.setFilePath(ConfigContent.getInstance().get().getFilePath());
        return httpDownConfigInfo;
    }

}
