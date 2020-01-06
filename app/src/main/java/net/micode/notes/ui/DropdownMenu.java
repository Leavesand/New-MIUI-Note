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
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;

import net.micode.notes.R;

public class DropdownMenu {
    //语句：定义按钮控件
    private Button mButton;
    //语句：定义弹出式菜单
    private PopupMenu mPopupMenu;
    //语句：定义菜单
    private Menu mMenu;

    /**
     * 方法：下拉菜单的构造方法
     * @param context
     * @param button
     * @param menuId
     */
    public DropdownMenu(Context context, Button button, int menuId) {
        mButton = button;
        //语句：设置其背景资源为下拉菜单的图标
        mButton.setBackgroundResource(R.drawable.dropdown_icon);
        //语句：实例化一个弹出式菜单
        mPopupMenu = new PopupMenu(context, mButton);
        //语句：获得弹出式菜单的菜单项
        mMenu = mPopupMenu.getMenu();
        //语句：使用菜单项填充弹出式菜单的视图
        mPopupMenu.getMenuInflater().inflate(menuId, mMenu);
        mButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                mPopupMenu.show();
            }
        }); //方法：调用mButton的setonClickListener方法，实例化一个点击事件监听器，如果监听到点击动作则显示菜单
    }

    //方法：设置下拉菜单的点击监听方法
    public void setOnDropdownMenuItemClickListener(OnMenuItemClickListener listener) {
        if (mPopupMenu != null) {
            mPopupMenu.setOnMenuItemClickListener(listener);
        }
    }
    //方法：查询菜单项
    public MenuItem findItem(int id) {
        return mMenu.findItem(id);
    }
    //方法：设置标题
    public void setTitle(CharSequence title) {
        mButton.setText(title);
    }
}
