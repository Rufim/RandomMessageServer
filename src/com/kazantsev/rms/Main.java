package com.kazantsev.rms;

import java.io.IOException;
import java.util.Scanner;

/**
 * Created by Rufim on 23.01.2015.
 */

public class Main {

    public static final int DefaultPort = 8283;
    public static final String DefaultHost = "localhost";
    private static final String TAG = "Server:";

    public static void main(String[] args) {
        Scanner in = new Scanner(System.in);

        System.out.println("Запустить программу в режиме серв-ера или клиента? (S(erver) / C(lient))");
        while (true) {
            char answer = Character.toLowerCase(in.nextLine().charAt(0));
            if (answer == 's') {
                try {
                    Server server = new Server(DefaultPort);
                    server.setRandom(true);
                    server.runServer();
                } catch (IOException e) {
                    Log.e(IOException.class, "Unknown exception");
                    Log.w(IOException.class, e);
                }
            } else if (answer == 'c') {
                System.out.println("Введите IP для подключения к серверу.");
                System.out.println("Формат: xxx.xxx.xxx.xxx:xxxx");

                String ip = in.nextLine();

                if(ip.length() == 0) {
                    ip = DefaultHost + ":" + DefaultPort;
                }


                Client client = null;
                try {
                    client = new Client(ip);
                    client.runChat();
                } catch (Exception e) {
                    Log.e(Exception.class, "Unknown exception");
                    Log.w(Exception.class, e);
                }
            } else {
                System.out.println("Некорректный ввод. Повторите.");
            }
        }
    }
}
