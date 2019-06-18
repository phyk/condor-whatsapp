package com.company;

import shared.SyncPipe;

import java.io.*;
import java.util.Scanner;

public class AndroidWhatsdumpAdapter implements Runnable{

    private OutputStream err;
    private OutputStream progress;
    private PrintWriter commandInput;
    private Process process;
    private boolean requestInput;

    public AndroidWhatsdumpAdapter(OutputStream errors, OutputStream progress)
    {
        this.err = errors;
        this.progress = progress;
    }


    public void run() {
//        String[] command = {"cmd", "/c", "start", "\"Whatsdump\"", new File("dist/whatsdump/whatsdump.exe").getAbsolutePath(),
//                "--wa-phone +4915753363836", "--wa-verify sms" };
        try {
            Scanner sc = new Scanner(System.in);

            this.process = Runtime.getRuntime().exec(new String[]{"cmd"});
            new Thread(new SyncPipe(this.process.getErrorStream(), err)).start();
            new Thread(new SyncPipe(this.process.getInputStream(), progress)).start();
            this.commandInput = new PrintWriter(this.process.getOutputStream());

            runCommand("set ANDROID_HOME=" + new File("android-sdk").getAbsolutePath());
            runCommand("set JAVA_HOME=" + new File("jdk").getAbsolutePath());
            runCommand(new File("dist/whatsdump/whatsdump.exe").getAbsolutePath() +
                    " --wa-phone +4915753363836" + " --wa-verify sms");
            //runCommand("0");

            synchronized (this)
            {
                this.requestInput = true;
            }
            int returnCode = this.process.waitFor();
            progress.write(returnCode);
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
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
    public boolean isRequestInput()
    {
        return requestInput;
    }

    public static void main (String args[]) throws InterruptedException {
        AndroidWhatsdumpAdapter awa = new AndroidWhatsdumpAdapter(System.err, System.out);
        Thread sub = new Thread(awa);
        sub.start();
        while(!awa.isRequestInput())
        {
            Thread.sleep(500);
        }
        awa.runCommand("0");
        awa.runCommand("n");
    }
}
