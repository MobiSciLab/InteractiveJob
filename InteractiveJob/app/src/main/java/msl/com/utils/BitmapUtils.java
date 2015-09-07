package msl.com.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;


public class BitmapUtils {

	private static final int UNCONSTRAINED = -1;
	private static final boolean ENABLE_LOG = true;
	private static final String TAG = "BitmapUtils";

	public static Bitmap compressToMiniThumbnail(Bitmap source,
			boolean needRecycleSource) {
		return compressToMiniThumbnail(source, needRecycleSource, 96);
	}
	
	/**
	 * saving bitmap to internal memory.
	 * @param context the Context want to use.
	 * @param bmpWantToSave bitmap want to save.
	 * @param fileNameWantToSave file name want to save.
	 * @return true if operation is successful, otherwise.
	 */
	public static boolean savingImageToInternalMemory(Context context,
			Bitmap bmpWantToSave, String fileNameWantToSave) {
		
		if (context == null)
			return false;
		if (bmpWantToSave == null)
			return false;
		if (TextUtils.isEmpty(fileNameWantToSave))
			return false;
		
		FileOutputStream outStream = null;
        try {
        	outStream = context.openFileOutput(fileNameWantToSave, Context.MODE_PRIVATE);
        	bmpWantToSave.compress(CompressFormat.PNG, 100, outStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				if (outStream != null)
					outStream.close();//flush();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	/**
	 * getting bitmap from internal memory with specific path.
	 * @param context the Context want to use.
	 * @param fileName the file name want to get.
	 * @return return a valid bitmap if the operation is successful, otherwise we will get null value or an exception.
	 * @throws FileNotFoundException
	 */
	public static Bitmap gettingBitmapFromInternalMemory(Context context,
			String fileName) throws FileNotFoundException {
		if (TextUtils.isEmpty(fileName))
			return null;
		if (context == null)
			return null;
		return BitmapFactory.decodeStream(context.openFileInput(fileName));
	}

	public static Bitmap compressToMiniThumbnail(Bitmap source,
			boolean needRecycleSource, int size) {
		if (source == null)
			return null;

		Log.d(TAG, "w=" + source.getWidth() + "  h=" + source.getHeight());

		int MINI_THUMB_TARGET_SIZE = size;

		if (source.getWidth() < size || source.getHeight() < size)
			return source;

		float scale;
		if (source.getWidth() < source.getHeight()) {
			scale = MINI_THUMB_TARGET_SIZE / source.getWidth();
		} else {
			scale = MINI_THUMB_TARGET_SIZE / source.getHeight();
		}
		Matrix matrix = new Matrix();
		matrix.setScale(scale, scale);
		Bitmap miniThumbnail = transform(matrix, source,
				MINI_THUMB_TARGET_SIZE, MINI_THUMB_TARGET_SIZE, false);
		if (needRecycleSource) {
			source.recycle();
		}

		return miniThumbnail;
	}
	
	/**
	 * Utility for center cropping an image with specific width and height. 
	 * @param inputBmp the bitmap want to crop.
	 * @param desireWidth 
	 * @param desireHeight
	 * @return an valid bitmap or null if an error has been occurred.
	 */
	public static Bitmap centerCropImage(Bitmap inputBmp, int desireWidth, int desireHeight) {
		
		if (inputBmp == null)
			return null;
		
		long t1 = System.currentTimeMillis();
        /*Options mOptions = new Options();
        // we must count sample value again to ensure we have no enter OOM exception.
        mOptions.inSampleSize = countSampleValue(
        		inputBmp.getWidth(), 
        		inputBmp.getHeight(),
				LayoutUtil.DISPLAY_WIDTH_PIXELS,
				LayoutUtil.DISPLAY_HEIGH_PIXELS);
        //inputBmp = BitmapFactory.decodeFile(mFilePath, mOptions);*/        
        
        int mOrgWidth = inputBmp.getWidth();
        int mOrgHeight = inputBmp.getHeight();
        
        if (mOrgWidth <=0 || mOrgHeight <= 0)
        	return null;
        
        boolean mNeedToCrop = true;
        
        int scaleWidth = mOrgWidth;
        int scaleHeight = mOrgHeight;
        if (mOrgWidth > mOrgHeight) {
        	scaleWidth = desireHeight * mOrgWidth / mOrgHeight;
        	scaleHeight = desireHeight;
        } else {
        	if (mOrgHeight > mOrgWidth) {
        		scaleWidth = desireWidth;
        		scaleHeight = desireWidth * mOrgHeight / mOrgWidth;
        	} else {
        		if (mOrgHeight == mOrgWidth) {
        			scaleWidth = desireWidth;
        			scaleHeight = desireHeight;
        			mNeedToCrop = false;
        		}
        	}
        }
        
        Log.d("App", ">>>oW=" + mOrgWidth + "  oH=" + mOrgHeight + "  sW=" + scaleWidth + "  sH" + scaleHeight);
        
        inputBmp = BitmapUtils.matrixResize(inputBmp, scaleWidth, scaleHeight);
		if (mNeedToCrop) {
			mOrgWidth = inputBmp.getWidth();
			mOrgHeight = inputBmp.getHeight();
			Log.i("App", ">>>oW=" + mOrgWidth + "  oH=" + mOrgHeight + "  sW=" + scaleWidth + "  sH" + scaleHeight);

			// create a matrix for the manipulation
			Matrix matrix = new Matrix();
			matrix.postScale(1, 1);

			int dx = 0;
			int dy = 0;
			if (scaleWidth > scaleHeight) {
				dx = (scaleWidth - desireWidth) / 2;
			} else {
				if (scaleHeight > scaleWidth) {
					dy = (scaleHeight - desireHeight) / 2;
				}
			}
			inputBmp = Bitmap.createBitmap(inputBmp, dx, dy,
					desireWidth, desireHeight, matrix, true);
		}
		
		long t2 = System.currentTimeMillis();
		Log.d("App", ">>> crop image cost:" + (t2 - t1));
		if (inputBmp != null)
			return inputBmp;
		return null;
	}

	public static synchronized Bitmap transform(Matrix scaler, Bitmap source,
			int targetWidth, int targetHeight, boolean scaleUp) {
		return transform(scaler, source, targetWidth, targetHeight, scaleUp,
				Bitmap.Config.RGB_565);
	}

	public static synchronized Bitmap transform(Matrix scaler, Bitmap source,
			int targetWidth, int targetHeight, boolean scaleUp,
			Bitmap.Config config) {
		if (config == null) {
			config = Bitmap.Config.RGB_565;
		}
		Log.d(TAG, "source " + source.getWidth() + " " + source.getHeight());
		int deltaX = source.getWidth() - targetWidth;
		int deltaY = source.getHeight() - targetHeight;
		Log.d(TAG, "deltaX " + deltaX + " deltaY " + deltaY);
		if ((!scaleUp) && ((deltaX < 0) || (deltaY < 0))) {
			Bitmap b2 = null;
			try {
				b2 = Bitmap.createBitmap(targetWidth, targetHeight, config);
			} catch (OutOfMemoryError oome) {
				Log.e(TAG, "memory is not enough to alloc bitmap", oome);
				return null;
			}
			Canvas c = new Canvas(b2);

			int deltaXHalf = Math.max(0, deltaX / 2);
			int deltaYHalf = Math.max(0, deltaY / 2);
			Rect src = new Rect(deltaXHalf, deltaYHalf, deltaXHalf
					+ Math.min(targetWidth, source.getWidth()), deltaYHalf
					+ Math.min(targetHeight, source.getHeight()));

			Rect dst = new Rect(0, 0, targetWidth, targetHeight);
			c.drawBitmap(source, src, dst, null);
			return b2;
		}
		float bitmapWidthF = source.getWidth();
		float bitmapHeightF = source.getHeight();

		float bitmapAspect = bitmapWidthF / bitmapHeightF;
		float viewAspect = targetWidth / targetHeight;

		if (bitmapAspect > viewAspect) {
			float scale = targetHeight / bitmapHeightF;
			if ((scale < 0.9F) || (scale > 1.0F))
				scaler.setScale(scale, scale);
			else
				scaler = null;
		} else {
			float scale = targetWidth / bitmapWidthF;
			if ((scale < 0.9F) || (scale > 1.0F))
				scaler.setScale(scale, scale);
			else
				scaler = null;
		}
		Bitmap b1;
		if (scaler != null) {
			try {
				b1 = Bitmap.createBitmap(source, 0, 0, source.getWidth(),
						source.getHeight(), scaler, true);
			} catch (OutOfMemoryError oome) {
				Log.e(TAG, "out of memory when create scaler one", oome);
				return null;
			}
		} else
			b1 = source;

		Log.d(TAG, "b1 " + b1.getWidth() + " " + b1.getHeight());
		int dx1 = Math.max(0, b1.getWidth() - targetWidth);
		int dy1 = Math.max(0, b1.getHeight() - targetHeight);

		Bitmap b2 = null;
		try {
			b2 = Bitmap.createBitmap(b1, dx1 / 2, dy1 / 2, targetWidth,
					targetHeight);
		} catch (OutOfMemoryError oome) {
			Log.e(TAG, "final tune image in transform cause oome", oome);
		}

		if (b1 != source) {
			b1.recycle();
		}
		return b2;
	}

	/**
	 * Compute the sample size as a function of minSideLength and
	 * maxNumOfPixels. minSideLength is used to specify that minimal width or
	 * height of a bitmap. maxNumOfPixels is used to specify the maximal size in
	 * pixels that is tolerable in terms of memory usage.
	 * 
	 * The function returns a sample size based on the constraints. Both size
	 * and minSideLength can be passed in as IImage.UNCONSTRAINED, which
	 * indicates no care of the corresponding constraint. The functions prefers
	 * returning a sample size that generates a smaller bitmap, unless
	 * minSideLength = IImage.UNCONSTRAINED.
	 * 
	 * Also, the function rounds up the sample size to a power of 2 or multiple
	 * of 8 because BitmapFactory only honors sample size this way. For example,
	 * BitmapFactory downsamples an image by 2 even though the request is 3. So
	 * we round up the sample size to avoid OOM.
	 */
	public static int computeSampleSize(byte[] byteIn, int maxResolutionX,
			int maxResolutionY) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// BitmapFactory.decodeStream(stream, null, options);
		// BitmapFactory.decodeFile(byteIn, options);
		BitmapFactory.decodeByteArray(byteIn, 0, byteIn.length);
		if (ENABLE_LOG) {
			Log.i(TAG, "org bitmap width=" + options.outWidth);
		}
		int maxNumOfPixels = maxResolutionX * maxResolutionY;
		int minSideLength = Math.min(maxResolutionX, maxResolutionY) / 2;
		return computeSampleSize(options, minSideLength, maxNumOfPixels);
	}

	public static int computeSampleSize(FileInputStream input,
			int maxResolutionX, int maxResolutionY) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		// BitmapFactory.decodeStream(stream, null, options);
		// BitmapFactory.decodeFile(byteIn, options);
		BitmapFactory.decodeStream(input, null, options);
		if (ENABLE_LOG) {
			Log.i(TAG, "org bitmap width=" + options.outWidth);
		}
		int maxNumOfPixels = maxResolutionX * maxResolutionY;
		int minSideLength = Math.min(maxResolutionX, maxResolutionY) / 2;
		return computeSampleSize(options, minSideLength, maxNumOfPixels);
	}

	public static Bitmap getBitmapByStream(String fileName, int maxResolutionX,
			int maxResolutionY) {
		Bitmap bitmapOrg = null;
		Bitmap result = null;
		try {

			int sample = BitmapUtils.computeSampleSize(fileName,
					maxResolutionX, maxResolutionY);
			BitmapFactory.Options options = new BitmapFactory.Options();
			options.inJustDecodeBounds = false;
			options.inSampleSize = sample;
			Log.e("PhucNT4", "Sample Size " + options.inSampleSize
					+ options.inJustDecodeBounds);
			bitmapOrg = BitmapFactory.decodeFile(fileName, options);
			// bitmapOrg = BitmapUtils.getBitmapByStream( is, WIDTH_IMAGE,
			// HEIGHT_IMAGE);
			// bitmapOrg = BitmapFactory.decodeStream(is, null, null);
			if (bitmapOrg != null) {
				int width = bitmapOrg.getWidth();
				int height = bitmapOrg.getHeight();
				Log.e("PhucNT4", "width   " + width + "  hegihtaaa  " + height);
				float scaleWidth = ((float) maxResolutionX) / width;
				float scaleHeight = ((float) maxResolutionY) / height;
				Matrix matrix = new Matrix();
				if (scaleWidth < 1 || scaleHeight < 1)
					matrix.postScale(scaleWidth, scaleHeight);

				result = Bitmap.createBitmap(bitmapOrg, 0, 0, width, height,
						matrix, true);
				// bitmapOrg.recycle(); FIXME we must check this case clearly because of FC issue (recycle bitmap)
			}
		} catch (Exception e) {
			Log.e("PhucNT4", "Out of memory");
			
		}

		return result;
	}

	public static int computeSampleSize(String path, int maxResolutionX,
			int maxResolutionY) {
		BitmapFactory.Options options = new BitmapFactory.Options();

		try {
			// 20110627 modified by NhatVT, START
//			options.inJustDecodeBounds = true;
//			BitmapFactory.decodeFile(path, options);
//			int heightRatio = (int) Math.ceil(options.outHeight
//					/ maxResolutionY);
//			int widthRatio = (int) Math.ceil(options.outWidth / maxResolutionX);
//			Log.i(TAG, "org bitmap width=" + options.outWidth);
//			if (heightRatio > 1 && widthRatio > 1) {
//				if (heightRatio > widthRatio) {
//					options.inSampleSize = heightRatio;
//				} else {
//					// Width ratio is larger, scale according to it
//					options.inSampleSize = widthRatio;
//				}
//			}
//			Log.i(TAG, "Sample Size " + options.inSampleSize);
			// change to using new calculation.
			if (TextUtils.isEmpty(path))
				return 1;
			options.inSampleSize = 1;
			options.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(path, options);
			if (options.outWidth > 0 && options.outHeight > 0)
				options.inSampleSize = countSampleValue(options.outWidth, options.outHeight, maxResolutionX, maxResolutionY);
			Log.i(TAG, "Sample Size " + options.inSampleSize);
			// 20110627 modified by NhatVT, END

		} catch (Exception e) {
			Log.i(TAG, "Unexpected exception", e);
		}

		return options.inSampleSize;
	}
	
	/*public static int computeSampleSize(String filePath, int maxResolutionX,
        int maxResolutionY) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        // BitmapFactory.decodeStream(stream, null, options);
        BitmapFactory.decodeFile(filePath, options);
            Log.i(TAG, "org bitmap width=" + options.outWidth);
        int maxNumOfPixels = maxResolutionX * maxResolutionY;
        int minSideLength = Math.min(maxResolutionX, maxResolutionY) / 2;
        return computeSampleSize(options, minSideLength, maxNumOfPixels);
    }*/

	public static int computeSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		int initialSize = computeInitialSampleSize(options, minSideLength,
				maxNumOfPixels);

		int roundedSize;
		if (initialSize <= 8) {
			roundedSize = 1;
			while (roundedSize < initialSize) {
				roundedSize <<= 1;
			}
		} else {
			roundedSize = (initialSize + 7) / 8 * 8;
		}

		return roundedSize;
	}

	public static int computeInitialSampleSize(BitmapFactory.Options options,
			int minSideLength, int maxNumOfPixels) {
		double w = options.outWidth;
		double h = options.outHeight;

		int lowerBound = (maxNumOfPixels == UNCONSTRAINED) ? 1 : (int) Math
				.ceil(Math.sqrt(w * h / maxNumOfPixels));
		int upperBound = (minSideLength == UNCONSTRAINED) ? 128 : (int) Math
				.min(Math.floor(w / minSideLength),
						Math.floor(h / minSideLength));

		if (upperBound < lowerBound) {
			// return the larger one when there is no overlapping zone.
			return lowerBound;
		}

		if ((maxNumOfPixels == UNCONSTRAINED)
				&& (minSideLength == UNCONSTRAINED)) {
			return 1;
		} else if (minSideLength == UNCONSTRAINED) {
			return lowerBound;
		} else {
			return upperBound;
		}
	}

	protected static Bitmap scaledResize(Bitmap source, int newWidth,
			int newHeight) {
		return Bitmap.createScaledBitmap(source, newWidth, newHeight, false);
	}

	public static Bitmap matrixResize(Bitmap source, int originalWidth,
			int orginalHeight, int newWidth, int newHeight) {
		Matrix matrix = new Matrix();
		matrix.postScale((float)newWidth / originalWidth, (float)newHeight / orginalHeight);
		return Bitmap.createBitmap(source, 0, 0, originalWidth, orginalHeight,
				matrix, true);
	}
	
	/**
	 * Resize bitmap to specific size by using Matrix
	 * @param source bitmap want to resize.
	 * @param newWidth new width.
	 * @param newHeight new height
	 * @return bitmap has been resized of null if an error occurred
	 */
	public static Bitmap matrixResize(Bitmap source, int newWidth, int newHeight) {
		if (source == null)
			return null;
		Matrix matrix = new Matrix();
		matrix.postScale((float)newWidth / source.getWidth(), (float)newHeight / source.getHeight());
		return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(),
				matrix, true);
	}
	
	/*public static Bitmap calcBitmapSizeForAnimatedView(Bitmap bmpSource) {
		if (bmpSource == null)
			return null;
		int orgWidth = bmpSource.getWidth();
		int orgHeigh = bmpSource.getHeight();
		int desireWidth = orgWidth;
		int desireHeigh = orgHeigh;
		Log.e(TAG, ">>> [calc new size] maxW=" + LayoutUtil.VIEW_EFFECT_MAX_WIDTH + " maxH=" + LayoutUtil.VIEW_EFFECT_MAX_HEIGH);
		Log.e(TAG, ">>> [calc new size] minW=" + LayoutUtil.VIEW_EFFECT_MIN_WIDTH + " minH=" + LayoutUtil.VIEW_EFFECT_MIN_HEIGH);
		Log.i(TAG, ">>> [calc new size] srcW=" + orgWidth + " srcH=" + orgHeigh);
		if (orgWidth > LayoutUtil.VIEW_EFFECT_MAX_WIDTH
				|| orgHeigh > LayoutUtil.VIEW_EFFECT_MAX_HEIGH
				|| orgWidth > LayoutUtil.VIEW_EFFECT_MIN_WIDTH
				|| orgHeigh > LayoutUtil.VIEW_EFFECT_MIN_HEIGH) {  
			// image is larger than our animated view
			if (orgWidth == orgHeigh) {
				desireWidth = desireHeigh = LayoutUtil.VIEW_EFFECT_MAX_WIDTH;
			} else {
				if (orgWidth > orgHeigh) {
					desireWidth = LayoutUtil.VIEW_EFFECT_MAX_WIDTH;
					desireHeigh = (orgHeigh * desireWidth) / orgWidth;
				} else {
					desireHeigh = LayoutUtil.VIEW_EFFECT_MAX_HEIGH;
					desireWidth = (orgWidth * desireHeigh) / orgHeigh;
				}
			}
		} else {
			if (orgWidth > LayoutUtil.VIEW_EFFECT_MIN_WIDTH
					|| orgHeigh > LayoutUtil.VIEW_EFFECT_MIN_HEIGH) { 
				// image is lager than minimum size of our animated view
				if (orgWidth == orgHeigh) {
					desireWidth = desireHeigh = LayoutUtil.VIEW_EFFECT_MAX_WIDTH;
				} else {
					
				}
			} else { 
				// image is too smaller, let make it zoom to minimum size of our animated view 
				if (orgWidth == orgHeigh) {
					desireWidth = desireHeigh = LayoutUtil.VIEW_EFFECT_MIN_WIDTH;
				} else {
					if (orgWidth > orgHeigh) {
						desireWidth = LayoutUtil.VIEW_EFFECT_MIN_WIDTH;
						desireHeigh = (orgHeigh * desireWidth) / orgWidth;
					} else {
						desireHeigh = LayoutUtil.VIEW_EFFECT_MIN_HEIGH;
						desireWidth = (orgWidth * desireHeigh) / orgHeigh;
					}
				}
//			}
		}
		Log.d(TAG, ">>> [calc new size] desireW=" + desireWidth + " desireH=" + desireHeigh);
		return matrixResize(bmpSource, desireWidth, desireHeigh);
	}*/

	public static int[] getOriginalSize(String imagePath) {
		if (TextUtils.isEmpty(imagePath))
			return null;
		
		BitmapFactory.Options options = new BitmapFactory.Options();
		int[] result = new int[2];
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(imagePath, options);
		Log.d(TAG, ">>> original bitmap with w=" + options.outWidth + "  h="+options.outHeight);
		result[0] = options.outWidth;
		result[1] = options.outHeight;
		return result;
	}
	
//	/**
//	 * Load bitmap from specific path
//	 * @param imagePath
//	 * @param newWidth The new bitmap's desired width.
//	 * @param newHeight The new bitmap's desired height.
//	 * @param useScaledResize set <b> true</b> if we want to scale image, otherwise.
//	 * @return return a valid bitmap if no error was not found, otherwise it will return null. 
//	 */
//	public static Bitmap loadBitmap(String imagePath, /*int originalWidth,
//			int originalHeight,*/ int newWidth, int newHeight,
//			boolean useScaledResize) {
//		
//		if (TextUtils.isEmpty(imagePath))
//			return null;
//		
//		try {
//			BitmapFactory.Options option = new BitmapFactory.Options();
//			int[] mBmpSize = getOriginalSize(imagePath);
////			option.inSampleSize = countSampleValue(mBmpSize[0],
////					mBmpSize[1], newWidth, newHeight);
//			 option.inSampleSize = computeSampleSize(imagePath, newWidth, newHeight);
//			Log.d(TAG, ">>> ORG BMP SAMPLESIZE=" + computeSampleSize(imagePath, newWidth, newHeight));
//			Bitmap bitmap = BitmapFactory.decodeFile(imagePath, option);
//			int afterSameplWidth = bitmap.getWidth();
//			int afterSameplHeight = bitmap.getHeight();
//			if ((afterSameplWidth != newWidth)
//					|| (afterSameplHeight != newHeight)) {
//				if (useScaledResize)
//					bitmap = scaledResize(bitmap, newWidth, newHeight);
//				else {
//					bitmap = matrixResize(bitmap, afterSameplWidth,
//							afterSameplHeight, newWidth, newHeight);
//				}
//			}
//			return bitmap;
//		} catch (Exception e) {
//			Log.e("Util", "[ImageUtil] " + e.getMessage(), e);
//		}
//		return null;
//	}
	
	public static int countSampleValue(int originalWidth, int originalHeight,
			int newWidth, int newHeight) {
		int sample = 1;

		// 20110628 added by NhatVT, START
		// prevent loop cycle in case our parameter is smaller than 0
		if (originalWidth <= 0 || originalHeight <= 0 
				|| newWidth <= 0 || newHeight <= 0)
			return sample;
		// 20110628 added by NhatVT, END
		while ((originalWidth >> 1 > newWidth)
				|| (originalHeight >> 1 > newHeight)) {
			Log.i(TAG, ">>>[S] originalWidth=" + originalWidth + "  originalHeight=" + originalHeight + " sample=" + sample);
			sample <<= 1;
			originalWidth >>= 1;
			originalHeight >>= 1;
			Log.d(TAG, ">>>[E] originalWidth=" + originalWidth + "  originalHeight=" + originalHeight + " sample=" + sample);
		}
		Log.d(TAG, ">>> countSampleValue=" + sample);
		return sample;
	}
	
	public static void saveView2PNG(View view, String fileName) {
		if (view == null) {
			return;
		}
		View rootView = view.getRootView();
		rootView.setDrawingCacheEnabled(true);
		Bitmap viewBmp = Bitmap.createBitmap(rootView.getDrawingCache());
		rootView.setDrawingCacheEnabled(false);

		saveBitmap2PNG(viewBmp, fileName, false);
	}

	public static void saveBitmap2PNG(Bitmap bitmap, String fileName,
			boolean doRecycle) {
		if ((bitmap == null) || (bitmap.isRecycled())) {
			return;
		}
		FileOutputStream outStream = null;
		try {
			String folder = (/*
							 * DeviceStorageManager.isSupportPhoneStorage() ?
							 * DeviceStorageManager.getPhoneStoragePath() :
							 * DeviceStorageManager.getExternalStoragePath()
							 */"") + "/"; // TODO need to fix this
			String path = folder + fileName + "-" + bitmap.getHeight() + "X"
					+ bitmap.getWidth() + ".png";

			outStream = new FileOutputStream(path);
			bitmap.compress(CompressFormat.PNG, 100, outStream);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
			if (doRecycle) {
				bitmap.recycle();
			}
			if (outStream != null) {
				try {
					outStream.close();
				} catch (IOException ioe) {
					ioe.printStackTrace();
				}
			}
		} finally {
			if (doRecycle) {
				bitmap.recycle();
			}
			if (outStream != null)
				try {
					outStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
}
