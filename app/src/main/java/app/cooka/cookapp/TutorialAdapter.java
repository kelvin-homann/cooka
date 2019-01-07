package app.cooka.cookapp;

import android.content.Context;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;

class TutorialAdapter extends PagerAdapter {
    private Context mContext;
    ArrayList<TutorialItem> tutorialItems;

    public TutorialAdapter(Context mContext, ArrayList<TutorialItem> items) {
        this.mContext = mContext;
        this.tutorialItems = items;
    }

    @Override
    public int getCount() {
        return tutorialItems.size();
    }

    @Override
    public boolean isViewFromObject(View view, Object object) {
        return view == object;
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        View itemView = LayoutInflater.from(mContext).inflate(R.layout.tutorial_item, container, false);

        TutorialItem item= tutorialItems.get(position);

        ImageView imageView = itemView.findViewById(R.id.iv_onboard);
        imageView.setImageResource(item.getImage());

        TextView tv_title= itemView.findViewById(R.id.tv_header);
        tv_title.setText(item.getTitle());

        TextView tv_content= itemView.findViewById(R.id.tv_desc);
        tv_content.setText(item.getDescription());

        container.addView(itemView);

        return itemView;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        container.removeView((LinearLayout) object);
    }

}

