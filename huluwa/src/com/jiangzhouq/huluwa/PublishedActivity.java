package com.jiangzhouq.huluwa;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

import com.jiangzhouq.huluwa.R;
import com.renn.rennsdk.RennClient;
import com.renn.rennsdk.RennClient.LoginListener;
import com.renn.rennsdk.RennExecutor.CallBack;
import com.renn.rennsdk.RennResponse;
import com.renn.rennsdk.exception.RennException;
import com.renn.rennsdk.param.PutStatusParam;
import com.renn.rennsdk.param.UploadPhotoParam;

public class PublishedActivity extends Activity implements OnClickListener{

	private GridView noScrollgridview;
	private GridAdapter adapter;
	private TextView activity_selectimg_send;
	
	List<String> picList ;
	private EditText edit_message;
	private ImageButton btn_renren;
	private ImageButton btn_qzone;
	private boolean renren_on = false;
	private RennClient renren;
	private ProgressDialog mProgressDialog;
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selectimg);
		Init();
	}
	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		delPics();
	}
	public void Init() {
		noScrollgridview = (GridView) findViewById(R.id.noScrollgridview);
		noScrollgridview.setSelector(new ColorDrawable(Color.TRANSPARENT));
		adapter = new GridAdapter(this);
		adapter.update();
		noScrollgridview.setAdapter(adapter);
		noScrollgridview.setOnItemClickListener(new OnItemClickListener() {

			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				if (arg2 == Bimp.bmp.size()) {
					new PopupWindows(PublishedActivity.this, noScrollgridview);
				} else {
					Intent intent = new Intent(PublishedActivity.this,
							PhotoActivity.class);
					intent.putExtra("ID", arg2);
					startActivity(intent);
				}
			}
		});
		activity_selectimg_send = (TextView) findViewById(R.id.activity_selectimg_send);
		activity_selectimg_send.setOnClickListener(this);
		btn_renren = (ImageButton) findViewById(R.id.btn_renren);
		btn_renren.setOnClickListener(this);
		btn_qzone = (ImageButton) findViewById(R.id.btn_qzone);
		btn_qzone.setOnClickListener(this);
		renren = RennClient.getInstance(this);
		edit_message = (EditText) findViewById(R.id.message);
	}

	private void getPics(){
		picList = new ArrayList<String>();			
		for (int i = 0; i < Bimp.drr.size(); i++) {
			String Str = Bimp.drr.get(i).substring( 
					Bimp.drr.get(i).lastIndexOf("/") + 1,
					Bimp.drr.get(i).lastIndexOf("."));
			picList.add(FileUtils.SDPATH+Str+".JPEG");				
		}
		// 高清的压缩图片全部就在  list 路径里面了
		// 高清的压缩过的 bmp 对象  都在 Bimp.bmp里面
		// 完成上传服务器后 .........
	}
	private void delPics(){
		FileUtils.deleteDir();
	}
	@SuppressLint("HandlerLeak")
	public class GridAdapter extends BaseAdapter {
		private LayoutInflater inflater; // 视图容器
		private int selectedPosition = -1;// 选中的位置
		private boolean shape;

		public boolean isShape() {
			return shape;
		}

		public void setShape(boolean shape) {
			this.shape = shape;
		}

		public GridAdapter(Context context) {
			inflater = LayoutInflater.from(context);
		}

		public void update() {
			loading();
		}

		public int getCount() {
			return (Bimp.bmp.size() + 1);
		}

		public Object getItem(int arg0) {

			return null;
		}

		public long getItemId(int arg0) {

			return 0;
		}

		public void setSelectedPosition(int position) {
			selectedPosition = position;
		}

		public int getSelectedPosition() {
			return selectedPosition;
		}

		/**
		 * ListView Item设置
		 */
		public View getView(int position, View convertView, ViewGroup parent) {
			final int coord = position;
			ViewHolder holder = null;
			if (convertView == null) {

				convertView = inflater.inflate(R.layout.item_published_grida,
						parent, false);
				holder = new ViewHolder();
				holder.image = (ImageView) convertView
						.findViewById(R.id.item_grida_image);
				convertView.setTag(holder);
			} else {
				holder = (ViewHolder) convertView.getTag();
			}

			if (position == Bimp.bmp.size()) {
				holder.image.setImageBitmap(BitmapFactory.decodeResource(
						getResources(), R.drawable.icon_addpic_unfocused));
				if (position == 9) {
					holder.image.setVisibility(View.GONE);
				}
			} else {
				holder.image.setImageBitmap(Bimp.bmp.get(position));
			}

			return convertView;
		}

		public class ViewHolder {
			public ImageView image;
		}

		Handler handler = new Handler() {
			public void handleMessage(Message msg) {
				switch (msg.what) {
				case 1:
					adapter.notifyDataSetChanged();
					break;
				}
				super.handleMessage(msg);
			}
		};

		public void loading() {
			new Thread(new Runnable() {
				public void run() {
					while (true) {
						if (Bimp.max == Bimp.drr.size()) {
							Message message = new Message();
							message.what = 1;
							handler.sendMessage(message);
							break;
						} else {
							try {
								String path = Bimp.drr.get(Bimp.max);
								System.out.println(path);
								Bitmap bm = Bimp.revitionImageSize(path);
								Bimp.bmp.add(bm);
								String newStr = path.substring(
										path.lastIndexOf("/") + 1,
										path.lastIndexOf("."));
								FileUtils.saveBitmap(bm, "" + newStr);
								Bimp.max += 1;
								Message message = new Message();
								message.what = 1;
								handler.sendMessage(message);
							} catch (IOException e) {

								e.printStackTrace();
							}
						}
					}
				}
			}).start();
		}
	}

	public String getString(String s) {
		String path = null;
		if (s == null)
			return "";
		for (int i = s.length() - 1; i > 0; i++) {
			s.charAt(i);
		}
		return path;
	}

	protected void onRestart() {
		adapter.update();
		super.onRestart();
	}

	public class PopupWindows extends PopupWindow {

		public PopupWindows(Context mContext, View parent) {

			View view = View
					.inflate(mContext, R.layout.item_popupwindows, null);
			view.startAnimation(AnimationUtils.loadAnimation(mContext,
					R.anim.fade_ins));
			LinearLayout ll_popup = (LinearLayout) view
					.findViewById(R.id.ll_popup);
			ll_popup.startAnimation(AnimationUtils.loadAnimation(mContext,
					R.anim.push_bottom_in_2));

			setWidth(LayoutParams.FILL_PARENT);
			setHeight(LayoutParams.FILL_PARENT);
			setBackgroundDrawable(new BitmapDrawable());
			setFocusable(true);
			setOutsideTouchable(true);
			setContentView(view);
			showAtLocation(parent, Gravity.BOTTOM, 0, 0);
			update();

			Button bt1 = (Button) view
					.findViewById(R.id.item_popupwindows_camera);
			Button bt2 = (Button) view
					.findViewById(R.id.item_popupwindows_Photo);
			Button bt3 = (Button) view
					.findViewById(R.id.item_popupwindows_cancel);
			bt1.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					photo();
					dismiss();
				}
			});
			bt2.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					Intent intent = new Intent(PublishedActivity.this,
							TestPicActivity.class);
					startActivity(intent);
					dismiss();
				}
			});
			bt3.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					dismiss();
				}
			});

		}
	}

	private static final int TAKE_PICTURE = 0x000000;
	private String path = "";

	public void photo() {
		Intent openCameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File file = new File(Environment.getExternalStorageDirectory()
				+ "/myimage/", String.valueOf(System.currentTimeMillis())
				+ ".jpg");
		path = file.getPath();
		Uri imageUri = Uri.fromFile(file);
		openCameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		startActivityForResult(openCameraIntent, TAKE_PICTURE);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case TAKE_PICTURE:
			if (Bimp.drr.size() < 9 && resultCode == -1) {
				Bimp.drr.add(path);
			}
			break;
		}
	}
	
	@Override
	public void onClick(View v) {
		switch(v.getId()){
			case R.id.activity_selectimg_send:
				if(edit_message.getText().toString().isEmpty()){
					break;
				}
//				delPics();
				getPics();
				if(picList.size() > 0){
					sendPhotoToRenren();
				}else{
					sendMessageToRenren();
				}
				break;
			case R.id.btn_renren:
				if(renren_on){
					renren_on = false;
					btn_renren.setImageResource(R.drawable.logo_renren_2);
				}else{
					if(!renren.isLogin()){
						renren.init(Constants.renren_ai, Constants.renren_ak, Constants.renren_sk);
						renren
						.setScope("read_user_blog read_user_photo read_user_status read_user_album "
								+ "read_user_comment read_user_share publish_blog publish_share "
								+ "send_notification photo_upload status_update create_album "
								+ "publish_comment publish_feed send_request");
						renren.setTokenType("bearer");
						renren.setLoginListener(new LoginListener() {
							@Override
							public void onLoginSuccess() {
								if (Constants.LOG_SWITCH)
									Log.d(Constants.LOG_TAG, "RennClient login seccessed!");
							}
							@Override
							public void onLoginCanceled() {
								if (Constants.LOG_SWITCH)
									Log.d(Constants.LOG_TAG, "RennClient login failed!");
							}

						});
						renren.login(this);
					}
					renren_on = true;
					btn_renren.setImageResource(R.drawable.logo_renren_1);
				}
				
				break;
			case R.id.btn_qzone:
				break;
		}
	}
	private void sendMessageToRenren(){
		PutStatusParam putStatusParam = new PutStatusParam();
        putStatusParam.setContent(edit_message.getText().toString());
        try {
        	renren.getRennService().sendAsynRequest(putStatusParam, new CallBack() {    
                
                @Override
                public void onSuccess(RennResponse response) {
                	if (Constants.LOG_SWITCH)
						Log.d(Constants.LOG_TAG, response.toString());
                    Toast.makeText(PublishedActivity.this, "状态发布成功", Toast.LENGTH_SHORT).show();  
                }
                
                @Override
                public void onFailed(String errorCode, String errorMessage) {
                	if (Constants.LOG_SWITCH)
						Log.d(Constants.LOG_TAG, errorCode+":"+errorMessage);
                    Toast.makeText(PublishedActivity.this, "状态发布失败", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (RennException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
	}
	private void sendPhotoToRenren(){
		UploadPhotoParam uploadPhotoParam = new UploadPhotoParam();
		for(int i = 0 ; i < picList.size() ; i++){
			uploadPhotoParam.setFile(new File(picList.get(i)));
			uploadPhotoParam.setDescription(edit_message.getText().toString());
			final int j = i;
	        try {
	        	renren.getRennService().sendAsynRequest(uploadPhotoParam, new CallBack() {    
	                
	                @Override
	                public void onSuccess(RennResponse response) {
	                	if (Constants.LOG_SWITCH)
							Log.d(Constants.LOG_TAG, "succeess! :" + j);
	                    Toast.makeText(PublishedActivity.this, "状态发布成功", Toast.LENGTH_SHORT).show();  
	                }
	                
	                @Override
	                public void onFailed(String errorCode, String errorMessage) {
	                	if (Constants.LOG_SWITCH)
							Log.d(Constants.LOG_TAG, errorCode+":"+errorMessage);
	                    Toast.makeText(PublishedActivity.this, "状态发布失败", Toast.LENGTH_SHORT).show();
	                }
	            });
	        } catch (RennException e1) {
	            // TODO Auto-generated catch block
	            e1.printStackTrace();
	        }
		}
	}
}
