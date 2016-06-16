package ru.ant.rc.web;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import ru.ant.iot.rpi.Shell;

import javax.servlet.ServletException;
import javax.servlet.ServletInputStream;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.sound.sampled.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * Created by Ant on 05.06.2016.
 */
@WebServlet(name = "SoundServlet", urlPatterns = {"/soundServlet"})
public class SoundServlet extends HttpServlet {

    private Logger log;

    @Override
    public void init() throws ServletException {
        super.init();
        log = Logger.getLogger(getClass());
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) {
        String command = request.getParameter("command");
        if(command == null) return;

        log.debug(command);
        switch (command){
            case "start":
                Shell.execute("soundStart");
                break;
            case "stop":
                Shell.execute("soundStop");
                break;
            case "send":
                try {
                    ServletInputStream is = request.getInputStream();
                    byte[] buf = IOUtils.toByteArray(is);

                    AudioInputStream ais = AudioSystem.getAudioInputStream(new ByteArrayInputStream(buf));
                    Clip clip = AudioSystem.getClip();
                    clip.open(ais);
                    clip.addLineListener(new LineListener() {
                        @Override
                        public void update(LineEvent event) {
                            if(event.getType().equals(LineEvent.Type.STOP)){
                                event.getLine().close();
                            }
                        }
                    });
                    clip.start();

                } catch (IOException | UnsupportedAudioFileException | LineUnavailableException e) {
                    log.error("Error transmitting sound.", e);
                }
                break;
        }

        //TODO: Uncomment for remote debug
//        response.addHeader("Access-Control-Allow-Origin", "*");

    }
}
