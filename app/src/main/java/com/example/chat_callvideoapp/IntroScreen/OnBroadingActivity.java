package com.example.chat_callvideoapp.IntroScreen;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.chat_callvideoapp.ActivityScreen.PhoneNumberActivity;
import com.example.chat_callvideoapp.MainActivity;
import com.example.chat_callvideoapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

import me.relex.circleindicator.CircleIndicator;

public class OnBroadingActivity extends AppCompatActivity {
    TextView tvNext , tvSkip;
    private ViewPager viewPager;
    RelativeLayout layout_nextPage;
    CircleIndicator circleIndicator;
    ViewPagerAdapter viewPagerAdapter;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_onbroading);
        init();
        action();
    }

    //khai báo component
    private void  init(){
        tvNext = findViewById(R.id.onBroading_tvNext);
        tvSkip = findViewById(R.id.onBroading_tvSkip);
        viewPager = findViewById(R.id.onBroading_viewPager);
        layout_nextPage = findViewById(R.id.onBroading_nextPage);
        circleIndicator = findViewById(R.id.onBroading_circle);
    }

    //thực hiện các tác vụ công việc
    public void action(){
        viewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager(),FragmentStatePagerAdapter.BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        viewPager.setAdapter(viewPagerAdapter);
        circleIndicator.setViewPager(viewPager);

        tvSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                viewPager.setCurrentItem(2);
            }
        });

        tvNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (viewPager.getCurrentItem() < 2){
                    viewPager.setCurrentItem(viewPager.getCurrentItem() + 1);
                }
            }
        });


        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
            }
            @Override
            public void onPageSelected(int position) {
                if (position == 2){
                    tvSkip.setVisibility(View.GONE);
                    layout_nextPage.setVisibility(View.GONE);
                }else{
                    tvSkip.setVisibility(View.VISIBLE);
                    layout_nextPage.setVisibility(View.VISIBLE);
                }
            }
            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        mAuth = FirebaseAuth.getInstance();
        if (mAuth.getCurrentUser() != null){
            Intent mIntent = new Intent(OnBroadingActivity.this, MainActivity.class);
            startActivity(mIntent);
            finish();
        }
    }
}