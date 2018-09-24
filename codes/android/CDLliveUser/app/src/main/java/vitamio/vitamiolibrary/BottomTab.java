package vitamio.vitamiolibrary;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.v7.app.AppCompatActivity;
import android.widget.RadioGroup;

import java.util.ArrayList;
import java.util.List;

import vitamio.vitamiolibrary.Fragments.HomeFragment;
import vitamio.vitamiolibrary.Fragments.RecordFragment;
import vitamio.vitamiolibrary.Fragments.UserCenterFragment;

public class BottomTab extends AppCompatActivity {

    private RadioGroup mRgTab;
    private List<Fragment> mFragmentList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bottom_tab);
        mRgTab = (RadioGroup) findViewById(R.id.rg_main);
        mRgTab.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                switch (checkedId) {
                    case R.id.rb_live:
                        changeFragment(HomeFragment.class.getName());
                        break;

                    case R.id.rb_record:
                        changeFragment(RecordFragment.class.getName());
                        break;

                    case R.id.rb_me:
                        changeFragment(UserCenterFragment.class.getName());
                        break;
                }
            }
        });
        if(savedInstanceState == null){
            changeFragment(HomeFragment.class.getName());
        }



    }

    /**
     *  初始化picasso使用okhttp作为网络请求框架
     */






    /**
     * show target fragment
     *
     * @param tag
     */
    public void changeFragment(String tag) {
        hideFragment();//在改变Fragment前，先把其他所有的Fragment隐藏
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        Fragment fragment = getFragmentManager().findFragmentByTag(tag);
        if (fragment != null) {
            transaction.show(fragment);
        } else {
            if (tag.equals(HomeFragment.class.getName())) {
                String userName = (String) getIntent().getSerializableExtra("USER_NAME");
                fragment = HomeFragment.newInstance(userName);
            } else if (tag.equals(RecordFragment.class.getName())) {
                fragment = RecordFragment.newInstance();
            } else if (tag.equals(UserCenterFragment.class.getName())) {
                String userName = (String) getIntent().getSerializableExtra("USER_NAME");
                String cookie = (String) getIntent().getSerializableExtra("mCookie");
                fragment = UserCenterFragment.newInstance(userName,cookie);
            }
            mFragmentList.add(fragment);
            transaction.add(R.id.fl_container, fragment, fragment.getClass().getName());//最后的参数为tag
        }
        transaction.commitAllowingStateLoss();

    }

    /**
     * hide all fragment
     */
    private void hideFragment() {
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        for (Fragment f : mFragmentList) {
            ft.hide(f);
        }
        ft.commit();
    }
}
