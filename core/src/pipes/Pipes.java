package pipes;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.PolygonShape;
import com.badlogic.gdx.physics.box2d.World;

import java.util.Random;

import helpers.GameInfo;

public class Pipes {

    private World world;
    private Body body, body2, body3;
    private Sprite pipe1, pipe2;
    private final float DISTANCE_BETWEEN_PIPES = 420f;
    private Random random = new Random();
    private OrthographicCamera mainCamera;

    public Pipes(World world, float x, OrthographicCamera mainCamera) {
        this.world = world;
        this.mainCamera = mainCamera;
        createPipes(x, getRandomY());
    }

    void createPipes(float x, float y) {
        pipe1 = new Sprite(new Texture("Pipes/Pipe 1.png"));
        pipe2 = new Sprite(new Texture("Pipes/Pipe 1.png"));
        pipe2.flip(false, true);

        pipe1.setPosition(x, y + DISTANCE_BETWEEN_PIPES);
        pipe2.setPosition(x, y - DISTANCE_BETWEEN_PIPES);

        BodyDef bodyDef = new BodyDef();
        bodyDef.type = BodyDef.BodyType.KinematicBody;
        bodyDef.position.set(x / GameInfo.PPM, y / GameInfo.PPM);

        body = world.createBody(bodyDef);
        body.setFixedRotation(false);

        PolygonShape shape = new PolygonShape();
        shape.setAsBox(pipe1.getWidth() / 2f / GameInfo.PPM,
                pipe1.getHeight() / 2f / GameInfo.PPM,
                new Vector2(0, DISTANCE_BETWEEN_PIPES / GameInfo.PPM), 0);

        FixtureDef fixtureDef = new FixtureDef();
        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = GameInfo.PIPE;

        body.createFixture(fixtureDef).setUserData("Pipe");

        shape.setAsBox(pipe2.getWidth() / 2f / GameInfo.PPM,
                pipe2.getHeight() / 2f / GameInfo.PPM,
                new Vector2(0, -DISTANCE_BETWEEN_PIPES / GameInfo.PPM), 0);

        fixtureDef.shape = shape;
        body.createFixture(fixtureDef).setUserData("Pipe");

        shape.setAsBox(3 / GameInfo.PPM,
                (DISTANCE_BETWEEN_PIPES - pipe1.getHeight() / 2f) / GameInfo.PPM);

        fixtureDef.shape = shape;
        fixtureDef.filter.categoryBits = GameInfo.SCORE;
        fixtureDef.isSensor = true;

        body.createFixture(fixtureDef).setUserData("Score");

        shape.dispose();
    }

    public void drawPipes(SpriteBatch batch){
        batch.draw(pipe1, pipe1.getX() - pipe1.getWidth() / 2f,
                pipe1.getY() - pipe1.getHeight() / 2f);
        batch.draw(pipe2, pipe2.getX() - pipe2.getWidth() / 2f,
                pipe2.getY() - pipe2.getHeight() / 2f);
    }

    public void updatePipes(){
        pipe1.setPosition(body.getPosition().x * GameInfo.PPM,
                body.getPosition().y * GameInfo.PPM + DISTANCE_BETWEEN_PIPES);
        pipe2.setPosition(body.getPosition().x * GameInfo.PPM,
                body.getPosition().y * GameInfo.PPM - DISTANCE_BETWEEN_PIPES);
    }

    public void movePipes(boolean isMoving) {
        if (isMoving) {
            body.setLinearVelocity(-2, 0);
            if (pipe1.getX() + GameInfo.WIDTH / 2f + pipe1.getWidth() < mainCamera.position.x)
                body.setActive(false);
            } else {
                body.setLinearVelocity(0, 0);
            }
    }

    float getRandomY(){
        float max = GameInfo.HEIGHT / 2f + 150;
        float min = GameInfo.HEIGHT / 2f - 150;
        return random.nextFloat() * (max - min) + min;
    }

    public void disposeAll() {
        pipe1.getTexture().dispose();
        pipe2.getTexture().dispose();
    }

}
