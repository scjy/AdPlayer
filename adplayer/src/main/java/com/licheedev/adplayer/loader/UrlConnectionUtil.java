package com.licheedev.adplayer.loader;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class UrlConnectionUtil {

    /**
     * 下载文件，兼容http和https
     *
     * @param urlStr
     * @param outputStream
     * @throws Exception
     */
    public static void download(String urlStr, OutputStream outputStream) throws Exception {

        URLConnection conn = null;

        try {
            URL url = new URL(urlStr);
            conn = url.openConnection();
            // 设置超时间为3秒
            conn.setConnectTimeout(3 * 1000);
            // 防止屏蔽程序抓取而返回403错误
            conn.setRequestProperty("User-Agent",
                "Mozilla/4.0 (compatible; MSIE 5.0; Windows NT; DigExt)");

            if (conn instanceof HttpsURLConnection) {
                // 配置https
                trustAllHosts((HttpsURLConnection) conn);
                ((HttpsURLConnection) conn).setHostnameVerifier(DO_NOT_VERIFY);
                conn.connect();
            } else if (conn instanceof HttpURLConnection) {
                conn.connect();
            } else {
                throw new IOException("Not support url=\"" + urlStr + "\"");
            }

            // 得到输入流
            try (InputStream in = conn.getInputStream()) {
                // 文件保存位置
                try (BufferedOutputStream out = new BufferedOutputStream(outputStream)) {
                    readAndWrite(in, out);
                }
            }
        } finally {
            if (conn instanceof HttpURLConnection) {
                ((HttpURLConnection) conn).disconnect();
            }
        }
    }

    private static void readAndWrite(InputStream in, OutputStream out) throws IOException {

        byte[] buffer = new byte[2048];
        int bytesRead;
        while ((bytesRead = in.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.flush();
    }

    /**
     * 覆盖java默认的证书验证
     */
    private static final TrustManager[] trustAllCerts = new TrustManager[] {
        new X509TrustManager() {
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[] {};
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType)
                throws CertificateException {
            }
        }
    };

    /**
     * 设置不验证主机
     */
    private static final HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    /**
     * 信任所有
     *
     * @param connection
     * @return
     */
    private static SSLSocketFactory trustAllHosts(HttpsURLConnection connection) {
        SSLSocketFactory oldFactory = connection.getSSLSocketFactory();
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            SSLSocketFactory newFactory = sc.getSocketFactory();
            connection.setSSLSocketFactory(newFactory);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return oldFactory;
    }
}