package com.yintong.adapter.item;

import com.yintong.enums.PayBankEnum;

/**
 * Created by duanmeng on 2017/2/21.
 */
public class PaymentItem {
    private int mLogo;
    private String mName;
    private PayBankEnum mPayBank;

    private boolean isChecked = false;
    public boolean isAdvise;

    public PaymentItem(int logo, PayBankEnum paybank){
        this.mLogo = logo;
        this.mPayBank = paybank;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public String getName() {
        return mPayBank.getBankName();
    }

    public int getLogo() {
        return mLogo;
    }

    public PayBankEnum getPayBank() {
        return mPayBank;
    }

}
