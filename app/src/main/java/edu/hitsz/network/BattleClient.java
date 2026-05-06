package edu.hitsz.network;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 联机对战客户端。负责：
 *   1. 与服务器建立 TCP 长连接；
 *   2. 启动后台线程持续读取对手的状态消息；
 *   3. 把分数 / 阵亡事件以行文本形式发送给服务器；
 *   4. 通过 Listener 在主线程回调上层 Activity / Game。
 */
public class BattleClient {

    public interface Listener {
        void onWaiting();
        void onStart();
        void onOpponentScore(int score);
        void onOpponentDead();
        void onBattleEnded(int myFinalScore, int opponentFinalScore);
        void onOpponentLeft();
        void onError(String message);
    }

    private static final String TAG = "BattleClient";

    private final String host;
    private final int port;
    private final Listener listener;
    private final Handler ui = new Handler(Looper.getMainLooper());

    private Socket socket;
    private BufferedReader in;
    private PrintWriter out;
    private Thread receiver;
    private final ExecutorService sendExecutor = Executors.newSingleThreadExecutor();
    private volatile boolean running = false;

    public BattleClient(String host, int port, Listener listener) {
        this.host = host;
        this.port = port;
        this.listener = listener;
    }

    public void connect() {
        new Thread(() -> {
            try {
                socket = new Socket();
                socket.connect(new InetSocketAddress(host, port), 5000);
                in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "utf-8"));
                out = new PrintWriter(new BufferedWriter(
                        new OutputStreamWriter(socket.getOutputStream(), "utf-8")), true);
                running = true;
                receiver = new Thread(this::receiveLoop, "BattleClient-Receiver");
                receiver.start();
            } catch (IOException ex) {
                Log.e(TAG, "connect failed", ex);
                postError("无法连接到服务器：" + ex.getMessage());
            }
        }, "BattleClient-Connect").start();
    }

    private void receiveLoop() {
        try {
            String line;
            while (running && (line = in.readLine()) != null) {
                handleLine(line.trim());
            }
        } catch (IOException ex) {
            if (running) Log.w(TAG, "receive loop terminated: " + ex.getMessage());
        } finally {
            running = false;
        }
    }

    private void handleLine(String line) {
        if (line.isEmpty()) return;
        if (line.equals("WAIT")) {
            ui.post(listener::onWaiting);
        } else if (line.equals("START")) {
            ui.post(listener::onStart);
        } else if (line.startsWith("OPP_SCORE ")) {
            try {
                int score = Integer.parseInt(line.substring(10).trim());
                ui.post(() -> listener.onOpponentScore(score));
            } catch (NumberFormatException ignored) {}
        } else if (line.equals("OPP_DEAD")) {
            ui.post(listener::onOpponentDead);
        } else if (line.startsWith("END ")) {
            String[] parts = line.split(" ");
            if (parts.length >= 3) {
                try {
                    int my = Integer.parseInt(parts[1]);
                    int opp = Integer.parseInt(parts[2]);
                    ui.post(() -> listener.onBattleEnded(my, opp));
                } catch (NumberFormatException ignored) {}
            }
        } else if (line.equals("OPP_LEFT")) {
            ui.post(listener::onOpponentLeft);
        }
    }

    public void sendScore(int score) {
        send("SCORE " + score);
    }

    public void sendDead() {
        send("DEAD");
    }

    private void send(String message) {
        if (sendExecutor.isShutdown()) return;
        sendExecutor.submit(() -> {
            if (out != null) out.println(message);
        });
    }

    public void disconnect() {
        running = false;
        sendExecutor.shutdownNow();
        try { if (in != null) in.close(); } catch (IOException ignored) {}
        if (out != null) out.close();
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}
    }

    private void postError(String message) {
        ui.post(() -> listener.onError(message));
    }
}
