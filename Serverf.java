    import java.io.*;
    import java.io.BufferedReader;
    import java.io.IOException;
    import java.io.InputStreamReader;
    import java.io.PrintWriter;
    import java.net.*;
    import java.time.LocalDateTime;
    import java.util.concurrent.ConcurrentHashMap;

    public class Serverf {
        public static final String ANSI_white = "\u001B[37m";
        String ServerName;
        public void setServerName(String ServerName) { this.ServerName = ServerName; }
        public String getServerName() {return ServerName; }
        int num_user = 0;
        int num_chat = 0;
        int portNumber;
        ConcurrentHashMap <Thread, ClientInfo> clientMap = new ConcurrentHashMap<>();
        static ConcurrentHashMap <String, Integer> ServerList = new ConcurrentHashMap<>();

        Serverf(String ServerName, int portNumber){
            this.ServerName = ServerName; this.portNumber = portNumber;
        }

        public void start(){
            try {
                /*socket 함수 호출, bind 함수 호출 */   
                ServerSocket serverSocket = new ServerSocket(portNumber); // 서버 소켓 생성
                System.out.println("서버("+ServerName+")가 시작되었습니다. 포트 번호: " + portNumber); // 출력  

                while (true) { // 무한반복
                    /*listen 함수 호출 accept 함수 호출  */
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("클라이언트가 연결되었습니다.");
                    sendMessageToAll("클라이언트가 연결되었습니다.");
                    increaseUserCount();
                    ClientInfo clientInfo = new ClientInfo(clientSocket);

                    Thread clientThread = new Thread(new ClientHandler(clientSocket, clientInfo, this));
                    clientMap.put(clientThread, clientInfo);  // Thread가 KEY Socket이 VALUE
                    clientThread.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        } // start

        public synchronized void increaseUserCount() {
            num_user++;
            System.out.println(ServerName + ": 현재 참가자 수: " + num_user);
            sendMessageToAll(ServerName + ": 현재 참가자 수: " + num_user);
        }

        public synchronized void decrementUserCount() {
            num_user--;
            System.out.println(ServerName + ": 현재 참가자 수: " + num_user);
            sendMessageToAll(ServerName + ": 현재 참가자 수: " + num_user);
        }
        
        public void removeClient(Thread clientThread) {
            clientMap.remove(clientThread);
        } // removeClient

        public void sendMessageToAll(String message){
            for(Thread clientThread : clientMap.keySet()){
                ClientInfo clientinfo = clientMap.get(clientThread);
                if(!clientinfo.getSocket().isClosed()){
                    clientinfo.getOut().println(message);
                }
                else {
                    removeClient(clientThread);
                }

            }
        } // sendMessageToAll

        public void createNewServerInstance(String ServerName){
            int port = getNextAvailablePort(portNumber);
            Serverf newServer = new Serverf(ServerName, port);
            ServerList.put(ServerName, port);
            sendMessageToAll("서버("+ServerName+")가 시작되었습니다.");
            new Thread(newServer::start).start();
        }

        private static synchronized int getNextAvailablePort(int nextPort) {
            while (portIsInUse(nextPort)) {
                nextPort++;
                if (nextPort > 65535) {
                    throw new RuntimeException("채팅방 생성 불가능");
                }
            }
            return nextPort++;
        }

        private static boolean portIsInUse(int port) {
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                return false;
            } catch (IOException e) {
                return true;
            }
        }

        public void joinServer(String servername, ClientInfo clientinfo) {
            int portnum = ServerList.get(servername);
            clientinfo.getOut().println("#PORT " + portnum);
            clientinfo.exit();
        }

        public void printlist(ClientInfo clientinfo){   
            for(String servername : ServerList.keySet()){
                System.out.println(servername);
                clientinfo.getOut().println(servername);

            }
        }

        public static void main(String[] args) {
            if (args.length < 1) {
                System.out.println("사용법: java Serverf [포트 번호]");
                System.exit(1);
            }
            String name = "main";
            Serverf main = new Serverf(name, Integer.parseInt(args[0]));
            ServerList.put(name, Integer.parseInt(args[0]));
            main.start();
        } // main
    } // Serverf

    class ClientHandler implements Runnable {
        public static final String ANSI_RESET = "\u001B[0m";
        public static final String ANSI_yello = "\u001B[33m";
        private Serverf serverf;
        private String name = null;
        private Socket clientSocket;
        private ClientInfo clientinfo;
        private BufferedReader in;
        LocalDateTime now;

        public ClientHandler(Socket socket, ClientInfo clientinfo, Serverf serverf) {
            this.clientSocket = socket; this.clientinfo = clientinfo; this.serverf = serverf;
        } // 생성자


        @Override
        public void run() {
            try {
                
                in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // 받음

                String message;
                String[] command;
                clientinfo.getOut().println(serverf.getServerName()+"채팅방에 연결되었습니다.");
                name = in.readLine();
                clientinfo.setName(name);
                
                while ((message = in.readLine()) != null) {
                    if (message.charAt(0) == '/'){  
                        
                        command = message.split(" ", 3);
                        if(command[0].equals("/w")){
                            String w_name = command[1];
                            String w_message = command[2];
                            boolean check = false;

                            for (ClientInfo client : serverf.clientMap.values()) { // 대상 사용자 찾기
                                if (client.getName().equals(w_name)) {
                                client.getOut().println(name + "님의 귓속말: " + w_message + "\n"); // 메시지 전송
                                check = true;
                                break;
                                }
                            }
                            if(!check) {
                            clientinfo.getOut().println("상대방이 없습니다.");
                            }
                        }
                        
                    
                        else if (command[0].equals("/server")){
                            if(command[1].equals("create")){
                                serverf.createNewServerInstance(command[2]);
                            }
                            else if (command[1].equals("join")){
                                serverf.joinServer(command[2], clientinfo);
                                break;
                            }
                            else if (command[1].equals("list")){
                                serverf.printlist(clientinfo);
                            }
                        }
                    }
                    else {
                        System.out.println(ANSI_yello + serverf.getServerName() + ANSI_RESET + "-" + message);
                    serverf.sendMessageToAll(ANSI_yello + serverf.getServerName() + ANSI_RESET + "-" + message );
                    }
                }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    try {
                        if (in != null) in.close();
                        if (clientinfo.getOut() != null) clientinfo.getOut().close();
                        if (clientSocket != null) clientSocket.close();
                    } catch (IOException e) {
                    e.printStackTrace();
                    }
                    serverf.decrementUserCount(); // 참가자 수 감소 및 메시지 전송
                    serverf.removeClient(Thread.currentThread());
                }
        } // run
    } // ClientHandler

    class ClientInfo{
        private PrintWriter Out; 
        private String name;
        private Socket clientSocket;
        public Socket getSocket() {return clientSocket;}
        public void exit() {
            try {
                if (Out != null) Out.close();
                if (clientSocket != null) clientSocket.close();
            } catch(IOException e) {
            e.printStackTrace();
            }
        }

        public String getName() {return name;}
        public PrintWriter getOut() {return Out;}
        public void setName(String name) {this.name = name;}
        public ClientInfo(Socket socket) throws IOException {
            this.clientSocket = socket;
            Out = new PrintWriter(clientSocket.getOutputStream(), true);
        }   
    }