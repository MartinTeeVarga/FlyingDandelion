/**
 * Copyright (C) 2013 Martin Varga <android@kul.is>
 */
package is.kul.flappydandelion;

import org.andengine.engine.camera.Camera;
import org.andengine.entity.IEntity;
import org.andengine.util.Constants;

public class FollowCamera extends Camera {

	private IEntity mChaseEntity;

	public FollowCamera(float pX, float pY, float pWidth, float pHeight) {
		super(pX, pY, pWidth, pHeight);
	}
	
	public void setChaseEntity(final IEntity pChaseEntity) {
		this.mChaseEntity = pChaseEntity;
	}

	@Override
	public void updateChaseEntity() {
		if (this.mChaseEntity != null) {
			final float[] centerCoordinates = this.mChaseEntity.getSceneCenterCoordinates();
			this.setCenter(centerCoordinates[Constants.VERTEX_INDEX_X] + 150, this.getCenterY());
		}
	}
	
	

}
