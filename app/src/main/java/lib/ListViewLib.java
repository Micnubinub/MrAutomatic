package lib;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

/**
 * Created by root on 29/12/14.
 */
public class ListViewLib extends ViewGroup {
    private final OnClickListener listener = new OnClickListener() {
        @Override
        public void onClick(View v) {

        }
    };
    private Dialog dialog;
    private ArrayList<StoreItem> storeItems;
    private ListView listView;
    private StoreListener storeListener;
    private int numCoins, descriptionTextSize, priceTextSize, itemNameTextSize, buyButtonTextSize;
    private int nameTextColor, priceTextColor, itemTextColor, buyTextColor, descriptionTextColor, coinTextColor, titleTextColor;
    private TextView title, coinText;
    private Adapter adapter;
    private ImageView coinIcon;
    private Drawable listItemBackground;
    private int padding;
    private int listViewDividerHeight, listViewPadding, listViewMargin;
    private Drawable storeItemBackground;
    private Typeface storeItemFont;
    private Typeface mainFont;
    private ImageView closeButton;
    private Drawable buyButtonBackground;

    public ListViewLib(Context context, Dialog dialog) {
        super(context);
        padding = dpToPixels(5);
        final LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

        title = new TextView(context);
        title.setPadding(padding, padding, padding, padding);
        title.setLayoutParams(params);

        coinText = new TextView(context);
        coinText.setLayoutParams(params);

        coinIcon = new ImageView(context);
        coinIcon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        coinIcon.setLayoutParams(new LayoutParams(dpToPixels(28), dpToPixels(28)));

        closeButton = new ImageView(context);
        closeButton.setPadding(padding / 2, padding / 2, padding / 2, padding / 2);
        closeButton.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
        closeButton.setLayoutParams(new LayoutParams(dpToPixels(28), dpToPixels(28)));
        closeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                close();
            }
        });

        listView = new ListView(context);
        listView.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));

        addView(coinText);
        addView(listView);
        addView(title);
        addView(coinIcon);
        addView(closeButton);
    }

    public void setCoinIcon(Drawable coinIcon) {
        this.coinIcon.setImageDrawable(coinIcon);
    }

    @Override
    protected void onLayout(boolean b, int i, int i2, int i3, int i4) {
        int tl = (getMeasuredWidth() - title.getMeasuredWidth()) / 2;
        title.layout(
                tl, padding,
                getMeasuredWidth() - tl,
                padding + title.getMeasuredWidth()
        );

        listView.layout(padding, padding + title.getMeasuredWidth() + padding,
                getMeasuredHeight() - padding,
                getMeasuredHeight() - coinIcon.getMeasuredHeight() - padding);

        closeButton.layout(getMeasuredWidth() - padding - closeButton.getMeasuredWidth(), padding,
                getMeasuredWidth() - padding,
                padding + closeButton.getMeasuredHeight());

        int coinTop = getMeasuredHeight() - padding - coinIcon.getMeasuredHeight();

        coinIcon.layout(padding, coinTop, padding + coinIcon.getMeasuredWidth(),
                getMeasuredHeight() - padding);

        coinText.layout(padding + coinIcon.getMeasuredWidth() + padding, coinTop,
                padding + coinIcon.getMeasuredWidth() + padding + coinText.getMeasuredWidth(), coinTop + coinText.getMeasuredHeight()
        );

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int measuredHeight = 0;
        int measuredWidth = 0;

        for (int i = 0; i < getChildCount(); i++) {
            final View child = getChildAt(i);
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            measuredHeight = Math.max(measuredHeight, child.getMeasuredHeight());
            measuredWidth += child.getMeasuredWidth();
        }

        setMeasuredDimension(resolveSizeAndState(measuredWidth, widthMeasureSpec, 0),
                resolveSizeAndState(measuredHeight, heightMeasureSpec, 0));
    }

    public void setItemNameTextSize(int itemNameTextSize) {
        this.itemNameTextSize = itemNameTextSize;
    }

    public void setDescriptionTextSize(int descriptionTextSize) {
        this.descriptionTextSize = descriptionTextSize;
    }

    public void setBuyButtonTextSize(int buyButtonTextSize) {
        this.buyButtonTextSize = buyButtonTextSize;
    }

    public void close() {
        try {
            dialog.dismiss();
            if (storeListener != null)
                storeListener.onStoreClosed();
        } catch (Exception e) {

        }
    }

    private void getListView() {
        if (listView == null)
            listView = new ListView(getContext());

        listView.setDividerHeight(listViewDividerHeight);
        listView.setPadding(listViewPadding, listViewPadding, listViewPadding, listViewPadding);
        adapter = new Adapter();
    }

    private int dpToPixels(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    public void setStoreItems(ArrayList<StoreItem> storeItems) {
        this.storeItems = storeItems;
        if (adapter != null)
            adapter.notifyDataSetChanged();
    }

    public void setStoreListener(StoreListener storeListener) {
        this.storeListener = storeListener;
    }

    private void buyItem(int position) {
        final StoreItem item = storeItems.get(position);

        if (item.bought) {
            if (storeListener != null)
                storeListener.onEquipItem(item);
        } else if (item.price <= numCoins) {
            if (storeListener != null)
                storeListener.onBuyItem(item);

            item.bought = true;
            setNumCoins(numCoins - item.price);
        } else {
            if (storeListener != null)
                storeListener.onFailedToBuyItem(item);
        }
    }

    public void setListItemBackground(Drawable listItemBackground) {
        this.listItemBackground = listItemBackground;
    }

    public void setStoreItemBackground(Drawable storeItemBackground) {
        //Todo implement
        this.storeItemBackground = storeItemBackground;
    }

    private void setStoreItemFont(Typeface typeface) {
        storeItemFont = typeface;
    }

    public void setNumCoins(int numCoins) {
        //Todo modify textview
        coinText.setText(String.valueOf(numCoins));
        this.numCoins = numCoins;
    }

    public void setNameTextColor(int nameTextColor) {
        this.nameTextColor = nameTextColor;
    }

    public void setPriceTextColor(int priceTextColor) {
        this.priceTextColor = priceTextColor;
    }

    public void setDescriptionTextColor(int descriptionTextColor) {
        this.descriptionTextColor = descriptionTextColor;
    }

    public void setCoinTextColor(int coinTextColor) {
        this.coinTextColor = coinTextColor;
    }

    public void setTitleTextColor(int titleTextColor) {
        this.titleTextColor = titleTextColor;
    }

    public void setTitle(String title) {
        this.title.setText(title);
    }

    public void setTitleSize(int size) {
        this.title.setTextSize(size);
    }

    public void setTitleSizeDp(int size) {
        this.title.setTextSize(dpToPixels(size));
    }

    public void setTitleFont(Typeface typeface) {
        this.title.setTypeface(typeface);
    }


    public void setCoinTextSize(int size) {
        coinText.setTextSize(size);
    }

    public void setCoinTextSizeDp(int size) {
        coinText.setTextSize(dpToPixels(size));
    }

    public void setCoinTextFont(Typeface typeface) {
        this.title.setTypeface(typeface);
    }

    public void setListViewDiviederHeight(int listViewDiviederHeight) {
        this.listViewDividerHeight = listViewDiviederHeight;
    }

    public void setListViewMargin(int listViewMargin) {
        this.listViewMargin = listViewMargin;
    }

    public void setListViewPadding(int listViewPadding) {
        this.listViewPadding = listViewPadding;
    }

    public void setMainFont(Typeface mainFont) {
        this.mainFont = mainFont;
    }

    public void setPriceTextSize(int priceTextSize) {
        this.priceTextSize = priceTextSize;
    }

    public void setBuyButtonBackground(Drawable buyButtonBackground) {
        this.buyButtonBackground = buyButtonBackground;
    }

    public void setCloseButton(Drawable closeButton) {
        this.closeButton.setImageDrawable(closeButton);
    }

    public interface StoreListener {
        void onBuyItem(StoreItem item);

        void onEquipItem(StoreItem item);

        void onFailedToBuyItem(StoreItem item);

        void onStoreOpened();

        void onStoreClosed();
    }

    public class StoreItem {
        final int price;
        final String name, description;
        final Drawable icon;
        boolean bought;

        public StoreItem(Drawable icon, String name, String description, int price, boolean bought) {
            this.icon = icon;
            this.price = price;
            this.bought = bought;
            this.name = name;
            this.description = description;
        }
    }

    private class Adapter extends BaseAdapter {
        @Override
        public int getCount() {
            return storeItems.size();
        }

        @Override
        public Object getItem(int position) {
            return storeItems.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ListItem view = new ListItem(getContext());
            final StoreItem item = storeItems.get(position);
            view.setName(item.name);
            view.setDescription(item.description);
            view.setBought(item.bought);
            view.setIcon(item.icon);
            view.setPrice(String.format("$%d", item.price));
            view.setPosition(position);

            return view;
        }
    }

    private class ListItem extends ViewGroup {
        final OnClickListener listener = new OnClickListener() {
            @Override
            public void onClick(View v) {
                buyItem(position);
            }
        };
        private TextView name, price, description;
        private Button buy;
        private int position;
        private boolean bought;
        private ImageView icon;


        public ListItem(Context context) {
            super(context);
            if (listItemBackground != null) {
                try {
                    setBackground(listItemBackground);
                } catch (Exception e) {
                    setBackgroundDrawable(listItemBackground);
                }
            }

            name = new TextView(context);
            description = new TextView(context);
            price = new TextView(context);
            buy = new Button(context);
            icon = new ImageView(context);

            final ViewGroup.LayoutParams params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);

            price.setLayoutParams(params);
            description.setLayoutParams(params);
            name.setLayoutParams(params);
            buy.setLayoutParams(params);

            price.setTypeface(storeItemFont);
            description.setTypeface(storeItemFont);
            name.setTypeface(storeItemFont);
            buy.setTypeface(storeItemFont);

            price.setTextSize(priceTextSize);
            name.setTextSize(itemNameTextSize);
            description.setTextSize(descriptionTextSize);

            price.setTextColor(priceTextColor);
            name.setTextColor(itemTextColor);
            description.setTextColor(descriptionTextColor);

            buy.setTextSize(buyButtonTextSize);
            buy.setTextColor(buyTextColor);
            buy.setOnClickListener(listener);

            if (buyButtonBackground != null) {
                try {
                    buy.setBackground(buyButtonBackground);
                } catch (Exception e) {
                    buy.setBackgroundDrawable(buyButtonBackground);
                }
            }

            icon.setLayoutParams(new LayoutParams(padding * 13, padding * 13));
            icon.setScaleType(ImageView.ScaleType.CENTER_INSIDE);

            name.setMaxLines(1);
            name.setEllipsize(TextUtils.TruncateAt.END);

            price.setMaxLines(1);
            price.setEllipsize(TextUtils.TruncateAt.END);

            description.setMaxLines(2);
            description.setEllipsize(TextUtils.TruncateAt.END);

            addView(name);
            addView(description);
            addView(price);
            addView(buy);
            addView(icon);

            setLayoutParams(new AbsListView.LayoutParams(LayoutParams.FILL_PARENT, padding * 15));
        }

        @Override
        protected void onLayout(boolean b, int i, int i2, int i3, int i4) {
            icon.layout(padding, padding, padding * 14, padding * 14);

            final int buyPrice = Math.max(buy.getMeasuredWidth(), price.getMeasuredHeight());
            final int top = (getMeasuredHeight() - buy.getMeasuredHeight() - price.getMeasuredHeight()) / 3;

            name.layout(padding * 15, padding, getMeasuredWidth() - padding - padding - buyPrice, padding + name.getMeasuredHeight());
            description.layout(padding * 15, padding + description.getMeasuredHeight(), getMeasuredWidth() - padding - padding - buyPrice, padding + description.getMeasuredHeight() + name.getMeasuredHeight());

            price.layout(getMeasuredWidth() - padding - price.getMeasuredWidth(), top, getMeasuredWidth() - padding, top + price.getMeasuredHeight());
            buy.layout(getMeasuredWidth() - padding - buy.getMeasuredWidth(),
                    top + top + price.getMeasuredHeight(), getMeasuredWidth() - padding, top + top + price.getMeasuredHeight() + buy.getMeasuredHeight());


        }

        @Override
        protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            int measuredHeight = 0;
            int measuredWidth = 0;

            for (int i = 0; i < getChildCount(); i++) {
                final View child = getChildAt(i);
                measureChild(child, widthMeasureSpec, heightMeasureSpec);
                measuredHeight = Math.max(measuredHeight, child.getMeasuredHeight());
                measuredWidth += child.getMeasuredWidth();
            }

            setMeasuredDimension(resolveSizeAndState(measuredWidth, widthMeasureSpec, 0),
                    resolveSizeAndState(measuredHeight, heightMeasureSpec, 0));
        }

        public void setBought(boolean bought) {
            this.bought = bought;
            buy.setText(bought ? "Equip" : "Buy");
        }

        public void setDescription(String description) {
            this.description.setText(description);
        }

        public void setIcon(Drawable icon) {
            this.icon.setImageDrawable(icon);
        }

        public void setPrice(String price) {
            this.price.setText(price);
        }

        public void setName(String name) {
            this.name.setText(name);
        }

        public int getPosition() {
            return position;
        }

        public void setPosition(int position) {
            this.position = position;
        }
    }
}
