package scr.client;

import scr.ConsoleHelper;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class BotClient extends Client {

    public static void main(String[] args) throws IOException {
        BotClient client = new BotClient();
        client.run();

    }

    public class BotSocketThread extends SocketThread{

        @Override
        protected void clientMainLoop() throws IOException, ClassNotFoundException {
            BotClient.this.sendTextMessage("Привет чатику. Я бот. Понимаю команды: дата, день, " +
                    "месяц, год, время, час, минуты, секунды.");
            super.clientMainLoop();
        }

        @Override
        protected void processIncomingMessage(String message) {
            ConsoleHelper.writeMessage(message);
            if(!message.contains(":")) return;
            String [] str = message.split(":");
            String question = str[1].trim();
            Calendar calendar = new GregorianCalendar();
            String format = "Информация для %s: %s";
            if (question.equals("дата")){
                sendTextMessage(String.format(format, str[0], new SimpleDateFormat("d.MM.YYYY").format(calendar.getTime())));
            } else if (question.equals("день")){
                sendTextMessage(String.format(format, str[0], new SimpleDateFormat("d").format(calendar.getTime())));
            } else  if(question.equals("месяц")){
                sendTextMessage(String.format(format, str[0], new SimpleDateFormat("MMMM").format(calendar.getTime())));
            } else if (question.equals("год")){
                sendTextMessage(String.format(format, str[0], new SimpleDateFormat("YYYY").format(calendar.getTime())));
            } else if(question.equals("время")){
                sendTextMessage(String.format(format, str[0], new SimpleDateFormat("H:mm:ss").format(calendar.getTime())));
            } else if(question.equals("час")){
                sendTextMessage(String.format(format, str[0], new SimpleDateFormat("H").format(calendar.getTime())));
            } else if(question.equals("минуты")){
                sendTextMessage(String.format(format, str[0], new SimpleDateFormat("m").format(calendar.getTime())));
            } else if (question.equals("секунды")){
                sendTextMessage(String.format(format, str[0], new SimpleDateFormat("s").format(calendar.getTime())));
            }
        }
    }

    @Override
    protected SocketThread getSocketThread() {
        return new BotSocketThread();
    }

    @Override
    protected boolean shouldSendTextFromConsole() {
        return false;
    }

    @Override
    protected String getUserName() {
        return "date_bot_" +(int) (Math.random() * 100);
    }
}
