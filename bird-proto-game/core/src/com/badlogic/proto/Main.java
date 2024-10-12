package com.badlogic.proto;

// import necessary classes from framework
import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Music;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;

public class Main implements ApplicationListener {
	// declare textures, sounds, and other global variables
    Texture backgroundTexture;
    Texture bucketTexture;
    Texture dropTexture;
    Sound dropSound;
    Music music;
    SpriteBatch spriteBatch;
    FitViewport viewport;
    Sprite bucketSprite;
    Vector2 touchPos;
    Array<Sprite> dropSprites; // array to hold falling food
    float dropTimer; // timer to contro droplet creation
    Rectangle bucketRectangle; // rect for player collision detection
    Rectangle dropRectangle; // rect for drop collision detection

    @Override
    public void create() {
    	// loading textures, and sounds
        backgroundTexture = new Texture("background2.png");
        bucketTexture = new Texture("birdartpixel.png");
        dropTexture = new Texture("foodpixel.png");
        dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.mp3"));
        music = Gdx.audio.newMusic(Gdx.files.internal("music.mp3"));
        // init spriteBatch and viewPort
        spriteBatch = new SpriteBatch();
        viewport = new FitViewport(10, 10);
        // create and config bucket sprite
        bucketSprite = new Sprite(bucketTexture);
        bucketSprite.setSize(1, 1);
        // init touch pos and drop sprites away
        touchPos = new Vector2();
        dropSprites = new Array<>();
        // init rectangles for collision detection
        bucketRectangle = new Rectangle();
        dropRectangle = new Rectangle();
        // config and play background music
        music.setLooping(true);
        music.setVolume(.5f);
        music.play();
    }

    @Override
    public void resize(int width, int height) {
    	// update viewport when window is resized
        viewport.update(width, height, true);
    }

    @Override
    public void render() {
    	// call methods for input handling, game logic, and drawing
        input();
        logic();
        draw();
    }

    private void input() {
        float speed = 4f; // speed of player movement
        float delta = Gdx.graphics.getDeltaTime(); // time since last frame
        
        // handle keyboard input for moving player
        if (Gdx.input.isKeyPressed(Input.Keys.RIGHT)) {
            bucketSprite.translateX(speed * delta); // move right
        } else if (Gdx.input.isKeyPressed(Input.Keys.LEFT)) {
            bucketSprite.translateX(-speed * delta); // move left
        }
        
        // handle touch input for moving the player
        if (Gdx.input.isTouched()) {
            touchPos.set(Gdx.input.getX(), Gdx.input.getY()); // get touch pos
            viewport.unproject(touchPos); // convert to world coord
            bucketSprite.setCenterX(touchPos.x); // move player to touch pos
        }
    }

    private void logic() {
    	// get world dimensions and player size
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        float bucketWidth = bucketSprite.getWidth();
        float bucketHeight = bucketSprite.getHeight();
        
        // clamp player pos within world boundaries
        // this prevents the player from moving outside of screen
        bucketSprite.setX(MathUtils.clamp(bucketSprite.getX(), 0, worldWidth - bucketWidth));
        
        // update player rectangle for collision detection
        float delta = Gdx.graphics.getDeltaTime();
        bucketRectangle.set(bucketSprite.getX(), bucketSprite.getY(), bucketWidth, bucketHeight);
        
        // update the pos of falling droplets(food)
        for (int i = dropSprites.size - 1; i >= 0; i--) {
            Sprite dropSprite = dropSprites.get(i);
            float dropWidth = dropSprite.getWidth();
            float dropHeight = dropSprite.getHeight();

            dropSprite.translateY(-2f * delta); // move droplets down
            dropRectangle.set(dropSprite.getX(), dropSprite.getY(), dropWidth, dropHeight);
            
            // remove food if it goes off-screen or if it collides with player
            if (dropSprite.getY() < -dropHeight) dropSprites.removeIndex(i); // remove food if it falls off the screen
            else if (bucketRectangle.overlaps(dropRectangle)) { 
                dropSprites.removeIndex(i);
                dropSound.play();
                System.out.println("Collected food"); // for dubugging purposes
            }
        }
       
        // incrementing drop timer
        dropTimer += delta;
        // create a new dropelet if timer exceeds 1 second
        if (dropTimer > 1f) {
            dropTimer = 0;
            createDroplet();
        }
    }

    private void draw() {
    	// clear screen with a black color
        ScreenUtils.clear(Color.BLACK);
        // apply viewport to the camera
        viewport.apply();
        // set projection matrix for the sprite batch
        spriteBatch.setProjectionMatrix(viewport.getCamera().combined);
        
        // begin by drawing sprites
        spriteBatch.begin();
        
        // get world dimensions
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        
        // draw background
        spriteBatch.draw(backgroundTexture, 0, 0, worldWidth, worldHeight);
        // draw player sprite
        bucketSprite.draw(spriteBatch);
        
        // draw all food sprites
        for (Sprite dropSprite : dropSprites) {
            dropSprite.draw(spriteBatch);
        }
        
        // end drawing sprites
        spriteBatch.end();
    }

    private void createDroplet() {
    	// set size of droplets
        float dropWidth = 1;
        float dropHeight = 1;
        // get world dimensions
        float worldWidth = viewport.getWorldWidth();
        float worldHeight = viewport.getWorldHeight();
        
        // create a new droplet sprite
        Sprite dropSprite = new Sprite(dropTexture);
        // set size of droplet sprite
        dropSprite.setSize(dropWidth, dropHeight);
        // set pos of droplet sprite to a random x-coord and y-coord
        dropSprite.setX(MathUtils.random(0f, worldWidth - dropWidth));
        dropSprite.setY(worldHeight);
        // add droplet food sprite to the array of droplets
        dropSprites.add(dropSprite);
    }

    @Override
    public void pause() {
    	// method when game is paused
        
    }

    @Override
    public void resume() {
    	// method when game is resumed
        
    }

    @Override
    public void dispose() {
        // method when game is disposed
    }
}
