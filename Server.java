package scr;



import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server {
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<String, Connection>();

    public static void main(String[] args) throws IOException {
        ConsoleHelper.writeMessage("Введите номер порта:");

        try (ServerSocket serverSocket = new ServerSocket(ConsoleHelper.readInt())) {
            System.out.println("Сервер запущен");
            Socket socket = null;
            while (true) {
                socket = serverSocket.accept();
                Handler handler = new Handler(socket);
                handler.start();
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }



    }

    public static void sendBroadcastMessage(Message message) {
        for (Map.Entry<String, Connection> m : connectionMap.entrySet()) {
            try {
                Connection c = m.getValue();
                c.send(message);
            } catch (IOException e) {
                System.out.println("Сообщение не может быть отправлено");
            }
        }
    }


    private static class Handler extends Thread {
        Socket socket;

        public Handler(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {

            try {
                ConsoleHelper.writeMessage("Было установлено соединение с сервером: " + socket.getRemoteSocketAddress());
                Connection connection = new Connection(socket);
                String user = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED, user));
                notifyUsers(connection, user);
                serverMainLoop(connection, user);

                for(Map.Entry<String,Connection> m : connectionMap.entrySet()){
                    String key = m.getKey();
                    Connection value = m.getValue();
                    if(key.equals(user)) connectionMap.remove(key, value);
                }
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, user));

                ConsoleHelper.writeMessage("Соединение с сервером было разорвано.");
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        private String serverHandshake(Connection connection) throws IOException, ClassNotFoundException {
            while (true) {
                connection.send(new Message(MessageType.NAME_REQUEST));
                Message answer = (Message) connection.receive();
                if (answer.getType() == MessageType.USER_NAME && !answer.getData().isEmpty()) {
                    if (!connectionMap.containsKey(answer.getData())) {
                        connectionMap.put(answer.getData(), connection);
                        connection.send(new Message(MessageType.NAME_ACCEPTED));
                        return answer.getData();
                    }
                }


            }
        }
       private void notifyUsers(Connection connection, String userName) throws IOException{
                for(Map.Entry<String, Connection> m : connectionMap.entrySet()){
                    String name = m.getKey();
                    if(!name.equals(userName)){
                        connection.send(new Message(MessageType.USER_ADDED, name));
                    }
                }
       }

       private void serverMainLoop(Connection connection, String userName) throws IOException, ClassNotFoundException{
                while (true){
                        Message message = connection.receive();
                        if(message.getType() != MessageType.TEXT){
                            ConsoleHelper.writeMessage("Ошибка!");
                        } else {
                            Message formatMessage = new Message(MessageType.TEXT, userName + ": "+ message.getData());
                            sendBroadcastMessage(formatMessage);

                        }


                }
       }
    }
}
