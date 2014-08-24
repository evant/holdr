package me.tatarka.socket.sample;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.ActionBarActivity;

import me.tatarka.samplelibrary.LibraryExampleFragment;
import me.tatarka.samplelibrary.TitledFragment;
import me.tatarka.socket.sample.sockets.Socket_ActivityMain;

public class MainActivity extends ActionBarActivity {
    private Socket_ActivityMain socket;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        socket = new Socket_ActivityMain(findViewById(android.R.id.content));
        socket.pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
    }
    
    private class MyPagerAdapter extends FragmentPagerAdapter {
        private TitledFragment[] fragments = new TitledFragment[] {
                new BasicExampleFragment(),
                new AdvancedExample(),
                new ListExampleFragment(),
                new LibraryExampleFragment()
        };
        
        public MyPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public TitledFragment getItem(int i) {
            return fragments[i];
        }

        @Override
        public int getCount() {
            return fragments.length;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return fragments[position].getTitle();
        }
    }
}
