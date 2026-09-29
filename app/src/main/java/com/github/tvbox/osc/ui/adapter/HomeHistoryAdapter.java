package com.github.tvbox.osc.ui.adapter;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.github.tvbox.osc.R;
import com.github.tvbox.osc.bean.VodInfo;
import com.github.tvbox.osc.util.ImgUtil;

import java.util.ArrayList;

import me.jessyan.autosize.utils.AutoSizeUtils;

/**
 * 首页「历史记录」横排卡片
 */
public class HomeHistoryAdapter extends BaseQuickAdapter<VodInfo, BaseViewHolder> {
    public HomeHistoryAdapter() {
        super(R.layout.item_history_card, new ArrayList<>());
    }

    @Override
    protected void convert(BaseViewHolder helper, VodInfo item) {
        helper.setText(R.id.tvName, item.name);
        TextView tvNote = helper.getView(R.id.tvNote);
        if (item.playNote == null || item.playNote.isEmpty()) {
            tvNote.setVisibility(View.GONE);
        } else {
            tvNote.setVisibility(View.VISIBLE);
            tvNote.setText(String.format("上次看到 %s", item.playNote));
        }
        ImageView ivPoster = helper.getView(R.id.ivPoster);
        String pic = item.pic == null ? "" : item.pic.trim();
        if (!TextUtils.isEmpty(pic)) {
            if (ImgUtil.isBase64Image(pic)) {
                ivPoster.setImageBitmap(ImgUtil.decodeBase64ToBitmap(pic));
            } else {
                ImgUtil.load(pic, ivPoster, AutoSizeUtils.mm2px(mContext, 10),
                        AutoSizeUtils.mm2px(mContext, 360), AutoSizeUtils.mm2px(mContext, 480), item.name);
            }
        } else {
            ivPoster.setImageDrawable(ImgUtil.createTextDrawable(item.name));
        }
    }
}
