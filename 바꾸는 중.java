import java.io.*; //DataOutputStream
import java.net.*; //Socket
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter; //time
import java.util.Scanner;

public class Client {

  private static Socket socket;
  private static Sender sender;
  private static Receiver receiver;
  private static String Ip;
  private static String name;
  private static boolean isjoin = false;
  public static void main(String args[]) {
    if(args.length!=3) {
      System.out.println("./Client IP Port name");
      System.exit(0);
    }

    Ip = args[0];
    int Port = Integer.parseInt(args[1]);
    name = args[2];

    try {
      socket = new Socket(Ip, Port);

      System.out.println("Server connect");
      sender = new Sender(socket, name); //send thread
      receiver = new Receiver(socket);

      new Thread(sender).start();
      new Thread(receiver).start();
      
    } catch(ConnectException e1) {
      e1.printStackTrace();
    } catch(Exception e2) {
      e2.printStackTrace();
    }

  } // main

  static class Sender extends Thread { //보내는 함수
    Time times = new Time(); //시간
    Socket socket;
    PrintWriter out;
    String name;
    String[] command;
    private volatile boolean shouldRun = true;

    Sender(Socket socket, String name) {
      this.socket = socket;
      this.name = name;
      try {
        out = new PrintWriter(socket.getOutputStream(), true);
      } catch(Exception e) {}
    } //생성자

    public void run() {
      Scanner sc = new Scanner(System.in);
      String message = "";
      String check;
      try {
         
        if(out!=null) {
          out.println(name);
          System.out.println(name + "이름으로 연결되었습니다. \n/help를 입력하시면 명령어를 알 수 있습니다.");
        }
        if(isjoin) {
          isjoin = false;
          out.println("join");
        }
        else
          out.println("njoin");

        while(shouldRun && !Thread.currentThread().isInterrupted()) {
          if(sc == null) {
            sc.next();
            continue;
          }
          message = sc.nextLine();
          if ("/server list".equalsIgnoreCase(message)) {
            out.println(message); // 서버에 명령어 전송
            continue; // 다음 입력을 기다림
          }
          else if("exit".equals(message)) {
            sc.close();
            socket.close();
            out.close();
            System.out.println("exit chating");
            System.exit(0);
          }
          else if(message.startsWith("/emoticon")) {
            check = message;
            int spaceCount = 0;
            for (int i = 0; i < check.length(); i++) {
                if (check.charAt(i) == ' ') {
                    spaceCount++;
                }
            }
            if (spaceCount == 1) {
              command = message.split(" ", 2);
              if(command[1].equals("list")) {
                System.out.println("/emoticon sunglass");
                System.out.println("/emoticon muyaho");
                System.out.println("/emoticon fail");
                System.out.println("/emoticon smile\n");
              }
              else if(command[1].equals("sunglass")) {
                message = "▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄\r\n" + //
                    "█░░░░░░░░▀█▄▀▄▀██████░▀█▄▀▄▀██████░\r\n" + //
                    "░░░░░░░░░░░▀█▄█▄███▀░░░ ▀██▄█▄███▀░";
                out.println(message);
              }
              else if(command[1].equals("muyaho")) 
                out.println(".⠀⢰⠒⠒⠒⢲⠆⠀⠀⢀⠤⢤⡀⠀⡴⠀⠀⢀⣀⣀⣰⣀⣀⡀⠀⠀⠀⠀⠀\r\n" + //
                    "⠀⠀⣎⣀⣀⣀⡜⠀⠀⢰⠃⠀⢠⠇⢰⠓⠂⠀⢀⠔⠒⠒⠲⡄⠀⠀⠀⠀⠀⠀\r\n" + //
                    "⠤⠤⠤⣤⠤⠤⠤⠄⠀⣇⠀⢀⠞⢀⡯⠤⠀⠀⠹⠤⡤⠤⠞⠁⠀⢠⠔⢤⣀⠆\r\n" + //
                    " ⠀⠀⢀⡏⠀⠀⠀⠀⠀⠈⠉⠁⠀⡸⠀⠀⠠⠤⠤⠴⠧⠤⠤⠄⠀⠀⠀⠀⠀");
              else if(command[1].equals("fail")) 
                out.println("▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄▄\r\n" + //
                    "████▌▄▌▄▐▐▌█████\r\n" + //
                    "████▌▄▌▄▐▐▌▀████\r\n" + //
                    "▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀▀");
              else if(command[1].equals("smile"))
                out.println("▒▒▒▒▒▒▒▒▒▒▒▒\r\n" + //
                    "▒▒▒▒▓▒▒▓▒▒▒▒\r\n" + //
                    "▒▒▒▒▓▒▒▓▒▒▒▒\r\n" + //
                    "▒▒▒▒▒▒▒▒▒▒▒▒\r\n" + //
                    "▒▓▒▒▒▒▒▒▒▒▓▒\r\n" + //
                    "▒▒▓▓▓▓▓▓▓▓▒▒\r\n" + //
                    "▒▒▒▒▒▒▒▒▒▒▒▒");
              else
                System.out.println("잘못된 명령어 입니다.");
            }
            else
              System.out.println("잘못된 명령어 입니다.");
          }
          else if(message.startsWith("/announcement")) {
            check = message;
            int spaceCount = 0;
            for (int i = 0; i < check.length(); i++) {
                if (check.charAt(i) == ' ') {
                    spaceCount++;
                }
            }
            if(spaceCount >= 1 && !(message.charAt(message.length()-1) == ' '))
              out.println(message);
            else
              System.out.println("잘못된 명령어 입니다.");
          }
          else if(message.startsWith("/kick")) {
            check = message;
            int spaceCount = 0;
            for (int i = 0; i < check.length(); i++) {
                if (check.charAt(i) == ' ') {
                    spaceCount++;
                }
            }
            if(spaceCount == 1)
              out.println(message);
          }
          else if("/help".equalsIgnoreCase(message)) { //fix
            System.out.println("exit : 채팅방 나가기");
            System.out.println("/w name message : 귓속말");
            System.out.println("/server create name : 채팅방 만들기");
            System.out.println("/server join name : 채팅방 들어가기");
            System.out.println("/sever list : 채팅방 리스트보기");
            System.out.println("/server delete : 채팅방 삭제하기");
            System.out.println("/user all list : 전체 유저 보기");
            System.out.println("/user list : 참여해 있는 방 유저 보기");
            System.out.println("/emoticon list: 이모티콘 종류 보기");
            System.out.println("/emoticon emoticonname : 이모티콘 보내기");
            System.out.println("/announcement color(red, blue, green) 내용 : 확성기");
            System.out.println("/kick name : 강퇴시키기(HOST만 가능)\n");
          }
          else if("/user list".equals(message)) {
            out.println(message);
          }
          else if("/user all list".equals(message)) {
            out.println(message);
          }
          else if(message.startsWith("/w")) {
            check = message;
            int spaceCount = 0;
            for (int i = 0; i < check.length(); i++) {
                if (check.charAt(i) == ' ') {
                    spaceCount++;
                }
            }
            if (spaceCount >= 2) {
              out.println(message);
            } else {
              System.out.println("잘못된 명령어 입니다.\n /w name message");
            }
          }
          else if(message.startsWith("/server")) {
            check = message;
            int spaceCount = 0;
            for (int i = 0; i < check.length(); i++) {
                if (check.charAt(i) == ' ') {
                    spaceCount++;
                }
            }
            if (spaceCount == 2 && !(message.charAt(message.length()-1) == ' ')) {
              out.println(message);
            } else if(spaceCount ==1 ) {
              out.println(message);
            } else {
              System.out.println("잘못된 명령어 입니다.\n /server (create or join) name");
            }
          }
          else {
            out.println(times.Gettime() + "[" + name + "] : " + message); //plus time
          }
        }
      } catch(IOException e) {
        e.printStackTrace();
      } 
    } //run

    public void close() {
      try {
          shouldRun = false;
          if (out != null) {
            out.close();
          }
          if (socket != null) {
            socket.close();
          }
      } catch (IOException e) {
          e.printStackTrace();
      }
    } //close

  } //Sender class

  static class Receiver extends Thread{ //받는 함수
    int Portnum;
    Socket socket;
    BufferedReader in;
    String[] command;
    String check;
    private volatile boolean shouldRun = true;

    Receiver(Socket socket) {
      this.socket = socket;
      try{
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
      } catch (IOException e) {
        e.printStackTrace();
      }
    } //생성자

    public void run() {
      try {
        while(shouldRun && !Thread.currentThread().isInterrupted()) {
          if(socket.isClosed()) {
            break;
          }
          String message = in.readLine();
          if("exit".equals(message)) {
            System.exit(0);
          }
          if(message.startsWith("#PORT")) {
            command = message.split(" ", 2);
            JoinServer(command[1]);
          }
          else
            System.out.println(message);
        } 
      } catch (IOException e) {
        e.printStackTrace();
      }
    } //run

    public void close() {
      try {
          shouldRun = false;
          if (in != null) {
              in.close();
          }
          if (socket != null) {
              socket.close();
          }
      } catch (IOException e) {
          e.printStackTrace();
      }
    }

  } //Receive

  static public class Time { //시간 함수
    public String Gettime() {
      LocalDateTime now = LocalDateTime.now();
      DateTimeFormatter formats = DateTimeFormatter.ofPattern("HH:mm:ss");
      return now.format(formats);
    }
  }//time

  public static void JoinServer(String newPort) {
    try {
        isjoin = true;
        closeall();

        socket = new Socket(Ip, Integer.parseInt(newPort));
        sender = new Sender(socket, name);   // 새 스레드 생성 및 시작
        receiver = new Receiver(socket);
        
        new Thread(sender).start();
        new Thread(receiver).start();

    } catch (IOException e) {
        e.printStackTrace();
    }
  }//joinserver

  private static void closeall() {
    try {
      if (sender != null) {
          sender.interrupt();
          sender.join();      
          sender.close();
          sender = null;
      }
      if (receiver != null) {
          receiver.interrupt();
          receiver.join();
          receiver.close();
          receiver = null;
      }
      if (socket != null && !socket.isClosed()) {
          socket.close();
      }
    } catch (InterruptedException | IOException e) {
      e.printStackTrace();
    }
  } //close all

} //main class