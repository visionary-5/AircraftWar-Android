# AircraftWar 项目详细说明

## 1. 项目概述

AircraftWar 是一个 Android 飞机大战项目。当前工程重点覆盖实验 5 和实验 6 的内容：实验 5 完成 SQLite 排行榜持久化，实验 6 完成基于 TCP Socket 的双人联机对战。在此基础上，项目保留了单机飞机大战的核心玩法，包括敌机生成、子弹发射、碰撞检测、道具效果、Boss、音效和难度变化。

项目分为两个 Gradle 模块：

- `app`：Android 客户端，包含游戏界面、单机模式、排行榜、联机客户端。
- `server`：Java 命令行服务端，负责联机对战配对和消息转发。

## 2. Android 客户端结构

### 2.1 主入口 MainActivity

`MainActivity` 是应用启动后的主菜单页面，主要职责是选择模式并进入对应界面：

- 读取手机或模拟器屏幕尺寸，保存到 `Main.screenWidth` 和 `Main.screenHeight`。
- 根据按钮创建单机游戏对象：`EasyGame`、`NormalGame`、`HardGame`。
- 打开排行榜页面 `LeaderboardActivity`，并通过 `EXTRA_DIFFICULTY` 指定排行榜难度。
- 打开联机配置弹窗，输入服务器 IP 和端口后启动 `BattleActivity`。

单机开始前会调用 `HeroAircraft.resetInstance()`，原因是英雄机使用单例模式。如果不重置，上一局的生命值、位置或状态可能影响下一局。

### 2.2 游戏主循环 AbstractGame

`AbstractGame` 继承 `SurfaceView`，实现 `SurfaceHolder.Callback` 和 `Runnable`。它是整个游戏运行的核心。

`SurfaceView` 适合游戏这种持续绘制场景，因为它可以配合独立线程进行画面刷新，避免把全部绘制压力放在 UI 线程上。项目中在 `surfaceCreated()` 启动游戏线程，在 `surfaceDestroyed()` 停止线程并释放背景音乐。

主循环位于 `run()`：

```text
while (isRunning) {
    action();
    draw();
    sleep(剩余帧时间);
}
```

`timeInterval` 为 40ms，约等于 25 FPS。每一帧先执行逻辑，再绘制画面。如果本帧耗时不足 40ms，就用 `Thread.sleep()` 补齐时间，使游戏速度稳定。

### 2.3 每帧逻辑 action()

`action()` 是每一帧的游戏逻辑入口，执行顺序大致如下：

1. 更新时间和难度。
2. 到达生成周期时创建敌机、精英敌机或 Boss。
3. 英雄机按 `heroShootPeriod` 发射子弹。
4. 敌机按 `enemyShootPeriod` 发射子弹。
5. 移动英雄子弹、敌方子弹、敌机、道具。
6. 进行碰撞检测。
7. 调用 `postProcessAction()` 做模式扩展处理。
8. 如果英雄机生命值小于等于 0，停止主循环并进入 `gameOver()`。

`OnlineGame` 就是通过重写 `postProcessAction()` 来上报联机分数，因此不会破坏原有单机主循环。

### 2.4 画面绘制 draw()

`draw()` 使用 `SurfaceHolder.lockCanvas()` 获取 Canvas，然后依次绘制：

- 滚动背景图。
- 敌方子弹。
- 英雄子弹。
- 敌机。
- 道具。
- 英雄机。
- 分数和生命值。

绘制结束后调用 `unlockCanvasAndPost()` 提交画面。背景通过 `backGroundTop` 每帧递增实现纵向滚动。

## 3. 游戏对象和设计模式

### 3.1 飞行物基类

`AbstractFlyingObject` 是敌机、子弹、道具等对象的共同父类，保存位置、速度、尺寸、有效状态等通用字段。它提供移动、碰撞区域判断、失效标记等基础能力。

### 3.2 英雄机单例

`HeroAircraft` 使用单例模式。游戏中同一时刻只需要一个英雄机对象，用单例能让各模块共享同一个玩家飞机状态。每局开始前必须重置单例，避免复用上一局残留状态。

### 3.3 工厂模式

敌机和道具创建使用工厂模式：

- 敌机工厂：`MobEnemyFactory`、`EliteEnemyFactory`、`ElitePlusEnemyFactory`、`BossEnemyFactory`。
- 道具工厂：`BloodPropFactory`、`FirePropFactory`、`BombPropFactory`、`SuperFirePropFactory`。

游戏主循环只依赖统一的工厂接口，不直接关心具体构造类。这样新增一种敌机或道具时，只需要增加对应产品类和工厂类，再在生成逻辑中接入。

### 3.4 策略模式

射击方式抽象为 `ShootStrategy`：

- `StraightShootStrategy`：直线射击。
- `ScatterShootStrategy`：散射。
- `CircleShootStrategy`：环形射击。
- `NoShootStrategy`：不发射子弹。

飞机类持有射击策略，调用 `shoot()` 时把具体算法交给策略对象。火力道具可以临时改变英雄机射击策略，持续一段时间后恢复直线射击。

### 3.5 观察者模式

炸弹道具 `BombProp` 使用观察者模式。可被炸弹影响的敌机和敌方子弹实现 `Observer` 接口。炸弹触发时注册这些对象，然后统一通知它们失效。

这种设计让炸弹道具不需要判断每一种敌机和子弹的具体类型，只依赖观察者接口，降低了道具和飞行物之间的耦合。

## 4. 音乐和音效实现

音乐播放由 `MusicThread` 完成，底层使用 Android `MediaPlayer` 组件。

资源文件放在 `app/src/main/res/raw/`：

- `bgm.wav`：普通背景音乐。
- `bgm_boss.wav`：Boss 背景音乐。
- `bullet_hit.wav`：子弹命中音效。
- `get_supply.wav`：拾取道具音效。
- `bomb_explosion.wav`：炸弹音效。
- `game_over.wav`：游戏结束音效。

`MusicThread` 内部维护一个 `soundMap`，把原 Java 版中的字符串路径映射到 Android raw 资源 ID。例如 `"src/videos/bgm.wav"` 映射到 `R.raw.bgm`。这样既适配了 Android 资源系统，也保留了原项目调用方式。

播放流程：

1. 构造 `MusicThread(context, filename, loop)`。
2. 根据 `filename` 找到 raw 资源 ID。
3. 在线程 `run()` 中调用 `MediaPlayer.create(context, resId)`。
4. 通过 `setLooping(loop)` 设置是否循环。
5. 调用 `start()` 播放。
6. 非循环音效播放完成后，在 `OnCompletionListener` 中调用 `stopMusic()` 释放资源。

背景音乐需要长期播放，所以 `bgm.wav` 和 `bgm_boss.wav` 通常使用 `loop = true`。短音效如命中、拾取道具、游戏结束使用非循环播放。

`AbstractGame.surfaceDestroyed()` 和 `gameOver()` 中都会停止并释放背景音乐，避免页面退出后音乐继续播放。

## 5. 实验 5：SQLite 排行榜

### 5.1 数据表设计

排行榜使用 Android SQLite，本地数据库由 `ScoreDBHelper` 创建：

```text
数据库名：scores.db
表名：score_records
```

字段：

- `id INTEGER PRIMARY KEY AUTOINCREMENT`：自增主键。
- `player_name TEXT NOT NULL`：玩家名。
- `score INTEGER NOT NULL`：得分。
- `record_time TEXT NOT NULL`：记录时间。
- `difficulty TEXT NOT NULL`：难度或模式。

`difficulty` 用来区分不同排行榜：

- `EASY`
- `NORMAL`
- `HARD`
- `BATTLE`

这样所有记录可以放在同一张表里，通过查询条件筛选不同模式。

### 5.2 SQLiteOpenHelper

`ScoreDBHelper` 继承 `SQLiteOpenHelper`：

- `onCreate()` 中执行 `CREATE TABLE IF NOT EXISTS` 创建表。
- `onUpgrade()` 中先删除旧表再重建，适合实验阶段快速更新表结构。

`ScoreDaoSQLiteImpl` 构造时使用 `context.getApplicationContext()` 创建 Helper，避免 Activity Context 被长期持有造成泄漏。

### 5.3 DAO 数据访问

`ScoreDaoSQLiteImpl` 实现 `ScoreDao` 接口，封装数据库操作：

- `insertScore()`：使用 `ContentValues` 插入玩家名、分数、时间、难度。
- `getAllScores()`：按 `difficulty` 查询，并按 `score DESC` 降序排列。
- `getTopScores()`：查询指定数量的高分记录。
- `deleteScore()`：按玩家名和难度删除。
- `deleteScoreById()`：按数据库自增 ID 删除，避免同名玩家删除多条记录。
- `updateScore()`：更新分数和时间。

查询时使用 `Cursor` 遍历结果，把每一行转换成 `ScoreRecord`。同时设置 `rowId` 和 `rank`，`rank` 用于展示名次，`rowId` 用于删除指定记录。

### 5.4 排行榜界面

`LeaderboardActivity` 负责展示排行榜：

1. 从 Intent 中读取 `EXTRA_DIFFICULTY`。
2. 创建 `ScoreDaoSQLiteImpl`。
3. 调用 `getAllScores(difficulty)` 获取记录。
4. 使用 `ListView + BaseAdapter` 显示排名、玩家名、分数、时间。
5. 每条记录带删除按钮，点击后按 `rowId` 删除，再重新查询并刷新 Adapter。

游戏结束时写入排行榜：

- 单机模式在 `AbstractGame.gameOver()` 中写入当前难度。
- 联机模式在 `BattleActivity.onBothDead()` 中写入 `BATTLE` 难度。

## 6. 实验 6：Socket 联机对战

### 6.1 联机模块组成

联机对战由客户端和服务端协作完成：

- `BattleServer`：Java 服务端，运行在电脑命令行中。
- `BattleClient`：Android 客户端 Socket 封装。
- `BattleActivity`：联机对战 Activity，负责连接状态、等待弹窗、结果弹窗。
- `OnlineGame`：联机游戏模式，继承 `NormalGame`，扩展分数同步和死亡上报。

服务端启动命令：

```bash
./gradlew :server:run
```

模拟器访问电脑服务端时，客户端 IP 填 `10.0.2.2`，端口默认 `9999`。`127.0.0.1` 在模拟器里表示模拟器自身，不是电脑。

### 6.2 通信协议

协议使用 TCP 长连接和行文本消息。每条消息以换行结束。

客户端发送：

```text
SCORE <int>
DEAD
```

服务端发送：

```text
WAIT
START
OPP_SCORE <int>
OPP_DEAD
END <myScore> <opponentScore>
OPP_LEFT
```

使用行文本的原因是实现简单、便于调试，服务端控制台和客户端日志都能直接看到消息含义。

### 6.3 服务端配对逻辑

`BattleServer` 启动后创建 `ServerSocket` 监听端口。每当有客户端连接：

1. `accept()` 返回一个 `Socket`。
2. 服务端创建 `ClientSession` 线程负责这个客户端的读写。
3. 调用 `tryPair()` 尝试配对。

配对使用 `ConcurrentLinkedQueue<ClientSession> waiting`：

- 如果等待队列为空，当前客户端进入队列，服务端发送 `WAIT`。
- 如果已有等待客户端，服务端取出等待者，与当前客户端创建 `Room`。
- `Room` 创建后，服务端向双方发送 `START`，客户端开始游戏。

`tryPair()` 使用 `synchronized` 修饰，避免多个客户端几乎同时连接时破坏配对顺序。

### 6.4 Room 房间逻辑

`Room` 保存一局对战状态：

- `a`、`b`：双方客户端。
- `aScore`、`bScore`：双方最新分数。
- `aDead`、`bDead`：双方是否阵亡。
- `ended`：房间是否已经结束。

分数同步：

1. 客户端发送 `SCORE 100`。
2. 服务端更新当前玩家分数。
3. 服务端给对手发送 `OPP_SCORE 100`。

死亡同步：

1. 某个客户端死亡后发送 `DEAD`。
2. 服务端标记该玩家死亡。
3. 服务端给对手发送 `OPP_DEAD`。
4. 如果双方都死亡，服务端向双方分别发送 `END`。

结束处理：

- 服务端发送 `END <myScore> <opponentScore>`。
- `ended` 置为 true，避免重复处理后续消息。
- 服务端关闭该房间双方连接。
- 正常结束关闭连接时不会再发送 `OPP_LEFT`。

### 6.5 Android 客户端连接

`BattleClient` 封装 TCP 连接：

- `connect()` 在后台线程中创建 `Socket`。
- `socket.connect(new InetSocketAddress(host, port), 5000)` 设置 5 秒连接超时。
- 连接成功后创建 `BufferedReader` 和 `PrintWriter`。
- 启动接收线程 `BattleClient-Receiver`，持续读取服务端消息。

接收线程用 `readLine()` 阻塞等待服务端消息。收到消息后由 `handleLine()` 解析，并通过 `Handler(Looper.getMainLooper())` 把回调切回主线程。

发送消息通过单线程 `ExecutorService` 执行，避免游戏线程直接做网络 I/O：

- `sendScore(score)` 发送 `SCORE <score>`。
- `sendDead()` 发送 `DEAD`。

### 6.6 Activity 状态控制

`BattleActivity` 实现 `BattleClient.Listener` 和 `OnlineGame.BattleHook`。

连接阶段：

- `onWaiting()`：显示等待对手弹窗，说明第一个玩家已经连接但还没有配对。
- `onBattleStart()`：关闭等待弹窗，创建 `OnlineGame` 并 `setContentView(game)`。
- `onError()`：连接失败时弹窗返回。

游戏阶段：

- `onOpponentScore()`：把对手分数传给 `OnlineGame`。
- `onOpponentDead()`：标记对手阵亡。
- `onOpponentLeft()`：显示对手离线。
- `onBattleEnded()`：展示最终胜负。

自己死亡后，`OnlineGame` 通过 `BattleHook.onSelfDead()` 通知 Activity。Activity 会显示“等待对手阵亡”弹窗；如果双方都死亡，则关闭等待弹窗并显示最终结果。

### 6.7 OnlineGame 扩展点

`OnlineGame` 继承 `NormalGame`，复用普通模式敌机、Boss、道具和难度逻辑，只扩展联机行为。

主要字段：

- `opponentScore`：对手当前分数。
- `opponentDead`：对手是否阵亡。
- `selfDead`：自己是否已经阵亡。
- `deadSent`：是否已经向服务端发送过 `DEAD`。
- `ended`：本局是否收到最终结果。

分数上报在 `postProcessAction()` 中完成。每帧比较当前分数和 `lastReportedScore`，只有分数变化才发送，避免每 40ms 发送重复消息。

死亡上报在 `gameOver()` 中完成：

1. 停止背景音乐。
2. 播放游戏结束音效。
3. 向服务端发送一次 `DEAD`。
4. 通知 `BattleActivity` 显示等待对手阵亡弹窗。

右上角对手分数通过重写 `paintScoreAndLife()` 绘制，格式为 `OPPONENT:<score>`。如果对手已阵亡，会附加 `(DEAD)`。

### 6.8 断线和一局结束后的处理

联机调试中最容易混淆的是“等待对手”和“卡死”。当前实现做了明确区分：

- 只有一个客户端连接：显示等待对手弹窗。
- 开局前服务器断开：显示连接失败。
- 开局后对手退出：显示对手离线。
- 双方正常结束：显示胜负结果，不再误报离线。

`BattleClient` 使用三个状态字段处理这些情况：

- `userDisconnected`：是否用户主动退出。
- `battleStarted`：是否已经收到 `START`。
- `battleEnded`：是否已经收到 `END`。

`BattleServer.Room` 使用 `ended` 标记，确保一局结束后关闭旧连接，下一局重新进入等待队列配对。

## 7. 调试和运行要点

### 7.1 服务端启动

在项目根目录运行：

```bash
./gradlew :server:run
```

Windows PowerShell 中是：

```powershell
.\gradlew :server:run --console=plain
```

看到下面日志表示服务端真正启动成功：

```text
[BattleServer] listening on port 9999
```

如果 Gradle 下载 toolchain 很慢，可以直接使用 Android Studio 自带 JDK，设置 `JAVA_HOME` 后再运行。

### 7.2 客户端连接

模拟器连接电脑服务端：

```text
服务器 IP：10.0.2.2
端口：9999
```

真机连接电脑服务端：

- 手机和电脑连接同一个局域网。
- IP 填电脑局域网 IP，例如 `192.168.x.x`。
- Windows 防火墙需要允许 Java 网络访问。

### 7.3 正常联机现象

- 第一个客户端进入后，服务端发送 `WAIT`，客户端显示等待对手。
- 第二个客户端进入后，服务端打印 `room#... paired`，两个客户端进入游戏。
- 一方分数变化时，另一方右上角对手分数更新。
- 一方死亡时，另一方看到对手死亡标记。
- 双方死亡后，服务端打印 `room#... ended`，客户端显示胜负结果。

## 8. 关键实现细节

- UI 只能在 Android 主线程更新，所以网络线程通过 `Handler(Looper.getMainLooper())` 回调 Activity。
- Socket 接收使用 `BufferedReader.readLine()`，发送使用 `PrintWriter.println()`，协议必须保持换行。
- 发送分数使用单线程 `ExecutorService`，避免阻塞游戏绘制线程。
- SQLite 查询后必须关闭 `Cursor`，当前 DAO 在查询结束后调用 `cursor.close()`。
- 背景音乐和 Boss 音乐循环播放，短音效播放完成后释放 `MediaPlayer`。
- `BattleActivity.onDestroy()` 会调用 `client.disconnect()`，退出联机页面时释放 socket。
- `ServerSocket` 可以一直运行，客户端每局结束后重新进入联机即可开始下一轮配对。
