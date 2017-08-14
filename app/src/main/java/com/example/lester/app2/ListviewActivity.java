package com.example.lester.app2;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
//import android.widget.ScrollView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ListviewActivity extends AppCompatActivity {

    private final static String TAG = ListviewActivity.class.getSimpleName();
    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private TextView mDataField;
    //private String mDeviceName;
    private String mDeviceAddress;
    private BLE_Service mBluetoothLeService;
    private boolean mConnected = false;

    private static String data;
    private ListAdapter adapter;
    private ListView listview;
    private ArrayList arrayList;
    public static ArrayList arrayList_price;
    public static ArrayList arrayList_id;
    private boolean Switch = true;
    private HashMap<String, String> hashMap;
    //private List<HashMap<String , String>> list;

    private FirebaseDatabase database;
    private DatabaseReference readRef;
    private String name;
    private String about;
    private ProgressDialog dialog;
    //private String number;
    private Button btnSend;
    /**
     * ****************************ServiceConnected********************************************
     */
    // Code to manage Service lifecycle.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mBluetoothLeService = ((BLE_Service.LocalBinder) service).getService();
            if (!mBluetoothLeService.initialize()) {
                Log.e(TAG, "Unable to initialize Bluetooth");
                finish();
            }
            Log.e(TAG, "mBluetoothLeService is okay");
            // Automatically connects to the device upon successful start-up initialization.
            //mBluetoothLeService.connect(mDeviceAddress);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            mBluetoothLeService = null;
        }
    };
    /**
     * ****************************BroadcastReceiver********************************************
     */
    // Handles various events fired by the Service.
    // ACTION_GATT_CONNECTED: connected to a GATT server.
    // ACTION_GATT_DISCONNECTED: disconnected from a GATT server.
    // ACTION_GATT_SERVICES_DISCOVERED: discovered GATT services.
    // ACTION_DATA_AVAILABLE: received data from the device.  This can be a result of read
    //                        or notification operations.
    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (BLE_Service.ACTION_GATT_CONNECTED.equals(action)) {
                Log.e(TAG, "Only gatt, just wait");
            } else if (BLE_Service.ACTION_GATT_DISCONNECTED.equals(action)) {
                mConnected = false;
                invalidateOptionsMenu();
                btnSend.setEnabled(false);
                clearUI();
            } else if (BLE_Service.ACTION_GATT_SERVICES_DISCOVERED.equals(action))
            {
                mConnected = true;
                mDataField.setText("");
                ShowDialog();
                btnSend.setEnabled(true);
                Log.e(TAG, "In what we need");
                invalidateOptionsMenu();
            } else if (BLE_Service.ACTION_DATA_AVAILABLE.equals(action)) {
                Log.e(TAG, "RECV DATA");
                data = intent.getStringExtra(BLE_Service.EXTRA_DATA);
                if (data != null) {
                    if (mDataField.length() > 500)
                        mDataField.setText("");
                    /**
                     ----- data=輸出數據 -----
                     */
                    //mDataField.append(data+"\n");
                    //String data2;
                    //data2 = data.replaceAll(" �","");
                    if (Switch) {
                        arrayList_id.add(data);
                        hashMap = new HashMap<>();
                        hashMap.put("title", data);
                        Switch = false;
                    } else {
                        int i = Integer.parseInt(data);
                        String DataToInteger = Integer.toString(i);
                        arrayList_price.add(i);
                        hashMap.put("text", DataToInteger);
                        arrayList.add(hashMap);
                        listview.setAdapter(adapter);
                        Switch = true;
                    }
                }
            }
        }
    };

    private void clearUI() {
        mDataField.setText(R.string.no_data);
    }

    /**
     * ****************************onCreate********************************************
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_listview);

        final Intent intent = getIntent();
        //mDeviceName = intent.getStringExtra(EXTRAS_DEVICE_NAME);
        mDeviceAddress = intent.getStringExtra(EXTRAS_DEVICE_ADDRESS);
        // Sets up UI references.
        mDataField = (TextView) findViewById(R.id.data_value);
        //edtSend = (EditText) this.findViewById(R.id.edtSend);
        //edtSend.setText("");
        //svResult = (ScrollView) this.findViewById(R.id.svResult);
        btnSend = (Button) this.findViewById(R.id.btnSend);
        btnSend.setOnClickListener(new ClickEvent());
        btnSend.setEnabled(false);
        //getActionBar().setTitle(mDeviceName);
        //getActionBar().setDisplayHomeAsUpEnabled(true);
        Intent gattServiceIntent = new Intent(this, BLE_Service.class);
        Log.d(TAG, "Try to bindService=" + bindService(gattServiceIntent, mServiceConnection, BIND_AUTO_CREATE));

        registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());

        listview = (ListView) findViewById(R.id.listView2);
        //使用arrayList2存入HashMap，用來顯示ListView上面的文字。
        arrayList = new ArrayList();
        arrayList_id = new ArrayList();
        arrayList_price = new ArrayList();
        adapter = new SimpleAdapter(
                this,
                arrayList,
                android.R.layout.simple_list_item_2,
                new String[]{"title", "text"},
                new int[]{android.R.id.text1, android.R.id.text2});
        // 5個參數 : context , List , layout , key1 & key2 , text1 & text2

        database = FirebaseDatabase.getInstance();
        /******************************************************************************************/
        listview.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                new AlertDialog.Builder(ListviewActivity.this)
                        .setTitle("want to delele?")
                        .setMessage("Want to delete item-" + (position + 1) + " ?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                arrayList.remove(position);
                                arrayList_id.remove(position);
                                arrayList_price.remove(position);
                                //arrayList2.notifyAll();
                                ((BaseAdapter) adapter).notifyDataSetChanged();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
                return true;
            }
        });
        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, final int position, long id) {
                String number2 = arrayList_id.get(position).toString();
                firebase(number2);
            }
        });
        /*****************************************************************************************/
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*registerReceiver(mGattUpdateReceiver, makeGattUpdateIntentFilter());
        if (mBLE_Service != null) {
            final boolean result = mBluetoothLeService.connect(mDeviceAddress);
            Log.d(TAG, "Connect request result=" + result);
        }*/
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(mGattUpdateReceiver);
        unbindService(mServiceConnection);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //this.unregisterReceiver(mGattUpdateReceiver);
        //unbindService(mServiceConnection);
        if (mBluetoothLeService != null) {
            mBluetoothLeService.close();
            mBluetoothLeService = null;
        }
        Log.d(TAG, "We are in destroy");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.gatt_services, menu);
        if (mConnected) {
            menu.findItem(R.id.menu_connect).setVisible(false);
            menu.findItem(R.id.menu_disconnect).setVisible(true);
            mDataField.setVisibility(View.GONE);
            //menu.findItem(R.id.menu_refresh2).setActionView(null);
        } else {
            menu.findItem(R.id.menu_connect).setVisible(true);
            menu.findItem(R.id.menu_disconnect).setVisible(false);
            mDataField.setVisibility(View.VISIBLE);
            //menu.findItem(R.id.menu_refresh2).setActionView(
            //R.layout.actionbar_progress);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_connect:
                mBluetoothLeService.connect(mDeviceAddress);
                return true;
            case R.id.menu_disconnect:
                mBluetoothLeService.disconnect();
                return true;
            case android.R.id.home:
                if (mConnected) {
                    mBluetoothLeService.disconnect();
                    mConnected = false;
                }
                onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void ShowDialog() {
        Toast.makeText(this, "連接成功", Toast.LENGTH_SHORT).show();
    }

    class ClickEvent implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            if (v == btnSend) {
                if (!mConnected) return;
                /*if (edtSend.length() < 1) {
                    Toast.makeText(ListviewActivity.this, "不能為空白", Toast.LENGTH_SHORT).show();
                    return;
                }*/
                //mBluetoothLeService.WriteValue("11");
                new AlertDialog.Builder(ListviewActivity.this)
                        .setTitle("確認視窗")
                        .setMessage("確定要結帳嗎?")
                        .setPositiveButton("確定",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent intent = new Intent(ListviewActivity.this, TestActivity.class);
                                        startActivity(intent);
                                        finish();
                                    }
                                })
                        .setNegativeButton("取消",
                                new DialogInterface.OnClickListener() {

                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        // TODO Auto-generated method stub
                                    }
                                }).show();

                //mBluetoothLeService.WriteValue(edtSend.getText().toString());

                /*InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm.isActive())
                    imm.hideSoftInputFromWindow(edtSend.getWindowToken(), 0);*/
                //Toast.makeText(ListviewActivity.this, edtSend.getText().toString(), Toast.LENGTH_SHORT).show();
                //if(edtSend.getText().toString()=="11")
                //arrayList.clear();
                //todo Send data
            }
        }
    }

    private static IntentFilter makeGattUpdateIntentFilter() {
        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BLE_Service.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BLE_Service.ACTION_GATT_DISCONNECTED);
        intentFilter.addAction(BLE_Service.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BLE_Service.ACTION_DATA_AVAILABLE);
        intentFilter.addAction(BluetoothDevice.ACTION_UUID);
        return intentFilter;
    }

    private void firebase(final String number) {
        dialog = ProgressDialog.show(ListviewActivity.this, "讀取中", "",true);
        readRef = database.getReference("shop");
        readRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                name = dataSnapshot.child("/" + number + "/name").getValue(String.class);
                about = dataSnapshot.child("/" + number + "/about").getValue(String.class);
                dialog.dismiss();
                new AlertDialog.Builder(ListviewActivity.this)
                        .setTitle(name)
                        .setMessage(about)
                        .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        })
                        .show();
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.i("", "Failed to read value.", error.toException());
            }
        });
    }
    /**
     * ProgressDialog 測試
     */
    /*public void Click(View arg0) {
        dialog = ProgressDialog.show(ListviewActivity.this,
                "讀取中", "",true);
        new Thread(new Runnable(){
            @Override
            public void run() {
                try{
                    Thread.sleep(3000);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
                finally{
                    dialog.dismiss();
                }
            }
        }).start();
    }*/
}