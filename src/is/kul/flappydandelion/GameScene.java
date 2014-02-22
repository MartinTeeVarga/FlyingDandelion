/**
 * Copyright (C) 2013 Martin Varga <android@kul.is>
 */
package is.kul.flappydandelion;

import java.util.Iterator;
import java.util.LinkedList;

import org.andengine.engine.camera.hud.HUD;
import org.andengine.engine.handler.IUpdateHandler;
import org.andengine.entity.Entity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.scene.IOnSceneTouchListener;
import org.andengine.entity.scene.Scene;
import org.andengine.entity.scene.background.ParallaxBackground;
import org.andengine.entity.scene.background.ParallaxBackground.ParallaxEntity;
import org.andengine.entity.sprite.Sprite;
import org.andengine.entity.sprite.TiledSprite;
import org.andengine.entity.text.Text;
import org.andengine.extension.physics.box2d.PhysicsConnector;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.extension.physics.box2d.util.constants.PhysicsConstants;
import org.andengine.input.touch.TouchEvent;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.debug.Debug;


import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Manifold;


public class GameScene extends Scene implements IOnSceneTouchListener, ContactListener {
	
	private static final long TIME_TO_RESSURECTION = 200;
	PhysicsWorld physics;
	
	enum State {
		NEW, PAUSED, PLAY, DEAD, AFTERLIFE;
	}
	
	Text infoText;
	Text scoreText;
	
	TiledSprite dandelion;
	Body dandelionBody;
	ParallaxBackground pb;
	
	State state = State.NEW;
	State lastState = state;
	long timestamp = 0;
	
	private int score = 0;
	private boolean scored;
	
	LinkedList<Pillar> pillars = new LinkedList<Pillar>();
	
	protected ResourceManager res = ResourceManager.getInstance();
	protected VertexBufferObjectManager vbom = ResourceManager.getInstance().vbom;
	
	
	
	public GameScene() {
		physics = new PhysicsWorld(new Vector2(0, 0), true);
		physics.setContactListener(this);
		PillarFactory.getInstance().create(physics);
		
		createBackground();
		createActor();
		createBounds();
		
		createText();
		
		res.camera.setChaseEntity(dandelion);
		
		sortChildren();
		setOnSceneTouchListener(this);
		
		registerUpdateHandler(physics);
	}

	private void createText() {
		HUD hud = new HUD();
		res.camera.setHUD(hud);
		infoText = new Text(Constants.CW / 2 , Constants.CH / 2 - 200, res.font, "12345678901234567890", vbom);
		hud.attachChild(infoText);
		
		scoreText = new Text(Constants.CW / 2 , Constants.CH / 2 + 200, res.font, "12345678901234567890", vbom);
		hud.attachChild(scoreText);
		
		Sprite banner = new Sprite(0, Constants.CH, res.bannerRegion, vbom) {

			@Override
			public boolean onAreaTouched(TouchEvent pSceneTouchEvent,
					float pTouchAreaLocalX, float pTouchAreaLocalY) {
				if (pSceneTouchEvent.isActionDown()) {
					res.activity.gotoPlayStore();
				}
				return true;
			}
			
		};
		banner.setAnchorCenter(0, 1);
		hud.registerTouchArea(banner);
		hud.attachChild(banner);
		
	}

	private void createBounds() {
		float bigNumber = 999999; // i dunno, just a big number
		res.repeatingGroundRegion.setTextureWidth(bigNumber);
		Sprite ground = new Sprite(0, -100, res.repeatingGroundRegion, vbom);
		ground.setAnchorCenter(0, 0);
		ground.setZIndex(10);
		attachChild(ground);
		
		Body groundBody = PhysicsFactory.createBoxBody(
				physics, ground, BodyType.StaticBody, Constants.WALL_FIXTURE);
		groundBody.setUserData(Constants.BODY_WALL);
		
		// just to limit the movement at the top
		@SuppressWarnings("unused")
		Body ceillingBody = PhysicsFactory.createBoxBody(
				physics, bigNumber / 2, 820, bigNumber, 20, BodyType.StaticBody, Constants.CEILLING_FIXTURE);
	}

	private void createActor() {
		dandelion = new TiledSprite(200, 400, res.dandelionRegion, vbom);
		dandelion.setZIndex(999);
		dandelion.registerUpdateHandler(new IUpdateHandler() {

			@Override
			public void onUpdate(float pSecondsElapsed) {
				if (dandelionBody.getLinearVelocity().y > -0.01) {
					dandelion.setCurrentTileIndex(0);
				} else {
					dandelion.setCurrentTileIndex(1);
				}
			}

			@Override
			public void reset() { }
		});
		dandelionBody = PhysicsFactory.createCircleBody(
				physics, dandelion, BodyType.DynamicBody, Constants.DANDELION_FIXTURE);
		dandelionBody.setFixedRotation(true);
		dandelionBody.setUserData(Constants.BODY_ACTOR);
		physics.registerPhysicsConnector(new PhysicsConnector(dandelion, dandelionBody));
		attachChild(dandelion);
	}

	private void createBackground() {
		pb = new ParallaxBackground(0.75f, 0.83f, 0.95f);
		Entity clouds = new Rectangle(0, 0, 1000, 800, vbom);
		clouds.setAnchorCenter(0, 0);
		clouds.setAlpha(0f);
		clouds.attachChild(new Sprite(100, 500, res.cloudRegion, vbom));
		clouds.attachChild(new Sprite(300, 700, res.cloudRegion, vbom));
		
		clouds.attachChild(new Sprite(500, 600, res.cloudRegion, vbom));
		clouds.attachChild(new Sprite(800, 730, res.cloudRegion, vbom));
		
		ParallaxEntity pe = new ParallaxEntity(-0.2f, clouds);
		pb.attachParallaxEntity(pe);
		setBackground(pb);
	}

	@Override
	public void reset() {
		super.reset();
		physics.setGravity(new Vector2(0, 0));
		
		Iterator<Pillar> pi = pillars.iterator();
		while (pi.hasNext()) {
		   Pillar p = pi.next();
		   PillarFactory.getInstance().recycle(p);
		   pi.remove();
		}
		
		
		PillarFactory.getInstance().reset();
		
		dandelionBody.setTransform(200 / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT,
				400 / PhysicsConstants.PIXEL_TO_METER_RATIO_DEFAULT, 0);
		
		addPillar();
		addPillar();
		addPillar();
		
		score = 0;
		
		infoText.setText(res.activity.getString(R.string.tap_to_play));
		infoText.setVisible(true);
		
		scoreText.setText(res.activity.getString(R.string.hiscore) + res.activity.getHighScore());
		infoText.setVisible(true);
		
		sortChildren();	

		unregisterUpdateHandler(physics);
		physics.onUpdate(0);
		
		state = State.NEW;
	}

	private void addPillar() {
		Pillar p = PillarFactory.getInstance().next();
		pillars.add(p);
		attachIfNotAttached(p);
	}

	@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		Debug.e("IS RUN:" + res.engine.isRunning() + " ev: " + pSceneTouchEvent.getAction());
		if (pSceneTouchEvent.isActionDown()) {
			if (state == State.PAUSED) {
				if (lastState != State.NEW) {
					registerUpdateHandler(physics);
				}
				state = lastState;
				Debug.d("->" + state);
			} else if (state == State.NEW) {
				registerUpdateHandler(physics);
				state = State.PLAY;
				Debug.d("->PLAY");
				physics.setGravity(new Vector2(0, Constants.GRAVITY));
				dandelionBody.setLinearVelocity(new Vector2(Constants.SPEED_X, 0));
				scoreText.setText("0");
				infoText.setVisible(false);
			} else if (state == State.DEAD) {
				// don't touch the dead!
			} else if (state == State.AFTERLIFE) {
				reset();
				state = State.NEW;
				Debug.d("->NEW");
			} else {
				Vector2 v = dandelionBody.getLinearVelocity();
				v.x = Constants.SPEED_X;
				v.y = Constants.SPEED_Y;
				dandelionBody.setLinearVelocity(v);
				Debug.d("TAP!");
				res.sndFly.play();
			}
		}
		return false;
	}

	public void resume() {
		Debug.d("Game resumed");
	}

	public void pause() {
		unregisterUpdateHandler(physics);
		lastState = state;
		state = State.PAUSED;
	}


	
	@Override
	protected void onManagedUpdate(float pSecondsElapsed) {
		super.onManagedUpdate(pSecondsElapsed);
		pb.setParallaxValue(res.camera.getCenterX());
		if (scored) {
			addPillar();
			sortChildren();
			scored = false;
			score++;
			scoreText.setText(String.valueOf(score));
		}
		
		// if first pillar is out of the screen, delete it
		if (!pillars.isEmpty()) {
			Pillar fp = pillars.getFirst();
			if (fp.getX() + fp.getWidth() < res.camera.getXMin()) {
				PillarFactory.getInstance().recycle(fp);
				pillars.remove();
			}
		}
		
		if (state == State.DEAD && timestamp + TIME_TO_RESSURECTION < System.currentTimeMillis()) {
			state = State.AFTERLIFE;
			Debug.d("->AFTERLIFE");
		}
	}

	private void attachIfNotAttached(Pillar p) {
		if (!p.hasParent()) {
			attachChild(p);
		}
		
	}

	@Override
	public void beginContact(Contact contact) {
		if (Constants.BODY_WALL.equals(contact.getFixtureA().getBody().getUserData()) ||
				Constants.BODY_WALL.equals(contact.getFixtureB().getBody().getUserData())) {
			state = State.DEAD;
			Debug.d("->DEAD");
			res.sndFail.play();
			if (score > res.activity.getHighScore()) {
				res.activity.setHighScore(score);
			}
			timestamp = System.currentTimeMillis();
			dandelionBody.setLinearVelocity(0, 0);
			for (Pillar p : pillars) {
				p.getPillarUpBody().setActive(false);
				p.getPillarDownBody().setActive(false);
			}			
		}
		
	}

	@Override
	public void endContact(Contact contact) {
		if (Constants.BODY_SENSOR.equals(contact.getFixtureA().getBody().getUserData()) ||
				Constants.BODY_SENSOR.equals(contact.getFixtureB().getBody().getUserData())) {
			scored = true;
		}
		
	}

	@Override
	public void preSolve(Contact contact, Manifold oldManifold) {
	}

	@Override
	public void postSolve(Contact contact, ContactImpulse impulse) {
	}
	
	
	
}
