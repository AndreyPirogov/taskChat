package scr.client;


import scr.Connection;
import scr.ConsoleHelper;
import scr.Message;
import scr.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client {
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public static void main(String[] args) throws IOException {
        Client client = new Client();
        client.run();

    }

    public class SocketThread  extends Thread{
        @Override
        public void run() {
            try {
                String addrees = getServerAddress();
                int port = getServerPort();
                Socket socket = new Socket(addrees,port);
                connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            } catch (IOException | ClassNotFoundException e) {
                notifyConnectionStatusChanged(false);
                e.printStackTrace();
            }

        }
        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
        }
        protected void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage("Участник с именем: " + userName + " присоеденился к чату.");
        }
        protected void informAboutDeletingNewUser(String userName){
            ConsoleHelper.writeMessage("Участник с именем: " + userName + " покинул чат.");
        }
        protected void notifyConnectionStatusChanged(boolean clientConnected){
            Client.this.clientConnected = clientConnected;
            synchronized (Client.this){
                Client.this.notify();
            }
        }
        protected void clientHandshake() throws IOException, ClassNotFoundException{
            while (true){
                Message m = connection.receive();

                if(m.getType() == MessageType.NAME_REQUEST){
                    connection.send(new Message(MessageType.USER_NAME, getUserName()));
                } else if (m.getType() == MessageType.NAME_ACCEPTED) {
                    notifyConnectionStatusChanged(true);
                    return;
                } else throw new IOException("Unexpected MessageType");

            }
        }

        protected void clientMainLoop() throws IOException, ClassNotFoundException{
            while (true) {
                Message m = connection.receive();
                if (m.getType() == MessageType.TEXT) {
                    processIncomingMessage(m.getData());
                } else if (m.getType() == MessageType.USER_ADDED) {
                    informAboutAddingNewUser(m.getData());
                } else if (m.getType() == MessageType.USER_REMOVED) {
                    informAboutDeletingNewUser(m.getData());
                } else {
                    throw new IOException("Unexpected MessageType");
                }
            }
        }


    }
    public void run() {
        try {
            SocketThread socketThread = getSocketThread();
            socketThread.setDaemon(true);
            socketThread.start();
            synchronized (this){
                wait();
            }
            if (clientConnected) ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
            while (clientConnected){
                String message =ConsoleHelper.readString();
                if(message.equals("exit")){
                    clientConnected = false;
                    return;
                }
                if(shouldSendTextFromConsole()){
                    sendTextMessage(message);
                }
            }

        } catch (InterruptedException e){
            ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
            clientConnected = false;
        }
    }

    protected String getServerAddress(){
      return  ConsoleHelper.readString();
    }
    protected int getServerPort(){
        return ConsoleHelper.readInt();
    }
    protected String getUserName(){
        return ConsoleHelper.readString();
    }
    protected boolean shouldSendTextFromConsole(){
        return true;
    }
    protected SocketThread getSocketThread(){
        return new SocketThread();
    }
    protected void sendTextMessage(String text){
        try {
            connection.send(new Message(MessageType.TEXT, text));
        } catch (IOException e){
            ConsoleHelper.writeMessage("Ошибка ввода");
            clientConnected = false;
        }
    }
}
