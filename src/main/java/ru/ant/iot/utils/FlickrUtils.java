package ru.ant.iot.utils;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.RequestContext;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.AuthInterface;
import com.flickr4java.flickr.auth.Permission;
import com.flickr4java.flickr.people.User;
import com.flickr4java.flickr.uploader.UploadMetaData;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import ru.ant.common.App;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by Ant on 24.05.2016.
 */
public class FlickrUtils {

    public static void generateAuth() throws IOException, FlickrException {

        Flickr flickr = new Flickr(App.getProperty("Flickr.auth.apiKey"), App.getProperty("Flickr.auth.sharedSecret"), new REST());
        AuthInterface authInterface = flickr.getAuthInterface();

        Token requestToken = authInterface.getRequestToken();
        System.out.println("Flickr.auth.token=" + requestToken.getToken());
        System.out.println("Flickr.auth.secret=" + requestToken.getSecret());
        String url = authInterface.getAuthorizationUrl(requestToken, Permission.WRITE);

        System.out.println("Follow this URL to authorise yourself on Flickr");
        System.out.println(url);
        System.out.println("Paste in the tokenKey it gives you");
    }

    public static void finalizeGeneration() throws FlickrException {
        Flickr f = new Flickr(App.getProperty("Flickr.auth.apiKey"), App.getProperty("Flickr.auth.sharedSecret"), new REST());

        AuthInterface authInterface = f.getAuthInterface();

        Token requestToken = new Token(App.getProperty("Flickr.auth.token"), App.getProperty("Flickr.auth.secret"));
        Token accessToken = authInterface.getAccessToken(requestToken, new Verifier(App.getProperty("Flickr.auth.tokenKey")));
        Auth auth = authInterface.checkToken(accessToken);
        System.out.println("Flickr.auth.token=" + accessToken.getToken());
        System.out.println("Flickr.auth.secret=" + accessToken.getSecret());
        System.out.println("Flickr.auth.user.id=" + auth.getUser().getId());
        System.out.println("Flickr.auth.user.username=" + auth.getUser().getUsername());
    }

    public static void uploadImage(InputStream is) throws FlickrException {
        Flickr f = new Flickr(App.getProperty("Flickr.auth.apiKey"), App.getProperty("Flickr.auth.sharedSecret"), new REST());

        UploadMetaData meta = new UploadMetaData();
        meta.setTitle("RaspiSnapshot");
        meta.setPublicFlag(true);

        User user = new User();
        user.setId(App.getProperty("Flickr.auth.user.id"));
        user.setUsername(App.getProperty("Flickr.auth.user.username"));
        user.setRealName("");
        Auth auth = new Auth(Permission.WRITE, user);
        auth.setToken(App.getProperty("Flickr.auth.token"));
        auth.setTokenSecret(App.getProperty("Flickr.auth.secret"));

        RequestContext.getRequestContext().setAuth(auth);

        String photoId = f.getUploader().upload(is, meta);

    }
}