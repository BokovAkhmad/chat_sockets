package client;
import server.Connection;
import server.ConsoleHelper;
import server.Message;
import server.MessageType;

import java.io.IOException;
import java.net.Socket;

public class Client
{
    public static void main(String[] args){
        Client client = new Client();
        client.run();
    }
    protected Connection connection;
    private volatile boolean clientConnected = false;

    public class SocketThread extends Thread{
        protected void clientHandshake() throws IOException, ClassNotFoundException{
            while(true){
                Message message = connection.receive();
                if(message.getType()==MessageType.NAME_REQUEST){
                    String name = getUserName();
                    connection.send(new Message(MessageType.USER_NAME,name));

                }
                else {
                    if(message.getType()==MessageType.NAME_ACCEPTED)
                    {
                        notifyConnectionStatusChanged(true);
                        return;
                    }
                    else   throw new IOException("Unexpected MessageType");
                }
            }
        }
        protected void clientMainLoop() throws IOException,ClassNotFoundException{
         while (true){
                          Message message =  connection.receive();
             if(message.getType()==MessageType.TEXT){
                 processIncomingMessage(message.getData());
             }
             else {
                 if(message.getType()==MessageType.USER_ADDED){
                     informAboutAddingNewUser(message.getData());
                 }
                 else {
                     if(message.getType()==MessageType.USER_REMOVED){
                         informAboutDeletingNewUser(message.getData());
                     }
                     else throw new IOException("Unexpected MessageType");

                 }
             }
         }
        }

        protected void processIncomingMessage(String message){
            ConsoleHelper.writeMessage(message);
        }
        protected void informAboutAddingNewUser(String userName){
            ConsoleHelper.writeMessage(String.format("Участник с именем %s присоединился к чату",userName));
        }
        protected void informAboutDeletingNewUser(String userName){
            ConsoleHelper.writeMessage(String.format("Участник с именем %s покинул чат",userName));
        }
        protected void notifyConnectionStatusChanged(boolean clientConnected){
            Client.this.clientConnected =clientConnected;
            synchronized (Client.this){
                Client.this.notify();
            }
        }

        @Override
        public void run()
        {
            String serverAddress =   getServerAddress();
            int serverPort = getServerPort();
            try
            {
                Socket socket = new Socket(serverAddress,serverPort);

               connection = new Connection(socket);
                clientHandshake();
                clientMainLoop();
            }
            catch (IOException e){
                notifyConnectionStatusChanged(false);
            }
            catch (ClassNotFoundException e){
                notifyConnectionStatusChanged(false);
            }
        }
    }

    public void run(){
        SocketThread socketThread = getSocketThread();
        socketThread.setDaemon(true);
        socketThread.start();

        synchronized (this){
            try{
                this.wait();
                if(clientConnected){
                    ConsoleHelper.writeMessage("Соединение установлено. Для выхода наберите команду 'exit'.");
                }
                else {
                    ConsoleHelper.writeMessage("Произошла ошибка во время работы клиента.");
                }
                while (clientConnected){
                    String s = ConsoleHelper.readString();
                    if(s.equals("exit")) return;
                    if(shouldSentTextFromConsole())
                        sendTextMessage(s);
                }
            }
            catch (InterruptedException e){
                ConsoleHelper.writeMessage("Error was occurred!");
            }
        }
    }
    protected  String getServerAddress(){
        ConsoleHelper.writeMessage("Enter server address");
       return ConsoleHelper.readString();
    }
    protected int getServerPort(){
        ConsoleHelper.writeMessage("Enter server port");
        return ConsoleHelper.readInt();
    }
    protected  String getUserName(){
        ConsoleHelper.writeMessage("Enter user name");
        return ConsoleHelper.readString();
    }
    protected boolean shouldSentTextFromConsole(){
        return  true;
    }

    protected SocketThread getSocketThread(){
        return  new SocketThread();
    }
    protected void sendTextMessage(String text){
        try{
            connection.send(new Message(MessageType.TEXT,text));
        }
        catch (IOException e){
            ConsoleHelper.writeMessage("Error was occurred!");
            clientConnected = false;
        }
    }

}
