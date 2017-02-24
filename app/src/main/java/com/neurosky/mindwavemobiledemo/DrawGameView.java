package com.neurosky.mindwavemobiledemo;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.Log;
import android.view.View;
import android.graphics.Canvas;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Random;

public class DrawGameView extends View {


    public Paint paint_brickbat = null,paint_line = null , paint_back = null;

    private int mBottom = 0;
    private int mHeight = 0;
    private int mLeft = 0;
    private int mWidth = 0;

    ArrayList<Brickbat> array_brickbat  = null;

    private int nowHeight = 0;

    // 亂數
    Random r = new Random();


    Role role =null;
    BackGround backGround = null;

    public boolean isFirst = true;

    private GameActivity gameactivity;

    public int nowPlayHeight = 0;
    public int afterPlayHeight ;
    public int playCount = 0;

    public boolean isGame = false;




    public DrawGameView(Context context,GameActivity activity) {
        super(context);
        gameactivity = activity;

    }

    public void initView(){

        paint_brickbat = new Paint();
        //設定鋸齒狀
        paint_brickbat.setAntiAlias(true);
        paint_brickbat.setColor(Color.argb(255, 255, 255, 0));

        paint_line = new Paint();
        paint_line.setColor(Color.BLACK);

        paint_back = new Paint();
        paint_back.setColor(Color.BLUE);

        mBottom = this.getBottom();
        mWidth = this.getWidth();
        mLeft = this.getLeft();
        mHeight = this.getHeight();

        array_brickbat = new ArrayList<Brickbat>();
        reset();

    }
    public void reset(){

        isGame = true;
        array_brickbat.clear();
        Brickbat bb = null;
        nowHeight = mBottom-200;
        for(int i =0;i<9;i++) {
            int a = r.nextInt(mWidth-200);
            bb = new Brickbat(a, nowHeight);
            array_brickbat.add(bb);
            nowHeight-=150;
        }

        //目前樓街
        nowPlayHeight = 0;
        // 上升多少
        playCount = 0;
        ((TextView) gameactivity.findViewById(R.id.textView)).setText("目前樓階："+nowPlayHeight + "階");

        isFirst = true;

        role = new Role();

        backGround = new BackGround();

        afterPlayHeight = gameactivity.getPreference();
        ((TextView) gameactivity.findViewById(R.id.textView2)).setText("  最高樓階："+afterPlayHeight + "階");
    }


    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // 背景
        canvas.drawBitmap(backGround.getBmp(),50,50, paint_back);


        //畫面數據

        //canvas.drawText("Height=" + mHeight, 30, 10, paint_line);
        //canvas.drawText("Botton="+mBottom,30,30,paint_line);


        // 界線
        for(int j = 0;j<2;j++) {
            for (int i = 0; i < 20; i++) {
                canvas.drawRect(0+(j*(mWidth-30)), 0 + 60 * i + 1, 30+(j*(mWidth-30)), 60 + 60 * i, paint_brickbat);
                canvas.drawLine(0+(j*(mWidth-30)), 60 + 60 * i, 30+(j*(mWidth-30)), 60 + 60 * i, paint_line);
            }
        }


        // 遊戲磚塊

        for (int i = 0; i < array_brickbat.size(); i++) {

            //canvas.drawRect(array_brickbat.get(i).frontx, array_brickbat.get(i).fronty, array_brickbat.get(i).behindx, array_brickbat.get(i).behindy, paint_brickbat);
            canvas.drawBitmap(array_brickbat.get(i).bmp1,array_brickbat.get(i).frontx-25,array_brickbat.get(i).fronty-35,null);
            //canvas.drawText("y:"+array_brickbat.get(i).fronty,array_brickbat.get(i).frontx,array_brickbat.get(i).fronty,paint_line);
        }

        //當角色位置  小於畫面的一半  畫面往上移
        if(role.behindy<mHeight/2){
            for (int i = 0; i < array_brickbat.size(); i++) {

                if (array_brickbat.get(i).fronty > mBottom) {
                    array_brickbat.get(i).resetXY();
                }

                array_brickbat.get(i).fronty += 10;
                array_brickbat.get(i).behindy += 10;

                playCount+=5;
                if(playCount>1000){
                    nowPlayHeight+=1;
                    ((TextView) gameactivity.findViewById(R.id.textView)).setText("目前樓階："+nowPlayHeight + "階");
                    playCount = 0;
                }

            }
            isFirst = false;
        }


        //   jump
        if(role.jumpCount>0){
            if(role.posy>50) {
                role.addposy(-15);
            }
            role.jumpCount -= 1;
            if(role.bonusCount>0) {
                role.bonusCount-=1;
            }

            role.isDown = true;
            role.isJumpAndDown = true;
        }// down
        else{
            int i =0;

            // 角色找尋有無踩點
            for (i = 0; i < array_brickbat.size(); i++) {
                if((array_brickbat.get(i).frontx<role.posx && array_brickbat.get(i).behindx>role.posx)
                        ||  (array_brickbat.get(i).frontx<role.behindx && array_brickbat.get(i).behindx>role.behindx)){
                    //Log.d("has left", "this brickbat posy:   " + array_brickbat.get(i).fronty);
                    if (array_brickbat.get(i).fronty > role.behindy
                            && (array_brickbat.get(i).fronty-role.behindy<16)) {
                        //Log.d("123", "123:   " + array_brickbat.get(i).fronty);
                        role.setposy(array_brickbat.get(i).fronty-1);
                        role.isDown = false;
                        role.isJumpAndDown = false;
                        break;
                    }
                }

            }
            if(i==array_brickbat.size()) {
                if(role.posy<mBottom-150) {
                    role.addposy(15);
                    role.isDown = true;
                    role.isJumpAndDown = true;
                }
                else if(!isFirst){
                    canvas.drawText("Game Over", 100, 100, paint_line);
                    if(nowPlayHeight>afterPlayHeight) {
                        gameactivity.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                gameactivity.savePreference(nowPlayHeight);
                            }
                        });

                    }
                    role.addposy(15);
                    role.isDown = true;
                    gameactivity.sp.play(gameactivity.sp_gameover,1,1,0,0,1);
                    gameactivity.getAlertDialog("提醒","Game Over").show();
                    isGame = false;
                    role.isJumpAndDown = false;
                }
                else if(isFirst){
                    role.isDown = false;
                    role.isJumpAndDown = false;
                }
            }
        }


        canvas.drawBitmap(role.getfirebmp(), role.posx, role.posy-20, null);

        //fire
        if(role.bonusCount>0) {
            if (role.isgoRight)
                canvas.drawBitmap(role.bmp_bonus, role.posx - 50, role.posy, null);
            else canvas.drawBitmap(role.bmp_bonus, role.posx + 50, role.posy, null);
        }
        //  人物
        canvas.drawBitmap(role.getBmp(), role.posx, role.posy, null);


        role.go();
    }
    Runnable updateRunnable = new Runnable(){
        @Override
        public void run(){
            if(isGame)
                invalidate();
            postDelayed(updateRunnable, 1000/30);//1000 ms / 30fps
        }
    };
    @Override
    protected void onLayout(boolean changed, int left, int top, int right,
                            int bottom) {
        // TODO Auto-generated method stub
        super.onLayout(changed, left, top, right, bottom);
        Log.d("goOnLayout", "onLayout");

        initView();
    }

    private class  Brickbat{

        int frontx = 0;
        int fronty = 0;
        int behindx = 0;
        int behindy = 0;

        Bitmap bmp1;

        Brickbat(int frontx,int fronty){
            this.frontx = frontx;
            this.fronty = fronty;
            this.behindx = frontx+200;
            this.behindy = fronty+50;

            bmp1 = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cloud_model);

            bmp1 = zoomImage(bmp1,260,120);
        }



        // 重新設定磚塊位置
        public void resetXY(){

            int a = r.nextInt(mWidth-200);

            frontx = a;
            fronty = nowHeight+200;
            behindx = frontx+200;
            behindy = fronty+50;
        }

    }
    public class Role{
        Bitmap bmp,bmp2,bmp3,bmp5,bmp6,bmp7,bmp8,bmp10;
        Bitmap bmpfire,bmpfire_green,bmpfire_blue,bmpfire_purple,bmpfire_yellow,bmpfire_red;

        Bitmap bmp_bonus;

        public int posx,posy,behindx,behindy;

        public int jumpCount ;
        public int bonusCount;


        public boolean isDown ,isJumpAndDown;
        public boolean isgoRight = true;

        public int whichRightbmp ,whichLeftbmp,chageDir,whichfirebmp;




        Role(){
            bmp = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cat_right1);
            bmp2 = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cat_right2);
            bmp3 = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cat_right3);

            bmp5 = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cat_rightjump);
            bmp6 = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cat_left1);
            bmp7 = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cat_left2);
            bmp8 = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cat_left3);

            bmp10 = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cat_leftjump);

            bmpfire = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cat_fire2);

            bmpfire_green = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cat_firegreen);
            bmpfire_blue = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cat_fireblue);
            bmpfire_purple = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cat_firepurple);
            bmpfire_yellow = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cat_fireyellow);
            bmpfire_red = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cat_firered);

            bmp_bonus = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_bonus_fire);

            whichfirebmp = 1;

            chageDir = 1;
            whichRightbmp = 1;
            whichLeftbmp = 1;
            posx = 100;
            posy = mHeight-bmp.getHeight();
            behindx = posx+bmp.getWidth();
            behindy = posy+bmp.getHeight();

            jumpCount = 0;
            bonusCount = 0;

            isDown = false;
            isJumpAndDown = false;

        }
        public Bitmap getBmp(){

            if(chageDir ==1) {
                if(isJumpAndDown){
                    return bmp5;
                }
                switch (whichRightbmp) {
                    case 1:
                        whichRightbmp = 2;
                        return bmp;
                    case 2:
                        whichRightbmp = 3;
                        return bmp2;
                    case 3:
                        whichRightbmp = 1;
                        return bmp3;
                    default:
                        break;
                }
            }
            else{
                if(isJumpAndDown){
                    return bmp10;
                }
                switch (whichLeftbmp) {
                    case 1:
                        whichLeftbmp = 2;
                        return bmp6;
                    case 2:
                        whichLeftbmp = 3;
                        return bmp7;
                    case 3:
                        whichLeftbmp = 1;
                        return bmp8;
                    default:
                        break;
                }
            }
            return bmp;

        }
        public Bitmap getfirebmp(){
            switch(whichfirebmp){
                case 1:
                    return bmpfire_green;
                case 2:
                    return bmpfire_blue;
                case 3:
                    return bmpfire_purple;
                case 4:
                    return bmpfire_yellow;
                case 5:
                    return bmpfire_red;
                default:
            }
            return bmpfire_green;
        }
        public void goLeft(){
            if(posx>0) {
                posx -= 10;
                behindx = posx+bmp.getWidth();
            }
            else{
                isgoRight = true;
            }
        }
        public void goRight(){
            if(posx<mWidth-160) {
                posx += 10;
                behindx = posx+bmp.getWidth();
            }
            else{
                isgoRight = false;
            }
        }
        public void jump(int r){
            if(!isDown) {
                jumpCount = r;
                gameactivity.sp.play(gameactivity.sp_jump,1,1,0,0,1);
            }
        }
        public void addposy(int i){
            posy += i;
            behindy = posy+bmp.getHeight();
        }
        public void setposy(int i){
            posy = i-bmp.getHeight();
            behindy = posy+bmp.getHeight();
        }

        public void go(){

            if(isgoRight){
                goRight();
                chageDir =1;
            }
            else{
                goLeft();
                chageDir =2;
            }

        }
    }

    public class BackGround{

        Bitmap bmp1,bmp2,bmp3,cacheBitmap;
        Canvas cacheCanvas;

        int i = 0;
        BackGround(){
            bmp1 = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cloud1);
            bmp2 = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cloud2);
            bmp3 = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_cloud3);

            cacheBitmap = Bitmap.createBitmap(mWidth, mHeight, Bitmap.Config.ARGB_8888);
            //cacheCanvas = new Canvas();
            //cacheCanvas.setBitmap(cacheBitmap);
        }

        public Bitmap getBmp(){
            return cacheBitmap;
        }


    }
    public Bitmap zoomImage(Bitmap bgimage, double newWidth, double newHeight) {
        // 获取这个图片的宽和高
        float width = bgimage.getWidth();
        float height = bgimage.getHeight();
        // 创建操作图片用的matrix对象
        Matrix matrix = new Matrix();
        // 计算宽高缩放率
        float scaleWidth = ((float) newWidth) / width;
        float scaleHeight = ((float) newHeight) / height;
        // 缩放图片动作
        matrix.postScale(scaleWidth, scaleHeight);

        Bitmap bitmap = Bitmap.createBitmap(bgimage, 0, 0, (int)width, (int)height,matrix, true);

        return bitmap;
    }


}
