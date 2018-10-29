package com.nepal.tia.event;

import android.view.View;

public interface OnItemActionListener<T> {

    void onItemEditAction(View v, int position, T t);

    void onItemDeleteAction(View v, int position, T t);
}
