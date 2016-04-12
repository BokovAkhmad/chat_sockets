package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Server
{
    private static Map<String, Connection> connectionMap = new ConcurrentHashMap<>();

    public static Map<String, Connection> getConnectionMap()
    {
        return connectionMap;
    }

    public static void main(String[] args) {
        ConsoleHelper.writeMessage("Введите номер порта (например 1234)");
        int port = ConsoleHelper.readInt();
        try(  ServerSocket serverSocket = new ServerSocket(port)){
            ConsoleHelper.writeMessage("Server was started");
            while (true){
                Socket server =  serverSocket.accept();
                Handler handler =new Handler(server);
                handler.start();
            }
        }
        catch (IOException e){
            ConsoleHelper.writeMessage("Error was occurred");
        }
    }

    private static class Handler extends Thread{
        private Socket socket;
        public Handler(Socket socket)
        {
            this.socket = socket;
        }
        @Override
        public void run()
        {
            String name = null;
            try(Connection connection = new Connection(socket)){
                ConsoleHelper.writeMessage("Установлено соединение с удаленным адресом " + connection.getRemoteSocketAddress());
                name = serverHandshake(connection);
                sendBroadcastMessage(new Message(MessageType.USER_ADDED,name));
                sendListOfUsers(connection,name);
                serverMainLoop(connection,name);
            }
            catch (IOException e){
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом");
            }
            catch (ClassNotFoundException e){
                ConsoleHelper.writeMessage("Произошла ошибка при обмене данными с удаленным адресом");
            }
            if(name!=null&&!name.isEmpty()){
                connectionMap.remove(name);
                sendBroadcastMessage(new Message(MessageType.USER_REMOVED, name));
            }

            ConsoleHelper.writeMessage("Соединение с удалённым адресом закрыто");
        }
// рукопожатие
        private String serverHandshake(Connection connection) throws IOException,ClassNotFoundException{
         while(true){
             connection.send(new Message(MessageType.NAME_REQUEST));
             Message receive = connection.receive();
             if(receive.getType()==MessageType.USER_NAME){
                 if( receive.getData()!=null &&!receive.getData().isEmpty()&&!connectionMap.keySet().contains(receive.getData())){
                     connectionMap.put(receive.getData(),connection);
                     connection.send(new Message(MessageType.NAME_ACCEPTED));
                     return receive.getData();
                 }
             }
         }
        }
        // отправка новому участнику информации об остальных клиентах (участниках чата)
        private void sendListOfUsers(Connection connection,String userName)throws IOException{
            Iterator<Map.Entry<String,Connection>> iterator = connectionMap.entrySet().iterator();
            while (iterator.hasNext()){
                Map.Entry<String,Connection> pair = iterator.next();
                Connection value = pair.getValue();
                String name = pair.getKey();
                Message message = new Message(MessageType.USER_ADDED,name);
                if(!name.equals(userName)){
                   connection.send(message);
                }
            }
        }
        // главный цикл обработки сообщений сервером
        private void serverMainLoop(Connection connection,String userName)throws IOException,ClassNotFoundException{
            while (true){
                Message message = connection.receive();
                if (message.getType()==MessageType.TEXT){
                    Message message1 = new Message(MessageType.TEXT,userName+": "+message.getData());
                    sendBroadcastMessage(message1);
                }
                else {
                    ConsoleHelper.writeMessage("Error");
                }
            }
        }
    }
    // send message to all connection from connectionMap
    public static void sendBroadcastMessage(Message message){
        Iterator<Map.Entry<String,Connection>> iterator = connectionMap.entrySet().iterator();
        while (iterator.hasNext()){
            Map.Entry<String,Connection> pair = iterator.next();
            Connection value = pair.getValue();
            try{
                value.send(message);
            }
            catch (IOException e){
                ConsoleHelper.writeMessage("Message was'not sent");
            }
        }
    }
}
