package app.cooka.cookapp;

import android.view.View;

interface CardPagerAdapter {
    View getViewAt(int pos);
    float getBaseCardElevation();
    int getCount();
}
