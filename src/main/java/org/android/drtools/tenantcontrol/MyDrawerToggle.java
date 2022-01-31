package org.android.drtools.tenantcontrol;

import android.app.Activity;
import android.view.View;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;

public class MyDrawerToggle extends ActionBarDrawerToggle {

    private final View mainContainer;
    private final View navDrawer;

    public MyDrawerToggle(Activity activity,
                          DrawerLayout drawerLayout,
                          Toolbar toolbar,
                          int openDrawerContentDescRes,
                          int closeDrawerContentDescRes,
                          View mainContainer,
                          View navDrawer) {
        super(activity, drawerLayout, toolbar, openDrawerContentDescRes, closeDrawerContentDescRes);
        this.mainContainer = mainContainer;
        this.navDrawer = navDrawer;
    }

    @Override
    public void onDrawerSlide(View drawerView, float slideOffset) {
        super.onDrawerSlide(drawerView, slideOffset);
        mainContainer.setTranslationX(navDrawer.getWidth() * slideOffset);
    }
}
