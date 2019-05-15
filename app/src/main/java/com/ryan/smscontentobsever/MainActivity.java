package com.ryan.smscontentobsever;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.EditText;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    public static final int SMS_CODE = 1;
    public static final int READ_SMS_PERMISSION = 2;

    private EditText mSmsCode;
    private SMSContentObserver smsContentObserver;

    Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case SMS_CODE:
                    mSmsCode.setText(msg.obj.toString());
                    break;
                default:
                    break;
            }

        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mSmsCode = (EditText) findViewById(R.id.smsCode);
        checkPermissionsAndRegisterObserver(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //注销smsContentObserver
        getContentResolver().unregisterContentObserver(smsContentObserver);
    }

    /**
     * 注册SmsContentObserver
     * <p>
     * 为什么注册的时候使用的URI是"content://sms/"，而直接是"content://sms/inbox"?
     * 通过测试发现只能监听此Uri “content://sms/”
     * 而不能监听其他的Uri，比如"content://sms/inbox"等
     */
    private void registerSmsContentObserver() {
        smsContentObserver = new SMSContentObserver(this, mHandler);
        getContentResolver().registerContentObserver(Uri.parse("content://sms/"), true, smsContentObserver);
    }

    /**
     * 检测是否有赋予READ_SMS权限，
     * 若有则注册SmsContentObserver
     * 若没有则提醒用户授权
     *
     * @param context
     */
    private void checkPermissionsAndRegisterObserver(Context context) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.READ_SMS}, READ_SMS_PERMISSION);
        } else {
            registerSmsContentObserver();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case READ_SMS_PERMISSION:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    registerSmsContentObserver();
                } else {
                    Toast.makeText(this, "You denied the permission", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                break;
        }
    }
}
