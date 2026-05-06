package edu.hitsz.server;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 联机对战服务器：基于 TCP Socket，将先后到达的客户端两两配对成一个房间，
 * 在房间内中转双方的实时分数 / 死亡 / 结束消息。
 *
 * 协议（行分隔的纯文本）：
 *   C → S : "SCORE <int>"        当前玩家最新得分
 *   C → S : "DEAD"               当前玩家阵亡
 *   S → C : "WAIT"               已连接，等待对手
 *   S → C : "START"              对手已就绪，对战开始
 *   S → C : "OPP_SCORE <int>"    对手得分变化
 *   S → C : "OPP_DEAD"           对手阵亡
 *   S → C : "END <my> <opp>"     双方均阵亡，对战结束
 *   S → C : "OPP_LEFT"           对手断连
 */
public class BattleServer {

    private static final int DEFAULT_PORT = 9999;

    private final ServerSocket serverSocket;
    private final ConcurrentLinkedQueue<ClientSession> waiting = new ConcurrentLinkedQueue<>();
    private final AtomicInteger roomCounter = new AtomicInteger();

    public BattleServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    public void start() {
        System.out.println("[BattleServer] listening on port " + serverSocket.getLocalPort());
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                System.out.println("[BattleServer] accepted: " + socket.getRemoteSocketAddress());
                ClientSession session = new ClientSession(socket);
                session.start();
                tryPair(session);
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private synchronized void tryPair(ClientSession session) {
        ClientSession partner = waiting.poll();
        if (partner == null || partner.isClosed()) {
            waiting.offer(session);
            session.send("WAIT");
        } else {
            Room room = new Room(roomCounter.incrementAndGet(), partner, session);
            partner.attachRoom(room);
            session.attachRoom(room);
            partner.send("START");
            session.send("START");
            System.out.println("[BattleServer] room#" + room.id + " paired");
        }
    }

    public static void main(String[] args) throws IOException {
        int port = DEFAULT_PORT;
        if (args.length > 0) {
            try { port = Integer.parseInt(args[0]); } catch (NumberFormatException ignored) {}
        }
        new BattleServer(port).start();
    }

    /* --------------------------- Room --------------------------- */
    static class Room {
        final int id;
        final ClientSession a;
        final ClientSession b;
        boolean aDead = false;
        boolean bDead = false;
        int aScore = 0;
        int bScore = 0;

        Room(int id, ClientSession a, ClientSession b) {
            this.id = id;
            this.a = a;
            this.b = b;
        }

        synchronized ClientSession opponentOf(ClientSession self) {
            return self == a ? b : a;
        }

        synchronized void onScore(ClientSession self, int score) {
            if (self == a) aScore = score; else bScore = score;
            ClientSession opp = opponentOf(self);
            opp.send("OPP_SCORE " + score);
        }

        synchronized void onDead(ClientSession self) {
            if (self == a) aDead = true; else bDead = true;
            ClientSession opp = opponentOf(self);
            opp.send("OPP_DEAD");
            if (aDead && bDead) {
                a.send("END " + aScore + " " + bScore);
                b.send("END " + bScore + " " + aScore);
            }
        }

        synchronized void onLeft(ClientSession self) {
            ClientSession opp = opponentOf(self);
            if (opp != null) opp.send("OPP_LEFT");
        }
    }

    /* --------------------------- Client session --------------------------- */
    class ClientSession extends Thread {
        private final Socket socket;
        private BufferedReader in;
        private PrintWriter out;
        private volatile Room room;
        private volatile boolean closed = false;

        ClientSession(Socket socket) {
            this.socket = socket;
            try {
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
                out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream(), "utf-8")), true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        void attachRoom(Room room) { this.room = room; }
        boolean isClosed() { return closed; }

        synchronized void send(String line) {
            if (out != null && !closed) {
                out.println(line);
            }
        }

        @Override
        public void run() {
            try {
                String line;
                while ((line = in.readLine()) != null) {
                    handle(line.trim());
                }
            } catch (IOException ex) {
                // client disconnected
            } finally {
                close();
            }
        }

        private void handle(String line) {
            if (line.isEmpty()) return;
            if (room == null) return;
            if (line.startsWith("SCORE ")) {
                try {
                    int s = Integer.parseInt(line.substring(6).trim());
                    room.onScore(this, s);
                } catch (NumberFormatException ignored) {}
            } else if (line.equals("DEAD")) {
                room.onDead(this);
            }
        }

        void close() {
            if (closed) return;
            closed = true;
            try { if (in != null) in.close(); } catch (IOException ignored) {}
            if (out != null) out.close();
            try { socket.close(); } catch (IOException ignored) {}
            waiting.remove(this);
            if (room != null) room.onLeft(this);
            System.out.println("[BattleServer] session closed: " + socket.getRemoteSocketAddress());
        }
    }
}
