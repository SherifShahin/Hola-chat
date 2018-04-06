package com.downloader.hola;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

/**
 * Created by Lenovo on 3/4/2018.
 */

class SectionsPagerAdapter  extends FragmentPagerAdapter
{

    public SectionsPagerAdapter(FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public Fragment getItem(int position)
    {
        switch (position) {
            case 0:
                ChatsFragment chatsFragment=new ChatsFragment();
                return chatsFragment;

            case 1:
                FriendsFragment friendsFragment=new FriendsFragment();
                return friendsFragment;

            case 2:
                RequestsFragment requestsFragment = new RequestsFragment();
                return requestsFragment;


            default:
                return null;
        }
    }

    @Override
    public int getCount()
    {
        return 3;
    }

    @Override
    public CharSequence getPageTitle(int position)
    {
        switch (position)
        {
            case 0:
                return "CHAT";

            case 1:
                return "FRIENDS";

            case 2:
                return "REQUESTS";


            default:
                return null;
        }
    }
}
