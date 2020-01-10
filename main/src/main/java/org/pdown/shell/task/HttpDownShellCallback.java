package org.pdown.shell.task;

import org.pdown.core.boot.HttpDownBootstrap;
import org.pdown.core.dispatch.HttpDownCallback;
import org.pdown.core.entity.ChunkInfo;

public class HttpDownShellCallback extends HttpDownCallback {

    private static HttpDownShellCallback callback;

    public synchronized static HttpDownShellCallback getCallback() {
        if (callback == null) {
            callback = new HttpDownShellCallback();
        }
        return callback;
    }

    public synchronized static void setCallback(HttpDownShellCallback callback) {
        if (HttpDownShellCallback.callback == null) {
            HttpDownShellCallback.callback = callback;
        }
    }

    @Override
    public void onStart(HttpDownBootstrap httpDownBootstrap) {
        super.onStart(httpDownBootstrap);
        System.out.println(httpDownBootstrap.getTaskInfo().getDownSize());
        System.out.println(httpDownBootstrap.getTaskInfo().getConnectInfoList().size());

        System.out.println("start");
    }

    @Override
    public void onProgress(HttpDownBootstrap httpDownBootstrap) {
        super.onProgress(httpDownBootstrap);
        System.out.println(httpDownBootstrap.getTaskInfo().getSpeed());
//        System.out.println("progress");
    }

    @Override
    public void onChunkError(HttpDownBootstrap httpDownBootstrap, ChunkInfo chunkInfo) {
        super.onChunkError(httpDownBootstrap, chunkInfo);
        System.out.println("error");
    }

    @Override
    public void onError(HttpDownBootstrap httpDownBootstrap) {
        System.out.println("erro22");
        super.onError(httpDownBootstrap);
    }

    @Override
    public void onChunkDone(HttpDownBootstrap httpDownBootstrap, ChunkInfo chunkInfo) {
//        System.out.println("chunkDone");
        super.onChunkDone(httpDownBootstrap, chunkInfo);

    }

    @Override
    public void onDone(HttpDownBootstrap httpDownBootstrap) {
        System.exit(0);
    }
}
