package com.epson.epos2_cashchanger;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.epson.epos2.Epos2Exception;
import com.epson.epos2.Log;
import com.epson.epos2.cashchanger.CashChanger;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_PERMISSION = 100;

    static Context mContext = null;
    static CashChanger mCashChanger = null;
    private SectionsPagerAdapter mSectionsPagerAdapter;
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestRuntimePermission();

        mContext = this;

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setOffscreenPageLimit(9);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);

        tabLayout.setupWithViewPager(mViewPager);

        try {
            Log.setLogSettings(mContext, Log.PERIOD_TEMPORARY, Log.OUTPUT_STORAGE, null, 0, 1, Log.LOGLEVEL_LOW);
        }
        catch (Exception e) {
            ShowMsg.showException(e, "setLogSettings", mContext);
        }
    }

    public static class PlaceholderFragment extends Fragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_connect, container, false);
            return rootView;
        }
    }

    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            Fragment fragment = null;
            switch (position) {
                case 0:
                    fragment = ConnectFragment.newInstance();
                    break;
                case 1:
                    fragment = SetConfigFragment.newInstance();
                    break;
                case 2:
                    fragment = DepositFragment.newInstance();
                    break;
                case 3:
                    fragment = DispenseChangeFragment.newInstance();
                    break;
                case 4:
                    fragment = DispenseCashFragment.newInstance();
                    break;
                case 5:
                    fragment = CashCountFragment.newInstance();
                    break;
                case 6:
                    fragment = CollectFragment.newInstance();
                    break;
                case 7:
                    fragment = DrawerFragment.newInstance();
                    break;
                case 8:
                    fragment = CommandFragment.newInstance();
                    break;
            }
            return fragment;
        }

        @Override
        public int getCount() {
            // Show 3 total pages.
            return 9;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return getString(R.string.tab_connect);
                case 1:
                    return getString(R.string.tab_setConfig);
                case 2:
                    return getString(R.string.tab_deposit);
                case 3:
                    return getString(R.string.tab_dispenseChange);
                case 4:
                    return getString(R.string.tab_dispenseCash);
                case 5:
                    return getString(R.string.tab_cashCount);
                case 6:
                    return getString(R.string.tab_collect);
                case 7:
                    return getString(R.string.tab_drawer);
                case 8:
                    return getString(R.string.tab_command);
            }
            return null;
        }
    }

    public static boolean initializeObject() {
        if (mCashChanger != null) {
            finalizeObject();
        }

        try {
            mCashChanger = new CashChanger(mContext);
        } catch (Epos2Exception e) {
            ShowMsg.showException(e, "CashChanger", mContext);
            return false;
        }
        return true;
    }

    public static void finalizeObject() {
    }

    private void requestRuntimePermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return;
        }

        int permissionStorage = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permissionStorage == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        if (requestCode != REQUEST_PERMISSION || grantResults.length == 0) {
            return;
        }

        if (grantResults[0] == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
        }
    }
}
