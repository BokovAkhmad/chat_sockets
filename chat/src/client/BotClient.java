package client;

import server.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class BotClient extends Client
{
   private static ArrayList<String> tmp = new ArrayList<>();
   static {
        for (int i = 0;i<99;i++){
            tmp.add(new String(String.format("date_bot_%d",i)));
        }
    }
    public static void main(String[] args)
    {
        BotClient botClient = new BotClient();
        botClient.run();
    }
    public class BotSocketThread extends SocketThread{
        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException
        {
            sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }
        @Override
        protected void processIncomingMessage(String message)
        {
            ConsoleHelper.writeMessage(message);
            if(message.contains(": ")){
                String[] tmp = message.split(": ");
                String name = tmp[0];
                String text = tmp[1];
                String answer;
                Calendar calendar = Calendar.getInstance();

                switch (text){
                    case "дата":
                        answer =  new SimpleDateFormat("d.MM.YYYY").format(calendar.getTime());
                        sendTextMessage(String.format("Информация для %s: %s",name,answer));
                        break;
                    case "день":
                        answer  =  new SimpleDateFormat("d").format(calendar.getTime());
                        sendTextMessage(String.format("Информация для %s: %s",name,answer));
                        break;
                    case "месяц":
                        answer =  new SimpleDateFormat("MMMM").format(calendar.getTime());
                        sendTextMessage(String.format("Информация для %s: %s",name,answer));
                        break;
                    case "год":
                        answer =  new SimpleDateFormat("YYYY").format(calendar.getTime());
                        sendTextMessage(String.format("Информация для %s: %s",name,answer));
                        break;
                    case "время":
                        answer =  new SimpleDateFormat("H:mm:ss").format(calendar.getTime());
                        sendTextMessage(String.format("Информация для %s: %s",name,answer));
                        break;
                    case "час":
                        answer =  new SimpleDateFormat("H").format(calendar.getTime());
                        sendTextMessage(String.format("Информация для %s: %s",name,answer));
                        break;
                    case "минуты":
                        answer =  new SimpleDateFormat("m").format(calendar.getTime());
                        sendTextMessage(String.format("Информация для %s: %s",name,answer));
                        break;
                    case "секунды":
                        answer =  new SimpleDateFormat("s").format(calendar.getTime());
                        sendTextMessage(String.format("Информация для %s: %s",name,answer));
                        break;
                    default: break;
                }

            }
        }
    }
    @Override
    protected SocketThread getSocketThread()
    {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSentTextFromConsole()
    {
        return false;
    }

    @Override
    protected String getUserName()
    {
        String s = tmp.remove(0);
        return s;
    }
}
