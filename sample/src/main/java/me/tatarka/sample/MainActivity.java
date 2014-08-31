package me.tatarka.sample;

import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v7.app.ActionBarActivity;

import me.tatarka.sample.holdr.Holdr_ActivityMain;
import me.tatarka.samplelibrary.LibraryExampleFragment;
import me.tatarka.samplelibrary.TitledFragment;

public class MainActivity extends ActionBarActivity {
    private Holdr_ActivityMain holdr;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        holdr = new Holdr_ActivityMain(findViewById(android.R.id.content));
        holdr.pager.setAdapter(new MyPagerAdapter(getSupportFragmentManager()));
    }
    
    private class MyPagerAdapter extends FragmentPagerAdapter {
        private TitledFragment[] fragments = new TitledFragment[] {
                new BasicExampleFragment(),
                new AdvancedExampleFragment(),
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
