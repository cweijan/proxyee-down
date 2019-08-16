package org.pdown.rest.controller;

import io.netty.handler.codec.http.HttpHeaderNames;
import io.netty.handler.codec.http.HttpHeaderValues;
import io.netty.util.AsciiString;
import org.pdown.gui.DownApplication;
import org.pdown.gui.extension.ExtensionContent;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.Set;

@RestController
@RequestMapping("/pac")
public class PacController {

  private static final String PAC_TEMPLATE = "function FindProxyForURL(url, host) {"
      + "  if (isInNet(host, '127.0.0.1', '255.0.0.255')"
      + "      || isInNet(dnsResolve(host), '127.0.0.1', '255.0.0.255')) {"
      + "    return 'DIRECT';"
      + "  }"
      + "  var domains = [{domains}];"
      + "  var match = false;"
      + "  for (var i = 0; i < domains.length; i++) {"
      + "    if (shExpMatch(host, domains[i])) {"
      + "      match = true;"
      + "      break;"
      + "    }"
      + "  }"
      + "  return match ? 'PROXY 127.0.0.1:{port}' : 'DIRECT';"
      + "}";

  @RequestMapping("/pdown.pac")
  public void build(HttpServletResponse response) throws Exception {
    Set<String> domains = ExtensionContent.getProxyWildCards();
    String pacContent = PAC_TEMPLATE.replace("{port}", DownApplication.INSTANCE.PROXY_PORT + "");
    if (domains != null && domains.size() > 0) {
      StringBuilder domainsBuilder = new StringBuilder();
      for (String domain : domains) {
        if (domainsBuilder.length() != 0) {
          domainsBuilder.append(",");
        }
        domainsBuilder.append("'" + domain + "'");
      }
      pacContent = pacContent.replace("{domains}", domainsBuilder.toString());
    } else {
      pacContent = pacContent.replace("{domains}", "");
    }
    response.setHeader(HttpHeaderNames.CONTENT_TYPE.toString(), AsciiString.cached("application/x-ns-proxy-autoconfig").toString());
    response.setHeader(HttpHeaderNames.CACHE_CONTROL.toString(), HttpHeaderValues.NO_CACHE.toString());
    response.setHeader(HttpHeaderNames.PRAGMA.toString(), HttpHeaderValues.NO_CACHE.toString());
    response.setHeader(HttpHeaderNames.EXPIRES.toString(), String.valueOf(0));
    response.getWriter().write(pacContent);

  }

}
