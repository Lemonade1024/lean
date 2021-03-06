package com;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

class SocketHandler implements Runnable {

    final static String CRLF = "\r\n";   // 1

    private Socket clientSocket;

    public SocketHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void handleSocket(Socket clientSocket) throws IOException {

        BufferedReader in = new BufferedReader(
                new InputStreamReader(clientSocket.getInputStream())
        );
        PrintWriter out = new PrintWriter(
                new BufferedWriter( new OutputStreamWriter(clientSocket.getOutputStream())),
                true
        );

        String requestHeader = "";
        String s;
        while ((s = in.readLine()) != null) {
            s += CRLF;  // 2 很重要，默认情况下in.readLine的结果中`\r\n`被去掉了
            requestHeader = requestHeader + s;
            if (s.equals(CRLF)){ // 3 此处HTTP请求头我们都得到了；如果从请求头中判断有请求正文，则还需要继续获取数据
                break;
            }
        }
        System.out.println("客户端请求头：");
        System.out.println(requestHeader);

        String responseBody = getHtml();

        String responseHeader = "HTTP/1.0 200 OK\r\n" +
                "Content-Type: text/plain; charset=UTF-8\r\n" +
                "Content-Length: "+responseBody.getBytes().length+"\r\n" +
                "\r\n"+getHtml();
        // 4 问题来了：1、浏览器如何探测编码 2、浏览器受到content-length后会按照什么方式判断？汉字的个数？字节数？

        System.out.println("响应头：");
        System.out.println(responseHeader);

        out.write(responseHeader);
        out.write(responseBody);
        out.flush();

        out.close();
        in.close();
        clientSocket.close();

    }

    @Override
    public void run() {
        try {
            handleSocket(clientSocket);
        } catch(Exception ex) {
            ex.printStackTrace();
        }
    }
    public String getHtml(){
        String s="";
        File file=new File("E:\\workspace\\lean\\src\\main\\resources\\t.html");
        try (final BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(file))){
            byte byteData;

            while ((byteData=(byte) bufferedInputStream.read())!=-1){
                s+=(char)byteData;
                System.out.print((char) byteData);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        return s;
    }

}

public class MyHTTPServer {

    public static void main(String[] args) throws Exception {

        int port = 8000;
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.println("启动服务，绑定端口： " + port);

        ExecutorService fixedThreadPool = Executors.newFixedThreadPool(30);  // 5

        while (true) {  // 6
            Socket clientSocket = serverSocket.accept();
            System.out.println("新的连接"
                    + clientSocket.getInetAddress() + ":" + clientSocket.getPort());
            try {
                fixedThreadPool.execute(new SocketHandler(clientSocket));
            } catch (Exception e) {
                System.out.println(e);
            }
        }
    }

}