/*******************************************************************************
 * Copyright (c) 2011 - 2012 Adrian Vielsack, Christof Urbaczek, Florian Rosenthal, Michael Hoff, Moritz Lüdecke, Philip Flohr.
 * 
 * This file is part of Sudowars,
 * Based on an official Android sample app
 * http://developer.android.com/training/implementing-navigation/lateral.html
 * 
 * Sudowars is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Sudowars is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Sudowars.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * 
 * 
 * Diese Datei ist Teil von Sudowars.
 * 
 * Sudowars ist Freie Software: Sie können es unter den Bedingungen
 * der GNU General Public License, wie von der Free Software Foundation,
 * Version 3 der Lizenz oder (nach Ihrer Option) jeder späteren
 * veröffentlichten Version, weiterverbreiten und/oder modifizieren.
 * 
 * Sudowars wird in der Hoffnung, dass es nützlich sein wird, aber
 * OHNE JEDE GEWÄHELEISTUNG, bereitgestellt; sogar ohne die implizite
 * Gewährleistung der MARKTFÄHIGKEIT oder EIGNUNG FÜR EINEN BESTIMMTEN ZWECK.
 * Siehe die GNU General Public License für weitere Details.
 * 
 * Sie sollten eine Kopie der GNU General Public License zusammen mit diesem
 * Programm erhalten haben. Wenn nicht, siehe <http://www.gnu.org/licenses/>.
 * 
 * Contributors:
 * initial API and implementation:
 * Adrian Vielsack
 * Christof Urbaczek
 * Florian Rosenthal
 * Michael Hoff
 * Moritz Lüdecke
 * Philip Flohr 
 ******************************************************************************/
package com.hat_cloud.sudoku.activity;


import android.app.ActionBar;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.hat_cloud.sudoku.R;

public class ManualActivity extends BaseActivity {
	/**
	 * tricker, if this activity is bound to the pool
	 */
	private boolean bound = false;
	
	/**
     * The {@link android.support.v4.view.PagerAdapter} that will provide fragments representing
     * each object in a collection.
     */
    ManualPagerAdapter mManualPagerAdapter;
    
    /**
     * The {@link ViewPager} that will display the object collection.
     */
    private ViewPager mViewPager;
    
    /*
     * (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
		setContentView(R.layout.manual);
		
		final ActionBar actionBar = getActionBar();
        if(actionBar!=null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
        mManualPagerAdapter = new ManualPagerAdapter(getSupportFragmentManager(), getResources());
        mViewPager = (ViewPager) findViewById(R.id.pager);
        mViewPager.setAdapter(mManualPagerAdapter);
    }
    

    /**
     * A {@link FragmentStatePagerAdapter} that returns a fragment
     * representing an object in the collection.
     */
    public static class ManualPagerAdapter extends FragmentStatePagerAdapter {
        private String[] mTitles;
        private static String[] mBodies;
        private static int[] mImages = new int[] {
        	R.drawable.screen1,
        	R.drawable.screen2,
        	R.drawable.screen3,
        	R.drawable.screen4,
        	R.drawable.screen5,
        	R.drawable.screen6,
    	};
    	
        public ManualPagerAdapter(FragmentManager fm, Resources res) {
            super(fm);
            mTitles = res.getStringArray(R.array.manual_title);
            mBodies = res.getStringArray(R.array.manual_body);
        }
        
        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new ManualObjectFragment();
            Bundle args = new Bundle();
            args.putInt(ManualObjectFragment.INDEX, i);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return mTitles.length;
        }
        
        @Override
        public CharSequence getPageTitle(int position) {
        	return mTitles[position];
        }
    }

    /**
     * The fragment object
     */
    public static class ManualObjectFragment extends Fragment {
        public static final String INDEX = "index";
        
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.manual_object, container, false);
            Bundle args = getArguments();
            int i = args.getInt(INDEX);
            ((TextView) rootView.findViewById(android.R.id.text1)).setText(
            		ManualPagerAdapter.mBodies[i]);
            ((ImageView) rootView.findViewById(R.id.image)).setImageResource(
            		ManualPagerAdapter.mImages[i]);
            return rootView;
        }
    }
}