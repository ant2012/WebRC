package ru.ant.iot.cloud.queue;

import com.flickr4java.flickr.Flickr;
import com.flickr4java.flickr.FlickrException;
import com.flickr4java.flickr.REST;
import com.flickr4java.flickr.RequestContext;
import com.flickr4java.flickr.auth.Auth;
import com.flickr4java.flickr.auth.AuthInterface;
import com.flickr4java.flickr.uploader.UploadMetaData;
import com.github.sarxos.webcam.Webcam;
import org.scribe.model.Token;
import org.scribe.model.Verifier;
import ru.ant.common.App;
import ru.ant.iot.ifttt.TaskReportTrigger;
import ru.ant.iot.rpi.Shell;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.ByteBuffer;

/**
 * Created by ant on 17.05.2016.
 */
public class SnapshotTask extends JsonTask {
    @Override
    public void execute() {
        Webcam webcam = Webcam.getDefault();
        try{
            webcam.open();
            try {
                BufferedImage img = webcam.getImage();
                ByteArrayOutputStream os = new ByteArrayOutputStream();
                ImageIO.write(img, "jpeg", os);
                InputStream is = new ByteArrayInputStream(os.toByteArray());

//                FlickrUtils.generateAuth();
//                FlickrUtils.finalizeGeneration();
                FlickrUtils.uploadImage(is);
            } catch (IOException e) {
                log.error("Error writing image", e);
            } catch (FlickrException e) {
                log.error("Flickr error", e);
            }

        }
        finally {
            webcam.close();
        }

        log.info("Snapshot task done");
    }
}
