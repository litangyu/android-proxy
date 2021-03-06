package com.lechucksoftware.proxy.proxysettings.ui.adapters;

import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.lechucksoftware.proxy.proxysettings.R;
import com.lechucksoftware.proxy.proxysettings.utils.billing.Inventory;
import com.lechucksoftware.proxy.proxysettings.utils.billing.Purchase;
import com.lechucksoftware.proxy.proxysettings.utils.billing.SkuDetails;

import java.util.ArrayList;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

/**
 * Created by Marco on 06/04/15.
 */
public class IabSkuRecyclerViewAdapter extends RecyclerView.Adapter<IabSkuRecyclerViewAdapter.IabSkuViewHolder>
{
    private final Inventory inventory;
    private int itemLayout;
    OnItemClickListener mItemClickListener;

    public interface OnItemClickListener
    {
        void onItemClick(View view , int position);
    }

    public class IabSkuViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener
    {
        @InjectView(R.id.iab_sku_disabled) RelativeLayout skuDisabled;
        @InjectView(R.id.iab_sku_cardview) CardView skuCardView;
        @InjectView(R.id.iab_sku_title) TextView skuTitle;
        @InjectView(R.id.iab_sku_description) TextView skuDescription;

        public IabSkuViewHolder(View itemView)
        {
            super(itemView);
            ButterKnife.inject(this, itemView);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View view)
        {
            int position = getLayoutPosition();
            Timber.d("Selected Recycler View Item %d", position);

            if (mItemClickListener != null)
            {
                mItemClickListener.onItemClick(view, position);
            }
        }
    }

    public IabSkuRecyclerViewAdapter(Inventory inventory, int itemLayout)
    {
        this.inventory = inventory;
        this.itemLayout = itemLayout;
    }

    public void setOnItemClickListener(final OnItemClickListener mItemClickListener)
    {
        this.mItemClickListener = mItemClickListener;
    }

    @Override
    public IabSkuViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater
                .from(parent.getContext())
                .inflate(this.itemLayout, parent, false);

        return new IabSkuViewHolder(v);
    }

    @Override
    public void onBindViewHolder(IabSkuViewHolder holder, int position)
    {
        SkuDetails iabSku = this.inventory.getAllSkus().get(position);

        holder.skuTitle.setText(iabSku.getTitle());
        holder.skuDescription.setText(iabSku.getDescription());

//        SkuDetails sku = inventory.getAllSkus().get(position);
//        if (inventory.hasPurchase(sku.getSku()))
//        {
//            Purchase p = inventory.getPurchase(sku.getSku());
//            Timber.d("SKU purchased: %s", p.toString());
//        }
    }

    @Override
    public int getItemCount()
    {
        if (inventory != null && inventory.getAllSkus() != null)
        {
            return this.inventory.getAllSkus().size();
        }
        else
        {
            return 0;
        }
    }
}
