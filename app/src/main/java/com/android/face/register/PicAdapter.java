package com.android.face.register;

import android.content.Context;
import android.os.AsyncTask;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

import com.ragentek.face.R;

public class PicAdapter extends RecyclerView.Adapter<PicAdapter.PicViewHolder> {
    private Context mContext;
    private LayoutInflater mInflater;
    private List<ItemObject> mItemObjects;
    private String mRootPath;
    private List<String> mFileTree;
    private int mPicFileCount;

    private OnItemClickListener mOnItemClickListener;
    private OnItemCheckListener mOnItemCheckListener;

    public PicAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(mContext);
        mItemObjects = new ArrayList<>();
        mFileTree = new ArrayList<>();
    }

    @Override
    public PicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = mInflater.inflate(R.layout.pic_item, parent, false);
        return new PicViewHolder(view);
    }

    @Override
    public int getItemCount() {
        return mItemObjects.size();
    }

    public ItemObject getItem(int position) {
        return mItemObjects.get(position);
    }

    @Override
    public void onBindViewHolder(PicViewHolder holder, int position) {
        holder.bindView(mItemObjects.get(position), position);
    }

    public void notifyItemChanged(int positionStart, int itemCount) {
        mPicFileCount = 0;
        notifyItemRangeChanged(positionStart, itemCount);
    }

    public void setPicFiles(List<ItemObject> itemObjects) {
        mItemObjects = itemObjects;
        notifyDataSetChanged();
    }

    public List<ItemObject> getPicFiles() {
        return mItemObjects;
    }

    public void addPicFiles(List<ItemObject> itemObjects) {
        mItemObjects.addAll(itemObjects);
        notifyDataSetChanged();
    }

    public void addPicFile(ItemObject itemObject) {
        mItemObjects.add(itemObject);
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mOnItemClickListener = listener;
    }

    public void setOnItemCheckListener(OnItemCheckListener listener) {
        mOnItemCheckListener = listener;
    }

    public boolean onBackPressed() {
        if (mFileTree.size() == 1) {
            mFileTree.remove(0);
            loadInternal(mRootPath);
            return true;
        } else if (mFileTree.size() > 1) {
            mFileTree.remove(mFileTree.size() - 1);
            loadInternal(mFileTree.get(mFileTree.size() - 1));
            return true;
        }
        return false;
    }

    public int getPicFileCount() {
        return mPicFileCount;
    }

    class PicViewHolder extends RecyclerView.ViewHolder {
        ImageView iconView;
        TextView textView;
        CheckBox checkBox;

        public PicViewHolder(View itemView) {
            super(itemView);
            iconView = (ImageView) itemView.findViewById(R.id.icon);
            textView = (TextView) itemView.findViewById(R.id.text);
            checkBox = (CheckBox) itemView.findViewById(R.id.checkbox);
        }

        public void bindView(ItemObject itemObject, int position) {
            if (itemObject.isDirectory()) {
                iconView.setImageResource(R.mipmap.fm_folder);
                checkBox.setVisibility(View.GONE);
            } else {
                iconView.setImageResource(R.mipmap.fm_picture);
                checkBox.setVisibility(View.VISIBLE);
                if (itemObject.checked) {
                    checkBox.setChecked(true);
                } else {
                    checkBox.setChecked(false);
                }
                mPicFileCount++;
            }
            textView.setText(itemObject.getName());
            setClickListener(position);
        }

        private void setClickListener(final int position) {
            itemView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    ItemObject itemObject = mItemObjects.get(position);
                    if (itemObject.isDirectory()) {
                        String path = itemObject.getFilePath();
                        mFileTree.add(path);
                        loadInternal(path);
                    } else {
                        itemObject.checked = !itemObject.checked;
                        checkBox.setChecked(itemObject.checked);
                        if (mOnItemCheckListener != null) {
                            mOnItemCheckListener.onItemCheck(itemObject, position);
                        }
                    }
                }
            });
        }
    }

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnItemCheckListener {
        void onItemCheck(ItemObject itemObject, int position);
    }

    public void load(String rootPath) {
        mRootPath = rootPath;
        mPicFileCount = 0;
        new PicLoader().execute(mRootPath);
    }

    private void loadInternal(String path) {
        mPicFileCount = 0;
        new PicLoader().execute(path);
    }

    private File[] getPicFiles(String dirPath) {
        File folder = new File(dirPath);
        File[] files = folder.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String filename) {
                String name = filename.toLowerCase();
                Log.d("sqm", "accept: " + filename);
                if (name.endsWith(".jpg")
                        || name.endsWith(".jpeg")
                        || name.endsWith(".png")
                        || name.endsWith(".bmp")
                        || new File(dir, filename).isDirectory()) {
                    Log.d("sqm", "accept: -----------------------true ,name=" + name);
                    return true;
                }
                return false;
            }
        });

        return files;
    }

    private List<ItemObject> generateItemObjects(File[] files) {
        if (files == null || files.length == 0) {
            return null;
        }
        ArrayList<ItemObject> itemObjects = new ArrayList<>();
        for (File file : files) {
            itemObjects.add(new ItemObject(file));
        }
        return itemObjects;
    }

    class PicLoader extends AsyncTask<String, Integer, List<ItemObject>> {

        @Override
        protected List<ItemObject> doInBackground(String... strings) {
            File[] files = getPicFiles(strings[0]);
            List<ItemObject> itemObjects = generateItemObjects(files);
            return itemObjects;
        }

        @Override
        protected void onPostExecute(List<ItemObject> itemObjects) {
            if (itemObjects != null) {
                setPicFiles(itemObjects);
            }
        }
    }
}
