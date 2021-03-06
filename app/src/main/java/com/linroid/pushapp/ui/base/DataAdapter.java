/*
 * Copyright (c) linroid 2015.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.linroid.pushapp.ui.base;

import android.support.v7.widget.RecyclerView;

import java.util.List;

import rx.functions.Action1;
import timber.log.Timber;

/**
 * Created by linroid on 7/20/15.
 */
public abstract class DataAdapter<T, VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<VH> implements Action1<List<T>> {
    protected List<T> data;


    public void setData(List<T> data) {
        this.data = data;
        notifyDataSetChanged();
    }

    public void insertItem(int location, T item) {
        data.add(location, item);
        notifyItemInserted(location);
    }

    public void insertItem(T item) {
        insertItem(0, item);
    }

    public void set(int location, T item) {
        data.set(location, item);
        notifyItemChanged(location);
    }

    public void remove(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }

    public void removeItem(T item) {
        for (int i=0; i<=this.data.size(); i++) {
            if(data.get(i)==item) {
                data.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    @Override
    public void call(List<T> ts) {
        this.data = ts;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return data == null ? 0 : data.size();
    }

    public List<T> getData() {
        return data;
    }
}
