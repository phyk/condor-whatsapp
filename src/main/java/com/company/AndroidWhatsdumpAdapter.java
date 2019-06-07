package com.company;

import shared.SyncPipe;

import java.io.*;

public class AndroidWhatsdumpAdapter {
    public static void main(String[] args) throws IOException, InterruptedException {
//        String[] command = {"cmd", "/c", "start", "\"Whatsdump\"", new File("dist/whatsdump/whatsdump.exe").getAbsolutePath(),
//                "--wa-phone +4915753363836", "--wa-verify sms" };

        String[] command =
                {
                        "cmd",
                };
        Process p = Runtime.getRuntime().exec(command);
        new Thread(new SyncPipe(p.getErrorStream(), System.err)).start();
        new Thread(new SyncPipe(p.getInputStream(), System.out)).start();
        PrintWriter stdin = new PrintWriter(p.getOutputStream());
        stdin.println("set ANDROID_HOME="+new File("android-sdk").getAbsolutePath());
        stdin.println("set JAVA_HOME="+new File("jdk").getAbsolutePath());
        stdin.println(new File("dist/whatsdump/whatsdump.exe").getAbsolutePath() +
                " --wa-phone +4915753363836" + " --wa-verify sms");
        stdin.println("0");
        stdin.println("n");
        // write any other commands you want here
        stdin.close();
        int returnCode = p.waitFor();
        System.out.println("Return code = " + returnCode);
    }
}
