package scenes;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Box2DDebugRenderer;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.badlogic.gdx.physics.box2d.World;
import com.badlogic.gdx.scenes.scene2d.actions.Actions;
import com.badlogic.gdx.scenes.scene2d.actions.RunnableAction;
import com.badlogic.gdx.scenes.scene2d.actions.SequenceAction;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.StretchViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import com.lefthander.flappybird.GameMain;

import bird.Bird;
import ground.GroundBody;
import helpers.GameInfo;
import hud.UIHud;
import pipes.Pipes;

public class Gameplay implements Screen, ContactListener {
    private World world;
    private GameMain game;
    private OrthographicCamera mainCamera;
    private Viewport gameViewport;
//    private OrthographicCamera debugCamera;
//    private Box2DDebugRenderer debugRenderer;
    private Array<Sprite> bgs = new Array<Sprite>();
    private Array<Sprite> grounds = new Array<Sprite>();
    private Bird bird;
    private GroundBody groundBody;
    private UIHud hud;
    private boolean firstTouch = false;
    private Array<Pipes> pipesArray = new Array<Pipes>();
    private Sound scoreSound, birdDiedSound, birdFlapSound;

    public Gameplay(GameMain game) {
        this.game = game;

        mainCamera = new OrthographicCamera(GameInfo.WIDTH, GameInfo.HEIGHT);
        mainCamera.position.set(GameInfo.WIDTH / 2f, GameInfo.HEIGHT / 2f, 0);

        gameViewport = new StretchViewport(GameInfo.WIDTH, GameInfo.HEIGHT, mainCamera);

     /*  debugCamera = new OrthographicCamera();
       debugCamera.setToOrtho(false, GameInfo.WIDTH / GameInfo.PPM, GameInfo.HEIGHT / GameInfo.PPM);
        debugCamera.position.set(GameInfo.WIDTH / 2f, GameInfo.HEIGHT / 2f, 0);
        debugRenderer = new Box2DDebugRenderer();*/
        hud = new UIHud(game);
        createBgsAndGrounds();
        world = new World(new Vector2(0, -9.8f), true);
        world.setContactListener(this);
        bird = new Bird(world, GameInfo.WIDTH / 2f - 80, GameInfo.HEIGHT / 2f);
        groundBody = new GroundBody(world, grounds.get(0));

        scoreSound = Gdx.audio.newSound(Gdx.files.internal("Sounds/Score.mp3"));
        birdDiedSound = Gdx.audio.newSound(Gdx.files.internal("Sounds/Dead.mp3"));
        birdFlapSound = Gdx.audio.newSound(Gdx.files.internal("Sounds/Fly.mp3"));
    }

    void checkForFirstTouch(){
        if(!firstTouch && Gdx.input.justTouched()) {
            firstTouch = true;
            bird.activateBird();
            createPipes();
        }

    }

    void update(){
        checkForFirstTouch();
        if(bird.getAlive()){
           moveDecoration(bgs);
           moveDecoration(grounds);
           birdFlap();
           updatePipes();
        }
    }

    void birdFlap(){
        if(Gdx.input.justTouched()){
            birdFlapSound.play();
            bird.birdFlap();
        }
    }

    void birdDied() {
        bird.setAlive(false);
        bird.birdDied();
        stopPipes();
        hud.getStage().clear();
        hud.showScore();
        Preferences prefs = Gdx.app.getPreferences("Data");
        int highscore = prefs.getInteger("Score");
        if(highscore < hud.getScore()) {
            prefs.putInteger("Score", hud.getScore());
            prefs.flush();
        }

        hud.createButtons();
        Gdx.input.setInputProcessor(hud.getStage());
    }

    void createBgsAndGrounds(){
        for(int i = 0; i < 3; i++) {
            Sprite bg = new Sprite(new Texture("Background/Day.jpg"));
            bg.setPosition(i * bg.getWidth(), 0);
            bgs.add(bg);
            Sprite ground = new Sprite(new Texture("Background/Ground.png"));
            ground.setPosition(i * ground.getWidth(), -ground.getHeight() / 2f - 55);
            grounds.add(ground);
        }
    }

    void createPipes(){
        RunnableAction run = new RunnableAction();
        run.setRunnable(new Runnable() {
            @Override
            public void run() {
                Pipes p = new Pipes(world, GameInfo.WIDTH + 100, mainCamera);
                pipesArray.add(p);
            }
        });
        SequenceAction sa = new SequenceAction();
        sa.addAction(Actions.delay(1.2f));
        sa.addAction(run);
        hud.getStage().addAction(Actions.forever(sa));
    }

    void drawDecoration (Array<Sprite> arr, SpriteBatch batch) {
        for(Sprite e : arr)
            batch.draw(e, e.getX(), e.getY());
    }

    void moveDecoration(Array<Sprite> arr){
        for(Sprite a : arr) {
            float x1 = a.getX() - 4f;
            a.setPosition(x1, a.getY());

            if(a.getX() + GameInfo.WIDTH + a.getWidth() / 2f < mainCamera.position.x){
                float x2 = a.getX() + a.getWidth() * bgs.size;
                a.setPosition(x2, a.getY());
            }
        }
    }

    void drawPipes(SpriteBatch batch){
        for(Pipes pipe: pipesArray)
            pipe.drawPipes(batch);
    }

    void updatePipes() {
        for (Pipes pipe : pipesArray) {
            pipe.updatePipes();
            pipe.movePipes(true);
        }
    }

    void stopPipes() {
        for(Pipes pipe : pipesArray)
            pipe.movePipes(false);
    }

    @Override
    public void show() {    }

    @Override
    public void render(float delta) {
        update();
        Gdx.gl.glClearColor(1, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        game.getBatch().begin();
        drawDecoration(bgs, game.getBatch());
        drawPipes(game.getBatch());
        drawDecoration(grounds, game.getBatch());
        bird.drawBird(game.getBatch());
        game.getBatch().end();
//        debugRenderer.render(world, debugCamera.combined);
        game.getBatch().setProjectionMatrix(hud.getStage().getCamera().combined);
        hud.getStage().draw();
        hud.getStage().act();
        bird.updateBird();
        world.step(Gdx.graphics.getDeltaTime(), 6, 2);

    }

    @Override
    public void resize(int width, int height) {
        gameViewport.update(width, height);
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public void hide() {

    }

    @Override
    public void dispose() {
        scoreSound.dispose();
        birdFlapSound.dispose();
        birdDiedSound.dispose();
        world.dispose();
        bird.getTexture().dispose();
        for(int i = 0; i < bgs.size; i++){
            bgs.get(i).getTexture().dispose();
            grounds.get(i).getTexture().dispose();
        }
        for(Pipes pipe : pipesArray) {
            pipe.disposeAll();
        }
//        debugRenderer.dispose();
        game.dispose();
        hud.getStage().dispose();
    }

    @Override
    public void beginContact(Contact contact) {
        Fixture body1, body2;
        if(contact.getFixtureA().getUserData() == "Bird") {
            body1 = contact.getFixtureA();
            body2 = contact.getFixtureB();
        } else {
            body1 = contact.getFixtureB();
            body2 = contact.getFixtureA();
        }

        if(body1.getUserData() == "Bird" && body2.getUserData() == "Score") {
            scoreSound.play();
            hud.incrementScore();
        } else {
            if(bird.getAlive()) {
                birdDiedSound.play();
                birdDied();
            }
        }
    }

    @Override
    public void endContact(Contact contact) {    }

    @Override
    public void preSolve(Contact contact, Manifold oldManifold) {    }

    @Override
    public void postSolve(Contact contact, ContactImpulse impulse) {    }
}
