package edu.hitsz.application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Handler;
import android.os.Looper;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import java.util.LinkedList;
import java.util.List;

import android.content.Intent;

import edu.hitsz.LeaderboardActivity;
import edu.hitsz.MainActivity;
import edu.hitsz.aircraft.AbstractAircraft;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.aircraft.BossEnemyFactory;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.EliteEnemyFactory;
import edu.hitsz.aircraft.ElitePlusEnemy;
import edu.hitsz.aircraft.ElitePlusEnemyFactory;
import edu.hitsz.aircraft.EnemyFactory;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.MobEnemyFactory;
import edu.hitsz.basic.AbstractFlyingObject;
import edu.hitsz.bullet.BaseBullet;
import edu.hitsz.dao.ScoreDaoSQLiteImpl;
import edu.hitsz.dto.ScoreRecord;
import edu.hitsz.observer.Observer;
import edu.hitsz.prop.AbstractProp;
import edu.hitsz.prop.BloodPropFactory;
import edu.hitsz.prop.BombProp;
import edu.hitsz.prop.BombPropFactory;
import edu.hitsz.prop.FirePropFactory;
import edu.hitsz.prop.PropFactory;
import edu.hitsz.prop.SuperFirePropFactory;

/**
 * 游戏抽象类
 */
public abstract class AbstractGame extends SurfaceView implements SurfaceHolder.Callback, Runnable {

    private int backGroundTop = 0;
    private int timeInterval = 40;

    protected final HeroAircraft heroAircraft;
    protected final List<AbstractAircraft> enemyAircrafts;
    protected final List<BaseBullet> heroBullets;
    protected final List<BaseBullet> enemyBullets;
    protected final List<AbstractProp> props;

    protected final EnemyFactory mobEnemyFactory;
    protected final EnemyFactory eliteEnemyFactory;
    protected final EnemyFactory elitePlusEnemyFactory;
    protected final EnemyFactory bossEnemyFactory;

    protected final PropFactory bloodPropFactory;
    protected final PropFactory firePropFactory;
    protected final PropFactory bombPropFactory;
    protected final PropFactory superFirePropFactory;

    protected String difficulty;
    protected int enemyMaxNumber;
    protected int score = 0;
    protected int bossScoreThreshold;
    protected int lastBossScore = 0;
    protected boolean bossExists = false;
    protected int elitePlusCycleCount = 0;
    protected int elitePlusCyclePeriod = 5;
    protected int time = 0;
    protected int cycleDuration;
    protected int cycleTime = 0;
    protected int heroShootPeriod;
    protected int heroShootTime = 0;
    protected int enemyShootPeriod = 600;
    protected int enemyShootTime = 0;
    protected double eliteProbability;
    protected int initialBossHp;
    protected int mobEnemyHp;
    protected int eliteEnemyHp;
    protected int mobEnemySpeed;
    protected int eliteEnemySpeed;

    protected boolean gameOverFlag = false;
    protected boolean soundEnabled = true;
    protected MusicThread bgmThread = null;

    private Thread drawingThread;
    private boolean isRunning = false;
    private SurfaceHolder mSurfaceHolder;
    private Paint mPaint;

    public AbstractGame(Context context, String difficulty, boolean soundEnabled) {
        super(context);
        this.difficulty = difficulty;
        this.soundEnabled = soundEnabled;

        mSurfaceHolder = this.getHolder();
        mSurfaceHolder.addCallback(this);
        mPaint = new Paint();

        ImageManager.init(context);
        ImageManager.setBackgroundImage(difficulty);

        heroAircraft = HeroAircraft.getInstance(
                Main.screenWidth / 2,
                Main.screenHeight - 200,
                0, 0, 1000);

        enemyAircrafts = new LinkedList<>();
        heroBullets = new LinkedList<>();
        enemyBullets = new LinkedList<>();
        props = new LinkedList<>();

        mobEnemyFactory = new MobEnemyFactory();
        eliteEnemyFactory = new EliteEnemyFactory();
        elitePlusEnemyFactory = new ElitePlusEnemyFactory();
        bossEnemyFactory = new BossEnemyFactory();
        bloodPropFactory = new BloodPropFactory();
        firePropFactory = new FirePropFactory();
        bombPropFactory = new BombPropFactory();
        superFirePropFactory = new SuperFirePropFactory();

        initGameParameters();
    }

    protected abstract void initGameParameters();
    protected abstract void generateEnemy();
    protected abstract void generateBoss();
    protected abstract boolean shouldIncreaseDifficulty();
    protected abstract void increaseDifficulty();
    protected abstract int getBossHp();

    @Override
    public void surfaceCreated(@NonNull SurfaceHolder holder) {
        isRunning = true;
        drawingThread = new Thread(this);
        drawingThread.start();
        if (soundEnabled) {
            bgmThread = new MusicThread(getContext(), "src/videos/bgm.wav", true);
            bgmThread.start();
        }
    }

    @Override
    public void surfaceChanged(@NonNull SurfaceHolder holder, int format, int width, int height) {
        Main.screenWidth = width;
        Main.screenHeight = height;
    }

    @Override
    public void surfaceDestroyed(@NonNull SurfaceHolder holder) {
        isRunning = false;
        if (bgmThread != null) {
            bgmThread.stopMusic();
            bgmThread = null;
        }
    }

    @Override
    public void run() {
        while (isRunning) {
            long start = System.currentTimeMillis();
            action();
            draw();
            long end = System.currentTimeMillis();
            if (end - start < timeInterval) {
                try {
                    Thread.sleep(timeInterval - (end - start));
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void draw() {
        Canvas canvas = mSurfaceHolder.lockCanvas();
        if (canvas != null) {
            try {
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR);
                canvas.drawBitmap(ImageManager.BACKGROUND_IMAGE, 0, backGroundTop - Main.screenHeight, mPaint);
                canvas.drawBitmap(ImageManager.BACKGROUND_IMAGE, 0, backGroundTop, mPaint);
                backGroundTop += 2;
                if (backGroundTop >= Main.screenHeight) backGroundTop = 0;

                paintImageWithPositionRevised(canvas, enemyBullets);
                paintImageWithPositionRevised(canvas, heroBullets);
                paintImageWithPositionRevised(canvas, enemyAircrafts);
                paintImageWithPositionRevised(canvas, props);

                canvas.drawBitmap(ImageManager.HERO_IMAGE, 
                        heroAircraft.getLocationX() - ImageManager.HERO_IMAGE.getWidth() / 2,
                        heroAircraft.getLocationY() - ImageManager.HERO_IMAGE.getHeight() / 2, mPaint);

                paintScoreAndLife(canvas);
            } finally {
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }

    private void paintImageWithPositionRevised(Canvas canvas, List<? extends AbstractFlyingObject> objects) {
        for (int i = 0; i < objects.size(); i++) {
            AbstractFlyingObject object = objects.get(i);
            Bitmap image = object.getImage();
            if (image != null) {
                canvas.drawBitmap(image, object.getLocationX() - image.getWidth() / 2,
                        object.getLocationY() - image.getHeight() / 2, mPaint);
            }
        }
    }

    protected void paintScoreAndLife(Canvas canvas) {
        mPaint.setColor(Color.RED);
        mPaint.setTextSize(50);
        mPaint.setFakeBoldText(true);
        canvas.drawText("SCORE:" + this.score, 30, 80, mPaint);
        canvas.drawText("LIFE:" + this.heroAircraft.getHp(), 30, 150, mPaint);
    }

    protected Paint getPaint() {
        return mPaint;
    }

    protected int getScore() {
        return score;
    }

    protected boolean isGameOver() {
        return gameOverFlag || (heroAircraft != null && heroAircraft.getHp() <= 0);
    }

    public final void action() {
        time += timeInterval;
        if (shouldIncreaseDifficulty()) increaseDifficulty();
        if (timeCountAndNewCycleJudge()) {
            if (bossScoreThreshold > 0 && score - lastBossScore >= bossScoreThreshold && !bossExists) generateBoss();
            elitePlusCycleCount++;
            if (elitePlusCycleCount >= elitePlusCyclePeriod && Math.random() < 0.5) {
                generateElitePlusEnemy();
                elitePlusCycleCount = 0;
            }
            if (enemyAircrafts.size() < enemyMaxNumber) generateEnemy();
        }
        heroShootTime += timeInterval;
        if (heroShootTime >= heroShootPeriod) {
            heroBullets.addAll(heroAircraft.shoot());
            heroShootTime = 0;
        }
        enemyShootTime += timeInterval;
        if (enemyShootTime >= enemyShootPeriod) {
            shootAction();
            enemyShootTime = 0;
        }
        bulletsMoveAction();
        aircraftsMoveAction();
        propsMoveAction();
        crashCheckAction();
        postProcessAction();

        if (gameOverFlag || heroAircraft.getHp() <= 0) {
            isRunning = false;
            gameOver();
        }
    }

    protected void gameOver() {
        if (bgmThread != null) {
            bgmThread.stopMusic();
            bgmThread = null;
        }
        if (soundEnabled) {
            new MusicThread(getContext(), "src/videos/game_over.wav").start();
        }
        // 在游戏线程中将得分存入 SQLite
        String recordTime = new java.text.SimpleDateFormat("MM-dd HH:mm", java.util.Locale.getDefault())
                .format(new java.util.Date());
        new ScoreDaoSQLiteImpl(getContext()).insertScore(
                new ScoreRecord("Player", score, recordTime), difficulty);

        // 使用 Handler 在 UI 线程执行跳转逻辑
        new Handler(Looper.getMainLooper()).post(() -> {
            // 让 MainActivity 恢复菜单布局（在后台）
            if (getContext() instanceof MainActivity) {
                ((MainActivity) getContext()).returnToMenu();
            }
            // 跳转到排行榜页面
            Intent intent = new Intent(getContext(), LeaderboardActivity.class);
            intent.putExtra(LeaderboardActivity.EXTRA_DIFFICULTY, difficulty);
            getContext().startActivity(intent);
        });
    }

    protected boolean timeCountAndNewCycleJudge() {
        cycleTime += timeInterval;
        if (cycleTime >= cycleDuration) {
            cycleTime %= cycleDuration;
            return true;
        } else {
            return false;
        }
    }

    protected void generateElitePlusEnemy() {
        int elitePlusWidth = ImageManager.ELITE_PLUS_ENEMY_IMAGE.getWidth();
        int locationX = (int) (Math.random() * (Main.screenWidth - elitePlusWidth));
        int locationY = (int) (Math.random() * Main.screenHeight * 0.05);
        enemyAircrafts.add(elitePlusEnemyFactory.createEnemy(locationX, locationY, (int) (Math.random() * 6 - 3), 10, 120));
    }

    protected void shootAction() {
        for (int i = 0; i < enemyAircrafts.size(); i++) {
            AbstractAircraft enemyAircraft = enemyAircrafts.get(i);
            if (enemyAircraft instanceof BossEnemy || enemyAircraft instanceof ElitePlusEnemy || enemyAircraft instanceof EliteEnemy) {
                enemyBullets.addAll(enemyAircraft.shoot());
            }
        }
    }

    protected void bulletsMoveAction() {
        for (int i = 0; i < heroBullets.size(); i++) heroBullets.get(i).forward();
        for (int i = 0; i < enemyBullets.size(); i++) enemyBullets.get(i).forward();
    }

    protected void aircraftsMoveAction() {
        for (int i = 0; i < enemyAircrafts.size(); i++) enemyAircrafts.get(i).forward();
    }

    protected void propsMoveAction() {
        for (int i = 0; i < props.size(); i++) props.get(i).forward();
    }

    protected void crashCheckAction() {
        for (int i = 0; i < enemyBullets.size(); i++) {
            BaseBullet bullet = enemyBullets.get(i);
            if (bullet.notValid()) continue;
            if (heroAircraft.crash(bullet)) {
                heroAircraft.decreaseHp(bullet.getPower());
                bullet.vanish();
            }
        }
        for (int i = 0; i < heroBullets.size(); i++) {
            BaseBullet bullet = heroBullets.get(i);
            if (bullet.notValid()) continue;
            for (int j = 0; j < enemyAircrafts.size(); j++) {
                AbstractAircraft enemyAircraft = enemyAircrafts.get(j);
                if (enemyAircraft.notValid()) continue;
                if (enemyAircraft.crash(bullet)) {
                    enemyAircraft.decreaseHp(bullet.getPower());
                    bullet.vanish();
                    if (soundEnabled) new MusicThread(getContext(), "src/videos/bullet_hit.wav").start();
                    if (enemyAircraft.notValid()) handleEnemyDestroyed(enemyAircraft);
                }
            }
        }
        for (int i = 0; i < props.size(); i++) {
            AbstractProp prop = props.get(i);
            if (prop.notValid()) continue;
            if (heroAircraft.crash(prop)) {
                if (prop instanceof BombProp) {
                    BombProp bombProp = (BombProp) prop;
                    for (int j = 0; j < enemyAircrafts.size(); j++) {
                        AbstractAircraft enemyAircraft = enemyAircrafts.get(j);
                        if (!enemyAircraft.notValid() && enemyAircraft instanceof Observer) bombProp.registerObserver((Observer) enemyAircraft);
                    }
                    for (int j = 0; j < enemyBullets.size(); j++) {
                        BaseBullet enemyBullet = enemyBullets.get(j);
                        if (!enemyBullet.notValid() && enemyBullet instanceof Observer) bombProp.registerObserver((Observer) enemyBullet);
                    }
                    score += bombProp.notifyObservers();
                    if (soundEnabled) new MusicThread(getContext(), "src/videos/bomb_explosion.wav").start();
                } else {
                    prop.activate(heroAircraft);
                    if (soundEnabled) new MusicThread(getContext(), "src/videos/get_supply.wav").start();
                }
                prop.vanish();
            }
        }
        for (int i = 0; i < enemyAircrafts.size(); i++) {
            AbstractAircraft enemyAircraft = enemyAircrafts.get(i);
            if (enemyAircraft.notValid()) continue;
            if (enemyAircraft.crash(heroAircraft) || heroAircraft.crash(enemyAircraft)) {
                enemyAircraft.vanish();
                heroAircraft.decreaseHp(Integer.MAX_VALUE);
                gameOverFlag = true;
            }
        }
    }

    protected void handleEnemyDestroyed(AbstractAircraft enemyAircraft) {
        if (enemyAircraft instanceof BossEnemy) {
            score += 300;
            bossExists = false;
            generateBossProp(enemyAircraft.getLocationX(), enemyAircraft.getLocationY());
        } else if (enemyAircraft instanceof ElitePlusEnemy) {
            score += 100;
            generateElitePlusProp(enemyAircraft.getLocationX(), enemyAircraft.getLocationY());
        } else if (enemyAircraft instanceof EliteEnemy) {
            score += 50;
            generateEliteProp(enemyAircraft.getLocationX(), enemyAircraft.getLocationY());
        } else {
            score += 10;
        }
    }

    protected void generateEliteProp(int x, int y) {
        double random = Math.random();
        if (random < 0.3) props.add(bloodPropFactory.createProp(x, y, 0, 5));
        else if (random < 0.6) props.add(firePropFactory.createProp(x, y, 0, 5));
        else if (random < 0.85) props.add(bombPropFactory.createProp(x, y, 0, 5));
        else props.add(superFirePropFactory.createProp(x, y, 0, 5));
    }

    protected void generateElitePlusProp(int x, int y) {
        double random = Math.random();
        if (random < 0.25) props.add(bloodPropFactory.createProp(x, y, 0, 5));
        else if (random < 0.5) props.add(firePropFactory.createProp(x, y, 0, 5));
        else if (random < 0.75) props.add(bombPropFactory.createProp(x, y, 0, 5));
        else props.add(superFirePropFactory.createProp(x, y, 0, 5));
    }

    protected void generateBossProp(int x, int y) {
        props.add(bloodPropFactory.createProp(x - 50, y, 0, 5));
        props.add(bombPropFactory.createProp(x, y, 0, 5));
        props.add(superFirePropFactory.createProp(x + 50, y, 0, 5));
    }

    protected void postProcessAction() {
        enemyBullets.removeIf(AbstractFlyingObject::notValid);
        heroBullets.removeIf(AbstractFlyingObject::notValid);
        enemyAircrafts.removeIf(AbstractFlyingObject::notValid);
        props.removeIf(AbstractFlyingObject::notValid);
        if (bossExists) {
            boolean bossStillExists = false;
            for (int i = 0; i < enemyAircrafts.size(); i++) {
                if (enemyAircrafts.get(i) instanceof BossEnemy) {
                    bossStillExists = true;
                    break;
                }
            }
            bossExists = bossStillExists;
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_MOVE || event.getAction() == MotionEvent.ACTION_DOWN) {
            heroAircraft.setLocation(event.getX(), event.getY());
        }
        return true;
    }
}
