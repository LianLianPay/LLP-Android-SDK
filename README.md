# LLP-android-SDK

欢迎来到连连银行卡收款的Android SDK仓库，本仓库中含有接入Demo工程及使用说明。

其中， ```LLSecurePayDemo-quick```为快捷收款的接入示例；```SecurePayDemo-auth```为认证收款的接入示例；```SecurePayDemo-repay```为分期收款的接入示例。

> 在本仓库的Demo中， ```EnvConstant.java```中有配置供演示及测试使用的商户私钥。为保障商户公私钥的安全， 建议您参考[签名机制](https://openllp.lianlianpay.com/docs/development/signature-overview)在您的**服务器端**完成签名的加签和验签过程。


## 主要内容

* [添加SDK至您的工程](#添加sdk至您的工程)

* [前置要求](#前置要求)

* [配置](#配置)

* [使用情况](#使用情况)

* [混淆说明](#混淆说明)

* [文档说明](#文档说明)

## 添加SDK至您的工程

将仓库中的```aar```文件添加到您的工程```libs/```目录中， 在```build.gradle```文件中的```dependencies```中添加如下内容:

```
compile(name:'securePay-180419-v3.2.1.3', ext:'aar')
```

> 最新的Android SDK请至```aars/```目录下进行获取。

此外， ```build.gradle```中需要配置如下内容:

```
repositories {
    jcenter()

    flatDir {
        dirs 'libs'
    }
}
```

## 前置要求

* Android 4.4 (API 19)及以上版本

## 配置

在您工程的```AndroidManifest.xml```中添加如下权限， 如有则可忽略。

```
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.ACCESS_WIFI_STATE" />
```

如您希望使用此SDK的自动读取短信验证码功能（仅支持部分机型）， 需添加短信读取权限。

```
<uses-permission android:name="android.permission.RECEIVE_SMS" />
<uses-permission android:name="android.permission.READ_SMS" />
```

> ```android.permission.READ_SMS```有可能会被某些安全软件认为是较为危险的操作， 请添加时自行评估。小米等深度定制手机需要针对该应用开启读取短信功能方可读取。小米设置路径：设置--> 应用--> 该应用--> 权限管理--> 短信记录--> 允许。三星Galaxy S5 等三星手机4.4以上系统版本的，如果配置了```android.permission.READ_SMS```，可能存在必须允许打开读取权限方可使用的问题，请测试后自行评估是否配置该权限。此外，部分手机安装了安全工具，有可能会拦截到短信，导致不能自动读取，加上手机厂商对手机短信的安全控制各不相同，适配所有机型较为困难。

权限配置完成后， 添加相应类声明:

```
<activity
    android:name="com.yintong.secure.activity.BaseActivity"
    android:configChanges="orientation|keyboardHidden|screenSize"
    android:theme="@android:style/Theme.Translucent.NoTitleBar"
    android:windowSoftInputMode="adjustResize" >
</activity>

<service
    android:name="com.yintong.secure.service.PayService">
</service>
```

## 使用情况

此SDK适用于收款类产品中的认证收款， 快捷收款，分期收款产品， 参数及使用时的注意事项详见[文档说明](#文档说明)。 下载[示例工程](https://github.com/LianLianPay/LLP-Android-SDK/releases)后参照示例工程接入即可。

## 混淆说明

在混淆文件中添加以下规则:

```
# 连连Demo混淆keep规则，如果使用了Demo中工具包com.yintong.pay.utils下面的类，请对应添加keep规则，否则混下会包签名错误
-keep public class com.yintong.auth.pay.utils.** {
    <fields>;
    <methods>;
}
# 连连混淆keep规则，请添加
-keep class com.yintong.secure.activityproxy.PayIntro$LLJavascriptInterface{*;}

# 连连混淆keep规则
-keep public class com.yintong.** {
    <fields>;
    <methods>;
}
```


## 文档说明

* [连连开放平台-Android-概述](https://openllp.lianlianpay.com/docs/receive-money/android/overview)

* [连连开放平台-Android-快捷收款](https://openllp.lianlianpay.com/docs/receive-money/android/express)

* [连连开放平台-Android-认证收款](https://openllp.lianlianpay.com/docs/receive-money/android/authenticate)

* [连连开放平台-Android-分期收款](https://openllp.lianlianpay.com/docs/receive-money/android/instalment)