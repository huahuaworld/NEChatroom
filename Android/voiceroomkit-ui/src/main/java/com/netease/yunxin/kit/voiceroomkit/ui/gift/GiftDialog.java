// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.kit.voiceroomkit.ui.gift;

import android.annotation.*;
import android.app.*;
import android.content.*;
import android.graphics.*;
import android.util.*;
import android.view.*;
import android.widget.*;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.*;
import com.netease.yunxin.kit.voiceroomkit.impl.utils.*;
import com.netease.yunxin.kit.voiceroomkit.ui.*;
import com.netease.yunxin.kit.voiceroomkit.ui.adapter.*;
import com.netease.yunxin.kit.voiceroomkit.ui.dialog.*;
import com.netease.yunxin.kit.voiceroomkit.ui.utils.*;
import java.util.*;

public class GiftDialog extends BottomBaseDialog {
  private GiftSendListener sendListener;
  private RecyclerView.ItemDecoration itemDecoration =
      new RecyclerView.ItemDecoration() {

        @Override
        public void getItemOffsets(
            @NonNull Rect outRect,
            @NonNull View view,
            @NonNull RecyclerView parent,
            @NonNull RecyclerView.State state) {
          if (parent.getChildAdapterPosition(view) == 0) {
            outRect.set(ScreenUtil.dip2px(16f), 0, 0, 0);
          } else {
            super.getItemOffsets(outRect, view, parent, state);
          }
        }
      };

  public GiftDialog(@NonNull Activity activity) {
    super(activity);
  }

  @Override
  protected void renderTopView(FrameLayout parent) {
    TextView titleView = new TextView(getContext());
    titleView.setText(R.string.voiceroom_send_gift);
    titleView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f);
    titleView.setGravity(Gravity.CENTER);
    titleView.setTextColor(Color.parseColor("#ff333333"));
    FrameLayout.LayoutParams layoutParams =
        new FrameLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
    parent.addView(titleView, layoutParams);
  }

  @Override
  protected void renderBottomView(FrameLayout parent) {
    View bottomView =
        LayoutInflater.from(getContext()).inflate(R.layout.view_dialog_bottom_gift, parent);
    // 礼物列表初始化
    RecyclerView rvGiftList = bottomView.findViewById(R.id.rv_dialog_gift_list);
    rvGiftList.setLayoutManager(
        new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));
    rvGiftList.removeItemDecoration(itemDecoration);
    rvGiftList.addItemDecoration(itemDecoration);
    InnerAdapter adapter = new InnerAdapter(getContext(), GiftCache.getGiftList());
    rvGiftList.setAdapter(adapter);

    // 发送礼物
    View sendGift = bottomView.findViewById(R.id.tv_dialog_send_gift);
    sendGift.setOnClickListener(
        v -> {
          if (sendListener != null) {
            dismiss();
            sendListener.onSendGift(adapter.getFocusedInfo().getGiftId());
          }
        });
  }

  /**
   * 弹窗展示
   *
   * @param listener 礼物发送回调
   */
  public void show(GiftSendListener listener) {
    sendListener = listener;
    show();
  }

  /** 礼物发送回调 */
  public interface GiftSendListener {
    void onSendGift(int giftId);
  }

  /** 内部礼物列表 adapter */
  private static class InnerAdapter extends LiveBaseAdapter<GiftInfo> {

    private Context context;
    private List<GiftInfo> dataSource;
    private GiftInfo focusedInfo;

    public InnerAdapter(Context context, List<GiftInfo> dataSource) {
      super(context, dataSource);
      if (dataSource != null && !dataSource.isEmpty()) {
        focusedInfo = dataSource.get(0);
      }
    }

    @Override
    protected int getLayoutId(int viewType) {
      return R.layout.view_item_dialog_gift;
    }

    @Override
    protected LiveViewHolder onCreateViewHolder(View itemView) {
      return new LiveViewHolder(itemView);
    }

    @Override
    protected void onBindViewHolder(
        LiveViewHolder holder, @SuppressLint("RecyclerView") GiftInfo itemData) {
      ImageView ivGift = holder.getView(R.id.iv_item_gift_icon);
      ivGift.setImageResource(itemData.getStaticIconResId());
      TextView tvName = holder.getView(R.id.tv_item_gift_name);
      tvName.setText(itemData.getName());
      TextView tvValue = holder.getView(R.id.tv_item_gift_value);
      tvValue.setText(formatValue(itemData.getCoinCount()));
      View border = holder.getView(R.id.rl_item_border);
      if (itemData == focusedInfo) {
        border.setBackgroundResource(R.drawable.layer_dialog_gift_chosen_bg);
      } else {
        border.setBackgroundColor(Color.TRANSPARENT);
      }
      holder.itemView.setOnClickListener(
          new View.OnClickListener() {

            @Override
            public void onClick(View v) {
              focusedInfo = itemData;
              notifyDataSetChanged();
            }
          });
    }

    GiftInfo getFocusedInfo() {
      return focusedInfo;
    }

    private String formatValue(long value) {
      return "(" + value + Utils.getApp().getString(R.string.voiceroom_coin) + ")";
    }
  }
}
