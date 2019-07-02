package xmu.mocom.test;

import java.io.*;
import java.util.LinkedList;
import java.util.List;



public class ReadRequest {

    public static List<Request> readRequest(String requestFilePath) throws IOException {
        List<Request> requestList = new LinkedList<>();
        File requestFile = new File(requestFilePath);
        InputStreamReader reader = new InputStreamReader(new FileInputStream(requestFile));
        BufferedReader bufferReader = new BufferedReader(reader);
        String lineTxt = "";
        while((lineTxt = bufferReader.readLine()) != null){
            String s[] = lineTxt.split("#");
            long startId = Long.parseLong(s[0]);
            long targetId = Long.parseLong(s[1]);
            Request r = new Request(startId, targetId);
            requestList.add(r);
        }
        return requestList;
    }
}
