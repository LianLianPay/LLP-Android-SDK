package com.yintong.enums;

/**
 * Created by PeterXu on 2017/7/3.
 */

public enum PayBankEnum {
    PAY_BANK_ICBC("01020000", "工银e支付", "工商银行", "推荐工行手机银行用户使用"),
    PAY_BANK_ABC("01030000", "农银快e付", "农业银行", "推荐农行手机银行用户使用"),
    PAY_BANK_BOC("01040000", "中国银行", "中国银行", "推荐中行手机银行用户使用"),
    PAY_BANK_CCB("01050000", "建行龙支付", "建设银行", "推荐建行手机银行用户使用"),
    PAY_BANK_CMB("03080000", "招商银行", "招商银行", "推荐招行手机银行用户使用");

    private String mBankcode;
    private String mBankname;
    private String mShortBankname;
    private String mBankIntro;

    PayBankEnum(String bankcode, String bankname, String shortbankname, String bankIntro){
        mBankcode = bankcode;
        mBankname = bankname;
        mShortBankname = shortbankname;
        mBankIntro = bankIntro;
    }

    public String getBankCode(){
        return mBankcode;
    }

    public String getBankName(){
        return mBankname;
    }

    public String getShortBankname(){
        return mShortBankname;
    }

    public String getBankIntro(){
        return mBankIntro;
    }
}
