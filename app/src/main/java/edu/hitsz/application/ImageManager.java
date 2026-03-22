package edu.hitsz.application;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.util.HashMap;
import java.util.Map;

import edu.hitsz.R;
import edu.hitsz.aircraft.BossEnemy;
import edu.hitsz.aircraft.EliteEnemy;
import edu.hitsz.aircraft.ElitePlusEnemy;
import edu.hitsz.aircraft.HeroAircraft;
import edu.hitsz.aircraft.MobEnemy;
import edu.hitsz.bullet.EnemyBullet;
import edu.hitsz.bullet.HeroBullet;
import edu.hitsz.prop.BloodProp;
import edu.hitsz.prop.BombProp;
import edu.hitsz.prop.FireProp;
import edu.hitsz.prop.SuperFireProp;

/**
 * 综合管理图片的加载，访问
 */
public class ImageManager {

    private static final Map<String, Bitmap> CLASSNAME_IMAGE_MAP = new HashMap<>();

    public static Bitmap BACKGROUND_IMAGE;
    public static Bitmap BACKGROUND_IMAGE_EASY;
    public static Bitmap BACKGROUND_IMAGE_NORMAL;
    public static Bitmap BACKGROUND_IMAGE_HARD;
    public static Bitmap HERO_IMAGE;
    public static Bitmap HERO_BULLET_IMAGE;
    public static Bitmap ENEMY_BULLET_IMAGE;
    public static Bitmap MOB_ENEMY_IMAGE;
    public static Bitmap ELITE_ENEMY_IMAGE;
    public static Bitmap ELITE_PLUS_ENEMY_IMAGE;
    public static Bitmap BOSS_ENEMY_IMAGE;
    public static Bitmap BLOOD_PROP_IMAGE;
    public static Bitmap BOMB_PROP_IMAGE;
    public static Bitmap FIRE_PROP_IMAGE;
    public static Bitmap SUPER_FIRE_PROP_IMAGE;

    public static void init(Context context) {
        // 加载并缩放背景图，使其适配全屏
        BACKGROUND_IMAGE_EASY = getScaledBitmap(context, R.drawable.bg, Main.screenWidth, Main.screenHeight);
        BACKGROUND_IMAGE_NORMAL = getScaledBitmap(context, R.drawable.bg2, Main.screenWidth, Main.screenHeight);
        BACKGROUND_IMAGE_HARD = getScaledBitmap(context, R.drawable.bg3, Main.screenWidth, Main.screenHeight);
        BACKGROUND_IMAGE = BACKGROUND_IMAGE_EASY;

        HERO_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.hero);
        MOB_ENEMY_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.mob);
        ELITE_ENEMY_IMAGE = MOB_ENEMY_IMAGE; // 如果没有 elite.png 则暂用 mob.png
        ELITE_PLUS_ENEMY_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.elite_plus);
        BOSS_ENEMY_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.boss);
        HERO_BULLET_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.bullet_hero);
        ENEMY_BULLET_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.bullet_enemy);
        BLOOD_PROP_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.prop_blood);
        BOMB_PROP_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.prop_bomb);
        FIRE_PROP_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.prop_bullet);
        SUPER_FIRE_PROP_IMAGE = BitmapFactory.decodeResource(context.getResources(), R.drawable.prop_bullet_plus);

        CLASSNAME_IMAGE_MAP.put(HeroAircraft.class.getName(), HERO_IMAGE);
        CLASSNAME_IMAGE_MAP.put(MobEnemy.class.getName(), MOB_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(EliteEnemy.class.getName(), ELITE_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(ElitePlusEnemy.class.getName(), ELITE_PLUS_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BossEnemy.class.getName(), BOSS_ENEMY_IMAGE);
        CLASSNAME_IMAGE_MAP.put(HeroBullet.class.getName(), HERO_BULLET_IMAGE);
        CLASSNAME_IMAGE_MAP.put(EnemyBullet.class.getName(), ENEMY_BULLET_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BloodProp.class.getName(), BLOOD_PROP_IMAGE);
        CLASSNAME_IMAGE_MAP.put(BombProp.class.getName(), BOMB_PROP_IMAGE);
        CLASSNAME_IMAGE_MAP.put(FireProp.class.getName(), FIRE_PROP_IMAGE);
        CLASSNAME_IMAGE_MAP.put(SuperFireProp.class.getName(), SUPER_FIRE_PROP_IMAGE);
    }

    private static Bitmap getScaledBitmap(Context context, int resId, int width, int height) {
        Bitmap src = BitmapFactory.decodeResource(context.getResources(), resId);
        if (width <= 0 || height <= 0) return src;
        return Bitmap.createScaledBitmap(src, width, height, true);
    }

    public static Bitmap get(String className){
        return CLASSNAME_IMAGE_MAP.get(className);
    }

    public static Bitmap get(Object obj){
        if (obj == null) return null;
        return get(obj.getClass().getName());
    }

    public static void setBackgroundImage(String difficulty) {
        switch (difficulty) {
            case "NORMAL": BACKGROUND_IMAGE = BACKGROUND_IMAGE_NORMAL; break;
            case "HARD": BACKGROUND_IMAGE = BACKGROUND_IMAGE_HARD; break;
            default: BACKGROUND_IMAGE = BACKGROUND_IMAGE_EASY; break;
        }
    }
}
