package ru.ant.iot.cloud.queue;

import com.flickr4java.flickr.FlickrException;
import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.ds.fswebcam.FsWebcamDriver;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by ant on 17.05.2016.
 */
public class SnapshotTask extends JsonTask {
    static{
        Webcam.setDriver(new FsWebcamDriver());
    }
    @Override
    public void execute() {
        List<Webcam> cams = Webcam.getWebcams();
        cams.forEach(c -> log.info(c.getName()));
        if(cams.size() == 0) {
            log.error("No camera detected!");
            return;
        }
        Webcam webcam = cams.get(0);
        webcam.setViewSize(new Dimension(640, 480));
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
