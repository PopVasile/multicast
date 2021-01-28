/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package multicast1;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;

public class Multicast1 {
    private final static String INET_ADDR = "224.0.0.3";
    private static int PORT = 8888;
    private static final String CLIENT_NAME = "PEER1";

    private static final int MAX_MESSAGE_NR = 10;
    private static int currentIndex = 0;
    private static final String[] lastMessages = new String[MAX_MESSAGE_NR];
     private static String lastmassage;
     
    public static void main(String args[]) {
        MulticastSocket multicastSocket;

        final String helloMessage = "Hello, world! I am " + CLIENT_NAME;

        try {
            InetAddress group = InetAddress.getByName(INET_ADDR);

            multicastSocket = new MulticastSocket(PORT);
            multicastSocket.joinGroup(group);

            startListening(multicastSocket, group);
            startSending(multicastSocket, group);

            byte[] message = helloMessage.getBytes();
            DatagramPacket messageOut = new DatagramPacket(message, message.length, group, PORT);
            multicastSocket.send(messageOut);
        } catch (SocketException e) {
            System.out.println("Socket: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("IO: " + e.getMessage());
        }
    }

    private static void startSending(MulticastSocket multicastSocket, InetAddress group) {
        new Thread(() -> {
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(System.in));
            try {
                while (true) {
                    String stringMessage = bufferedReader.readLine();
                    if (stringMessage.equalsIgnoreCase("x")) {
                        multicastSocket.leaveGroup(group);
                        multicastSocket.close();
                        System.exit(0);
                    }
                   
                   if (stringMessage.equalsIgnoreCase("r")) {
                        String stringMessageAndClientName = CLIENT_NAME + ": " + lastmassage;
                        byte[] message = stringMessageAndClientName.getBytes();
                        DatagramPacket messageOut = new DatagramPacket(message, message.length, group, PORT);
                        multicastSocket.send(messageOut);
                    }
                    else
                     {
                        String stringMessageAndClientName = CLIENT_NAME + ": " + stringMessage;
                        byte[] message = stringMessageAndClientName.getBytes();
                        DatagramPacket messageOut = new DatagramPacket(message, message.length, group, PORT);
                        multicastSocket.send(messageOut);
                     }

                    currentIndex++;
                    currentIndex = currentIndex % MAX_MESSAGE_NR;
                    lastMessages[currentIndex] = stringMessage;

                    System.out.println("sent " + stringMessage);
                if (stringMessage.equalsIgnoreCase("DEL")) {  
                        multicastSocket.leaveGroup(group);
                        multicastSocket.close();
                        System.exit(0);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static void startListening(MulticastSocket multicastSocket, InetAddress group) {
        System.out.println("prepare to receive");
        new Thread(() -> {
            while (true) {
                byte[] buffer = new byte[1000];
                DatagramPacket messageIn = new DatagramPacket(buffer, buffer.length);
                try {
                    multicastSocket.receive(messageIn);
                    String messageInString = new String(messageIn.getData(), 0, messageIn.getLength());
                   
                    if (messageInString.toUpperCase().contains("GET MESSAGES " + CLIENT_NAME.toUpperCase())) {
                       if (messageInString.equalsIgnoreCase("DEL")) {  
                        multicastSocket.leaveGroup(group);
                        multicastSocket.close();
                        System.exit(0);
                    } 
                       StringBuilder stringBuilder = new StringBuilder();
                        stringBuilder.append(CLIENT_NAME).append(": ");
                        for (String message : lastMessages) {
                            stringBuilder.append("\n").append(message);
                        }
                        byte[] message = stringBuilder.toString().getBytes();
                        DatagramPacket messageOut = new DatagramPacket(message, message.length, group, PORT);
                        multicastSocket.send(messageOut);
                    }

                    System.out.println("Received:" + messageInString);
                   
                    lastmassage=messageInString;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
}
