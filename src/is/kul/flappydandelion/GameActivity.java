package is.kul.flappydandelion;

import java.io.IOException;

import org.andengine.engine.Engine;
import org.andengine.engine.LimitedFPSEngine;
import org.andengine.engine.camera.Camera;
import org.andengine.engine.options.EngineOptions;
import org.andengine.engine.options.ScreenOrientation;
import org.andengine.engine.options.WakeLockOptions;
import org.andengine.engine.options.resolutionpolicy.FillResolutionPolicy;
import org.andengine.engine.options.resolutionpolicy.IResolutionPolicy;
import org.andengine.entity.scene.Scene;
import org.andengine.ui.activity.BaseGameActivity;
import org.andengine.util.debug.Debug;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;

public class GameActivity extends BaseGameActivity {

	private Camera camera;
	
	private GameScene gameScene;

	
	SharedPreferences prefs;

	@Override
	public Engine onCreateEngine(EngineOptions pEngineOptions) {
		Engine engine = new LimitedFPSEngine(pEngineOptions, Constants.FPS_LIMIT);
		return engine;
	}

	public void setHighScore(int score) {
    	SharedPreferences.Editor settingsEditor = prefs.edit();
    	settingsEditor.putInt(Constants.KEY_HISCORE, score);
    	settingsEditor.commit();
	}
	
	public int getHighScore() {
		return prefs.getInt(Constants.KEY_HISCORE, 0);		
	}	
	
	@Override
	public EngineOptions onCreateEngineOptions() {
		
		prefs = PreferenceManager.getDefaultSharedPreferences(this);
		camera = new FollowCamera(0, 0, Constants.CW, Constants.CH);
		IResolutionPolicy resolutionPolicy = new FillResolutionPolicy();
		EngineOptions engineOptions = new EngineOptions(true,
				ScreenOrientation.PORTRAIT_FIXED, resolutionPolicy, camera);
		engineOptions.getAudioOptions().setNeedsMusic(true).setNeedsSound(true);
		engineOptions.setWakeLockOptions(WakeLockOptions.SCREEN_ON);
		return engineOptions;
	}

	@Override
	public void onCreateResources(
			OnCreateResourcesCallback pOnCreateResourcesCallback)
			throws IOException {
		ResourceManager.getInstance().create(this, getEngine(), camera, getVertexBufferObjectManager());
		ResourceManager.getInstance().loadFont();
		ResourceManager.getInstance().loadGameResources();
		pOnCreateResourcesCallback.onCreateResourcesFinished();
	}

	@Override
	public void onCreateScene(OnCreateSceneCallback pOnCreateSceneCallback)
			throws IOException {
		gameScene = new GameScene();
		pOnCreateSceneCallback.onCreateSceneFinished(gameScene);

	}

	@Override
	public void onPopulateScene(Scene pScene,
			OnPopulateSceneCallback pOnPopulateSceneCallback)
			throws IOException {
		pScene.reset();
		pOnPopulateSceneCallback.onPopulateSceneFinished();

	}

	@Override
	public synchronized void onResumeGame() {
		super.onResumeGame();
		gameScene.resume();
	}

	@Override
	public synchronized void onPauseGame() {
		super.onPauseGame();
		gameScene.pause();
	}

	public void gotoPlayStore() {
		try {
			Intent i = new Intent(Intent.ACTION_VIEW, 
					Uri.parse("market://details?id=" + getString(R.string.google_play_app_id)));
			ResourceManager.getInstance().activity.startActivity(i);

		} catch (Exception ex) {
			Debug.w("Google Play Store not installed");
			Intent i = new Intent(Intent.ACTION_VIEW, 
					Uri.parse("http://play.google.com/store/apps/details?id=" + getString(R.string.google_play_app_id)));
			ResourceManager.getInstance().activity.startActivity(i);
		}
		
	}

	
}
