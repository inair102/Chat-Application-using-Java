//package edu.nyu.cs9053.homework11;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

/**
 * User: blangel
 * Date: 11/23/14
 * Time: 4:32 PM
 */
public class NonBlockingChatter implements Chatter {
	
	private final SocketChannel chatServerChannel;
	private final Pipe.SourceChannel userInput;
	private final ByteBuffer readBuff;
	private final ByteBuffer writeBuff;
	private static final int Buffer_Size = 1024;
	private static final Charset UTF8 = Charset.forName("UTF-8");

    public NonBlockingChatter(SocketChannel chatServerChannel,
                              Pipe.SourceChannel userInput) throws IOException {
        // TODO
    	this.chatServerChannel = chatServerChannel;
    	this.userInput = userInput;
    	this.readBuff = ByteBuffer.allocate(Buffer_Size);
    	this.writeBuff = ByteBuffer.allocate(Buffer_Size);   	
    }

    @Override public void run() {
        // TODO
        try {
            Selector selector = Selector.open();
            chatServerChannel.configureBlocking(false);
            chatServerChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
            userInput.configureBlocking(false);
            userInput.register(selector, SelectionKey.OP_READ);
            while (!Thread.currentThread().isInterrupted()) {
    			mainWork(selector);
    		}
        } catch (IOException ioe) {
            System.out.printf("Exception - %s%n", ioe.getMessage());
            Thread.currentThread().interrupt();
        }
    }
    
    public void mainWork(Selector selector) throws IOException{
   		int readyChannel = selector.select();
   		if(readyChannel < 1){
   			return;
   		}
    	
    	Set<SelectionKey> keysSelected = selector.selectedKeys();
    	Iterator<SelectionKey> keyItr = keysSelected.iterator();
    	
    	while (keyItr.hasNext()){
    		SelectionKey key = keyItr.next();
            if (key.isReadable()){
	    		Channel channel = key.channel();
	    		if (channel == chatServerChannel){
	    			readFromServer();
                } else if (channel == userInput){
                    readFromUserInput();
                }
            }
            else if (key.isWritable()){
	    		Channel channel = key.channel();
                if (channel == chatServerChannel) {
                    writeToServer();
                }
            }
            keyItr.remove();
    	}
    }
    private void readFromServer() throws IOException {
        chatServerChannel.read(readBuff);
        readBuff.flip();
        CharsetDecoder decoder = UTF8.newDecoder();
        CharBuffer charBuffer = decoder.decode(readBuff);
        System.out.printf("%s", charBuffer.toString());
        readBuff.clear();
    }

    private void readFromUserInput() throws IOException {
        userInput.read(readBuff);
        readBuff.flip();
        writeBuff.put(readBuff);
        readBuff.clear();
    }

    private void writeToServer() throws IOException {
        if (writeBuff.position() > 0) {
            writeBuff.flip();
            chatServerChannel.write(writeBuff);
            writeBuff.clear();
        }
    }
}
