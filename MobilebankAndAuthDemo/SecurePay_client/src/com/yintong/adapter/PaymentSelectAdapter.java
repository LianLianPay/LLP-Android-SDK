package com.yintong.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.yintong.adapter.item.PaymentItem;
import com.yintong.secure.simple.demo.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by duanmeng on 2017/2/21.
 */
public class PaymentSelectAdapter extends BaseAdapter {
    private Context mContext;
    private static List<PaymentItem> mPaymentList = new ArrayList<PaymentItem>();

    public PaymentSelectAdapter(Context context) {
        mContext = context;
    }

    @Override
    public int getCount() {
        return mPaymentList == null ? 0 : mPaymentList.size();
    }

    @Nullable
    @Override
    public Object getItem(int position) {
        if (mPaymentList == null || position < 0 || position >= mPaymentList.size()){
            return null;
        }else {
            return mPaymentList.get(position);
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Nullable
    @Override
    public View getView(int position, @Nullable View view, ViewGroup parent) {
        ViewHolder holder = null;
        if (view == null){
            view = LayoutInflater.from(mContext).inflate(R.layout.payment_item, parent, false);
            holder = new ViewHolder(view);
            view.setTag(holder);
        }else {
            holder = (ViewHolder)view.getTag();
        }
        PaymentItem paymentItem = mPaymentList.get(position);
        holder.paymentLogo.setImageResource(paymentItem.getLogo());
        holder.paymentName.setText(paymentItem.getName());
        holder.paymentIntro.setText(paymentItem.getPayBank().getBankIntro());
        if (paymentItem.isChecked()){
            holder.ivChecked.setImageResource(R.drawable.checkbox_checked);
        }else {
            holder.ivChecked.setImageResource(R.drawable.checbox_normal);
        }

        view.setBackgroundResource(R.drawable.bg_item_bankcard);  //设置item的默认背景及点击效果
        return view;
    }

    public void setData(List<PaymentItem> payList){
        this.mPaymentList = payList;
        notifyDataSetChanged();
    }

    class ViewHolder{
        ImageView paymentLogo;
        TextView paymentName;
        TextView paymentIntro;
        ImageView ivChecked;

        ViewHolder(@NonNull View view){
            paymentLogo = (ImageView)view.findViewById(R.id.iv_payment_logo);
            paymentName = (TextView)view.findViewById(R.id.tv_payment_name);
            paymentIntro = (TextView)view.findViewById(R.id.tv_payment_intro);
            ivChecked = (ImageView)view.findViewById(R.id.iv_checked);
        }
    }
}
