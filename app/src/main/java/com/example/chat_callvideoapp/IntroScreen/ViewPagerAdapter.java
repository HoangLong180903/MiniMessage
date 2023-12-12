package com.example.chat_callvideoapp.IntroScreen;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.example.chat_callvideoapp.OnBroadingFragment.PageOne;
import com.example.chat_callvideoapp.OnBroadingFragment.PageThree;
import com.example.chat_callvideoapp.OnBroadingFragment.PageTwo;

public class ViewPagerAdapter extends FragmentStatePagerAdapter {
    public ViewPagerAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 0){
            return new PageOne();
        }else if (position == 1){
            return new PageTwo();
        }else if (position == 2){
            return new PageThree();
        }else {
            return new PageOne();
        }
    }

    @Override
    public int getCount() {
        return 3;
    }
}
