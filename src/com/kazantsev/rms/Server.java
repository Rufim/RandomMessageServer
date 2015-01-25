package com.kazantsev.rms;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Rufim on 23.01.2015.
 */
public class Server {

    ServerSocket server;
    boolean random = false;
    Thread randomChatThread;

    /**
     * Специальная "обёртка" для ArrayList, которая обеспечивает доступ к
     * массиву из разных нитей
     */
    List<Connection> connections = Collections.synchronizedList(new ArrayList<Connection>());

    public Server(int port) throws IOException {
        server = new ServerSocket(port);
    }

    public Server(ServerSocket server) {
        this.server = server;
    }

    /**
     * Запуск сервера
     */
    public void runServer() {
        try {

            if(random) {
                randomChatThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            Client client = new Client("localhost:" + server.getLocalPort());
                            client.runRandomChat();
                        } catch (Exception e) {
                            Log.e(Exception.class, "Unknown exception");
                            Log.w(Exception.class, e);
                        }
                    }
                });
                randomChatThread.start();
            }

            while (true) {
                Socket socket = server.accept();

                Connection con = new Connection(socket);
                connections.add(con);
                con.start();

            }

        } catch (IOException e) {
            Log.e(IOException.class, "Unknown exception");
            Log.w(IOException.class, e);
        } finally {
            closeAll();
        }
    }

    private void closeAll() {
        try {
            server.close();

            // Перебор всех Connection и вызов метода close() для каждого. Блок
            // synchronized {} необходим для правильного доступа к одним данным
            // их разных нитей
            synchronized (connections) {
                Iterator<Connection> iter = connections.iterator();
                while (iter.hasNext()) {
                    ((Connection) iter.next()).close();
                }
            }
        } catch (Exception e) {
            Log.e(Exception.class, "Потоки не были закрыты!");
            Log.w(Exception.class, e);
        }
    }

    public void setRandom(boolean random) {
       this.random = random;
    }


    private class Connection extends Thread {
        private BufferedReader in;
        private PrintWriter out;
        private Socket socket;

        private String name = "";

        /**
         * Инициализирует поля объекта и получает имя пользователя
         *
         * @param socket сокет, полученный из server.accept()
         */
        public Connection(Socket socket) {
            this.socket = socket;

            try {
                in = new BufferedReader(new InputStreamReader(
                        socket.getInputStream()));
                out = new PrintWriter(socket.getOutputStream(), true);

            } catch (IOException e) {
                Log.e(IOException.class, "Unknown exception");
                Log.w(IOException.class, e);
                close();
            }
        }

        /**
         * Запрашивает имя пользователя и ожидает от него сообщений. При
         * получении каждого сообщения, оно вместе с именем пользователя
         * пересылается всем остальным.
         *
         * @see java.lang.Thread#run()
         */
        @Override
        public void run() {
            try {
                name = in.readLine();
                // TODO: Нужно реализовать Service в андройд приложении, чтобы работало нормально
                // Отправляем всем клиентам сообщение о том, что зашёл новый пользователь
               /* synchronized (connections) {
                    Iterator<Connection> iter = connections.iterator();
                    while (iter.hasNext()) {
                        ((Connection) iter.next()).out.println(name + " cames now");
                    }
                } */

                String str = "";
                while (true) {
                    str = in.readLine();
                    if (str.equals("exit")) break;

                    // Отправляем всем клиентам очередное сообщение
                    synchronized (connections) {
                        Iterator<Connection> iter = connections.iterator();
                        while (iter.hasNext()) {
                            ((Connection) iter.next()).out.println(name + ": " + str);
                        }
                    }
                }

         /*       synchronized (connections) {
                    Iterator<Connection> iter = connections.iterator();
                    while (iter.hasNext()) {
                        ((Connection) iter.next()).out.println(name + " has left");
                    }
                } */
            } catch (IOException e) {
                Log.e(IOException.class, "Unknown exception");
                Log.w(IOException.class, e);
            } finally {
                close();
            }
        }

        public void close() {
            try {
                in.close();
                out.close();
                socket.close();

                // Если больше не осталось соединений, закрываем всё, что есть и
                // завершаем работу сервера
                synchronized (connections) {
                    connections.remove(this);
                    if (connections.size() == 0) {
                        Server.this.closeAll();
                        System.exit(0);
                    }
                }
            } catch (Exception e) {
                Log.e(Exception.class, "Потоки не были закрыты!");
                Log.w(Exception.class, e);
            }
        }
    }
}
