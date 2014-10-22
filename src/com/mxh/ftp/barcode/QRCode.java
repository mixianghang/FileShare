package com.mxh.ftp.barcode;

import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.Display;
import android.view.WindowManager;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;

public class QRCode {
//	/		  生成二维码图片
	  private static Context mContext;
	  private static final int WHITE = 0xFFFFFFFF;
	  private static final int BLACK = 0xFF000000;
	QRCode(Context context){
		mContext=context;
	}
	  public static  Bitmap encodeAsBitmap(Context context,String contents, BarcodeFormat format) throws WriterException {
		  WindowManager manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
		    Display display = manager.getDefaultDisplay();
		    int width1 = display.getWidth();
		    int height1 = display.getHeight();
		    int smallersmallerDimension = width1 < height1 ? width1 : height1;
		    smallersmallerDimension = smallersmallerDimension * 7 / 8;
		  String contentsToEncode = contents;
		    if (contentsToEncode == null) {
		      return null;
		    }
		    Map<EncodeHintType,Object> hints = null;
//		    获取编码格式
		    String encoding = guessAppropriateEncoding(contentsToEncode);
		    if (encoding != null) {
		      hints = new EnumMap<EncodeHintType,Object>(EncodeHintType.class);
		      hints.put(EncodeHintType.CHARACTER_SET, encoding);
		    }
		    MultiFormatWriter writer = new MultiFormatWriter();
		    BitMatrix result = writer.encode(contentsToEncode, format, smallersmallerDimension, smallersmallerDimension, hints);
		    int width = result.getWidth();
		    int height = result.getHeight();
		    int[] pixels = new int[(width+2) * (height+2)];
		    for (int y = 1; y <=height; y++) {
		      int offset = y * (width+2);
		      for (int x = 1; x <= width; x++) {
		        pixels[offset + x] = result.get(x-1, y-1) ? BLACK : WHITE;
		      }
		    }
		//    
		    for(int y=0;y<height+2;y++){
		    	pixels[y*(width+2)]=WHITE;
		    	pixels[(y+1)*(width+2)-1]=WHITE;
		    }
		    for(int x=0;x<width+2;x++){
		    	pixels[x]=WHITE;
		    	pixels[(x+(width+2)*(height+1))]=WHITE;
		    }
//生成Bitmap图片
		    Bitmap bitmap = Bitmap.createBitmap(width+2, height+2, Bitmap.Config.ARGB_8888);
		    bitmap.setPixels(pixels, 0, width+2, 0, 0, width+2, height+2);
		    return bitmap;
		  }
	  
	  public static String decodeImage(Bitmap img){
		  Result rawResult = null; 
	      LuminanceSource source;
	      Bitmap bit=img;
	      try { 
	    	  
//	          if (bit == null) { 
//	              System.out.println("the decode image may be not exit."); 
//	          } 
	         source = new RGBLuminanceSource(bit); 
	          BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));; 
	          rawResult = new MultiFormatReader().decode(bitmap); 
	           
	      } catch (Exception e) { 
//	    	  System.out.println("the decode image may be not exit."); 
	          e.printStackTrace(); 
	      } 
	      if(rawResult==null){
	    	  return null;
	      }
	      return rawResult.getText();
	  }
//	  Bitmap bit=BitmapFactory.decodeFile(imgPath);
	  
//	  Map<String, Object> obj=new LinkedHashMap();
//	  obj.put(JSON_GOODS_NAME, goodsName);
//	  obj.put(JSON_YEAR,year);
//	  obj.put(JSON_MONTH, month);
//	  obj.put(JSON_DAY, day);
//	  obj.put(JSON_GOODS_Id,preGoodsId);
//	  obj.put(JSON_IMG_PATH, imgPath);
//	  obj.put(JSON_TYPE, goodsType);
//	  obj.put(JSON_QUALITY, qualityTime);
//	  String jsonString=this.createJsonString(obj);
	  private static String guessAppropriateEncoding(CharSequence contents) {
		    // Very crude at the moment
		    for (int i = 0; i < contents.length(); i++) {
		      if (contents.charAt(i) > 0xFF) {
		        return "UTF-8";
		      }
		    }
		    return null;
		  }
}
