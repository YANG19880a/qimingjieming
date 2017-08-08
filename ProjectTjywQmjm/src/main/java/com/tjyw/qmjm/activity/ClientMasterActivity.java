package com.tjyw.qmjm.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.support.v4.util.Pair;

import com.aspsine.fragmentnavigator.FragmentNavigator;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigation;
import com.aurelhubert.ahbottomnavigation.AHBottomNavigationItem;
import com.gyf.barlibrary.ImmersionBar;
import com.tjyw.qmjm.ClientQmjmApplication;
import com.tjyw.qmjm.R;
import com.tjyw.qmjm.adapter.ClientMasterAdapter;

import butterknife.BindView;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * Created by stephen on 07/08/2017.
 */
public class ClientMasterActivity extends BaseActivity {

    @BindView(R.id.atomPubClientMasterNavigation)
    protected AHBottomNavigation atomPubClientMasterNavigation;

    protected FragmentNavigator fragmentNavigator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.atom_client_master);

        ImmersionBar.with(this)
                .statusBarDarkFont(true)
                .init();

        fragmentNavigator = new FragmentNavigator(getSupportFragmentManager(), ClientMasterAdapter.newInstance(this), R.id.masterFragmentContainer);
        fragmentNavigator.onCreate(savedInstanceState);

        atomPubClientMasterNavigation.setAccentColor(ContextCompat.getColor(getApplicationContext(), R.color.atom_ewsh_textColorBlack));
        atomPubClientMasterNavigation.setInactiveColor(ContextCompat.getColor(getApplicationContext(), R.color.atom_ewsh_textColorGrey));

        int size = ClientMasterAdapter.MASTER_TAB_RESOURCE.size();
        for (int i = 0; i < size; i ++) {
            Pair<Integer, Integer> masterTabResource = ClientMasterAdapter.getMasterTabResource(i);
            if (null != masterTabResource) {
                atomPubClientMasterNavigation.addItem(
                        new AHBottomNavigationItem(
                                ClientQmjmApplication.pGetString(masterTabResource.first),
                                ContextCompat.getDrawable(ClientQmjmApplication.getContext(), masterTabResource.second)
                        )
                );
            }
        }

        atomPubClientMasterNavigation.setTitleState(AHBottomNavigation.TitleState.ALWAYS_SHOW);
        atomPubClientMasterNavigation.setOnTabSelectedListener(new AHBottomNavigation.OnTabSelectedListener() {
            @Override
            public boolean onTabSelected(int position, boolean wasSelected) {
                fragmentNavigator.showFragment(position, false, false);
                return true;
            }
        });

        atomPubClientMasterNavigation.setCurrentItem(ClientMasterAdapter.POSITION.NAMING, true);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
}