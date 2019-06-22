package com.company;

import javafx.beans.property.SimpleBooleanProperty;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shared.SyncPipe;

import java.io.*;
import java.util.Scanner;

public class AndroidWhatsdumpAdapter implements Runnable{

    private static Logger log = LogManager.getLogger("condor-whatsapp-main");
    private final ProcessHandler processHandler;
    private OutputStream err = new OutputStreamLogger(true);
    private OutputStream progress = new OutputStreamLogger(false);
    private PrintWriter commandInput;
    private Process process;

    public SimpleBooleanProperty requestInputProperty() {
        return requestInput;
    }

    private SimpleBooleanProperty requestInput = new SimpleBooleanProperty(false);
    private SimpleBooleanProperty isDone = new SimpleBooleanProperty(false);
    private String phoneNumber;

    public AndroidWhatsdumpAdapter(ProcessHandler processHandler, String phoneNumber)
    {
        this.phoneNumber = phoneNumber;
        this.processHandler = processHandler;
    }


    public void run() {
//        String[] command = {"cmd", "/c", "start", "\"Whatsdump\"", new File("dist/whatsdump/whatsdump.exe").getAbsolutePath(),
//                "--wa-phone +4915753363836", "--wa-verify sms" };
        try {

            this.process = Runtime.getRuntime().exec(new String[]{"cmd"});
            new Thread(new SyncPipe(this.process.getErrorStream(), err)).start();
            new Thread(new SyncPipe(this.process.getInputStream(), progress)).start();
            this.commandInput = new PrintWriter(this.process.getOutputStream());

            runCommand("set ANDROID_HOME=" + new File("android-sdk").getAbsolutePath());
            runCommand("set JAVA_HOME=" + new File("jdk").getAbsolutePath());
            runCommand(new File("dist/whatsdump/whatsdump.exe").getAbsolutePath() +
                    " --wa-phone "+phoneNumber + " --wa-verify sms");
            runCommand("0");

            synchronized (this)
            {
                this.requestInput.set(true);
            }
            int returnCode = this.process.waitFor();
            this.isDone.set(true);
            progress.write(returnCode);
        } catch (InterruptedException e) {
            log.error(e.getLocalizedMessage());
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        } finally {
            this.commandInput.close();
            try {
                this.err.close();
                this.progress.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            this.process.destroy();
        }
    }

    public void runCommand(String command)
    {
        this.commandInput.println(command);
        this.commandInput.flush();
    }

    public static void main (String args[]) throws InterruptedException {
        AndroidWhatsdumpAdapter awa = new AndroidWhatsdumpAdapter(null, "+4915753363836");
        Thread sub = new Thread(awa);
        sub.start();
        while(!awa.requestInputProperty().getValue())
        {
            Thread.sleep(500);
        }
        awa.runCommand("0");
        awa.runCommand("n");
    }

    private class OutputStreamLogger extends OutputStream
    {
        private String mem;
        private boolean isErrorStream;

        @Override
        public void write (int b) {
            byte[] bytes = new byte[1];
            bytes[0] = (byte) (b & 0xff);
            mem = mem + new String(bytes);

            if (mem.endsWith ("\n")) {
                mem = mem.substring (0, mem.length () - 1);
                flush ();
            }
        }
        public void flush () {
            processHandler.passMessage(mem);
            if(isErrorStream)
            {
                log.error(mem);
            }
            else
                log.trace(mem);
            mem = "";
        }
        private OutputStreamLogger(boolean isErrorStream)
        {
            this.isErrorStream = isErrorStream;
        }
    }
}
