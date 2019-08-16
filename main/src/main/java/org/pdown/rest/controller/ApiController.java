package org.pdown.rest.controller;

import org.pdown.gui.DownApplication;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    @RequestMapping("/createTask")
    public void createTask(HttpServletRequest request, HttpServletResponse response) throws Exception {
        Map<String, String> map = getQueryParams(request);
        DownApplication.INSTANCE.loadUri("/#/tasks?request=" + map.get("request") + "&response=" + map.get("response"));
        response.setHeader("Access-Control-Allow-Origin", "*");
    }

    private Map<String, String> getQueryParams(HttpServletRequest request) {
        Map<String, String> map = new HashMap<>();
        String queryString = request.getQueryString();
        if (!StringUtils.isEmpty(queryString)) {
            String[] params = queryString.split("&");
            for (String param : params) {
                String[] kv = param.split("=");
                if (kv.length == 2) {
                    map.put(kv[0], kv[1]);
                }
            }
        }
        return map;
    }
}
