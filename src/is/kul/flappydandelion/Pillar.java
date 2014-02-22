/**
 * Copyright (C) 2013 Martin Varga <android@kul.is>
 */
package is.kul.flappydandelion;

import org.andengine.entity.Entity;
import org.andengine.entity.IEntity;
import org.andengine.entity.primitive.Rectangle;
import org.andengine.entity.sprite.Sprite;
import org.andengine.extension.physics.box2d.PhysicsFactory;
import org.andengine.extension.physics.box2d.PhysicsWorld;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.color.Color;

import com.badlogic.gdx.physics.box2d.Body;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;

public class Pillar extends Entity {
	private Sprite pillarUp;
	private Sprite pillarDown;
	
	private Body pillarUpBody;
	private Body pillarDownBody;
	private Body scoreSensor;
	
	final float shift = 88;
	
	public Pillar(float x, float y, TextureRegion reg, VertexBufferObjectManager vbom, PhysicsWorld physics) {
		super(x, y);
		pillarUp = new Sprite(0, shift, 96, 384, reg, vbom);
		pillarUp.setFlippedVertical(true);
		pillarUp.setAnchorCenterY(0);
		attachChild(pillarUp);
		
		pillarUpBody = PhysicsFactory.createBoxBody(physics, pillarUp, BodyType.StaticBody, Constants.WALL_FIXTURE);
		pillarUpBody.setUserData(Constants.BODY_WALL);
		
		pillarDown = new Sprite(0, -shift, 96, 384, reg, vbom);
		pillarDown.setAnchorCenterY(1);
		attachChild(pillarDown);	

		pillarDownBody = PhysicsFactory.createBoxBody(physics, pillarDown, BodyType.StaticBody, Constants.WALL_FIXTURE);
		pillarDownBody.setUserData(Constants.BODY_WALL);

		Rectangle r = new Rectangle(0, 0, 1, 9999, vbom); // just to make sure it's big
		r.setColor(Color.GREEN);
		r.setAlpha(0f);
		attachChild(r);
		
		scoreSensor = PhysicsFactory.createBoxBody(physics, r, BodyType.StaticBody, Constants.SENSOR_FIXTURE);
		scoreSensor.setUserData(Constants.BODY_SENSOR);

	}

	@Override
	public boolean collidesWith(IEntity pOtherEntity) {
		return pillarUp.collidesWith(pOtherEntity) || pillarDown.collidesWith(pOtherEntity);
	}

	public Body getPillarUpBody() {
		return pillarUpBody;
	}

	public Body getPillarDownBody() {
		return pillarDownBody;
	}

	public Body getScoreSensor() {
		return scoreSensor;
	}
	
	public float getPillarShift() {
		return pillarUp.getHeight() / 2 + shift;
	}

	@Override
	public float getWidth() {
		return pillarDown.getWidth();
	}
	
	
}
