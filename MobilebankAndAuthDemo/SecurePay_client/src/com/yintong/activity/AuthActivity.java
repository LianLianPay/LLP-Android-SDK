
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.yintong.pay.utils.BaseHelper;
import com.yintong.pay.utils.Constants;
import com.yintong.pay.utils.Md5Algorithm;
import com.yintong.pay.utils.MobileSecurePayer;
import com.yintong.pay.utils.PayOrder;
import com.yintong.pay.utils.ResultChecker;
import com.yintong.pay.utils.Rsa;
import com.yintong.secure.demo.env.EnvConstants;
import com.yintong.secure.simple.demo.R;

/**
 * 认证支付
 * @author kristain
 */
public class AuthActivity extends Activity {

    private Button jump_btn;
//    private TestButton ts_btn;

    // 支付验证方式 0：标准版；1：卡前置方式；2：单独签约
    // 可以在menu中选择支付方式
    private int pay_type_flag = 0;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.authpay);
//        ts_btn = (TestButton) findViewById(R.id.test_btn);
//        ts_btn.init();
        
        jump_btn = (Button) findViewById(R.id.jump_btn);

        jump_btn.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {

                PayOrder order = null;
                if (pay_type_flag == 0) {
                    // 手势码+短信验证方式
                    order = constructGesturePayOrder();
                    // order.setShareing_data(((TextView)
                    // findViewById(R.id.share_money)).getText()
                    // .toString().trim());
                    String content4Pay = BaseHelper.toJSONString(order);
                    // 关键 content4Pay 用于提交到支付SDK的订单支付串，如遇到签名错误的情况，请将该信息帖给我们的技术支持
                    Log.i(AuthActivity.class.getSimpleName(), content4Pay);

                    MobileSecurePayer msp = new MobileSecurePayer();
                    boolean bRet = msp.payAuth(content4Pay, mHandler,
                            Constants.RQF_PAY, AuthActivity.this, false);

                    Log.i(AuthActivity.class.getSimpleName(), String.valueOf(bRet));

                } else if (pay_type_flag == 1) {
                    // 卡前置方式
                    if (TextUtils.isEmpty(((EditText) findViewById(R.id.bankno))
                            .getText().toString())
                            && TextUtils.isEmpty(((EditText) findViewById(R.id.agree_no))
                                    .getText().toString())) {
                        Toast.makeText(AuthActivity.this, "卡前置模式，必须填入银行卡卡号或者协议号", Toast.LENGTH_LONG)
                                .show();
                        return;
                    }
                    order = constructPreCardPayOrder();
                    // order.setShareing_data(((TextView)findViewById(R.id.share_money)).getText().toString().trim());
                    String content4Pay = BaseHelper.toJSONString(order);
                    // 关键 content4Pay 用于提交到支付SDK的订单支付串，如遇到签名错误的情况，请将该信息帖给我们的技术支持
                    Log.i(AuthActivity.class.getSimpleName(), content4Pay);

                    MobileSecurePayer msp = new MobileSecurePayer();
                    boolean bRet = msp.payAuth(content4Pay, mHandler,
                            Constants.RQF_PAY, AuthActivity.this, false);

                    Log.i(AuthActivity.class.getSimpleName(), String.valueOf(bRet));

                } else if (pay_type_flag == 2) {
                    order = constructSignCard();
                    String content4Pay = BaseHelper.toJSONString(order);

                    // 关键 content4Pay 用于提交到支付SDK的订单支付串，如遇到签名错误的情况，请将该信息帖给我们的技术支持
                    Log.i(AuthActivity.class.getSimpleName(), content4Pay);

                    MobileSecurePayer msp = new MobileSecurePayer();
                    boolean bRet = msp.paySign(content4Pay, mHandler,
                            Constants.RQF_PAY, AuthActivity.this, false);

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
                            	BaseHelper.showDialog(AuthActivity.this, "提示",
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
                                
                                BaseHelper.showDialog(AuthActivity.this, "提示",
                                        "支付成功，交易状态码：" + retCode+" 返回报文:"+strRet,
                                        android.R.drawable.ic_dialog_alert);
                            }
    
                        } else if (Constants.RET_CODE_PROCESS.equals(retCode)) {
                            // TODO 处理中，掉单的情形
                            String resulPay = objContent.optString("result_pay");
                            if (Constants.RESULT_PAY_PROCESSING
                                    .equalsIgnoreCase(resulPay)) {
                                BaseHelper.showDialog(AuthActivity.this, "提示",
                                        objContent.optString("ret_msg") + "交易状态码："
                                                + retCode+" 返回报文:"+strRet,
                                        android.R.drawable.ic_dialog_alert);
                            }

                        } else {
                            // TODO 失败
                            BaseHelper.showDialog(AuthActivity.this, "提示", retMsg
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
        // MD5 签名方式
//        order.setSign_type(PayOrder.SIGN_TYPE_MD5);
        // RSA 签名方式
         order.setSign_type(PayOrder.SIGN_TYPE_RSA);
        order.setValid_order("100");

        order.setUser_id(((EditText) findViewById(R.id.userid))
                .getText().toString());
        order.setId_no(((EditText) findViewById(R.id.idcard)).getText()
                .toString());
        order.setAcct_name(((EditText) findViewById(R.id.name))
                .getText().toString());
        order.setMoney_order(((EditText) findViewById(R.id.money))
                .getText().toString());
        
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

        order.setUser_id(((EditText) findViewById(R.id.userid))
                .getText().toString());
        order.setId_no(((EditText) findViewById(R.id.idcard)).getText()
                .toString());

        order.setAcct_name(((EditText) findViewById(R.id.name))
                .getText().toString());

        order.setCard_no(((EditText) findViewById(R.id.bankno))
                .getText().toString());
        
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_xml_file, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (R.id.menu_1 == item.getItemId()) {
            findViewById(R.id.layout_precard).setVisibility(View.GONE);
            findViewById(R.id.money).setVisibility(View.VISIBLE);
            pay_type_flag = 0;
        } else if (R.id.menu_2 == item.getItemId()) {
            pay_type_flag = 1;
            findViewById(R.id.layout_precard).setVisibility(View.VISIBLE);
            findViewById(R.id.money).setVisibility(View.VISIBLE);
            findViewById(R.id.agree_no).setVisibility(View.VISIBLE);
        } else if (R.id.menu_3 == item.getItemId()) {
            pay_type_flag = 2;
            findViewById(R.id.layout_precard).setVisibility(View.VISIBLE);
            findViewById(R.id.money).setVisibility(View.GONE);
            findViewById(R.id.agree_no).setVisibility(View.GONE);

        }

        return super.onOptionsItemSelected(item);
    }

}
