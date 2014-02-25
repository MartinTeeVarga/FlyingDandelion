/**
 * Copyright (C) 2013 Martin Varga <android@kul.is>
 */
package is.kul.flappydandelion;


import org.andengine.audio.sound.Sound;
import org.andengine.audio.sound.SoundFactory;
import org.andengine.engine.Engine;
import org.andengine.engine.camera.Camera;
import org.andengine.opengl.font.Font;
import org.andengine.opengl.font.FontFactory;
import org.andengine.opengl.texture.TextureOptions;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureAtlasBuilder;
import org.andengine.opengl.texture.atlas.buildable.builder.ITextureAtlasBuilder.TextureAtlasBuilderException;
import org.andengine.opengl.texture.bitmap.BitmapTextureFormat;
import org.andengine.opengl.texture.region.TextureRegion;
import org.andengine.opengl.texture.region.TiledTextureRegion;
import org.andengine.opengl.vbo.VertexBufferObjectManager;
import org.andengine.util.adt.color.Color;

import android.graphics.Typeface;

public class ResourceManager {
	private static final ResourceManager INSTANCE = new ResourceManager();

	//font
	public Font font;
	
	//common objects
	public GameActivity activity;
	public Engine engine;
	public Camera camera;
	public VertexBufferObjectManager vbom;
	
	//gfx
	private BitmapTextureAtlas repeatingGroundAtlas;
	
	public TextureRegion repeatingGroundRegion;
	
	private BuildableBitmapTextureAtlas gameObjectsAtlas;
	
	public TextureRegion cloudRegion;
	public TiledTextureRegion dandelionRegion;
	public TextureRegion pillarRegion;
	
	public TextureRegion bannerRegion;
	
	//sfx
	public Sound sndFly;
	public Sound sndFail;
	
	private ResourceManager() {}
	
	public static ResourceManager getInstance() {
		return INSTANCE;
	}
	
	public void create(GameActivity activity, Engine engine, Camera camera, VertexBufferObjectManager vbom) {
		this.activity = activity;
		this.engine = engine;
		this.camera = camera;
		this.vbom = vbom;
	}
	
	public void loadFont() {
		font = FontFactory.createStroke(activity.getFontManager(), activity.getTextureManager(), 256, 256, Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD), 50,
				true, Color.WHITE_ABGR_PACKED_INT, 2, Color.BLACK_ABGR_PACKED_INT);
		font.load();
	}

	public void unloadFont() {
		font.unload();
	}
	
	//splash
	public void loadGameResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");	
		
		repeatingGroundAtlas = new BitmapTextureAtlas(activity.getTextureManager(), 256, 256, TextureOptions.REPEATING_BILINEAR_PREMULTIPLYALPHA);
		repeatingGroundRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(repeatingGroundAtlas, activity, "ground.png", 0, 0);
		repeatingGroundAtlas.load();
		
		gameObjectsAtlas = new BuildableBitmapTextureAtlas(activity.getTextureManager(), 
					1024, 1024, BitmapTextureFormat.RGBA_8888, TextureOptions.BILINEAR);
			
		cloudRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
				gameObjectsAtlas, activity.getAssets(), "cloud.png");
			
		pillarRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
				gameObjectsAtlas, activity.getAssets(), "pillar.png");
			
		dandelionRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(
				gameObjectsAtlas, activity.getAssets(), "dandelion.png", 2, 1);
		
		bannerRegion = BitmapTextureAtlasTextureRegionFactory.createFromAsset(
				gameObjectsAtlas, activity.getAssets(), "banner.png");
		
		try {
			gameObjectsAtlas.build(new BlackPawnTextureAtlasBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(2, 0, 2));
			gameObjectsAtlas.load();
			
		} catch (final TextureAtlasBuilderException e) {
			throw new RuntimeException("Error while loading Splash textures", e);
		}		
		
		try {
			sndFly = SoundFactory.createSoundFromAsset(activity.getEngine().getSoundManager(), activity, "sfx/fly.wav");
			sndFail = SoundFactory.createSoundFromAsset(activity.getEngine().getSoundManager(), activity, "sfx/fail.wav");
		} catch (Exception e) {
			throw new RuntimeException("Error while loading sounds", e);
		} 
	}		
}
