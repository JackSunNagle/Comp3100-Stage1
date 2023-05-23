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
            //System.out.println("Received from HELO: " + recInString);

            // Sends AUTH
            dout.write((auth + "\n").getBytes());
            dout.flush();
            // Received OK
            recInString = in.readLine();
            //System.out.println("Received from AUTH: " + recInString);

            // String value that holds the command
            String serverCommand;

            // boolean value on wether the largest server has been found
            //Boolean largestServerFound = false;

            // String of the received jobn message
            String jobnMessage;

            // Declare varible of the serverIDcount
            int serverIDcount = 0;
            
            // sets boolean to false
            foundLargeServer=false;

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
                //System.out.println("JOBN: " + jobnMessage);

                // initiate a string array that will populate an array of the JOBN details ( JOBN 172 4 320 2 50 120 ) 4 is job ID
                String jobnDetails[] = jobnMessage.split(" ");
                // initalise int for the jobnID
                int jobnID = 0;

                // Stores the server command and the jobID if the server command is JOBN
                serverCommand = jobnDetails[0];
                //System.out.println("serverCommand: " + serverCommand);

                // If the commant is equal to JOBN 
                if (serverCommand.equals("JOBN")) {
                    //jobn details are parsed to integer from string and assigned to jobnID
                    jobnID = Integer.parseInt(jobnDetails[2]);

                    // if the foundLargeServer is false, it will run the find largest server method
                    if(foundLargeServer.equals(false)){
                        findLargestServer();
                    }

                    // if the serverIDcount is greater than or equal to the number of large servers, reset the count
                    if(serverIDcount>=numOfServers){
                        serverIDcount = 0;
                    }
                    
                    System.out.println( "JOBNID: "+jobnID+", Largest Size Type: "+largestSizeType+", Server ID: "+serverIDcount);

                    // SCHD job using the jobnID the largest server type, and the server count
                    dout.write((schd + " " + jobnID + " " + largestSizeType + " " + serverIDcount + "\n").getBytes());
                    dout.flush();
                    recInString = in.readLine();
                    serverIDcount++;
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

    public static void findLargestServer() {
        try {
            // Initialise String for the received Data string after GETS All 
            String getsAllString;
            // Array that stores the splits Values of the Data message
            String[] getsAllMessageSplit;
            // Int for the total servers
            Integer totalServer;

            // Sends GETS ALL
            dout.write((getsAll + "\n").getBytes());
            dout.flush();
            // Received DATA X YYY, this X is the number of Servers
            recInString = in.readLine();

            // Store the string data here
            getsAllString = recInString;
            //System.out.println("getsAll: " + getsAllString);
            
            // Split the Data line string [DATA], [X], [YYY]
            getsAllMessageSplit = getsAllString.split(" ");
            // totalServer = X parse string to int
            totalServer = Integer.parseInt(getsAllMessageSplit[1]);
            //System.out.println("total server amount: " + totalServer);

            // Sends OK
            dout.write((ok + "\n").getBytes());
            dout.flush();
            // Receive Server Details (ServerType X STATUS X Y XXXX XXXXX X X) | Y is number of Cores in server

            // initiate a string array that will hold the amount of X servers, & initate new string arrays that will hold the server size type & Core size
            String serverArray[] = new String[totalServer];
            String serverSizeType[] = new String[totalServer];
            int coreSizeArray[] = new int[totalServer];

            // Loop X times, each time a line of records
            for (int i = 0; i < totalServer; i++) {
                serverArray[i] = in.readLine();
                System.out.println("server lines: " + serverArray[i]);

                // Split the OK line string ( [ServerType] [X] [STATUS] [X] [Y] [XXXX] [XXXXX] [X] [X] )
                String okLineArray[] = serverArray[i].split(" ");

                // Populate serverSizeType with the ServerType value of the OK line string
                serverSizeType[i] = okLineArray[0];
                // Populate coreSizeArray with the Y value of the OK line string, parsed to Integer from string
                coreSizeArray[i] = Integer.parseInt(okLineArray[4]);

                // if the server size type in the loop is the same as the saved largest size type number of servers is increased by 1
                if (serverSizeType[i].equals(largestSizeType)) {
                    numOfServers++;
                }

                // if the core size of the server in the loop is greater than the saved largestNumOfCores
                if (coreSizeArray[i] > largestNumOfCores) {
                    largestSizeType = serverSizeType[i];               // largestSizeType is equal to the new type
                    largestNumOfCores = coreSizeArray[i];               // largest number of cores, is now equal to the number of cores
                    numOfServers = 1;               // number of server counter is reset to 1 (This is done in the case of a new bigger server found)
                }
            }

            System.out.println("largest Core Size: " + largestNumOfCores);
            System.out.println("largest Core Size Type: " + largestSizeType);
            System.out.println("Number of Largest Servers: " + numOfServers);

            // Send OK
            dout.write((ok + "\n").getBytes());
            dout.flush();
            recInString = in.readLine();

            // largest server was found so set foundLargeServer to true
            foundLargeServer=true;

        } catch (Exception e) {
            System.out.println(e);
        }
    }
}
