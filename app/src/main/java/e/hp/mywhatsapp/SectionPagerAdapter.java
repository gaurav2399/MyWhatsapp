package e.hp.mywhatsapp;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

class SectionPagerAdapter extends FragmentPagerAdapter{

    public SectionPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position){

            case 0:
                RequestsFragment requestsFragment=new RequestsFragment();
                return requestsFragment;

            case 1:
                ChatFragment chatFragment=new ChatFragment();
                return chatFragment;

            case 2:
                FriendsFragment friendsFragment=new FriendsFragment();
                return friendsFragment;

                default:
                    return null;
        }
    }

    @Override
    public int getCount() {
        return 3;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {

        switch (position){
            case 0:
                return "REQUESTS";
            case 1:
                return "CHATS";
            case 2:
                return "FRIENDS";
                default:
                    return null;
        }
    }
}
