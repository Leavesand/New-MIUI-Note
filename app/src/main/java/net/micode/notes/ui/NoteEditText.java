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
import android.graphics.Rect;
import android.text.Layout;
import android.text.Selection;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.URLSpan;
import android.util.AttributeSet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.widget.EditText;

import net.micode.notes.R;

import java.util.HashMap;
import java.util.Map;

//类：便签编辑文本类，继承了Android控件EditText
public class NoteEditText extends EditText {
    //语句：常量标识
    private static final String TAG = "NoteEditText";
    //语句：声明整型变量，文本索引
    private int mIndex;
    //语句：声明整型变量
    private int mSelectionStartBeforeDelete;

    //语句：声明字符串常量，标志电话、网址、邮件
    private static final String SCHEME_TEL = "tel:" ;
    private static final String SCHEME_HTTP = "http:" ;
    private static final String SCHEME_EMAIL = "mailto:" ;

    //语句块：设置映射，将文本内容（电话、网址、邮件）做链接处理
    private static final Map<String, Integer> sSchemaActionResMap = new HashMap<String, Integer>();
    static {
        sSchemaActionResMap.put(SCHEME_TEL, R.string.note_link_tel);
        sSchemaActionResMap.put(SCHEME_HTTP, R.string.note_link_web);
        sSchemaActionResMap.put(SCHEME_EMAIL, R.string.note_link_email);
    }

    /**
     * Call by the {@link NoteEditActivity} to delete or add edit text
     * 接口：该接口用于实现对TextView组件中的文字信息进行修改
     */
    public interface OnTextViewChangeListener {
        /**
         * Delete current edit text when {@link KeyEvent#KEYCODE_DEL} happens
         * and the text is null
         * 方法：当delete键按下时删除当前编辑的文字块
         */
        void onEditTextDelete(int index, String text);

        /**
         * Add edit text after current edit text when {@link KeyEvent#KEYCODE_ENTER}
         * happen
         * 方法：当enter键按下时添加一个文字编辑块
         */
        void onEditTextEnter(int index, String text);

        /**
         * Hide or show item option when text change
         * 方法：当文字发生变化时隐藏或者显示设置
         */
        void onTextChange(int index, boolean hasText);
    }

    //语句：声明文本视图变化监听器
    private OnTextViewChangeListener mOnTextViewChangeListener;

    //方法：构造方法，实例化NoteEditText
    public NoteEditText(Context context) {
        super(context, null);
        mIndex = 0;
    }

    //方法：设置索引号
    public void setIndex(int index) {
        mIndex = index;
    }

    //方法：设置文本视图变化监听器
    public void setOnTextViewChangeListener(OnTextViewChangeListener listener) {
        mOnTextViewChangeListener = listener;
    }

    //方法：构造方法，是由参数集（文本编辑风格）实例化NoteEditText
    public NoteEditText(Context context, AttributeSet attrs) {
        super(context, attrs, android.R.attr.editTextStyle);
    }
    //方法：构造方法，是由参数集（文本编辑风格、定义风格）实例化NoteEditText
    public NoteEditText(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        // TODO Auto-generated constructor stub
    }

    //方法：监听触摸事件
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:

                int x = (int) event.getX();
                int y = (int) event.getY();
                x -= getTotalPaddingLeft();
                y -= getTotalPaddingTop();
                x += getScrollX();
                y += getScrollY();

                //语句：获取布局
                Layout layout = getLayout();
                //语句：获取纵向的行数
                int line = layout.getLineForVertical(y);
                //语句：获取横向的偏移量
                int off = layout.getOffsetForHorizontal(line, x);
                Selection.setSelection(getText(), off);
                break;
        }
        //语句：继续调用父类的监听事件方法
        return super.onTouchEvent(event);
    }

    /**
     * 方法：监听键盘按键按下
     * @param keyCode 键盘按键的编码
     * @param event 按键事件
     * @return boolean
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            //语句块：按下回车键
            case KeyEvent.KEYCODE_ENTER:
                //语句块：如果文本视图没有变化
                if (mOnTextViewChangeListener != null) {
                    return false;
                }
                break;
                //语句块：按下删除键
            case KeyEvent.KEYCODE_DEL:
                //语句：获取删除文本的开始位置
                mSelectionStartBeforeDelete = getSelectionStart();
                break;
            default:
                break;
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * 方法：监听按键抬起
     * @param keyCode
     * @param event
     * @return
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        switch(keyCode) {
            //语句块：抬起删除键
            case KeyEvent.KEYCODE_DEL:
                //语句块：如果文本视图发生变化
                if (mOnTextViewChangeListener != null) {
                    //语句块：选择了删除的文字
                    if (0 == mSelectionStartBeforeDelete && mIndex != 0) {
                        //语句：监听文本的删除
                        mOnTextViewChangeListener.onEditTextDelete(mIndex, getText().toString());
                        return true;
                    }
                } else {
                    //语句：报告文本视图变化监听器没有设置
                    Log.d(TAG, "OnTextViewChangeListener was not seted");
                }
                break;
                //语句块：抬起回车键
            case KeyEvent.KEYCODE_ENTER:
                //语句块：如果文本视图发生变化
                if (mOnTextViewChangeListener != null) {
                    //语句：获取选择区域的起点位置
                    int selectionStart = getSelectionStart();
                    //语句：获取选择区域后面的文本信息
                    String text = getText().subSequence(selectionStart, length()).toString();
                    //语句：实现文本换行的功能
                    setText(getText().subSequence(0, selectionStart));
                    //语句：将选择区域内的文字移到下一行
                    mOnTextViewChangeListener.onEditTextEnter(mIndex + 1, text);
                } else {
                    //语句：报告文本视图变化监听器没有设置
                    Log.d(TAG, "OnTextViewChangeListener was not seted");
                }
                break;
            default:
                break;
        }
        return super.onKeyUp(keyCode, event);
    }

    /**
     * 处理当前视图下的焦点改变事件
     * @param focused 代表获得或失去焦点
     * @param direction
     * @param previouslyFocusedRect 上一个访问的焦点区域
     */
    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        //语句块：如果文本视图发生变化
        if (mOnTextViewChangeListener != null) {
            //语句块：当焦点存在或者当前文本不为空时，监听文本的变化
            if (!focused && TextUtils.isEmpty(getText())) {
                mOnTextViewChangeListener.onTextChange(mIndex, false);
            } else {
                mOnTextViewChangeListener.onTextChange(mIndex, true);
            }
        }
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
    }

    /**
     * 方法：创建环境菜单
     */
    @Override
    protected void onCreateContextMenu(ContextMenu menu) {
        if (getText() instanceof Spanned) {
            //语句块：获取高亮元素
            int selStart = getSelectionStart();
            int selEnd = getSelectionEnd();

            int min = Math.min(selStart, selEnd);
            int max = Math.max(selStart, selEnd);

            final URLSpan[] urls = ((Spanned) getText()).getSpans(min, max, URLSpan.class);
            //语句块：针对不同的高亮元素，使用不同的操作进行处理
            if (urls.length == 1) {
                int defaultResId = 0;
                for(String schema: sSchemaActionResMap.keySet()) {
                    if(urls[0].getURL().indexOf(schema) >= 0) {
                        defaultResId = sSchemaActionResMap.get(schema);
                        break;
                    }
                }

                if (defaultResId == 0) {
                    defaultResId = R.string.note_link_other;
                }
                //语句块：添加环境菜单的点击事件
                menu.add(0, 0, 0, defaultResId).setOnMenuItemClickListener(
                        new OnMenuItemClickListener() {
                            public boolean onMenuItemClick(MenuItem item) {
                                // goto a new intent
                                urls[0].onClick(NoteEditText.this);
                                return true;
                            }
                        });
            }
        }
        super.onCreateContextMenu(menu);
    }
}
