import java.net.Socket;
import java.net.InetAddress;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.util.concurrent.TimeUnit;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class dsClient {
   
    DataOutputStream dout;
    BufferedReader bin;

    public static void main(String[] args) {
            try {
                //InetAddress aHost = InetAddress.getByName(args[0]);
                //int aPort = Integer.parseInt(args[1]);
                //Socket s = new Socket(aHost, aPort);
               
                Socket s = new Socket("127.0.0.1", 50000);

                DataOutputStream dout = new DataOutputStream(s.getOutputStream());
               
                //DataInputStream din = new DataInputStream(s.getInputStream());
BufferedReader bin = new BufferedReader(new InputStreamReader(s.getInputStream()));
String str;
String jobn;
String getsAllStr;

                System.out.println("Target IP: " + s.getInetAddress() + " Target Port: " + s.getPort());
                System.out.println("Local IP: " + s.getLocalAddress() + " local Port: " + s.getLocalPort());

                dout.write(("HELO\n").getBytes());
                dout.flush();
                System.out.println("SENT: HELO");

                str = bin.readLine();
                System.out.println("RCVD: " + str);
               
                dout.write(("AUTH nagle\n").getBytes());
                dout.flush();
                System.out.println("SENT: AUTH");
               
                str=bin.readLine();
                System.out.println("RCVD: " + str);
               
                dout.write(("REDY\n").getBytes());
                dout.flush();
                System.out.println("SENT: REDY");
               
                str=bin.readLine();
                System.out.println("RCVD: " + str);

// rec: JOBN XX YY we need to store the job ID form here
jobn=str;
System.out.println("------");
System.out.println(jobn);
System.out.println("------");
// send: GETS ALL
dout.write(("GETS ALL\n").getBytes());
dout.flush();
System.out.println("SENT: GETS ALL");

str=bin.readLine();
getsAllStr=str;

System.out.println(getsAllStr);
// rec: DATA X YYY this X is the following line number

// send: OK
// loop X times, each time a line of records
// send: OK
//rec:  .  (the received thing here is a DOT)
// find a way to locate the largest server type and server ID
// do the SCHD
                               
                dout.write(("QUIT\n").getBytes());
                dout.flush();
                System.out.println("SENT: QUIT");
               
                str=bin.readLine();
                System.out.println("RCVD: " + str);
               
                bin.close();
                dout.close();
                s.close();
            }
            catch (Exception e) {System.out.println(e);}
        }

}


