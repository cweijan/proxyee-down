package org.pdown.gui.extension.mitm.ssl;

import com.github.monkeywie.proxyee.crt.CertUtil;
import com.github.monkeywie.proxyee.server.HttpProxyCACertFactory;

import java.io.File;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;

import org.pdown.gui.util.UserConfig;
import org.pdown.rest.util.PathUtil;

public class PDownCACertFactory implements HttpProxyCACertFactory {

    private static final String SSL_PATH = UserConfig.getConfigPath("ssl") ;

    @Override
    public X509Certificate getCACert() throws Exception {
        return CertUtil.loadCert(SSL_PATH + "ca.crt");
    }

    @Override
    public PrivateKey getCAPriKey() throws Exception {
        return CertUtil.loadPriKey(SSL_PATH + ".ca_pri.der");
    }
}
