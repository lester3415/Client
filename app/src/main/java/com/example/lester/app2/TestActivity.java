package com.example.lester.app2;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Hashtable;

public class TestActivity extends AppCompatActivity {
    private ArrayAdapter adapter2;
    private ListView listView2;

    int QR_WIDTH = 200,QR_HEIGHT = 200;
    ImageView mImageView;
    Button mScanButton;

    private int open=1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test);

        mImageView = (ImageView)findViewById(R.id.image);

        //listView2 = (ListView)findViewById(R.id.list2);
        //TextView textView = (TextView)findViewById(R.id.textView);

        //textView.setText(arrayList2.toString());
        //adapter2 = new ArrayAdapter<>(this ,android.R.layout.simple_list_item_1,arrayList2);
        //listView2.setAdapter(adapter2);
        generateQRCode();

    }

    /********************************************/
    public void generateQRCode() {
        //得到信息对应的像素数组
        int[] pixels = generatePixels();


        if (pixels != null) {
            Bitmap bitmap = Bitmap.createBitmap(
                    QR_WIDTH, QR_HEIGHT, Bitmap.Config.ARGB_8888);

            //根据像素数组生成bitmap
            //这部分参数含义查看一下API描述，比较容易弄懂
            bitmap.setPixels(pixels, 0, QR_WIDTH, 0, 0, QR_WIDTH, QR_HEIGHT);

            //将bitmap显示到界面上
            mImageView.setImageBitmap(bitmap);

            //mScanButton.setEnabled(true);
        }
    }
    /********************************************/
    private int[] generatePixels() {
        //得到需要写入的信息
        String data = createData();

        int[] pixels = null;

        //定义编码的附加信息，这里指定以utf-8编码
        Hashtable<EncodeHintType, Object> hints = new Hashtable<>();
        hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        hints.put(EncodeHintType.MARGIN, 1);

        try {
            //这里以QRcodeWriter，将信息编码成矩阵
            //参数分别为信息、编码格式、矩阵宽、矩阵高及附加信息
            BitMatrix bitMatrix = new QRCodeWriter()
                    .encode(data, BarcodeFormat.QR_CODE, QR_WIDTH, QR_HEIGHT, hints);

            //得到矩阵后，就可以根据矩阵得到像素数组
            pixels = new int[QR_WIDTH * QR_HEIGHT];
            for (int y = 0; y < QR_HEIGHT; ++y) {
                for (int x = 0; x < QR_WIDTH; ++x) {
                    //轮寻矩阵的每一个元素
                    //我在这里比较传统的，用黑白来形成像素数组
                    //按需可进行调整
                    pixels[y * QR_WIDTH + x] =
                            bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE;
                }
            }
        } catch (WriterException e) {
            Log.d("ZJTest", e.toString());
        }

        return pixels;
    }
    /********************************************/
    private String createData() {
        String data = null;

        try {
            JSONObject jsonObject1 = new JSONObject();
            JSONObject jsonObject2 = new JSONObject();

            int sum = 0;
            int Object_price_ToInteger;

            for(int i = 0; i< ListviewActivity.arrayList_id.size(); i++){
                Object_price_ToInteger = Integer.parseInt(ListviewActivity.arrayList_price.get(i).toString());
                sum = sum + Object_price_ToInteger;
                /*myRef.child("shopping cart")
                        .child(ListviewActivity.arrayList_id.get(i).toString())
                        .child("price")
                        .setValue(Object_price_ToInteger);*/

                jsonObject1.put(ListviewActivity.arrayList_id.get(i).toString(),Object_price_ToInteger);
                Log.i("json",jsonObject1.toString());
            }
            jsonObject2.put("shop",jsonObject1);
            data = jsonObject2.toString();
            Log.i("json",data);

        } catch (JSONException e) {
            Log.d("What???????????????????", e.toString());
        }
        return data;
    }
    /*private String createData() {
        String data;
        data = "Long Time No 系";
        return data;
    }*/
    public void exit(View view){
        finish();
    }

    protected void onResume(){
        super.onResume();

        FirebaseDatabase database = FirebaseDatabase.getInstance();
        final DatabaseReference myRef = database.getReference("customer/id/password/flag");
        myRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                String value = dataSnapshot.getValue(String.class);
                Log.i("json",value);
                if(open==1){
                    if(value.equals("complete")){
                        if(!isFinishing()){
                            Log.i("json2","OK");
                            new AlertDialog.Builder(TestActivity.this)
                                    .setTitle("結帳成功")
                                    //.setMessage("")
                                    .setNegativeButton("Cancel", null)
                                    .show();
                            //myRef.setValue(false);
                            //Log.i("json2",value);
                            open=0;
                        }
                    }
                }


            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.w("json", "Failed to read value.", error.toException());
            }
        });
    }
}
