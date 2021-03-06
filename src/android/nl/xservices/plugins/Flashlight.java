package nl.xservices.plugins;

import android.hardware.Camera;
import android.util.Log;
import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONArray;
import org.json.JSONException;

public class Flashlight extends CordovaPlugin {

  private static final String ACTION_AVAILABLE = "available";
  private static final String ACTION_SWITCH_ON = "switchOn";
  private static final String ACTION_SWITCH_OFF = "switchOff";

  private Camera mCamera;

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    Log.d("Flashlight", "Plugin Called: " + action);
    if (action.equals(ACTION_SWITCH_ON)) {
      mCamera = Camera.open();
      toggleTorch(true, callbackContext);
      return true;
    } else if (action.equals(ACTION_SWITCH_OFF)) {
      toggleTorch(false, callbackContext);
      // we need to release the camera, so other apps can use it
      new Thread(new Runnable() {
        public void run() {
          mCamera.setPreviewCallback(null);
          mCamera.stopPreview();
          mCamera.release();
        }
      }).start();
      return true;
    } else if (action.equals(ACTION_AVAILABLE)) {
      callbackContext.success(isCapable() ? 1 : 0);
      return true;
    } else {
      callbackContext.error("flashlight." + action + " is not a supported function.");
      return false;
    }
  }

  protected boolean isCapable() {
    return mCamera != null &&
        mCamera.getParameters().getSupportedFlashModes() != null &&
        mCamera.getParameters().getSupportedFlashModes().contains(Camera.Parameters.FLASH_MODE_TORCH);
  }

  protected void toggleTorch(boolean switchOn, CallbackContext callbackContext) {
    final Camera.Parameters mParameters = mCamera.getParameters();
    if (isCapable()) {
      mParameters.setFlashMode(switchOn ? Camera.Parameters.FLASH_MODE_TORCH : Camera.Parameters.FLASH_MODE_OFF);
      mCamera.setParameters(mParameters);
      callbackContext.success();
    } else {
      callbackContext.error("Device is not capable of using the flashlight. Please test with flashlight.available()");
    }
  }
}