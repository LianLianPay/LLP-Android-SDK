
package com.yintong.activity;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.yintong.pay.utils.BaseHelper;
import com.yintong.pay.utils.Constants;
import com.yintong.pay.utils.Md5Algorithm;
import com.yintong.pay.utils.MobileSecurePayer;
import com.yintong.pay.utils.PayOrder;
import com.yintong.secure.demo.env.EnvConstants;
import com.yintong.secure.simple.demo.R;

/**
 * 快捷支付
 * @author kristain
 *
 */
public class StandActivity extends Activity implements OnClickListener {

    // 支付验证方式 0：标准版本， 1：卡前置方式，接入时，只需要配置一种即可，Demo为说明用。可以在menu中选择支付方式。
    private int pay_type_flag = 0;
    private boolean is_preauth = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.standpay);

        findViewById(R.id.jump_btn).setOnClickListener(this);
        findViewById(R.id.preauth_btn).setOnClickListener(this);

    }

    public void onClick(View v) {
        // TODO Auto-generated method stub
        if (v == findViewById(R.id.jump_btn)) {
            is_preauth = false;
        } else if (v == findViewById(R.id.preauth_btn)) {
            is_preauth = true;
        }
        PayOrder order = null;
        if (pay_type_flag == 0) {
            // 标准模式
            order = constructGesturePayOrder();
        } else if (pay_type_flag == 1) {
            // TODO 卡前置方式, 如果传入的是卡号，卡号必须大于等于14位
            if (TextUtils
                    .isEmpty(((EditText) findViewById(R.id.bankno))
                            .getText().toString())
                    && TextUtils
                            .isEmpty(((EditText) findViewById(R.id.agree_no))
                                    .getText().toString())) {
                Toast.makeText(StandActivity.this,
                        "卡前置模式，必须填入银行卡卡号或者协议号", Toast.LENGTH_LONG)
                        .show();
                return;
            }
            order = constructPreCardPayOrder();

        }
        
//        order.setCard_no("9558374827648478467");
        
        String content4Pay = BaseHelper.toJSONString(order);

        // 关键 content4Pay 用于提交到支付SDK的订单支付串，如遇到签名错误的情况，请将该信息帖给我们的技术支持
        Log.i(StandActivity.class.getSimpleName(), content4Pay);
        MobileSecurePayer msp = new MobileSecurePayer();
        if (is_preauth) {
            boolean bRet = msp.payPreAuth(content4Pay, mHandler,
                    Constants.RQF_PAY, StandActivity.this, false);
            Log.i(StandActivity.class.getSimpleName(), String.valueOf(bRet));
        } else {
            boolean bRet = msp.pay(content4Pay, mHandler,
                    Constants.RQF_PAY, StandActivity.this, false);
            Log.i(StandActivity.class.getSimpleName(), String.valueOf(bRet));
        }

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
                            if (pay_type_flag == 1) {
                                TextView tv_agree_no = (TextView) findViewById(R.id.tv_agree_no);
                                tv_agree_no.setVisibility(View.VISIBLE);
                                tv_agree_no.setText(objContent.optString(
                                        "agreementno", ""));
                            }
                            BaseHelper.showDialog(StandActivity.this, "提示",
                                    "支付成功，交易状态码：" + retCode +" 返回报文:"+strRet,
                                    android.R.drawable.ic_dialog_alert);
                        } else if (Constants.RET_CODE_PROCESS.equals(retCode)) {
                            // TODO 处理中，掉单的情形
                            String resulPay = objContent.optString("result_pay");
                            if (Constants.RESULT_PAY_PROCESSING
                                    .equalsIgnoreCase(resulPay)) {
                                BaseHelper.showDialog(StandActivity.this, "提示",
                                        objContent.optString("ret_msg") + "交易状态码："
                                                + retCode +" 返回报文:"+strRet,
                                        android.R.drawable.ic_dialog_alert);
                            }

                        } else {
                            // TODO 失败
                            BaseHelper.showDialog(StandActivity.this, "提示", retMsg
                                    + "，交易状态码:" + retCode +" 返回报文:"+strRet,
                                    android.R.drawable.ic_dialog_alert);
                        }
                    }
                        break;
                }
                super.handleMessage(msg);
            }
        };

    }

    private PayOrder constructGesturePayOrder() {
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
        // TODO MD5 签名方式
        order.setSign_type(PayOrder.SIGN_TYPE_MD5);
        // TODO RSA 签名方式
        // order.setSign_type(PayOrder.SIGN_TYPE_RSA);
        order.setValid_order("100");

        order.setUser_id(((EditText) findViewById(R.id.userid))
                .getText().toString());
        order.setId_no(((EditText) findViewById(R.id.idcard)).getText()
                .toString());
        order.setAcct_name(((EditText) findViewById(R.id.name))
                .getText().toString());
        order.setMoney_order(((EditText) findViewById(R.id.money))
                .getText().toString());

        int id = ((RadioGroup) findViewById(R.id.flag_modify_group))
                .getCheckedRadioButtonId();
        if (id == R.id.flag_modify_0) {
            order.setFlag_modify("0");
        } else if (id == R.id.flag_modify_1) {
            order.setFlag_modify("1");
        }
        // 风险控制参数
        order.setRisk_item(constructRiskItem());

        String sign = "";
        if (is_preauth) {
            order.setOid_partner(EnvConstants.PARTNER_PREAUTH);
        } else {
            order.setOid_partner(EnvConstants.PARTNER);
        }
        String content = BaseHelper.sortParam(order);
        // TODO MD5 签名方式, 签名方式包括两种，一种是MD5，一种是RSA 这个在商户站管理里有对验签方式和签名Key的配置。
        if (is_preauth) {
            sign = Md5Algorithm.getInstance().sign(content, EnvConstants.MD5_KEY_PREAUTH);
        } else {
            sign = Md5Algorithm.getInstance().sign(content, EnvConstants.MD5_KEY);
        }
        // RSA 签名方式
        // sign = Rsa.sign(content, EnvConstants.RSA_PRIVATE);
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
        order.setSign_type(PayOrder.SIGN_TYPE_MD5);
        order.setValid_order("100");

        order.setUser_id(((EditText) findViewById(R.id.userid))
                .getText().toString());
        order.setId_no(((EditText) findViewById(R.id.idcard)).getText()
                .toString());

        order.setAcct_name(((EditText) findViewById(R.id.name))
                .getText().toString());
        order.setMoney_order(((EditText) findViewById(R.id.money))
                .getText().toString());

        // 银行卡卡号，该卡首次支付时必填
        order.setCard_no(((EditText) findViewById(R.id.bankno))
                .getText().toString());
        // 银行卡历次支付时填写，可以查询得到，协议号匹配会进入SDK，
        order.setNo_agree(((EditText) findViewById(R.id.agree_no)).getText()
                .toString());

        int id = ((RadioGroup) findViewById(R.id.flag_modify_group))
                .getCheckedRadioButtonId();
        if (id == R.id.flag_modify_0) {
            order.setFlag_modify("0");
        } else if (id == R.id.flag_modify_1) {
            order.setFlag_modify("1");
        }
        // 风险控制参数
        order.setRisk_item(constructRiskItem());

        String sign = "";
        // TODO 商户号
        if (is_preauth) {
            order.setOid_partner(EnvConstants.PARTNER_PREAUTH);
        } else {
            order.setOid_partner(EnvConstants.PARTNER);
        }
        String content = BaseHelper.sortParam(order);
        // TODO MD5 签名方式, 签名方式包括两种，一种是MD5，一种是RSA 这个在商户站管理里有对验签方式和签名Key的配置。
        if (is_preauth) {
            sign = Md5Algorithm.getInstance().sign(content, EnvConstants.MD5_KEY_PREAUTH);
        } else {
            sign = Md5Algorithm.getInstance().sign(content, EnvConstants.MD5_KEY);
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.stand_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.menu_1 == item.getItemId()) {
            findViewById(R.id.layout_precard).setVisibility(View.GONE);
            pay_type_flag = 0;
        } else if (R.id.menu_2 == item.getItemId()) {
            pay_type_flag = 1;
            findViewById(R.id.layout_precard).setVisibility(View.VISIBLE);
        }

        return super.onOptionsItemSelected(item);
    }
}
