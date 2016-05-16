package com.mochalov.photo;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.mochalov.photo.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.ThumbnailUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DirAdapter  extends ArrayAdapter<String>{
	Context context;
	ArrayList<String> list;
	int resource;

	private String tempDirectory;

	private int THUMBSIZE = 128;
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (convertView == null) { 
			LayoutInflater inflater = (LayoutInflater) context.
				getSystemService(Context.LAYOUT_INFLATER_SERVICE); 
			convertView = inflater.inflate(R.layout.list_dir_item, null);
		}
		
		String str = list.get(position);
		
		TextView text = (TextView)convertView.findViewById(R.id.listDirItemTextView);
		text.setText(str);
		
		Bitmap ThumbImage  = null;
		
		File dir = new File(tempDirectory+"/"+str); 
		if (dir.isDirectory()) {
			String[] children = dir.list();
			if (children.length > 0)
			{
				String path = tempDirectory+"/"+str+"/"+children[0];
				ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeFile(path), THUMBSIZE, THUMBSIZE);
				
				ExifInterface exif = null;
				int orientation = 0;
				try {
					exif = new ExifInterface(path);
					orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
					
					switch(orientation) {
				    case ExifInterface.ORIENTATION_ROTATE_90:
				    	ThumbImage = rotateImage(ThumbImage, 90);
				        break;
				    case ExifInterface.ORIENTATION_ROTATE_180:
				    	ThumbImage = rotateImage(ThumbImage, 180);
				        break;
				    // etc.
					}					
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				text = (TextView)convertView.findViewById(R.id.listDirItemTextViewInfo);
				text.setText("orientation "+orientation+" aperture "+exif.getAttribute(ExifInterface.TAG_APERTURE));
								
			}
		}
		
		if (ThumbImage == null)
			ThumbImage = ThumbnailUtils.extractThumbnail(BitmapFactory.decodeResource(context.getResources(),
	                R.drawable.not_found), THUMBSIZE, THUMBSIZE);
		

		ImageView img = (ImageView)convertView.findViewById(R.id.listDirItemImageView);
		img.setImageBitmap(ThumbImage);
		
		return convertView;
	}

	public Bitmap rotateImage(Bitmap source, float angle)
	{
	      Matrix matrix = new Matrix();
	      matrix.postRotate(angle);
	      return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}	

public DirAdapter(Context context, int res, ArrayList<String> values, String tempDirectory){
		super(context, res, values);
		this.list = values;
		this.resource = res;
		this.context = context;
		this.tempDirectory = tempDirectory;
	}

	public int getCount(){
		return list.size();
	}

	public long getItemId(int position){
		return position;
	}	

}
