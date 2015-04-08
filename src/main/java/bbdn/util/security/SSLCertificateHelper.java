package bbdn.util.security;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.X509TrustManager;


public class SSLCertificateHelper {
    
	private static final String SSL = "SSL";
	
    /**
     * A 'hacky' way of attempting to ignore a SSL certificate.
     * @throws Exception
     */
    public static void ignoreCertificates() throws Exception {
            TrustManager tm = new TrustManager();
            TrustManager[] trustAllCerts = {tm};
            
            // create an all trusting HostnameVerifier
            HostnameVerifier AllowAllHostnameVerifier = new HostnameVerifier() {
                    public boolean verify(String urlHostName, SSLSession session) {
                            return true;
                    }
            };
            
            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance(SSL);
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(AllowAllHostnameVerifier);
    }
    
    /**
     * Used to attempt to ignore SSL certificates.
     */
    public static class TrustManager implements X509TrustManager {
            @Override
            public void checkClientTrusted(X509Certificate[] arg0, String arg1)throws CertificateException {}

            @Override
            public void checkServerTrusted(X509Certificate[] arg0, String arg1)throws CertificateException {}

            @Override
            public X509Certificate[] getAcceptedIssuers() {return null;}
    }
}
