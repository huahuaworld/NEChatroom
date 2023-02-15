// Copyright (c) 2022 NetEase, Inc. All rights reserved.
// Use of this source code is governed by a MIT license that can be
// found in the LICENSE file.

package com.netease.yunxin.app.chatroom.main;

import android.os.Bundle;
import android.text.TextUtils;
import com.google.android.material.tabs.TabLayout;
import com.gyf.immersionbar.ImmersionBar;
import com.netease.yunxin.app.chatroom.Constants;
import com.netease.yunxin.app.chatroom.R;
import com.netease.yunxin.app.chatroom.activity.BaseActivity;
import com.netease.yunxin.app.chatroom.config.AppConfig;
import com.netease.yunxin.app.chatroom.databinding.ActivityMainBinding;
import com.netease.yunxin.app.chatroom.main.pager.MainPagerAdapter;
import com.netease.yunxin.app.chatroom.utils.NavUtils;
import com.netease.yunxin.kit.alog.ALog;
import com.netease.yunxin.kit.common.ui.utils.ToastUtils;
import com.netease.yunxin.kit.copyrightedmedia.api.SongScene;
import com.netease.yunxin.kit.listentogetherkit.api.NEListenTogetherCallback;
import com.netease.yunxin.kit.listentogetherkit.api.NEListenTogetherKit;
import com.netease.yunxin.kit.login.AuthorManager;
import com.netease.yunxin.kit.login.model.UserInfo;
import com.netease.yunxin.kit.ordersong.core.NEOrderSongService;
import com.netease.yunxin.kit.voiceroomkit.ui.statusbar.StatusBarConfig;
import java.util.Objects;
import kotlin.Unit;

public class MainActivity extends BaseActivity {

  private static final String TAG = "MainActivity";
  private static final int TAB_HOME = 0;
  private static final int TAB_MINE = 1;
  private ActivityMainBinding binding;
  public int curTabIndex = -1;

  @Override
  protected boolean needTransparentStatusBar() {
    return true;
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    binding = ActivityMainBinding.inflate(getLayoutInflater());
    setContentView(binding.getRoot());
    login(AuthorManager.INSTANCE.getUserInfo());
    ImmersionBar bar = ImmersionBar.with(this).statusBarDarkFont(true);
    bar.init();
    curTabIndex = -1;
    if (AuthorManager.INSTANCE.getUserInfo() == null) {
      NavUtils.toSplash(MainActivity.this);
      finish();
    }
    initViews();
  }

  private void initViews() {
    binding.vpFragment.setAdapter(new MainPagerAdapter(getSupportFragmentManager()));
    binding.vpFragment.setOffscreenPageLimit(2);
    binding.tlTab.setupWithViewPager(binding.vpFragment);
    binding.tlTab.removeAllTabs();
    binding.tlTab.setTabGravity(TabLayout.GRAVITY_CENTER);
    binding.tlTab.setSelectedTabIndicator(null);
    binding.tlTab.addTab(
        binding.tlTab.newTab().setCustomView(R.layout.view_item_home_tab_app), 0, true);
    binding.tlTab.addTab(
        binding.tlTab.newTab().setCustomView(R.layout.view_item_home_tab_user), 1, false);
    binding.vpFragment.addOnPageChangeListener(
        new TabLayout.TabLayoutOnPageChangeListener(binding.tlTab) {

          @Override
          public void onPageSelected(int position) {
            TabLayout.Tab item = binding.tlTab.getTabAt(position);
            if (item != null) {
              item.select();
            }
            super.onPageSelected(position);
          }
        });
  }

  @Override
  public void onBackPressed() {
    moveTaskToBack(true);
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    curTabIndex = -1;
    ALog.flush(true);
  }

  @Override
  protected StatusBarConfig provideStatusBarConfig() {
    return new StatusBarConfig.Builder().statusBarDarkFont(false).build();
  }

  @Override
  protected void onKickOut() {
    AuthorManager.INSTANCE.launchLogin(MainActivity.this, Constants.MAIN_PAGE_ACTION, false);
  }

  private void login(UserInfo userInfo) {
    if (userInfo == null) {
      ALog.d(TAG, "login but userInfo == null");
      return;
    }

    if (TextUtils.isEmpty(userInfo.getAccountId())) {
      ALog.d(TAG, "login but userInfo.getAccountId() == null");
      return;
    }

    if (TextUtils.isEmpty(userInfo.getAccessToken())) {
      ALog.d(TAG, "login but userInfo.getAccessToken() == null");
      return;
    }
    NEListenTogetherKit.getInstance()
        .login(
            Objects.requireNonNull(userInfo.getAccountId()),
            Objects.requireNonNull(userInfo.getAccessToken()),
            new NEListenTogetherCallback<Unit>() {

              @Override
              public void onSuccess(Unit unit) {
                ALog.d(TAG, "VoiceRoomKit login success");
                String serverUrl = "";
                NEOrderSongService.INSTANCE.initialize(
                    MainActivity.this.getApplicationContext(), AppConfig.getAppKey(), serverUrl);
                NEOrderSongService.INSTANCE.setSongScene(SongScene.TYPE_LISTENING_TO_MUSIC);
              }

              @Override
              public void onFailure(int code, String msg) {
                ALog.d(TAG, "VoiceRoomKit login failed code = " + code + ", msg = " + msg);
                ToastUtils.INSTANCE.showShortToast(
                    MainActivity.this,
                    "VoiceRoomKit login failed code = " + code + ", msg = " + msg);
                finish();
              }
            });
  }
}
