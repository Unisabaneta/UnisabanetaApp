package com.apps.unisabanetaapp;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.firebase.analytics.FirebaseAnalytics;

/**
 * Fragmento donde administraran los subfragmentos Notas, Horario y Record
 */
public class FragmentoSAG extends Fragment {

    TabLayout tabs;
    View view;
    ViewPager viewPager;
    PagerAdapter adapter;

    private FirebaseAnalytics mFirebaseAnalytics;
    private Bundle bundle;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_sag, container, false);
        return view;
    }


    @Override
    public void onActivityCreated (Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        mFirebaseAnalytics = FirebaseAnalytics.getInstance(getContext());
        bundle = new Bundle();

        tabs = (TabLayout) view.findViewById(R.id.tab_layout);
        viewPager = (ViewPager) view.findViewById(R.id.pager);
        adapter = new PagerAdapter(getChildFragmentManager(), tabs.getTabCount());
        viewPager.setOffscreenPageLimit(2);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabs));
        tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {

            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                if (tab.getPosition()==0){
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Fragmento_Horario");
                    mFirebaseAnalytics.logEvent("Fragmento_Horario", bundle);
                }

                if (tab.getPosition()==1){
                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Fragmento_Notas");
                    mFirebaseAnalytics.logEvent("Fragmento_Notas", bundle);
                }

                if(tab.getPosition()==2) {

                    bundle.putString(FirebaseAnalytics.Param.CONTENT_TYPE, "Fragmento_Record");
                    mFirebaseAnalytics.logEvent("Fragmento_Record", bundle);
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) { }

            @Override
            public void onTabReselected(TabLayout.Tab tab) { }
        });


    }




    public class PagerAdapter extends FragmentStatePagerAdapter {
        int mNumOfTabs;


        public PagerAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    FragmentoHorario tab1 = new FragmentoHorario();
                    return tab1;
                case 1:
                    FragmentoNotas tab2 = new FragmentoNotas();
                    return tab2;
                case 2:
                    FragmentoRecord tab3 = new FragmentoRecord();
                    return tab3;
                default:
                    return null;
            }
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }
}
