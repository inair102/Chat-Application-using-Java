import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BlockingChatter implements Chatter {
	
	private final InputStream chatServerInput;
	private final OutputStream chatServerOutput;
	private final InputStream userInput;
    private static final Charset UTF8 = Charset.forName("UTF-8");
	

    public BlockingChatter(InputStream chatServerInput, OutputStream chatServerOutput, InputStream userInput) {
        // TODO
    	this.chatServerInput = chatServerInput;
    	this.chatServerOutput = chatServerOutput;
    	this.userInput = userInput;
    }

    @Override public void run() {
        // TODO
        Thread userChat = new Thread(new Runnable(){
            @Override public void run(){
                try{
                    while (!Thread.currentThread().isInterrupted()){
                        BufferedReader input = new BufferedReader(new InputStreamReader(userInput));
                        String line = input.readLine();
                        synchronized (chatServerOutput){
                            chatServerOutput.write(line.getBytes(UTF8));
                        }
                    }
                }catch (IOException ioe){
                    System.out.printf("Exception - %s%n", ioe.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        });

        Thread serverChat = new Thread(new Runnable(){
            @Override public void run(){
                try{
                    while (!Thread.currentThread().isInterrupted()){
                        BufferedReader input = new BufferedReader(new InputStreamReader(chatServerInput));
                        String line = input.readLine();
                        if(line!=null){
                            System.out.printf("%s%n", line);
                        }
                    }
                }catch (IOException ioe){
                    System.out.printf("Exception - %s%n", ioe.getMessage());
                    Thread.currentThread().interrupt();
                }
            }
        });
        userChat.start();
        serverChat.start();
    }
}