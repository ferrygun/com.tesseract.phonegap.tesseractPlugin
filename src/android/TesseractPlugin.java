package com.tesseract.phonegap;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import com.googlecode.tesseract.android.TessBaseAPI;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.Environment;
import android.util.Log;

import android.content.Context;

public class TesseractPlugin extends CordovaPlugin {
	public static final String DATA_PATH = Environment.getExternalStorageDirectory().toString() + "/OCRFolder/";
	public static final String _path = DATA_PATH + "/ocr.jpg";
	private static final String lang = "eng";
	private static final String TAG = "OCRFolder";

	@Override
	public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {

		try {
			String imagePath = Environment.getExternalStorageDirectory().toString() + args.getString(0);
			String result = onPhotoTaken();
			Log.v(TAG, "Result:" + result);
			this.echo(result, callbackContext);
			return true;
		} catch (Exception e) {
			Log.v(TAG, "Exception in Execute:" + e.getMessage());
			callbackContext.error(e.getMessage());
			return false;
		}
	}


	private void echo(String imagePath, CallbackContext callbackContext) {
        if (imagePath != null && imagePath.length() > 0) {
            callbackContext.success(imagePath);
        } else {
            callbackContext.error("Expected one non-empty string argument.");
        }
    }
	
	

	public String onPhotoTaken() {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 4;

		Bitmap bitmap = BitmapFactory.decodeFile(_path, options);

		try {
			ExifInterface exif = new ExifInterface(_path);
			int exifOrientation = exif.getAttributeInt(
					ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_NORMAL);

			Log.v(TAG, "Orient: " + exifOrientation);

			int rotate = 0;

			switch (exifOrientation) {
			case ExifInterface.ORIENTATION_ROTATE_90:
				rotate = 90;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				rotate = 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_270:
				rotate = 270;
				break;
			}

			Log.v(TAG, "Rotation: " + rotate);

			if (rotate != 0) {

				// Getting width & height of the given image.
				int w = bitmap.getWidth();
				int h = bitmap.getHeight();

				// Setting pre rotate
				Matrix mtx = new Matrix();
				mtx.preRotate(rotate);

				// Rotating Bitmap
				bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, mtx, false);
			}

			// Convert to ARGB_8888, required by tess
			bitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

		} catch (IOException e) {
			Log.e(TAG, "Couldn't correct orientation: " + e.toString());
		}

		// _image.setImageBitmap( bitmap );
		
		Log.v(TAG, "Before baseApi");

		TessBaseAPI baseApi = new TessBaseAPI();
		baseApi.setDebug(true);
		baseApi.init(DATA_PATH, lang);
		baseApi.setImage(bitmap);
		
		String recognizedText = "";
		recognizedText = baseApi.getUTF8Text();
		
		baseApi.end();

		// You now have the text in recognizedText var, you can do anything with it.
		// We will display a stripped out trimmed alpha-numeric version of it (if lang is eng)
		// so that garbage doesn't make it to the display.

		Log.v(TAG, "OCRED TEXT: " + recognizedText);

		if ( lang.equalsIgnoreCase("eng") ) {
			recognizedText = recognizedText.replaceAll("[^a-zA-Z0-9]+", " ");
		}
		
		recognizedText = recognizedText.trim();
		Log.v(TAG, "Recognized Text: " + recognizedText);
		return recognizedText;
	}
		

}
