
package com.yintong.auth.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.yintong.auth.R;
import com.yintong.auth.pay.utils.BaseHelper;
import com.yintong.auth.pay.utils.Constants;
import com.yintong.auth.pay.utils.Md5Algorithm;
import com.yintong.auth.pay.utils.MobileSecurePayer;
import com.yintong.auth.pay.utils.PayOrder;
import com.yintong.auth.pay.utils.Rsa;
import com.yintong.auth.secure.demo.env.EnvConstants;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 新认证(全渠道)支付
 */
public class NewAuthActivity extends Activity {
    private View mLayoutPrecard;
    private EditText mEtMoney, mEtAgreeNo, mEtBankno, mEtUserId, mEtIdcard, mEtName;
    private Button jump_btn;
    private RadioGroup mRgType;

    // 支付验证方式 0：标准版；1：卡前置方式；2：单独签约
    private int pay_type_flag = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authpay);

        initView();
        initEvent();
    }

    private void initView(){
        mLayoutPrecard = findViewById(R.id.layout_precard);
        mRgType = (RadioGroup)findViewById(R.id.rg_type);
        jump_btn = (Button) findViewById(R.id.jump_btn);
        mEtMoney = (EditText) findViewById(R.id.money);
        mEtAgreeNo = (EditText)findViewById(R.id.agree_no);
        mEtBankno = (EditText) findViewById(R.id.bankno);
        mEtUserId = (EditText) findViewById(R.id.userid);
        mEtIdcard = (EditText) findViewById(R.id.idcard);
        mEtName = (EditText) findViewById(R.id.name);
    }

    private void initEvent(){
        mRgType.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.rb_not_precard){
                    pay_type_flag = 0;
                    mLayoutPrecard.setVisibility(View.GONE);
                    mEtMoney.setVisibility(View.VISIBLE);
                }else if (checkedId == R.id.rb_precard){
                    pay_type_flag = 1;
                    mLayoutPrecard.setVisibility(View.VISIBLE);
                    mEtMoney.setVisibility(View.VISIBLE);
                    mEtAgreeNo.setVisibility(View.VISIBLE);
                }else if (checkedId == R.id.rb_sign){
                    pay_type_flag = 2;
                    mLayoutPrecard.setVisibility(View.VISIBLE);
                    mEtMoney.setVisibility(View.GONE);
                    mEtAgreeNo.setVisibility(View.GONE);
                }
            }
        });
        jump_btn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                PayOrder order = null;
                if (pay_type_flag == 0) { // 非卡前置方式
                    order = constructStandardPayOrder();
                    String content4Pay = BaseHelper.toJSONString(order);

                    MobileSecurePayer msp = new MobileSecurePayer();
                    boolean bRet = msp.payNewAuth(content4Pay, mHandler,
                            Constants.RQF_PAY, NewAuthActivity.this, false);
                    Log.i(NewAuthActivity.class.getSimpleName(), String.valueOf(bRet));
                } else if (pay_type_flag == 1) { // 卡前置方式
                    if (TextUtils.isEmpty(mEtBankno.getText().toString())
                            && TextUtils.isEmpty(mEtAgreeNo.getText().toString())) {
                        Toast.makeText(NewAuthActivity.this,
                                "卡前置模式，必须填入银行卡卡号或者协议号", Toast.LENGTH_LONG)
                                .show();
                        return;
                    }
                    order = constructPreCardPayOrder();
                    String content4Pay = BaseHelper.toJSONString(order);

                    MobileSecurePayer msp = new MobileSecurePayer();
                    boolean bRet = msp.payNewAuth(content4Pay, mHandler,
                            Constants.RQF_PAY, NewAuthActivity.this, false);

                    Log.i(NewAuthActivity.class.getSimpleName(), String.valueOf(bRet));
                } else if (pay_type_flag == 2) {  //卡签约
                    order = constructSignCard();
                    String content4Pay = BaseHelper.toJSONString(order);

                    MobileSecurePayer msp = new MobileSecurePayer();
                    boolean bRet = msp.payNewAuthSign(content4Pay, mHandler,
                                    Constants.RQF_SIGN, NewAuthActivity.this, false);

                }
            }
        });
    }

    private Handler mHandler = createHandler();
    private Handler createHandler() {
        return new Handler() {
            public void handleMessage(Message msg) {
                String strRet = (String) msg.obj;
                switch (msg.what) {
                    case Constants.RQF_PAY: {
                        JSONObject objContent = BaseHelper.string2JSON(strRet);
                        String retCode = objContent.optString("ret_code");
                        String retMsg = objContent.optString("ret_msg");

                        // 成功
                        if (Constants.RET_CODE_SUCCESS.equals(retCode)) {
                            // TODO 卡前置模式返回的银行卡绑定协议号，用来下次支付时使用，此处仅作为示例使用。正式接入时去掉
                            if(2 == pay_type_flag) {
                            	BaseHelper.showDialog(NewAuthActivity.this, "提示",
                                        "签约成功，交易状态码：" + retCode+" 返回报文:"+strRet,
                                        android.R.drawable.ic_dialog_alert);
                            	break;
                            }else {
                            	if (pay_type_flag == 1) {
                                    TextView tv_agree_no = (TextView) findViewById(R.id.tv_agree_no);
                                    tv_agree_no.setVisibility(View.VISIBLE);
                                    tv_agree_no.setText(objContent.optString(
                                            "agreementno", ""));
                                }
                                
                                BaseHelper.showDialog(NewAuthActivity.this, "提示",
                                        "支付成功，交易状态码：" + retCode+" 返回报文:"+strRet,
                                        android.R.drawable.ic_dialog_alert);
                            }
    
                        } else if (Constants.RET_CODE_PROCESS.equals(retCode)) {
                            // TODO 处理中，掉单的情形
                            String resulPay = objContent.optString("result_pay");
                            if (Constants.RESULT_PAY_PROCESSING
                                    .equalsIgnoreCase(resulPay)) {
                                BaseHelper.showDialog(NewAuthActivity.this, "提示",
                                        objContent.optString("ret_msg") + "交易状态码："
                                                + retCode+" 返回报文:"+strRet,
                                        android.R.drawable.ic_dialog_alert);
                            }

                        } else {
                            // TODO 失败
                            BaseHelper.showDialog(NewAuthActivity.this, "提示", retMsg
                                    + "，交易状态码:" + retCode+" 返回报文:"+strRet,
                                    android.R.drawable.ic_dialog_alert);
                        }
                    }
                        break;
                }
                super.handleMessage(msg);
            }
        };

    }

    private PayOrder constructStandardPayOrder() {
        SimpleDateFormat dataFormat = new SimpleDateFormat(
                "yyyyMMddHHmmss");
        Date date = new Date();
        String timeString = dataFormat.format(date);

        PayOrder order = new PayOrder();
        order.setBusi_partner("101001");
        order.setNo_order(timeString);
        order.setDt_order(timeString);
        order.setName_goods("龙禧大酒店中餐厅：2-3人浪漫套餐X1");
        order.setNotify_url(Constants.NOTIFY_URL);
        // MD5 签名方式
//        order.setSign_type(PayOrder.SIGN_TYPE_MD5);
        // RSA 签名方式
         order.setSign_type(PayOrder.SIGN_TYPE_RSA);
        order.setValid_order("100");

        order.setUser_id(mEtUserId.getText().toString());
        order.setId_no(mEtIdcard.getText().toString());
        order.setAcct_name(mEtName.getText().toString());
        order.setMoney_order(mEtMoney.getText().toString());
        
        // 风险控制参数
        order.setRisk_item(constructRiskItem());

        order.setFlag_modify("1");
        String sign = "";
        order.setOid_partner(EnvConstants.PARTNER);
        String content = BaseHelper.sortParam(order);
        // MD5 签名方式, 签名方式包括两种，一种是MD5，一种是RSA 这个在商户站管理里有对验签方式和签名Key的配置。
//        sign = Md5Algorithm.getInstance().sign(content,
//                EnvConstants.MD5_KEY);
        // RSA 签名方式
         sign = Rsa.sign(content, EnvConstants.RSA_PRIVATE);
        order.setSign(sign);
        return order;
    }

    private PayOrder constructPreCardPayOrder() {

        SimpleDateFormat dataFormat = new SimpleDateFormat(
                "yyyyMMddHHmmss");
        Date date = new Date();
        String timeString = dataFormat.format(date);

        PayOrder order = new PayOrder();
        order.setBusi_partner("101001");
        order.setNo_order(timeString);
        order.setDt_order(timeString);
        order.setName_goods("龙禧大酒店中餐厅：2-3人浪漫套餐X1");
        order.setNotify_url(Constants.NOTIFY_URL);
        // MD5 签名方式
        order.setSign_type(PayOrder.SIGN_TYPE_MD5);
        // RSA 签名方式
        // order.setSign_type(PayOrder.SIGN_TYPE_RSA);

        order.setValid_order("100");

        order.setUser_id(mEtUserId.getText().toString());
        order.setId_no(mEtIdcard.getText().toString());

        order.setAcct_name(mEtName.getText().toString());
        order.setMoney_order(mEtMoney.getText().toString());

        // 银行卡卡号，该卡首次支付时必填
        order.setCard_no(mEtBankno.getText().toString());
        // 银行卡历次支付时填写，可以查询得到，协议号匹配会进入SDK，
        order.setNo_agree(mEtAgreeNo.getText().toString());
        
        // 风险控制参数
        order.setRisk_item(constructRiskItem());

        String sign = "";
        order.setOid_partner(EnvConstants.PARTNER);
        String content = BaseHelper.sortParam(order);
        // MD5 签名方式
        sign = Md5Algorithm.getInstance().sign(content,
                EnvConstants.MD5_KEY);
        // RSA 签名方式
//         sign = Rsa.sign(content, EnvConstants.RSA_PRIVATE);
        order.setSign(sign);
        return order;
    }

    private PayOrder constructSignCard() {

        PayOrder order = new PayOrder();
        order.setSign_type(PayOrder.SIGN_TYPE_MD5);
        // RSA 签名方式
        // order.setSign_type(PayOrder.SIGN_TYPE_RSA);

        order.setUser_id(mEtUserId.getText().toString());
        order.setId_no(mEtIdcard.getText().toString());

        order.setAcct_name(mEtName.getText().toString());

        order.setCard_no(mEtBankno.getText().toString());
        
        // 风险控制参数
        order.setRisk_item(constructRiskItem());
        
        String sign = "";
        order.setOid_partner(EnvConstants.PARTNER);
        String content = BaseHelper.sortParamForSignCard(order);
        // MD5 签名方式
        sign = Md5Algorithm.getInstance().sign(content,
                EnvConstants.MD5_KEY);
        // RSA 签名方式
        // sign = Rsa.sign(content, EnvConstants.RSA_PRIVATE);
        order.setSign(sign);
        return order;
    }
    
    private String constructRiskItem() {
        JSONObject mRiskItem = new JSONObject();
        try {
            mRiskItem.put("user_info_bind_phone", "13958069593");
            mRiskItem.put("user_info_dt_register", "201407251110120");
            mRiskItem.put("frms_ware_category", "4.0");
            mRiskItem.put("request_imei", "1122111221");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mRiskItem.toString();
    }

}
