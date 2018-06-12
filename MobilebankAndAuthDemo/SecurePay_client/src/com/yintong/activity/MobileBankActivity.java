package com.yintong.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.yintong.adapter.PaymentSelectAdapter;
import com.yintong.adapter.item.PaymentItem;
import com.yintong.enums.PayBankEnum;
import com.yintong.pay.utils.BaseHelper;
import com.yintong.pay.utils.Constants;
import com.yintong.pay.utils.MobileSecurePayer;
import com.yintong.pay.utils.PartnerConfig;
import com.yintong.pay.utils.PayOrder;
import com.yintong.pay.utils.ResultChecker;
import com.yintong.secure.demo.env.EnvConstants;
import com.yintong.secure.simple.demo.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 演示版，无需参数页，直接进入列表页
 */
public class MobileBankActivity extends Activity implements AdapterView.OnItemClickListener {
    private static final int REQUEST_CODE_PAY_SELECT = 1;
    private static final int REQUEST_CODE_PAY_SIGN = 2;

    private static final int REQUEST_CODE_PAY_LIST = 3;

    private static final String TAG = "PayListActivity";
    private ListView mLvPayment;
    private PaymentSelectAdapter mAdapter;
    private List<PaymentItem> paymentList;
    private Button mBtPay;

    private JSONObject payOrder;

    boolean isTestServer = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mobile_bank);

        initView();
        initEvent();
        initPaymentList();
        initData();
    }

    private void initView(){
        mLvPayment = (ListView)findViewById(R.id.lv_payment);
        mAdapter = new PaymentSelectAdapter(this);
        mLvPayment.setAdapter(mAdapter);
        mBtPay = (Button)findViewById(R.id.bt_pay);
    }

    private void initEvent(){
        mLvPayment.setOnItemClickListener(this);
        mBtPay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mPayItemChecked != null){
                    PayBankEnum payBank = mPayItemChecked.getPayBank();
                    doPayMobileBank(payBank.getBankCode());
                }
            }
        });
    }

    private void initData(){
        String payorderStr = doPayOrSign();
        if (!TextUtils.isEmpty(payorderStr)) {
            payOrder = BaseHelper.string2JSON(payorderStr);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    /**
     * 根据服务器端返回的支付方式列表进行显示支付方式
     * 对应的顺序：银行卡、钱包、微信、支付宝、三星Pay
     */
    private void initPaymentList(){

        paymentList = new ArrayList<PaymentItem>();
        PaymentItem payItem;

        payItem = new PaymentItem(R.drawable.bank_icbc, PayBankEnum.PAY_BANK_ICBC);
        payItem.setChecked(true);
        paymentList.add(payItem);
        mPayItemChecked = payItem;

        payItem = new PaymentItem(R.drawable.bank_abc, PayBankEnum.PAY_BANK_ABC);
        paymentList.add(payItem);

        payItem = new PaymentItem(R.drawable.bank_boc, PayBankEnum.PAY_BANK_BOC);
        paymentList.add(payItem);

        payItem = new PaymentItem(R.drawable.bank_ccb, PayBankEnum.PAY_BANK_CCB);
        paymentList.add(payItem);

        payItem = new PaymentItem(R.drawable.bank_cmb, PayBankEnum.PAY_BANK_CMB);
        paymentList.add(payItem);

        mAdapter.setData(paymentList);
    }

    private PaymentItem mPayItemChecked;
    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        if (paymentList == null || position >= paymentList.size()){
            return;
        }
        mPayItemChecked.setChecked(false);
        mPayItemChecked = paymentList.get(position);
        mPayItemChecked.setChecked(true);
        mAdapter.notifyDataSetChanged();
    }

    private void doPayMobileBank(final String bankCode){

        try {
            payOrder.put("bank_code", bankCode);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String content4Pay = payOrder.toString();

        MobileSecurePayer msp = new MobileSecurePayer();

        boolean bRet = msp.payMobileBank(content4Pay, mHandler,
                Constants.RQF_PAY, MobileBankActivity.this, isTestServer);

        Log.i(MobileBankActivity.class.getSimpleName(), String.valueOf(bRet));

    }

    /**
     * 支付成功，返回商户
     * 支付失败，根据用户的选择，继续支付或者返回商户
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_PAY_LIST && data != null && data.getExtras() != null){
            String result = (String) data.getExtras().get("result");
            Toast.makeText(this, result, Toast.LENGTH_SHORT).show();
        }
    }

    private Handler mHandler = createHandler();

    private Handler createHandler() {
        return new Handler() {
            public void handleMessage(Message msg) {
                String strRet = (String) msg.obj;

                JSONObject objContent = BaseHelper.string2JSON(strRet);
                String retCode = objContent.optString("ret_code");
                String retMsg = objContent.optString("ret_msg");

                switch (msg.what) {
                    case Constants.RQF_PAY: {

                        if (Constants.RET_CODE_SUCCESS.equals(retCode)) { // 成功
                            ResultChecker checker = new ResultChecker(strRet);
                            String pay_partnter = payOrder.optString("oid_partner", "");
                            checker.checkSign();
                            {

                                BaseHelper.showDialog(MobileBankActivity.this, "提示",
                                        "支付成功，交易状态码：" + retCode+" 返回报文:"+strRet,
                                        android.R.drawable.ic_dialog_alert);
                            }

                        } else {
                            // TODO 失败
                            BaseHelper.showDialog(MobileBankActivity.this, "提示", retMsg
                                            + "，交易状态码:" + retCode+" 返回报文:"+strRet,
                                    android.R.drawable.ic_dialog_alert);
                        }

//                        if (retCode.equals("LE0000")
//                                || retCode.equals("LE0001")
//                                || retCode.equals("LE0002")){
//
//                            finish();
//                        }
//                        else {
//                            BaseHelper.showDialog(PayListActivity.this, "提示", retMsg
//                                            + "，交易状态码:" + retCode,
//                                    android.R.drawable.ic_dialog_alert);
//                        }

                    }
                    break;
                }
                super.handleMessage(msg);
            }
        };

    }

    private String doPayOrSign() {
        MobileSecurePayer msp = new MobileSecurePayer();

//        msp.setCAPTCHA_Switch(false);

        // 非卡前置
        PayOrder order = constructStandardPayOrder();

        String content4Pay = BaseHelper.toJSONString(order);

        // 关键 content4Pay 用于提交到支付SDK的订单支付串，如遇到签名错误的情况，请将该信息帖给我们的技术支持
        Log.i(MobileBankActivity.class.getSimpleName(), content4Pay);

        boolean bRet = false;

        Log.i(MobileBankActivity.class.getSimpleName(), String.valueOf(bRet));
        return content4Pay;
    }

    private PayOrder constructStandardPayOrder() {
        PayOrder order = new PayOrder();
        order.setOid_partner(EnvConstants.PARTNER_MOBILE_BANK);
        buildPayParam(order);
        buildUserParam(order);
        buildRiskParam(order);
        order.setFlag_modify("1");
        // MD5签名
        order.setSign_type(PayOrder.SIGN_TYPE_MD5);
        // RSA 签名方式
//        order.setSign_type(PayOrder.SIGN_TYPE_RSA);
        BaseHelper.fillSign(order, isSignMode());

        return order;
    }

    private boolean isSignMode(){
//        return pay_type_flag == 2;
        return false;
    }

    private void buildPayParam(PayOrder order) {
        order.setBusi_partner("101001");
        String timeString = getTimeString();
        order.setNo_order(timeString);
        order.setDt_order(timeString);
        order.setName_goods("连连测试商品");
        order.setNotify_url(Constants.NOTIFY_URL);
        order.setValid_order("100");
        order.setMoney_order(getMoney());
    }

    private String getTimeString() {
        SimpleDateFormat dataFormat = new SimpleDateFormat("yyyyMMddHHmmss");
        Date date = new Date();
        return dataFormat.format(date);
    }

    private String getMoney() {
        return "0.01";
    }

    private void buildUserParam(PayOrder order) {
//        order.setPlatform(((EditText) findViewById(R.id.platform))
//                .getText().toString());
    }

    private void buildRiskParam(PayOrder order) {
        // 风险控制参数 必填
        order.setRisk_item(constructRiskItem());
    }

    private String constructRiskItem() {
        JSONObject mRiskItem = new JSONObject();
        try {
            mRiskItem.put("user_info_bind_phone", "13958069593");
            mRiskItem.put("user_info_dt_register", "201407251110120");
            mRiskItem.put("frms_ware_category", "4.0");
            mRiskItem.put("request_imei", "1122111221");
//            mRiskItem.put("product_type", "4");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return mRiskItem.toString();
    }

}
