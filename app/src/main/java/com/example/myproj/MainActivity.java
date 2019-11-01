package com.example.myproj;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.example.myproj.baidu.LogoResult;
import com.example.myproj.baidu.image;
import com.google.gson.Gson;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class MainActivity extends AppCompatActivity {
    private ImageView photo;
    private String uploadFileName;
    private byte[] fileBuf;
    private String uploadUrl = "http://121.199.23.49:8010/upload";
    private Uri imageUri;
    private String accessToken="24.ec1b80b363d80daea15a6a25b3dbb5b4.2592000.1575020069.282335-16234596";
    private int REQUEST_CODE_CAMERA=2;
    private Bitmap map;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        photo = findViewById(R.id.photoshow);

    }

    public void select(View view) {
        String[] permissions = new String[]{
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        };
        //进行sdcard的读写请求
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, permissions, 1);
        } else {
            openGallery(); //打开相册，进行选择
        }
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openGallery();
                } else {
                    Toast.makeText(this, "读相册的操作被拒绝", Toast.LENGTH_LONG).show();
                }
                break;

        }
    }

    private void openGallery() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, 1);
    }

    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                handleSelect(data);
                break;
            case 2:
                if (resultCode == RESULT_OK) {
                    try {

                        //利用ContentResolver,查询临时文件，并使用BitMapFactory,从输入流中创建BitMap
                        //同样需要配合Provider,在Manifest.xml中加以配置

                                    InputStream inputStream_map = getContentResolver().openInputStream(imageUri);
                                    InputStream inputStream_byte=getContentResolver().openInputStream(imageUri);
                                    fileBuf = convertToBytes(inputStream_byte);

                                    map = BitmapFactory.decodeStream(inputStream_map);
                                    photo.setImageBitmap(map);

                                    uploadFileName = System.currentTimeMillis() + ".jpg";
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                    }
                }

        }



    //选择后照片的读取工作
    private void handleSelect(Intent intent) {
        Cursor cursor = null;
        Uri uri = intent.getData();
        cursor = getContentResolver().query(uri, null, null, null, null);
        if (cursor.moveToFirst()) {
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
            uploadFileName = cursor.getString(columnIndex);
        }
        try {
            InputStream inputStream_byte = getContentResolver().openInputStream(uri);
            InputStream inputStream_map = getContentResolver().openInputStream(uri);

            fileBuf = convertToBytes(inputStream_byte);

            Bitmap bitmap = BitmapFactory.decodeStream(inputStream_map);
            photo.setImageBitmap(bitmap);

            //编码
            String img = Base64.encodeToString(fileBuf, Base64.DEFAULT);
            inputStream_byte.close();
            inputStream_map.close();
            //getBDapi(img);


        } catch (Exception e) {
            e.printStackTrace();
        }
        cursor.close();
    }

    //文件上传的处理
    public void upload() {
        new Thread() {
            @Override
            public void run() {
                OkHttpClient client1 = new OkHttpClient();
                //上传文件域的请求体部分
                RequestBody formBody = RequestBody
                        .create(MediaType.parse("JPGE"), fileBuf);
                //整个上传的请求体部分（普通表单+文件上传域）
                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("title", "Square Logo")
                        //filename:avatar,originname:abc.jpg
                        .addFormDataPart("picture", uploadFileName, formBody)
                        .build();
                Request request = new Request.Builder()
                        .url(uploadUrl)
                        .post(requestBody)
                        .build();

                try {
                    Response response = client1.newCall(request).execute();
                    Log.i("数据", response.body().string() + "....");
                } catch (IOException e) {
                    e.printStackTrace();
                }


            }
        }.start();
    }

    private byte[] convertToBytes(InputStream inputStream) throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int len = 0;
        while ((len = inputStream.read(buf)) > 0) {
            out.write(buf, 0, len);
        }
        out.close();
        System.out.println("convertlength"+out.toByteArray().length);
        return out.toByteArray();
    }


    public void getBDapi(final String img) {
        new Thread() {
            public void run() {
                try {
                    //获取accessToken
                    String url = "https://aip.baidubce.com/oauth/2.0/token?grant_type=client_credentials&client_id=MOkwaPnZOSGcGNxK0myvjUFo&client_secret=xOa3pOYFckYW7aPN5L0GYR1ZVGsh4EDF";
                    MediaType json = MediaType.get("application/json; charset=utf-8");
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = RequestBody.create(json, JSON.toJSONString(LogoResult.class));
                    Request request = new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();
                    Response response = client.newCall(request).execute();
                    String s = response.body().string();
                    String accessToken = s.split(",")[3].split(":")[1].replace("\"", "");
                    System.out.println(accessToken);

                     //调用地标识别接口
                    String url2 = "https://aip.baidubce.com/rest/2.0/face/v3/detect?access_token=24.92fd062c410c85e7d563e758acccb0af.2592000.1574862037.282335-16234596";
                    //String imgUrl="https://ss0.bdstatic.com/70cFuHSh_Q1YnxGkpoWK1HF6hhy/it/u=2490940314,913609336&fm=26&gp=0.jpg";
                    Map<String, String> map = new HashMap<>();

                    map.put("image", img);
                    map.put("image_type", "BASE64");

                    String userJson = new Gson().toJson(map);
                    RequestBody body1 = RequestBody.create(json, userJson);

                    Request request1 = new Request.Builder()
                            .url(url2)
                            .post(body1)
                            .build();
                    Response response1 = client.newCall(request1).execute();
                    String result1 = response1.body().string();
                    Log.i("para", result1);

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    public void takePhoto(View view) throws Exception {
        //创建临时文件来保存照片
        File outImg = new File(getExternalCacheDir(), "temp.jpg");
        if (outImg.exists()) outImg.delete();
        outImg.createNewFile();

        System.out.println("0");
        //复杂的Uri创建方式
        if (Build.VERSION.SDK_INT >= 24)
            //这是Android 7后，更加安全的获取文件uri的方式（需要配合Provider,在Manifest.xml中加以配置）
        {
            imageUri = FileProvider.getUriForFile(this, "xjtu.lxh.camera.fileprovider", outImg);

        }

        else
            imageUri = Uri.fromFile(outImg);



        //利用actionName和Extra,启动《相机Activity》
        Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");

        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);

        startActivityForResult(intent,REQUEST_CODE_CAMERA);

        //到此，启动了相机，等待用户拍照
    }

    public void addFace(View view) {
            new Thread() {

                public void run() {
                    String url="https://aip.baidubce.com/rest/2.0/face/v3/faceset/user/add?access_token="+accessToken;
                    try {
                    //先上传到服务器
                       upload();
                       //对图片压缩
                        String img64 = compressBitmap(map,2048000,false);
                        //创建json对象
                        Map<String,String> map = new HashMap<>();
                        map.put("group_id","lxh1");
                        map.put("user_id","962422");
                        map.put("image",img64);
                        map.put("image_type","BASE64");
                        String faceJson = new Gson().toJson(map);
                    //post请求
                    MediaType json = MediaType.get("application/json; charset=utf-8");
                    OkHttpClient client = new OkHttpClient();
                    RequestBody body = RequestBody.create(json, faceJson);
                    Request request1 = new Request.Builder()
                            .url(url)
                            .post(body)
                            .build();
                    Response response = null;

                        response = client.newCall(request1).execute();
                        String result = response.body().string();
                        System.out.println(result);

                    } catch (
                            IOException e) {
                        e.printStackTrace();
                    }
                }
            }.start();
    }
    public String compressBitmap(Bitmap bitmap, double maxSize, boolean needRecycle) {
        if (bitmap == null) {
            return null;
        } else {
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            //计算等比缩放
            double x = Math.sqrt(maxSize / (width * height));
            Bitmap tmp = Bitmap.createScaledBitmap(bitmap, (int) Math.floor(width * x), (int) Math.floor(height * x), true);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            int options = 100;
            //生产byte[]
            tmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
            //判断byte[]与上线存储空间的大小
            if (baos.toByteArray().length > maxSize) {
                //根据内存大小的比例，进行质量的压缩
                options = (int) Math.ceil((maxSize / baos.toByteArray().length) * 100);
                baos.reset();
                tmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
                //循环压缩
                while (baos.toByteArray().length > maxSize) {
                    baos.reset();
                    options -= 1.5;
                    tmp.compress(Bitmap.CompressFormat.JPEG, options, baos);
                }
                recycle(tmp);
                if (needRecycle) {
                    recycle(bitmap);
                }
            }
            byte[] data = baos.toByteArray();
            String image64 = Base64.encodeToString(data,Base64.DEFAULT);
            System.out.println(image64.length());
            try {
                baos.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return image64;
        }
    }

    /**
     * 回收Bitmap
     * @param thumbBmp  需要被回收的bitmap
     */
    public static void recycle(Bitmap thumbBmp) {
        if (thumbBmp != null && !thumbBmp.isRecycled()) {
            thumbBmp.recycle();
        }
    }
}

