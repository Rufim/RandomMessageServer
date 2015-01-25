package com.kazantsev.rms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

/**
 * Created by Rufim on 23.01.2015.
 */
public class Client {

    Socket socket;
    BufferedReader in;
    boolean stopRandom = false;

    public Client(Socket socket) {
        this.socket = socket;
    }

    public Client(String address) throws IOException {
        HostAndPort hostAndPort = HostAndPort.fromString(address);
        socket = new Socket(hostAndPort.getHostText(), hostAndPort.getPort());
    }

    public void runChat() throws Exception {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scan = new Scanner(System.in);
            System.out.println("Введите свой ник:");
            out.println(scan.nextLine());
            String str = "";
            Listener listener= new Listener();
            listener.start();
            while (!str.equals("exit")) {
                str = scan.nextLine();
                out.println(str);
            }
            listener.setStop();
        } catch (Exception e) {
            Log.e(Exception.class, "Unknown exception");
            Log.w(Exception.class, e);
        }
    };

    public void runRandomChat() throws Exception {
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            System.out.println("Введите свой ник:");
            out.println("RandomUser");
            String str = "";
            Listener listener = new Listener();
            listener.start();
            Random rng = new Random(System.currentTimeMillis());
            while (!stopRandom) {
                str = getRandomString(rng.nextInt(100));
                out.println(str);
                Thread.sleep(15000);
            }
            listener.setStop();
        } catch (Exception e) {
            Log.e(Exception.class, "Unknown exception");
            Log.w(Exception.class, e);
        }
    }

    public void setStopRandom(boolean stopRandom) {
        this.stopRandom = stopRandom;
    }

    public String getRandomString(int length) {
        Random rng = new Random(System.currentTimeMillis());
        char[] chars = new char[length];
        String validChars = "abcdefghijklmnopqrstuvwxyz ABCEDFGHIJKLMNOPQRSTUVWXYZ1234567890";
        for (int i = 0; i < length; i++) {
            chars[i] = validChars.charAt(rng.nextInt(validChars.length()));
        }
        return new String(chars);
    }

    private class Listener extends Thread {
        private boolean stoped;

        public void setStop() {
            stoped = true;
        }

        @Override
        public void run() {
            try {
                while (!stoped) {
                    String str = in.readLine();
                    System.out.println(str);
                }
            } catch (IOException e) {
                Log.e(IOException.class, "Ошибка при получении сообщения.");
                Log.w(IOException.class, e);
            }
        }
    }
}
