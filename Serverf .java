        import java.io.*;
        import java.io.BufferedReader;
        import java.io.IOException;
        import java.io.InputStreamReader;
        import java.io.PrintWriter;
        import java.net.*;
        import java.time.LocalDateTime;
    import java.util.Collections;
    import java.util.HashSet;
    import java.util.Scanner;
    import java.util.Set;
    import java.util.concurrent.ConcurrentHashMap;

        public class Serverf {
            public static final String ANSI_RESET = "\u001B[0m";
            public static final String ANSI_RED = "\u001B[31m";
            public static final String ANSI_GREEN = "\u001B[32m";
            public static final String ANSI_BLUE = "\u001B[34m";

            private ServerSocket serverSocket;
            String ServerName;
            public void setServerName(String ServerName) { this.ServerName = ServerName; }
            public String getServerName() {return ServerName; }
            int num_user = 0;
            static int allnum_user = 0;
            int portNumber;
            String Hostname;
            static public void stopallServer(boolean value){
                exitsendToAll(); closeAllServerSockets(); System.out.println("프로그램을 종료 합니다.");
            }
            static ConcurrentHashMap <Thread, ClientInfo> clientMap = new ConcurrentHashMap<>();
            static ConcurrentHashMap <String, Integer> ServerList = new ConcurrentHashMap<>();
            ConcurrentHashMap <Thread, ClientInfo> Serverinclient = new ConcurrentHashMap<>();
            private static Set<Serverf> allServerInstances = Collections.synchronizedSet(new HashSet<>());
            Serverf(String ServerName, int portNumber, String Hostname){
                this.ServerName = ServerName; this.portNumber = portNumber; this.Hostname = Hostname;
                allServerInstances.add(this); // 서버 인스턴스를 컬렉션에 추가
            }

            public void start(){
                try {
                    /*socket 함수 호출, bind 함수 호출 */   
                    serverSocket = new ServerSocket(portNumber); // 서버 소켓 생성
                    System.out.println("서버("+ServerName+")가 시작되었습니다. 포트 번호: " + portNumber); // 출력  

                    while (true) {
                        /*listen 함수 호출 accept 함수 호출  */
                        try{
                        Socket clientSocket = serverSocket.accept();
                        System.out.println("클라이언트가 연결되었습니다.");
                        increaseUserCount();
                        ClientInfo clientInfo = new ClientInfo(clientSocket);

                        Thread clientThread = new Thread(new ClientHandler(clientSocket, clientInfo, this));
                        clientThread.start();
                        } catch (SocketException e) {
                            System.out.println("서버 소켓이 닫혔습니다.");
                            break;
                        }
                    }
                } catch (IOException e) {
                    
                } finally {
                    if (serverSocket != null && !serverSocket.isClosed()) {
                        try {
                            serverSocket.close(); // 서버 소켓을 닫습니다.
                            System.out.println("서버 소켓이 닫혔습니다.");
                        } catch (IOException e) {
                            
                        }
                    }
                }
            } // start

            public void closeServerSocket() {
                try {
                    if (serverSocket != null && !serverSocket.isClosed()) {
                        System.out.println(ServerName);
                        serverSocket.close();
                    }
                } catch (IOException e) {
                    
                } finally {
                    /*allServerInstances.remove(this); // 서버 인스턴스를 컬렉션에서 제거*/
                }
            }

            public static void closeAllServerSockets() {
                for (Serverf server : allServerInstances) { // allServerInstances는 모든 서버 인스턴스를 저장하는 컬렉션
                    server.closeServerSocket();
                }
            }

            public synchronized void increaseUserCount() {
                num_user++;
                allnum_user++;
            }

            public synchronized void decrementUserCount() {
                num_user--;
                allnum_user--;
            }
            
            public void removeClient(Thread clientThread) {
                clientMap.remove(clientThread);
                Serverinclient.remove(clientThread);
            } // removeClient

            public static void exitsendToAll(){        
                for(Thread clientThread : clientMap.keySet()){
                    ClientInfo clientinfo = clientMap.get(clientThread);
                    if(!clientinfo.getSocket().isClosed()){
                        clientinfo.getOut().println("exit");
                    }
                }
            }

            public void sendMessageToAll(String message){
                System.out.println(message);
                
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

            public void sendMessageToThisServer(String message){
                System.out.println(message);
                for(Thread clientThread : Serverinclient.keySet()){
                    ClientInfo clientinfo = Serverinclient.get(clientThread);
                    if(!clientinfo.getSocket().isClosed()){
                        clientinfo.getOut().println(message);
                    }
                    else {
                        removeClient(clientThread);
                    }

                }
            } // sendMessageToThisServer

            public void deleteServer(ClientInfo clientinfo){
                if(!Hostname.equals(clientinfo.getName())){
                    clientinfo.getOut().println(ANSI_RED+"호스트만 서버를 삭제할 수 있습니다."+ANSI_RESET+" HOST: "+Hostname);
                    return;
                }
                if (num_user == 1) {
                    sendMessageToAll(ANSI_RED+ "서버가 삭제되었습니다."+ANSI_RESET);
                    ServerList.remove(this.getServerName());
                    clientinfo.getOut().println("#PORT "+ ServerList.get("main"));
                    closeServerSocket();
                } else {
                    clientinfo.getOut().println(ANSI_RED+"서버에 호스트만 존재해야 합니다."+ANSI_RESET+" HOST: "+Hostname);
                }
            }

            public synchronized void createNewServerInstance(String ServerName, ClientInfo clientinfo){
                for (String servername : ServerList.keySet()){
                    if(servername.equals(ServerName)){
                        clientinfo.getOut().println(ANSI_RED + ServerName + "채팅방이 이미 존재합니다." + ANSI_RESET);
                        return;
                    }
                }
                int port = getNextAvailablePort(portNumber);
                Serverf newServer = new Serverf(ServerName, port, clientinfo.getName());
                ServerList.put(ServerName, port);
                sendMessageToAll("서버("+ServerName+")가 시작되었습니다. HOST: "+ clientinfo.getName());
                new Thread(newServer::start).start();
            }

            private static int getNextAvailablePort(int nextPort) {
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

            public boolean joinServer(String ServerName, ClientInfo clientinfo) {
                for (String servername : ServerList.keySet()){
                    if(servername.equals(ServerName)){
                        int portnum = ServerList.get(ServerName);
                        
                        clientinfo.getOut().println("#PORT " + portnum);
                        return true;
                    }
                }
                clientinfo.getOut().println(ANSI_RED + ServerName + "존재하지 않는 채팅방입니다." + ANSI_RESET);
                return false;
            }

            public void printlist(ClientInfo clientinfo){   
                for(String servername : ServerList.keySet()){
                    System.out.println(servername);
                    clientinfo.getOut().println(servername);

                }
            }

            public void userList(ClientInfo clientinfo){
                for(Thread clientThread : Serverinclient.keySet()){
                    ClientInfo info= Serverinclient.get(clientThread);
                    clientinfo.getOut().println(info.getName());
                }
                clientinfo.getOut().println(ServerName + " 채팅방 참가자 수: " + num_user);
            }

            public void userallList(ClientInfo clientinfo){
                for(Thread clientThread : clientMap.keySet()){
                    ClientInfo info= clientMap.get(clientThread);
                    clientinfo.getOut().println(info.getName());
                }
                clientinfo.getOut().println("모든 채팅방 참가자 수: " + allnum_user);
            }

            public void announcement(String color, String message, ClientInfo clientinfo){
                if(Hostname.equals(clientinfo.getName())){
                    if (color.equals("red"))
                        sendMessageToThisServer(ANSI_RED + "공지사항 : " +message+ANSI_RESET);
                    else if (color.equals("blue"))
                        sendMessageToThisServer(ANSI_BLUE + "공지사항 : " +message+ANSI_RESET);    
                    else if (color.equals("green"))
                        sendMessageToThisServer(ANSI_GREEN + "공지사항 : " +message+ANSI_RESET);
                    else 
                        sendMessageToThisServer("공지사항 : " +message);
                }
                else
                    clientinfo.getOut().println("HOST가 아닙니다. HOST: "+Hostname);
            }

            public void kick(String clientname, ClientInfo clientinfo){
                boolean check = false;
                if (Hostname.equals(clientinfo.getName())){
                    for (Thread clientThread : Serverinclient.keySet()) { // 대상 사용자 찾기
                        ClientInfo client= Serverinclient.get(clientThread);
                            if (client.getName().equals(clientname)) {
                                sendMessageToThisServer(ANSI_RED + clientinfo.getName()+"님이 "+client.getName()+"님을 강제퇴장 시켰습니다."+ANSI_RESET);
                                client.getOut().println("#PORT "+ ServerList.get("main"));
                                check = true;
                                    break;
                                }
                    }
                    if(!check) {
                        clientinfo.getOut().println(ANSI_RED + "상대방이 없습니다." + ANSI_RESET);
                        return;
                    }
                }
                else
                    clientinfo.getOut().println("HOST가 아닙니다. HOST: "+Hostname);
            }

            public static void main(String[] args) {
                if (args.length < 1) {
                    System.out.println("사용법: java Serverf [포트 번호]");
                    System.exit(1);
                }
                String name = "main";
                int mainport = Integer.parseInt(args[0]);
                Serverf main = new Serverf(name, mainport, "HOST"); // HOST가 MAIN의 방장
                ServerList.put(name, mainport);
                new Thread(main::start).start();
                try (Scanner scanner = new Scanner(System.in)) {
                    while (true) {
                        String command = scanner.nextLine();
                        if ("exit".equalsIgnoreCase(command)) {
                            Serverf.stopallServer(true);
                            for(int i = 3; i > 0; i--){
                                System.out.println(i+"초 뒤 프로그램이 종료됩니다.");
                                Thread.sleep(1000);
                            }
                            System.exit(0);
                        }
                    }
                }catch (Exception e) {
                    
                }
            } // main
        } // Serverf

        class ClientHandler implements Runnable {
            public static final String ANSI_RESET = "\u001B[0m";
            public static final String ANSI_yello = "\u001B[33m";
            public static final String ANSI_RED = "\u001B[31m";
            public static final String ANSI_BLUE = "\u001B[34m";
            private Serverf serverf;
            private String name = null;
            private Socket clientSocket;
            private ClientInfo clientinfo;
            private BufferedReader in;
            String check;
            boolean equalsname = false;
            public String Flag;
            public ClientHandler(Socket socket, ClientInfo clientinfo, Serverf serverf) {
                this.clientSocket = socket; this.clientinfo = clientinfo; this.serverf = serverf;
            } // 생성자


            @Override
            public void run() {
                try {
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream())); // 받음
                    String message;
                    String[] command;
                    clientinfo.getOut().println(ANSI_RED + serverf.getServerName()+"채팅방에 연결되었습니다." + ANSI_RESET);
                    name = in.readLine();
                    clientinfo.setName(name);
                    for (Thread thread : Serverf.clientMap.keySet()){  // 이름 중복비교
                        ClientInfo exit = Serverf.clientMap.get(thread);
                            if (name.equals(exit.getName())){
                                clientinfo.getOut().println(ANSI_RED + "중복 이름입니다. 다시 접속해 주세요" + ANSI_RESET);
                                equalsname = true;
                                clientinfo.getOut().println("exit");
                                throw new Exception("중복 이름 발견"); // 예외를 발생시킵니다.
                            }
                        }
                    
                    
                    serverf.sendMessageToThisServer(clientinfo.getName()+"님이 접속했습니다.");
                    
                    serverf.Serverinclient.put(Thread.currentThread(), this.clientinfo);
                    Serverf.clientMap.put(Thread.currentThread(), this.clientinfo);  // Thread가 KEY Socket이 VALUE
                    Flag = in.readLine();
                    if(Flag.equals("join")) {clientinfo.getOut().println(ANSI_RED+"Enter를 입력하시면 채팅을 시작합니다."+ANSI_RESET);}
                    while ((message = in.readLine()) != null) {
                        if (message.charAt(0) == '/'){  
                            check = message;
                            int spaceCount = 0;
                            for (int i = 0; i < check.length(); i++) {
                                if (check.charAt(i) == ' ') {
                                    spaceCount++;
                                }
                            }
                            if(spaceCount >= 2)
                                command = message.split(" ", 3);
                            else if(spaceCount ==  1)
                                command = message.split(" ", 2);
                            else
                                continue;
                        
                            if(command[0].equals("/w")){
                                String w_name = command[1];
                                String w_message = command[2];
                                boolean check = false;

                                for (ClientInfo client : serverf.clientMap.values()) { // 대상 사용자 찾기
                                    if (client.getName().equals(w_name)) {
                                    System.out.println(ANSI_BLUE + name + "님의 귓속말: " + w_message + "\n" + ANSI_RESET);
                                    client.getOut().println(ANSI_BLUE + name + "님의 귓속말: " + w_message + "\n" + ANSI_RESET); // 메시지 전송
                                    clientinfo.getOut().println(ANSI_BLUE + w_name + "님에게 전달: " + w_message + "\n" + ANSI_RESET);
                                    check = true;
                                    break;
                                    }
                                }
                                if(!check) {
                                    clientinfo.getOut().println(ANSI_BLUE+ "상대방이 없습니다." + ANSI_RESET);
                                }
                            }
                                
                            else if (command[0].equals("/user")){
                                if(command[1].equals("list")){
                                    serverf.userList(clientinfo);
                                }
                                else if(command[1].equals("all")){
                                    if(command[2].equals("list")){
                                        serverf.userallList(clientinfo);
                                    }
                                    else clientinfo.getOut().println("잘못된 명령어 입니다.");
                                }
                                else clientinfo.getOut().println("잘못된 명령어 입니다.");
                            }

                            else if (command[0].equals("/announcement")){
                                if (spaceCount >= 2) {
                                   if ((command[1].equals("red")) || (command[1].equals("blue")) ||(command[1].equals("green"))){
                                        serverf.announcement(command[1], command[2], clientinfo);
                                   }
                                    else clientinfo.getOut().println("잘못된 명령어 입니다.");
                                }
                                else
                                    clientinfo.getOut().println("잘못된 명령어 입니다.");
                            }
                                
                            
                            
                            else if (command[0].equals("/kick")){
                                serverf.kick(command[1], clientinfo);
                            }

                            else if (command[0].equals("/server")){
                                if(spaceCount >= 2){
                                    if(command[1].equals("create")){
                                        serverf.createNewServerInstance(command[2], clientinfo);
                                    }
                                    else if (command[1].equals("join")){
                                            if (serverf.joinServer(command[2], clientinfo))
                                            break;
                                    }
                                    else clientinfo.getOut().println("잘못된 명령어 입니다.");
                                }   
                                else if(spaceCount == 1 ){
                                    if (command[1].equals("list")){
                                        serverf.printlist(clientinfo);
                                    }
                                    else if(command[1].equals("delete")){
                                        serverf.deleteServer(clientinfo);
                                    }
                                    else clientinfo.getOut().println("잘못된 명령어 입니다.");
                                }
                                else clientinfo.getOut().println("잘못된 명령어 입니다.");
                            }
                        }
                        else {
                        serverf.sendMessageToThisServer(ANSI_yello + serverf.getServerName() + ANSI_RESET + "-" + message );
                        }
                    }
                    } catch (IOException e) {
                        
                    } catch (Exception e) {
                        System.out.println("중복 이름 예외 발생: " + e.getMessage());
                    } finally {
                        if(!equalsname){
                        serverf.sendMessageToThisServer(ANSI_yello + clientinfo.getName() + "님의 접속이 종료되었습니다." + ANSI_RESET);
                        }
                        try {
                            if (in != null) in.close();
                            if (clientinfo.getOut() != null) clientinfo.getOut().close();
                            if (clientSocket != null) clientSocket.close();
                        } catch (IOException e) {
                        
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
            public String getName() {return name;}
            public PrintWriter getOut() {return Out;}
            public void setName(String name) {this.name = name;}
            public ClientInfo(Socket socket) throws IOException {
                this.clientSocket = socket;
                Out = new PrintWriter(clientSocket.getOutputStream(), true);
            }
        }