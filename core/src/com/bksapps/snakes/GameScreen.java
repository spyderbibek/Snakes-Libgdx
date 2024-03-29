package com.bksapps.snakes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.ScreenAdapter;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import java.awt.Color;

import static com.bksapps.snakes.GameScreen.STATE.PLAYING;

/**
 * Created by TERRORMASTER on 1/30/2018.
 */

public class GameScreen extends ScreenAdapter {

    public enum STATE {
        PLAYING, GAMEOVER
    }

    private STATE state= PLAYING;
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private Texture snakeHead;
    private Texture snakeBody;
    private Texture apple;
    private BitmapFont bitmapFont;
    private GlyphLayout layout;
    private Array<BodyPart>bodyParts=new Array<BodyPart>();
    private Camera camera;
    private Viewport viewPort;

    //Constant Variables
    private static final float WORLD_WIDTH=640;
    private static final float WORLD_HEIGHT=480;
    private static final int GRID_CELL = 32;
    private static final float MOVE_TIME=0.2f;
    private static final int RIGHT=0;
    private static final int LEFT=1;
    private static final int UP=2;
    private static final int DOWN=3;
    private static final int SNAKE_MOVEMENT=32;
    private static final String GAME_OVER_TEXT = "Game Over!";
    private static final int POINTS_PER_APPLE = 20;

    //Variables
    private float timer=MOVE_TIME;
    private float snakeX=0, snakeY=0;
    private int SNAKE_DIRECTION=RIGHT;
    private boolean appleAvailable=false;
    private boolean directionSet=false;
    private float appleX,appleY;
    private float snakeXBeforeUpdate = 0, snakeYBeforeUpdate = 0;
    private int score=0;

    @Override
    public void show() {
        camera=new OrthographicCamera(Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
        camera.position.set(WORLD_WIDTH/2, WORLD_HEIGHT/2,0);
        camera.update();
        viewPort=new FitViewport(WORLD_WIDTH, WORLD_HEIGHT, camera);
        batch= new SpriteBatch();
        shapeRenderer=new ShapeRenderer();
        snakeHead=new Texture(Gdx.files.internal("snakehead.png"));
        snakeBody= new Texture(Gdx.files.internal("snakebody.png"));
        apple=new Texture(Gdx.files.internal("apple.png"));
        bitmapFont=new BitmapFont();
        layout=new GlyphLayout();


    }

    @Override
    public void render(float delta) {
        switch (state){
            case PLAYING:{
                queryInput();
                updateSnake(delta);
                checkAppleCollision();
                checkAndPlaceApple();
            }
            break;
            case GAMEOVER:{
                checkForRestart();
            }
            break;
        }
        clearScreen();
        //drawGrid();
        draw();
        }


    private void updateSnake(float delta) {
        timer -= delta;
        if (timer <= 0) {
            timer = MOVE_TIME;
            moveSnake();
            checkForOutOfBounds();
            updateBodyPartsPosition();
            checkSnakeBodyCollision();
            directionSet = false;
        }
    }

    private void drawGrid(){
        shapeRenderer.setProjectionMatrix(camera.projection);
        shapeRenderer.setTransformMatrix(camera.view);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for(int x=0;x<viewPort.getWorldWidth();x+=GRID_CELL){
            for(int y=0;y<viewPort.getWorldHeight();y+=GRID_CELL){
                shapeRenderer.rect(x,y,GRID_CELL,GRID_CELL);
            }
        }
        shapeRenderer.end();

    }

    private void updateIfNotOppositeDirection(int newSnakeDirection, int oppositeDirection) {
        if ((SNAKE_DIRECTION != oppositeDirection) || bodyParts.size==0){
            SNAKE_DIRECTION = newSnakeDirection;
        }
    }

    private void updateDirection(int newSnakeDirection){
        if(!directionSet && SNAKE_DIRECTION!=newSnakeDirection){
            directionSet=true;
            switch (newSnakeDirection){
                case LEFT:{
                    updateIfNotOppositeDirection(newSnakeDirection, RIGHT);
                }
                break;
                case RIGHT:{
                    updateIfNotOppositeDirection(newSnakeDirection, LEFT);
                }
                break;
                case UP:{
                    updateIfNotOppositeDirection(newSnakeDirection, DOWN);
                }
                break;
                case DOWN:{
                    updateIfNotOppositeDirection(newSnakeDirection, UP);
                }
                break;
            }
        }
    }

    private void checkSnakeBodyCollision(){
        for(BodyPart bodyPart:bodyParts){
            if(bodyPart.x==snakeX && bodyPart.y==snakeY){
                state=STATE.GAMEOVER;
            }
        }
    }

    private void checkForOutOfBounds() {
        if (snakeX >= viewPort.getWorldWidth()) {
            snakeX = 0;
        }
        if (snakeX < 0) {
            snakeX = viewPort.getWorldWidth()-SNAKE_MOVEMENT;
        }
        if(snakeY>=viewPort.getWorldHeight()){
            snakeY=0;
        }
        if(snakeY< 0){
            snakeY=viewPort.getWorldHeight()-SNAKE_MOVEMENT;
        }
    }

    private void moveSnake(){
        snakeXBeforeUpdate = snakeX;
        snakeYBeforeUpdate = snakeY;
        switch (SNAKE_DIRECTION){
            case RIGHT:{
                snakeX+=SNAKE_MOVEMENT;
                return;
            }case LEFT:{
                snakeX-=SNAKE_MOVEMENT;
                return;
            } case UP:{
                snakeY+=SNAKE_MOVEMENT;
                return;
            }case DOWN:{
                snakeY-=SNAKE_MOVEMENT;
                return;
            }
        }
    }

    private void updateBodyPartsPosition(){
        if(bodyParts.size>0){
            BodyPart bodyPart= bodyParts.removeIndex(0);
            bodyPart.updateBodyPosition(snakeXBeforeUpdate,snakeYBeforeUpdate);
            bodyParts.add(bodyPart);
        }
    }

    private void queryInput(){
        boolean lPressed=Gdx.input.isKeyPressed(Input.Keys.LEFT);
        boolean rPressed=Gdx.input.isKeyPressed(Input.Keys.RIGHT);
        boolean uPressed=Gdx.input.isKeyPressed(Input.Keys.UP);
        boolean dPressed=Gdx.input.isKeyPressed(Input.Keys.DOWN);

        if(lPressed){
            updateDirection(LEFT);
        }
        if(rPressed){
            updateDirection(RIGHT);
        }
        if(uPressed){
            updateDirection(UP);
        }
        if(dPressed){
            updateDirection(DOWN);
        }
    }

    private void clearScreen(){
        Gdx.gl.glClearColor(Color.BLACK.getRed(), Color.BLACK.getGreen(),
                Color.BLACK.getBlue(), Color.BLACK.getAlpha());
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
    }

    private void draw(){
        batch.setProjectionMatrix(camera.projection);
        batch.setTransformMatrix(camera.view);
        batch.begin();
        batch.draw(snakeHead,snakeX,snakeY);
        for(BodyPart bodyPart:bodyParts){
            bodyPart.draw(batch);
        }
        if(appleAvailable){
            batch.draw(apple,appleX,appleY);
        }
        drawScore();

        if(state==STATE.GAMEOVER){
            layout.setText(bitmapFont,GAME_OVER_TEXT);
            bitmapFont.draw(batch,layout, (viewPort.getWorldWidth()-layout.width)/2, (viewPort.getWorldHeight()-layout.height)/2);
            layout.setText(bitmapFont, "Press <SPACE> to restart the game.");
            bitmapFont.draw(batch,layout, (viewPort.getWorldWidth()-layout.width)/2, (viewPort.getWorldHeight()-layout.height)/2-20);
        }
        batch.end();
    }

    private void drawScore(){
        if(state==STATE.PLAYING){
            layout.setText(bitmapFont,String.valueOf(score));
            bitmapFont.draw(batch, layout, (viewPort.getWorldWidth()-layout.width)/2, (viewPort.getWorldHeight()-layout.height));
        }
    }

    private void addToScore() {
        score += POINTS_PER_APPLE;
    }

    private void checkForRestart(){
        if(Gdx.input.isKeyPressed(Input.Keys.SPACE)){
            doRestart();
        }
    }

    private void doRestart(){
        state=STATE.PLAYING;
        bodyParts.clear();
        SNAKE_DIRECTION=RIGHT;
        directionSet=false;
        timer=MOVE_TIME;
        snakeX=0;
        snakeY=0;
        snakeXBeforeUpdate=0;
        snakeYBeforeUpdate=0;
        score=0;
        appleAvailable=false;
    }

    private void checkAndPlaceApple(){
        if(!appleAvailable){
            do{
                appleX= MathUtils.random((int)(viewPort.getWorldWidth()/SNAKE_MOVEMENT)-1)*SNAKE_MOVEMENT;
                appleY=MathUtils.random((int)(viewPort.getWorldHeight()/SNAKE_MOVEMENT)-1)*SNAKE_MOVEMENT;
                appleAvailable=true;
            }while(appleX==snakeX && appleY==snakeY);
        }
    }

    private void checkAppleCollision(){
        if(appleAvailable && appleX==snakeX && appleY==snakeY){
            BodyPart bodyPart = new BodyPart(snakeBody);
            bodyPart.updateBodyPosition(snakeX, snakeY);
            bodyParts.insert(0,bodyPart);
            addToScore();
            appleAvailable=false;
        }
    }

    public class BodyPart{
        private float x,y;
        private Texture texture;

        public BodyPart(Texture texture){
            this.texture=texture;
        }

        public void updateBodyPosition(float x, float y){
            this.x=x;
            this.y=y;
        }

        public void draw(Batch batch){
            if(!(x==snakeX && y==snakeY)){
                batch.draw(texture,x,y);
            }
        }
    }
}
