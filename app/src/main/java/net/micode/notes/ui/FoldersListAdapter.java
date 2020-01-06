/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.micode.notes.ui;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import net.micode.notes.R;
import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.NoteColumns;

/**
 * 文件夹列表适配器，用于显示文件夹列表的视图
 * @version v1.0
 * @author www.micode.net
 */
public class FoldersListAdapter extends CursorAdapter {
    //
    public static final String [] PROJECTION = {
        NoteColumns.ID,
        NoteColumns.SNIPPET
    };
    //语句：定义常量，文件夹ID栏编号
    public static final int ID_COLUMN   = 0;
    //语句：定义常量，文件夹名称栏编号
    public static final int NAME_COLUMN = 1;

    public FoldersListAdapter(Context context, Cursor c) {
        super(context, c);
        // TODO Auto-generated constructor stub
    }

    //方法：建立新视图，返回创建的文件夹列表
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return new FolderListItem(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        //语句块：如果参数视图为文件夹视图的实例，根据当前游标的ID绑定相应文件夹的视图
        if (view instanceof FolderListItem) {
            String folderName = (cursor.getLong(ID_COLUMN) == Notes.ID_ROOT_FOLDER) ? context
                    .getString(R.string.menu_move_parent_folder) : cursor.getString(NAME_COLUMN);
            //语句：绑定文件夹视图
            ((FolderListItem) view).bind(folderName);
        }
    }

    //方法：获取文件夹名称
    public String getFolderName(Context context, int position) {
        //语句：获取当前游标
        Cursor cursor = (Cursor) getItem(position);
        //语句：选定当前文件夹
        return (cursor.getLong(ID_COLUMN) == Notes.ID_ROOT_FOLDER) ? context
                .getString(R.string.menu_move_parent_folder) : cursor.getString(NAME_COLUMN);
    }

    //类：文件夹
    private class FolderListItem extends LinearLayout {
        private TextView mName;

        //方法：FolderListItem的构造方法，使用folder_list_item布局填充当前界面
        public FolderListItem(Context context) {
            super(context);
            inflate(context, R.layout.folder_list_item, this);
            mName = (TextView) findViewById(R.id.tv_folder_name);
        }
        //方法：绑定文件夹名称
        public void bind(String name) {
            mName.setText(name);
        }
    }

}
