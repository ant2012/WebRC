package org.afraid.freedns;

import org.apache.http.HttpEntity;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.apache.log4j.Logger;
import ru.ant.common.App;

import java.io.IOException;

/**
 * Created by ant on 19.05.2016.
 */
public class DdnsUpdater {
    private final static String URL_TEMPLATE = "http://%1$s:%2$s@freedns.afraid.org/nic/update?hostname=%3$s&myip=%4$s";
    private static Logger log = Logger.getLogger(DdnsUpdater.class);

    public static String update(String ip){
        String login = App.getProperty("org.afraid.freedns.DdnsUpdater.login");
        String password = App.getProperty("org.afraid.freedns.DdnsUpdater.password");
        String domain = App.getProperty("org.afraid.freedns.DdnsUpdater.domain");
        String url = String.format(URL_TEMPLATE, login, password, domain, ip);

        HttpClient client = HttpClientBuilder.create().build();
        HttpGet get = new HttpGet(url);
        get.setHeader(HTTP.CONTENT_TYPE, "text/plain; charset=UTF-8");
        HttpEntity responseEntity = null;
        try {
            responseEntity = client.execute(get).getEntity();
            if(responseEntity != null)
                return EntityUtils.toString(responseEntity);
        } catch (IOException e) {
            log.error("Ddns error", e);
        }
        return null;
    }
}
