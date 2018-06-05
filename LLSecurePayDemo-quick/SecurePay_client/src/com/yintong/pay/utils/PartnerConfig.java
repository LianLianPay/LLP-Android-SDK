
package com.yintong.pay.utils;

import com.yintong.secure.demo.env.EnvConstants;

public class PartnerConfig {

    // 合作商户ID。
    public static final String PARTNER = EnvConstants.PARTNER;

    public static final String MD5_KEY = EnvConstants.MD5_KEY;
    // 商户（RSA）私钥 TODO 强烈建议将私钥配置到服务器上，否则有安全隐患
    public static final String RSA_PRIVATE = EnvConstants.RSA_PRIVATE;
    // 银通支付（RSA）公钥
    public static final String RSA_YT_PUBLIC = EnvConstants.RSA_YT_PUBLIC;

}
