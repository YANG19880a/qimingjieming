package com.tjyw.qmjm.activity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.text.TextUtils;

import com.brianjmelton.stanley.ProxyGenerator;
import com.tjyw.atom.network.RetroHttpMethods;
import com.tjyw.atom.network.RxSchedulersHelper;
import com.tjyw.atom.network.interfaces.IPrefUser;
import com.tjyw.atom.network.model.PayOrder;
import com.tjyw.atom.network.model.UserInfo;
import com.tjyw.atom.network.presenter.UserPresenter;
import com.tjyw.atom.network.result.RetroResult;
import com.tjyw.atom.network.utils.JsonUtil;
import com.tjyw.qmjm.R;
import com.tjyw.qmjm.adapter.ClientMasterAdapter;
import com.tjyw.qmjm.factory.IClientActivityLaunchFactory;

import java.util.concurrent.TimeUnit;

import nucleus.factory.RequiresPresenter;
import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;

/**
 * Created by stephen on 17-8-1.
 */
@RequiresPresenter(UserPresenter.class)
public class ClientWelcomeActivity extends BaseActivity<UserPresenter<ClientWelcomeActivity>> {

    static final int DELAY_TO_LAUNCH_MASTER = 2000;

    protected long createMillisTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.atom_master_welcome);
        immersionBarWith()
                .statusBarDarkFont(true)
                .init();

        createMillisTime = System.currentTimeMillis();
        IPrefUser user = new ProxyGenerator().create(getApplicationContext(), IPrefUser.class);
        if (null != user && ! TextUtils.isEmpty(user.getUserSession())) { // 判断本地是否有用户信息
            RetroHttpMethods.PAY().postPayOrder(1) // 有用户信息时，只请求init接口初始化
                    .compose(RxSchedulersHelper.<RetroResult<PayOrder>>io_main())
                    .subscribe(new Action1<RetroResult<PayOrder>>() {
                        @Override
                        public void call(RetroResult<PayOrder> result) { // 成功后进入主界面
                            launchMasterActivity();
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) { // 初始化接口失败不退出APP，也进入主界面
                            throwable.printStackTrace();
                            launchMasterActivity();
                        }
                    });
        } else { // 没有用户信息时，先请求register接口创建用户，再访问init接口初始化
            Observable.merge(
                    RetroHttpMethods.USER().postUserRegister(1),
                    RetroHttpMethods.PAY().postPayPreview(1, 1)
            ).compose(RxSchedulersHelper.<RetroResult<?>>io_main())
                    .subscribe(new Action1<RetroResult<?>>() {
                        @Override
                        public void call(RetroResult<?> result) {
                            if (result.items instanceof UserInfo) {
                                saveUserInfo((UserInfo) result.items);
                            }
                        }
                    }, new Action1<Throwable>() {
                        @Override
                        public void call(Throwable throwable) { // 请求失败时，用户信息如果已经存在，则进入主界面
                            throwable.printStackTrace();
                            IPrefUser user = new ProxyGenerator().create(getApplicationContext(), IPrefUser.class);
                            if (null != user && ! TextUtils.isEmpty(user.getUserSession())) {
                                launchMasterActivity();
                            } else { // 否则说明注册接口出现异常，弹窗提示并退出应用
                                new AlertDialog.Builder(ClientWelcomeActivity.this)
                                        .setTitle(R.string.atom_pub_resStringTip)
                                        .setMessage(R.string.atom_pub_resStringNetworkBroken)
                                        .setPositiveButton(R.string.atom_pub_resStringOK, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                finish();
                                            }
                                        });
                            }
                        }
                    }, new Action0() {
                        @Override
                        public void call() { // 两个接口稳定请求结果回来后，进入主界面
                            Observable.timer(1, TimeUnit.SECONDS)
                                    .compose(RxSchedulersHelper.<Long>io_main())
                                    .subscribe(new Action1<Long>() {
                                        @Override
                                        public void call(Long aLong) {
                                            launchMasterActivity();
                                        }
                                    });
                        }
                    });
        }
    }

    protected void saveUserInfo(UserInfo userInfo) {
        if (null != userInfo) {
            IPrefUser user = new ProxyGenerator().create(getApplicationContext(), IPrefUser.class);
            if (null != user) {
                user.setUserSession(userInfo.sessionKey);
                user.setUserInfo(JsonUtil.getInstance().toJsonString(userInfo));
            }
        }
    }

    protected void launchMasterActivity() {
        createMillisTime = Math.abs(createMillisTime - System.currentTimeMillis());
        if (createMillisTime < DELAY_TO_LAUNCH_MASTER) {
            Observable.timer((DELAY_TO_LAUNCH_MASTER - createMillisTime), TimeUnit.MILLISECONDS)
                    .compose(RxSchedulersHelper.<Long>io_main())
                    .subscribe(new Action1<Long>() {
                        @Override
                        public void call(Long aLong) {
                            IClientActivityLaunchFactory.launchClientMasterActivity(ClientWelcomeActivity.this, ClientMasterAdapter.POSITION.NAMING, false);
                            finish();
                        }
                    });
        } else {
            IClientActivityLaunchFactory.launchClientMasterActivity(this, ClientMasterAdapter.POSITION.NAMING, false);
            finish();
        }
    }
}
