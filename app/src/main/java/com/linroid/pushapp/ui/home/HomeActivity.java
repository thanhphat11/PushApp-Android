/*
 * Copyright (c) linroid 2015.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.linroid.pushapp.ui.home;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.linroid.pushapp.App;
import com.linroid.pushapp.BuildConfig;
import com.linroid.pushapp.Constants;
import com.linroid.pushapp.R;
import com.linroid.pushapp.api.FirService;
import com.linroid.pushapp.model.Account;
import com.linroid.pushapp.model.FirVersion;
import com.linroid.pushapp.service.ApkAutoInstallService;
import com.linroid.pushapp.ui.base.BaseActivity;
import com.linroid.pushapp.ui.bind.BindActivity;
import com.linroid.pushapp.ui.setting.SettingActivity;
import com.linroid.pushapp.util.AndroidUtil;
import com.linroid.pushapp.util.IntentUtil;
import com.linroid.pushapp.util.Once;

import javax.inject.Inject;

import butterknife.Bind;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;

public class HomeActivity extends BaseActivity {
    public static final String EXTRA_MESSAGE = "message";
    private static final String STATE_PAGER_POSITION = "pager_position";
    @Bind(R.id.tabLayout)
    TabLayout tabLayout;
    @Bind(R.id.viewpager)
    ViewPager pager;
    @Bind(R.id.toolbar)
    protected Toolbar toolbar;
    @Bind(R.id.fab)
    FloatingActionButton fab;

    @Inject
    Account auth;
    @Inject
    SharedPreferences preferences;
    @Inject
    Once once;
    @Inject
    FirService firApi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        App.from(this).component().inject(this);
        checkBind();
        checkVersion();
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            ViewCompat.setElevation(toolbar, 0);
        }
        pager.setAdapter(new HomePagerAdapter(getSupportFragmentManager(), getResources()));
        tabLayout.setupWithViewPager(pager);
        pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                if (position == 1) {
                    if (positionOffset > 0.5f) {
                        if (!fab.isShown()) {
                            fab.setVisibility(View.VISIBLE);
                        }
                        float scale = (positionOffset - 0.5f) * 2;
                        fab.setScaleX(scale);
                        fab.setScaleY(scale);
                        return;
                    }
                }

                if (position != 2 && fab.isShown()) {
                    fab.setVisibility(View.GONE);
                }
            }

            @Override
            public void onPageSelected(int position) {

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        if (savedInstanceState != null) {
            int pagerPosition = savedInstanceState.getInt(STATE_PAGER_POSITION);
            pager.setCurrentItem(pagerPosition);
        }
    }


    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(STATE_PAGER_POSITION, pager.getCurrentItem());
    }

    private void checkAccessibilityService() {
//        boolean confirmed = preferences.getBoolean(Constants.SP_AUTO_INSTALL_CONFIRMED, false);
        if (ApkAutoInstallService.available()
                && !AndroidUtil.isAccessibilitySettingsOn(this, ApkAutoInstallService.class.getCanonicalName())) {
            new AlertDialog.Builder(this).setTitle(R.string.title_auto_install_confirm_dialog)
                    .setMessage(R.string.msg_auto_install_confirm_dialog)
                    .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//                            SharedPreferences.Editor editor = preferences.edit();
//                            editor.putBoolean(Constants.SP_AUTO_INSTALL_CONFIRMED, true);
////                            editor.putBoolean(Constants.SP_AUTO_INSTALL, true);
//                            editor.apply();
                            dialog.cancel();
                            startActivityForResult(IntentUtil.accessibilitySettings(), 0);
                        }
                    })
                    .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
//                            preferences.edit().putBoolean(Constants.SP_AUTO_INSTALL_CONFIRMED, true).apply();
                            Snackbar.make(pager, R.string.msg_auto_install_confirm_cancel, Snackbar.LENGTH_LONG);
                        }
                    }).show();
        }
    }

    private void checkBind() {
        if (!auth.isValid()) {
            Timber.w("device does not bind");
            Intent intent = new Intent(this, BindActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onStart() {
        super.onStart();
        checkAccessibilityService();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (data != null && data.hasExtra(EXTRA_MESSAGE)) {
                Snackbar.make(pager, data.getStringExtra(EXTRA_MESSAGE), Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected int provideContentViewId() {
        return R.layout.activity_home;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_settings) {
            Intent intent = new Intent(this, SettingActivity.class);
            startActivity(intent);
        } else if (item.getItemId() == R.id.action_device_qrcode) {
//            LayoutInflater inflater = LayoutInflater.from(this);
//            View view = inflater.inflate(R.layout.dialog_device_token, null);
//
//            getWindow().addContentView(view, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            new DeviceTokenDialog().show(getSupportFragmentManager(), null);
        }
        return super.onOptionsItemSelected(item);
    }

    public void checkVersion() {
        Subscription subscription = firApi.lastVersion(BuildConfig.APPLICATION_ID)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<FirVersion>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(final FirVersion version) {
                        if (!(version.getVersion() > BuildConfig.VERSION_CODE)) {
                            once.show(getString(R.string.once_version, version.getVersion()), new Once.OnceCallback() {
                                @Override
                                public void onOnce() {
                                    Snackbar.make(fab, getString(R.string.msg_new_version, version.getVersionShort()), Snackbar.LENGTH_INDEFINITE)
                                            .setAction(R.string.btn_update_version, new View.OnClickListener() {
                                                @Override
                                                public void onClick(View v) {
                                                    startActivity(IntentUtil.openUri(version.getUpdateUrl()));
                                                }
                                            })
                                            .show();
                                }
                            });
                        }
                    }
                });
        addSubscription(subscription);
    }
}
