package com.yintong.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.yintong.secure.simple.demo.R;

public class MainActivity extends Activity implements View.OnClickListener {
    private TextView mTvSecurePay, mTvMobileBankPay;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        initEvent();
    }

    private void initView(){
        mTvSecurePay = (TextView)findViewById(R.id.demo_tv_secure_pay);
        mTvMobileBankPay = (TextView)findViewById(R.id.demo_tv_mobilebank_pay);
    }

    private void initEvent(){
        mTvSecurePay.setOnClickListener(this);
        mTvMobileBankPay.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.demo_tv_secure_pay){
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, AuthActivity.class);
            startActivity(intent);
        }else if (view.getId() == R.id.demo_tv_mobilebank_pay){
            Intent intent = new Intent();
            intent.setClass(MainActivity.this, MobileBankActivity.class);
            startActivity(intent);
        }
    }
}
