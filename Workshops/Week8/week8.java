import java.net.Socket;
import java.net.InetAddress;
import java.io.DataInputStream;
import java.io.DataOutputStream;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class week8 {

    // Declares variables 
    static Socket sckt;
    static DataOutputStream dout;
    static BufferedReader in;

    static String largestSizeType = "";
    static int largestNumOfCores = 0;
    static int numOfServers = 0;
    static Boolean foundLargeServer;

    // value of the bufferedReader lines that are read
    static String recInString;
    static String getsAllString;

    // Declares command messages used
    static String helo = "HELO";
    static String auth = "AUTH " + System.getProperty("user.name");
    static String redy = "REDY";
    static String getsAll = "GETS Capable 3 500 1000";
    static String ok = "OK";
    static String schd = "SCHD";

    public static void main(String[] args) {
        try {

            sckt = new Socket("127.0.0.1", 50000);
            dout = new DataOutputStream(sckt.getOutputStream());
            in = new BufferedReader(new InputStreamReader(sckt.getInputStream()));

            //System.out.println("Target IP: " + sckt.getInetAddress() + " Target Port: " + sckt.getPort());
            //System.out.println("Local IP: " + sckt.getLocalAddress() + " local Port: " + sckt.getLocalPort());

            // SERVER HANDSHAKE START

            // Sends HELO
            dout.write((helo + "\n").getBytes());
            dout.flush();
            // Received OK
            recInString = in.readLine();
            System.out.println("Received from HELO: " + recInString);

            // Sends AUTH
            dout.write((auth + "\n").getBytes());
            dout.flush();
            // Received OK
            recInString = in.readLine();
            System.out.println("Received from AUTH: " + recInString);

            // String value that holds the command
            String serverCommand;

            // String of the received jobn message
            String jobnMessage;

            String getsAllMessageSplit[];
            Integer totalServer;

            while (true) {

            // while loop that runs and breaks if the BufferedReader contains "NONE"
                if(recInString.contains("NONE")){
                    break;
                }

                // Sends REDY
                dout.write((redy + "\n").getBytes());
                dout.flush();
                // Received JOBN XX YY
                recInString = in.readLine();

                // we need to store the JOBN ID line here
                jobnMessage = recInString;
                System.out.println("JOBN: " + jobnMessage);
                
                // initiate a string array that will populate an array of the JOBN details ( JOBN 172 4 320 2 50 120 ) 4 is job ID
                String jobnDetails[] = jobnMessage.split(" ");
                // initalise int for the jobnID
                int jobnID = 0;

                // Stores the server command and the jobID if the server command is JOBN
                serverCommand = jobnDetails[0];
                System.out.println("serverCommand: " + serverCommand);


                // If the commant is equal to JOBN 
                if (serverCommand.equals("JOBN")) {
                    //jobn details are parsed to integer from string and assigned to jobnID
                    jobnID = Integer.parseInt(jobnDetails[2]);
                    

                        // Sends GETS ALL
                        dout.write((getsAll + "\n").getBytes());
                        dout.flush();
                        // Received DATA X YYY, this X is the number of Servers
                        recInString = in.readLine();
                        
                        // Store the string data here
                        getsAllString = recInString;
                        System.out.println("getsCapable: " + getsAllString);

                        // Split the Data line string [DATA], [X], [YYY]
                        getsAllMessageSplit = getsAllString.split(" ");
                        // totalServer = X parse string to int
                        totalServer = Integer.parseInt(getsAllMessageSplit[1]);
                        //System.out.println("total server amount: " + totalServer);
                    
                        // Sends OK
                        dout.write((ok + "\n").getBytes());
                        dout.flush();
                        // Receive Server Details (ServerType X STATUS X Y XXXX XXXXX X X) | Y is number of Cores in server
                        
                        String serverArray[] = new String[totalServer];

                        for (int i = 0; i < totalServer; i++) {
                            serverArray[i] = in.readLine();
                            //System.out.println("server lines: " + serverArray[i]);
                        }
                    
                        // Split the OK line string ( [ServerType] [X] [STATUS] [X] [Y] [XXXX] [XXXXX] [X] [X] )
                        String okLineArray[] = serverArray[0].split(" ");

                        String serverSizeType = okLineArray[0];

                        String serverID = okLineArray[1];
        
                    
                    System.out.println( "JOBNID: "+jobnID+", Size Type: "+serverSizeType+", Server ID: "+serverID);
                    
                    dout.write((ok + "\n").getBytes());
                    dout.flush();
                    recInString = in.readLine();


                    // SCHD job using the jobnID the largest server type, and the server count
                    dout.write((schd + " " + jobnID + " " + serverSizeType + " " + serverID + "\n").getBytes());
                    dout.flush();
                    recInString = in.readLine();
                    
                }
                else{
                    // if the command is not JOBN ie. JCPL do nothing
                }

            }
            dout.write(("QUIT" + "\n").getBytes());
            dout.flush();
            recInString = in.readLine();

            in.close();
            dout.close();
            sckt.close();

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
