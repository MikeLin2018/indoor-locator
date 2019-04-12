package com.locateme.indoor_locator;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.View;

public class SwipeToDeleteCallback extends ItemTouchHelper.SimpleCallback {

    private RecyclerView.Adapter mAdapter;
    private Drawable icon;
    private final ColorDrawable background;

    public SwipeToDeleteCallback(RecyclerView.Adapter adapter) {
        super(0, ItemTouchHelper.LEFT);
        mAdapter = adapter;
        if (mAdapter instanceof BuildingFragment.BuildingAdapter) {
            icon = ContextCompat.getDrawable((Context) ((BuildingFragment.BuildingAdapter) mAdapter).getContext(), R.drawable.ic_delete);
        } else if (mAdapter instanceof RoomFragment.RoomAdapter) {
            icon = ContextCompat.getDrawable((Context) ((RoomFragment.RoomAdapter) mAdapter).getContext(), R.drawable.ic_delete);
        }

        background = new ColorDrawable(Color.RED);
    }

    @Override
    public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);


        // Code borrow from Zachery Osborn on Medium.com
        View itemView = viewHolder.itemView;
        int backgroundCornerOffset = 20;
        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconBottom = iconTop + icon.getIntrinsicHeight();
        int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
        int iconRight = itemView.getRight() - iconMargin;
        if (dX < 0) { // Swiping to left
            icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
            background.setBounds(itemView.getRight() + ((int) dX) - backgroundCornerOffset,
                    itemView.getTop(), itemView.getRight(), itemView.getBottom());
        } else { // UnSwiped
            background.setBounds(0, 0, 0, 0);
        }

        background.draw(c);
        icon.draw(c);
    }

    @Override
    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {

        return false;
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        int position = viewHolder.getAdapterPosition();
        if (mAdapter instanceof BuildingFragment.BuildingAdapter) {
           // ((BuildingFragment.BuildingAdapter) mAdapter).deleteItem(position);
              new AlertDialog.Builder(viewHolder.itemView.getContext())
                      .setTitle("Warning")
                      .setMessage("want to delete?")
                      .setPositiveButton("OK",
                              new DialogInterface.OnClickListener() {
                                  public void onClick(DialogInterface dialog, int which) {
                                      ((BuildingFragment.BuildingAdapter) mAdapter).deleteItem(position);
                                  }
                              })
                      .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                          public void onClick(DialogInterface dialog, int which) {
                              mAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                          }
                      }).create().show();

        } else if (mAdapter instanceof RoomFragment.RoomAdapter) {
            new AlertDialog.Builder(viewHolder.itemView.getContext())
                    .setTitle("Warning")
                    .setMessage("want to delete?")
                    .setPositiveButton("OK",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                        ((RoomFragment.RoomAdapter) mAdapter).deleteItem(position);
                                }
                            })
                    .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {mAdapter.notifyItemChanged(viewHolder.getAdapterPosition());
                        }
                    }).create().show();
        }

    }

}
