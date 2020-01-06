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

import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.app.SearchManager;
import android.appwidget.AppWidgetManager;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Paint;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.format.DateUtils;
import android.text.style.BackgroundColorSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RatingBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import net.micode.notes.R;
import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.TextNote;
import net.micode.notes.model.WorkingNote;
import net.micode.notes.model.WorkingNote.NoteSettingChangedListener;
import net.micode.notes.tool.DataUtils;
import net.micode.notes.tool.ResourceParser;
import net.micode.notes.tool.ResourceParser.TextAppearanceResources;
import net.micode.notes.ui.DateTimePickerDialog.OnDateTimeSetListener;
import net.micode.notes.ui.NoteEditText.OnTextViewChangeListener;
import net.micode.notes.ui.PasswordView;
import net.micode.notes.widget.NoteWidgetProvider_2x;
import net.micode.notes.widget.NoteWidgetProvider_4x;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 该类实现了对当前便签界面进行编辑功能，继承了父类Activity并实现了接口，封装有方法39个
 * @author www.micode.net
 */
public class NoteEditActivity extends Activity implements OnClickListener,
        NoteSettingChangedListener, OnTextViewChangeListener {
    /**
     * 该私有类为便签编辑界面的标题栏部分
     */
    private class HeadViewHolder {
        //修改时间（文本视图）
        public TextView tvModified;
        //提醒标志（图像视图）
        public ImageView ivAlertIcon;
        //提醒时间（文本视图）
        public TextView tvAlertDate;
        //设置背景颜色（图像视图）
        public ImageView ibSetBgColor;
    }

    /**
     * 常量，实现选择背景颜色按钮（未选择）和资源解析器中的颜色ID对应
     */
    private static final Map<Integer, Integer> sBgSelectorBtnsMap = new HashMap<Integer, Integer>();
    static {
        sBgSelectorBtnsMap.put(R.id.iv_bg_yellow, ResourceParser.YELLOW);
        sBgSelectorBtnsMap.put(R.id.iv_bg_red, ResourceParser.RED);
        sBgSelectorBtnsMap.put(R.id.iv_bg_blue, ResourceParser.BLUE);
        sBgSelectorBtnsMap.put(R.id.iv_bg_green, ResourceParser.GREEN);
        sBgSelectorBtnsMap.put(R.id.iv_bg_white, ResourceParser.WHITE);
    }

    /**
     * 常量，实现资源解析器中的颜色ID与背景颜色选择选择按钮（已选择）对应
     */
    private static final Map<Integer, Integer> sBgSelectorSelectionMap = new HashMap<Integer, Integer>();
    static {
        sBgSelectorSelectionMap.put(ResourceParser.YELLOW, R.id.iv_bg_yellow_select);
        sBgSelectorSelectionMap.put(ResourceParser.RED, R.id.iv_bg_red_select);
        sBgSelectorSelectionMap.put(ResourceParser.BLUE, R.id.iv_bg_blue_select);
        sBgSelectorSelectionMap.put(ResourceParser.GREEN, R.id.iv_bg_green_select);
        sBgSelectorSelectionMap.put(ResourceParser.WHITE, R.id.iv_bg_white_select);
    }

    /**
     * 常量，实现字体大小按钮（未选择）和资源解析器中的字号ID对应
     */
    private static final Map<Integer, Integer> sFontSizeBtnsMap = new HashMap<Integer, Integer>();
    static {
        sFontSizeBtnsMap.put(R.id.ll_font_large, ResourceParser.TEXT_LARGE);
        sFontSizeBtnsMap.put(R.id.ll_font_small, ResourceParser.TEXT_SMALL);
        sFontSizeBtnsMap.put(R.id.ll_font_normal, ResourceParser.TEXT_MEDIUM);
        sFontSizeBtnsMap.put(R.id.ll_font_super, ResourceParser.TEXT_SUPER);
    }

    /**
     * 常量，实现资源解析器中字号ID与字体大小按钮（已选择）对应
     */
    private static final Map<Integer, Integer> sFontSelectorSelectionMap = new HashMap<Integer, Integer>();
    static {
        sFontSelectorSelectionMap.put(ResourceParser.TEXT_LARGE, R.id.iv_large_select);
        sFontSelectorSelectionMap.put(ResourceParser.TEXT_SMALL, R.id.iv_small_select);
        sFontSelectorSelectionMap.put(ResourceParser.TEXT_MEDIUM, R.id.iv_medium_select);
        sFontSelectorSelectionMap.put(ResourceParser.TEXT_SUPER, R.id.iv_super_select);
    }
    //常量标识
    private static final String TAG = "NoteEditActivity";

    //便签编辑标题栏
    private HeadViewHolder mNoteHeaderHolder;

    //便签的顶部视图块
    private View mHeadViewPanel;

    //便签的背景色选择器
    private View mNoteBgColorSelector;

    //便签的字体大小选择器
    private View mFontSizeSelector;

    //便签编辑器
    private EditText mNoteEditor;

    //便签编辑块
    private View mNoteEditorPanel;

    //定义私有变量 活动便签
    private WorkingNote mWorkingNote;

    //定义私有变量 共享设置 用来保存配置参数
    private SharedPreferences mSharedPrefs;
    //定义私有变量 字体大小Id
    private int mFontSizeId;

    //定义常量 字体大小设置
    private static final String PREFERENCE_FONT_SIZE = "pref_font_size";

    private static final int SHORTCUT_ICON_TITLE_MAX_LEN = 10;

    //定义字符串常量 已标记
    public static final String TAG_CHECKED = String.valueOf('\u221A');
    //定义字符串常量 未标记
    public static final String TAG_UNCHECKED = String.valueOf('\u25A1');

    //线性布局，用于编辑文本
    private LinearLayout mEditTextList;
    //用户请求
    private String mUserQuery;
    //正则表达式模式
    private Pattern mPattern;

    /**
     * 重写Activity类的onCreate方法
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.setContentView(R.layout.note_edit);


        //如果未保存实例状态且当前Activity未初始化完成，则结束当前Activity
        if (savedInstanceState == null && !initActivityState(getIntent())) {
            finish();
            return;
        }
        initResources();
    }

    /**
     * Current activity may be killed when the memory is low. Once it is killed, for another time
     * user load this activity, we should restore the former state
     * 当当前活动界面在释放内存时被关闭，则再次调用该Activity时依据保存的实例恢复到被关闭之前的状态
     */
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);
        /**
         * 如果保存了关闭前的实例状态且其中包含着便签标识符，则将该标识符添加到初始化Activity的Intent中
         */
        if (savedInstanceState != null && savedInstanceState.containsKey(Intent.EXTRA_UID)) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.putExtra(Intent.EXTRA_UID, savedInstanceState.getLong(Intent.EXTRA_UID));
            if (!initActivityState(intent)) {
                finish();
                return;
            }
            Log.d(TAG, "Restoring from killed activity");
        }
    }

    /**
     * 初始化Activity的状态
     * @param intent 创建时传递给该Activity的intent参数,用于描述让该Activity完成的动作、附加属性信息
     * @return boolean 返回是否成功初始化了活动状态
     */
    private boolean initActivityState(Intent intent) {
        /**
         * If the user specified the {@link Intent#ACTION_VIEW} but not provided with id,
         * then jump to the NotesListActivity
         */
        mWorkingNote = null;
        /**
         * 判断初始化该Activity的Intent所包含的action是ACTION_VIEW还是ACTION_INSERT_OR_EDIT，然后进行不同的操作
         */
        if (TextUtils.equals(Intent.ACTION_VIEW, intent.getAction())) {
            //当前Activity获取intent中的附加信息（字符串）EXTRA_UID标识noteId，默认值为0
            long noteId = intent.getLongExtra(Intent.EXTRA_UID, 0);
            //初始化用户请求为空字符串
            mUserQuery = "";

            /**
             * Starting from the searched result
             */
            if (intent.hasExtra(SearchManager.EXTRA_DATA_KEY)) {
                //将查找到的便签标识符转化为long类型，保存在变量noteId中
                noteId = Long.parseLong(intent.getStringExtra(SearchManager.EXTRA_DATA_KEY));
                //将用户请求的字符串保存在变量mUserQuery中
                mUserQuery = intent.getStringExtra(SearchManager.USER_QUERY);
            }
            /**
             * 如果在数据库中没有查找到相应的便签标识符，则切换到文件夹列表界面
             */
            if (!DataUtils.visibleInNoteDatabase(getContentResolver(), noteId, Notes.TYPE_NOTE)) {
                //设置Intent目的组件为NotesListActivity
                Intent jump = new Intent(this, NotesListActivity.class);
                //启动目的组件
                startActivity(jump);
                //显示消息提示框，消息为便签不存在
                showToast(R.string.error_note_not_exist);
                //结束NoteEditActivity的生命周期
                finish();
                return false;
            } else {
                //如果在数据库中查找到相应的便签标识符noteId，则将该便签加载到mWorkingNote变量中
                mWorkingNote = WorkingNote.load(this, noteId);
                //如果活动便签为空，显示错误信息
                if (mWorkingNote == null) {
                    Log.e(TAG, "load note failed with note id" + noteId);
                    finish();
                    return false;
                }
            }
            //设置软键盘输入模式
            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN
                            | WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);
        } else if(TextUtils.equals(Intent.ACTION_INSERT_OR_EDIT, intent.getAction())) {
            // New note
            //初始化便签的各属性
            long folderId = intent.getLongExtra(Notes.INTENT_EXTRA_FOLDER_ID, 0);
            int widgetId = intent.getIntExtra(Notes.INTENT_EXTRA_WIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            int widgetType = intent.getIntExtra(Notes.INTENT_EXTRA_WIDGET_TYPE,
                    Notes.TYPE_WIDGET_INVALIDE);
            int bgResId = intent.getIntExtra(Notes.INTENT_EXTRA_BACKGROUND_ID,
                    ResourceParser.getDefaultBgId(this));

            // Parse call-record note
            //解析通话记录便签
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            long callDate = intent.getLongExtra(Notes.INTENT_EXTRA_CALL_DATE, 0);
            /**
             * 如果便签的通话日期和通话号码均存在，则把其当做通话便签进行处理,否则当做普通便签进行初始化
             */
            if (callDate != 0 && phoneNumber != null) {
                //如果记录的通话号码为空，则显示警告
                if (TextUtils.isEmpty(phoneNumber)) {
                    Log.w(TAG, "The call record number is null");
                }
                long noteId = 0;
                //通过通话号码和通话日期在数据库中查找noteId
                if ((noteId = DataUtils.getNoteIdByPhoneNumberAndCallDate(getContentResolver(),
                        phoneNumber, callDate)) > 0) {
                    mWorkingNote = WorkingNote.load(this, noteId);
                    if (mWorkingNote == null) {
                        Log.e(TAG, "load call note failed with note id" + noteId);
                        finish();
                        return false;
                    }
                } else {
                    //没有查找到noteId信息则重新建立空便签，使用之前默认参量对便签进行初始化
                    mWorkingNote = WorkingNote.createEmptyNote(this, folderId, widgetId,
                            widgetType, bgResId);
                    //将便签转化为通话记录便签
                    mWorkingNote.convertToCallNote(phoneNumber, callDate);
                }
            } else {
                //否则按照普通的创建
                mWorkingNote = WorkingNote.createEmptyNote(this, folderId, widgetId, widgetType,
                        bgResId);
            }

            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE
                            | WindowManager.LayoutParams.SOFT_INPUT_STATE_VISIBLE);
        } else {
            //显示错误信息，Intent没有指定动作，不应支持
            Log.e(TAG, "Intent not specified action, should not support");
            finish();
            return false;
        }
        //调用mWorkingNote的setOnSettingStatusChangedListener()方法设置状态变化监听器
        mWorkingNote.setOnSettingStatusChangedListener(this);



        //返回true表示初始化该Activity成功
        return true;
    }

    /**
     * 重写Activity的onResume方法，将标签编辑界面作为界面的最上层呈现
     */
    @Override
    protected void onResume() {
        super.onResume();
        //调用初始化便签界面方法
        initNoteScreen();
    }

    /**
     * 初始化便签界面的窗口
     */
    private void initNoteScreen() {
        //设置便签界面的文字外观
        mNoteEditor.setTextAppearance(this, TextAppearanceResources
                .getTexAppearanceResource(mFontSizeId));
        /**
         * 判断是否为清单列表界面，是则切换到清单列表的处理过程，否则继续执行
         */
        if (mWorkingNote.getCheckListMode() == TextNote.MODE_CHECK_LIST) {
            switchToListMode(mWorkingNote.getContent());
        } else {
            mNoteEditor.setText(getHighlightQueryResult(mWorkingNote.getContent(), mUserQuery));
            //设置光标在便签文档的末尾
            mNoteEditor.setSelection(mNoteEditor.getText().length());
        }
        for (Integer id : sBgSelectorSelectionMap.keySet()) {
            findViewById(sBgSelectorSelectionMap.get(id)).setVisibility(View.GONE);
        }
        //设置便签头部背景和编辑部分的背景颜色
        mHeadViewPanel.setBackgroundResource(mWorkingNote.getTitleBgResId());
        mNoteEditorPanel.setBackgroundResource(mWorkingNote.getBgColorResId());

        //设置便签头的修改时间
        mNoteHeaderHolder.tvModified.setText(DateUtils.formatDateTime(this,
                mWorkingNote.getModifiedDate(), DateUtils.FORMAT_SHOW_DATE
                        | DateUtils.FORMAT_NUMERIC_DATE | DateUtils.FORMAT_SHOW_TIME
                        | DateUtils.FORMAT_SHOW_YEAR));

        /**
         * TODO: Add the menu for setting alert. Currently disable it because the DateTimePicker
         * is not ready
         */
        showAlertHeader();

/*字数统计*/

        final TextView WordNUm = (TextView) findViewById(R.id.textCount);
        mNoteEditor.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence s, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                WordNUm.setText("当前字数为："+(s.length()));           }
        });
        if(mWorkingNote.hasPassword()) {
            final PasswordView pwdView = new PasswordView(this, PasswordView.CUR_PWD_VIEW);
            RelativeLayout.LayoutParams LP = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,
                    RelativeLayout.LayoutParams.MATCH_PARENT);

            addContentView(pwdView,LP);
            /**
             * 口令输入完毕后执行的操作
             */
            pwdView.setOnFinishInput(new OnPasswordInputFinish() {
                @Override
                public void inputFinish() {
                    //在当前界面下实例化一个对话框
                    final AlertDialog.Builder builder_c0 = new AlertDialog.Builder(NoteEditActivity.this);
                    //设置对话框标题
                    builder_c0.setTitle(getString(R.string.alert_title_encrypt));
                    //设置对话框图标
                    builder_c0.setIcon(android.R.drawable.ic_dialog_info);
                    /**
                     * 如果口令输入正确
                     */
                    if (pwdView.getStrPassword().equals(mWorkingNote.getPassword())) {
                        ViewGroup vg = (ViewGroup) pwdView.getParent();
                        vg.removeView(pwdView);
                        /*
                        //设置对话框消息
                        builder_c0.setMessage(getString(R.string.alert_message_decrypt));
                        */
                        /**
                         * 设置对话框确认按钮的功能

                        builder_c0.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //点击时关闭对话框和输入口令界面
                                AlertDialog alertdialog = builder_c0.show();
                                alertdialog.dismiss();
                                ViewGroup vg = (ViewGroup) pwdView.getParent();
                                vg.removeView(pwdView);
                            }
                        });
                        */
                    } else {
                        builder_c0.setMessage(getString(R.string.alert_message_decrypt_error));
                        builder_c0.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                finish();
                            }
                        });
                        builder_c0.show();
                    }

                }
            });
            pwdView.getCancelImageView().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(NoteEditActivity.this, "Return", Toast.LENGTH_SHORT).show();
                    finish();
                }
            });
        }else{
            //Toast.makeText(NoteEditActivity.this, "XXXX", Toast.LENGTH_SHORT).show();
        }
    }

    //显示便签提醒的标头
    private void showAlertHeader() {

        if (mWorkingNote.hasClockAlert()) {
            long time = System.currentTimeMillis();
            /**
             * 如果当前时间已超过活动便签的提醒时间，则置便签标头为过期，否则将显示当前时间到提醒时间的时长
             */
            if (time > mWorkingNote.getAlertDate()) {
                mNoteHeaderHolder.tvAlertDate.setText(R.string.note_alert_expired);
            } else {
                mNoteHeaderHolder.tvAlertDate.setText(DateUtils.getRelativeTimeSpanString(
                        mWorkingNote.getAlertDate(), time, DateUtils.MINUTE_IN_MILLIS));
            }
            mNoteHeaderHolder.tvAlertDate.setVisibility(View.VISIBLE);
            mNoteHeaderHolder.ivAlertIcon.setVisibility(View.VISIBLE);
        } else {
            mNoteHeaderHolder.tvAlertDate.setVisibility(View.GONE);
            mNoteHeaderHolder.ivAlertIcon.setVisibility(View.GONE);
        }
    }

    /**
     * 重写onNewIntent方法，监听发送给NoteEditActivity的意图
     * @param intent
     */
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        initActivityState(intent);
    }

    /**
     * 监听保存实例状态方法
     * @param outState
     */
    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        /**
         * For new note without note id, we should firstly save it to
         * generate a id. If the editing note is not worth saving, there
         * is no id which is equivalent to create new note
         * 如果当前工作便签没有分配noteId则先调用saveNote()保存便签
         */
        if (!mWorkingNote.existInDatabase()) {
            saveNote();
        }
        //将生成的noteId保存到outState中
        outState.putLong(Intent.EXTRA_UID, mWorkingNote.getNoteId());
        Log.d(TAG, "Save working note id: " + mWorkingNote.getNoteId() + " onSaveInstanceState");
    }

    /**
     * 分发触摸事件
     * @param ev 触摸点
     * @return boolean
     */
    @Override
    public boolean dispatchTouchEvent(MotionEvent ev) {
        //由便签背景选择器消耗触摸事件
        if (mNoteBgColorSelector.getVisibility() == View.VISIBLE
                && !inRangeOfView(mNoteBgColorSelector, ev)) {
            mNoteBgColorSelector.setVisibility(View.GONE);
            return true;
        }
        //由便签字号选择器消耗触摸事件
        if (mFontSizeSelector.getVisibility() == View.VISIBLE
                && !inRangeOfView(mFontSizeSelector, ev)) {
            mFontSizeSelector.setVisibility(View.GONE);
            return true;
        }
        //未被消耗时调用父类的方法
        return super.dispatchTouchEvent(ev);
    }

    /**
     * 判断触摸点是否在视图内部
     * @param view
     * @param ev 触摸点
     * @return boolean
     */
    private boolean inRangeOfView(View view, MotionEvent ev) {
        int []location = new int[2];
        view.getLocationOnScreen(location);
        int x = location[0];
        int y = location[1];
        if (ev.getX() < x
                || ev.getX() > (x + view.getWidth())
                || ev.getY() < y
                || ev.getY() > (y + view.getHeight())) {
                    return false;
                }

        return true;
    }

    /**
     *
     */
    private void initResources() {
        //初始化便签标题栏
        mHeadViewPanel = findViewById(R.id.note_title);
        mNoteHeaderHolder = new HeadViewHolder();
        mNoteHeaderHolder.tvModified = (TextView) findViewById(R.id.tv_modified_date);
        mNoteHeaderHolder.ivAlertIcon = (ImageView) findViewById(R.id.iv_alert_icon);
        mNoteHeaderHolder.tvAlertDate = (TextView) findViewById(R.id.tv_alert_date);
        mNoteHeaderHolder.ibSetBgColor = (ImageView) findViewById(R.id.btn_set_bg_color);
        mNoteHeaderHolder.ibSetBgColor.setOnClickListener(this);
        //初始化便签编辑部分界面
        mNoteEditor = (EditText) findViewById(R.id.note_edit_view);
        mNoteEditorPanel = findViewById(R.id.sv_note_edit);
        //初始化便签背景选择界面
        mNoteBgColorSelector = findViewById(R.id.note_bg_color_selector);
        /**
         * 对所有颜色选择按钮设置监听器
         */
        for (int id : sBgSelectorBtnsMap.keySet()) {
            ImageView iv = (ImageView) findViewById(id);
            //设置点击事件监听器
            iv.setOnClickListener(this);
        }
        //初始化便签文字字号选择器
        mFontSizeSelector = findViewById(R.id.font_size_selector);
        for (int id : sFontSizeBtnsMap.keySet()) {
            View view = findViewById(id);
            view.setOnClickListener(this);
        };
        //初始化共享设置
        mSharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        //将字体大小标识设置为默认大小
        mFontSizeId = mSharedPrefs.getInt(PREFERENCE_FONT_SIZE, ResourceParser.BG_DEFAULT_FONT_SIZE);
        /**
         * HACKME: Fix bug of store the resource id in shared preference.
         * The id may larger than the length of resources, in this case,
         * return the {@link ResourceParser#BG_DEFAULT_FONT_SIZE}
         */
        if(mFontSizeId >= TextAppearanceResources.getResourcesSize()) {
            mFontSizeId = ResourceParser.BG_DEFAULT_FONT_SIZE;
        }
        mEditTextList = (LinearLayout) findViewById(R.id.note_edit_list);


    }

    /**
     * 重写onPause方法，将便签编辑界面暂停（部分可见）
     */
    @Override
    protected void onPause() {
        super.onPause();
        //保存便签
        if(saveNote()) {
            Log.d(TAG, "Note data was saved with length:" + mWorkingNote.getContent().length());
        }
        clearSettingState();
    }

    /**
     * 更新桌面挂件
     */
    private void updateWidget() {
        //实例化Intent，用于和AppWidgetManager进行消息交换
        Intent intent = new Intent(AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        //判断当前工作中的便签的桌面挂件的类型,并根据不同类型的桌面挂件指定Intent不同的目的组件
        if (mWorkingNote.getWidgetType() == Notes.TYPE_WIDGET_2X) {
            intent.setClass(this, NoteWidgetProvider_2x.class);
        } else if (mWorkingNote.getWidgetType() == Notes.TYPE_WIDGET_4X) {
            intent.setClass(this, NoteWidgetProvider_4x.class);
        } else {
            Log.e(TAG, "Unspported widget type");
            return;
        }
        //将桌面挂件的标识作为附加信息添加到Intent中
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, new int[] {
            mWorkingNote.getWidgetId()
        });
        //将指定好参数的Intent对象通过广播发送出去以启动相应的Activity
        sendBroadcast(intent);
        //设置当前Activity结束后将结束信息发送给其父活动
        setResult(RESULT_OK, intent);
    }

    /**
     * 点击事件
     * @param v
     */
    public void onClick(View v) {

        //获取点击目标
        int id = v.getId();
        //点击设置背景色图标 or 点击颜色图标 or 点击字体图标
        if (id == R.id.btn_set_bg_color) {
            //将便签背景选择器界面设为可见
            mNoteBgColorSelector.setVisibility(View.VISIBLE);
            //将备选颜色界面设为可见
            findViewById(sBgSelectorSelectionMap.get(mWorkingNote.getBgColorId())).setVisibility(
                    View.VISIBLE);
        } else if (sBgSelectorBtnsMap.containsKey(id)) {
            //将备选颜色界面设为不可见
            findViewById(sBgSelectorSelectionMap.get(mWorkingNote.getBgColorId())).setVisibility(
                    View.GONE);
            //设置背景色
            mWorkingNote.setBgColorId(sBgSelectorBtnsMap.get(id));
            //将便签背景选择器界面设为不可见
            mNoteBgColorSelector.setVisibility(View.GONE);
        } else if (sFontSizeBtnsMap.containsKey(id)) {
            //将备选字体界面设为可见
            findViewById(sFontSelectorSelectionMap.get(mFontSizeId)).setVisibility(View.GONE);
            //使用mFontSizeId变量存储选定大小
            mFontSizeId = sFontSizeBtnsMap.get(id);
            //将共享设置中的字体进行修改
            mSharedPrefs.edit().putInt(PREFERENCE_FONT_SIZE, mFontSizeId).commit();

            findViewById(sFontSelectorSelectionMap.get(mFontSizeId)).setVisibility(View.VISIBLE);
            //如果便签为清单模式
            if (mWorkingNote.getCheckListMode() == TextNote.MODE_CHECK_LIST) {
                //获取工作文字
                getWorkingText();
                //转成列表模式
                switchToListMode(mWorkingNote.getContent());
            } else {
                //将字体资源设置为当前大小
                mNoteEditor.setTextAppearance(this,
                        TextAppearanceResources.getTexAppearanceResource(mFontSizeId));
            }
            //将字体大小选择器界面设为不可见
            mFontSizeSelector.setVisibility(View.GONE);
        }

    }

    @Override
    public void onBackPressed() {
        /**
         * 如果字体选择器和背景选择器均不可见，即为便签编辑界面，点击返回键退出，保存便签
         * 如果字体选择器和背景选择器可见，则点击返回键时关闭选择界面
         */
        if(clearSettingState()) {
            return;
        }
        //保存便签
        saveNote();
        super.onBackPressed();
    }

    /**
     * 清除设置状态
     * @return boolean 如果选择器为可见，则返回true，在便签编辑界面返回false
     */
    private boolean clearSettingState() {
        //将字体选择器和背景颜色选择器设为不可见
        if (mNoteBgColorSelector.getVisibility() == View.VISIBLE) {
            mNoteBgColorSelector.setVisibility(View.GONE);
            return true;
        } else if (mFontSizeSelector.getVisibility() == View.VISIBLE) {
            mFontSizeSelector.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    /**
     * 监听背景颜色变化
     */
    public void onBackgroundColorChanged() {
        //将便签背景色设置为可见，并显示该视图
        findViewById(sBgSelectorSelectionMap.get(mWorkingNote.getBgColorId())).setVisibility(
                View.VISIBLE);
        //将文件编辑区域的背景资源设置为便签背景色
        mNoteEditorPanel.setBackgroundResource(mWorkingNote.getBgColorResId());
        //将便签头的背景资源设置为标题的背景色
        mHeadViewPanel.setBackgroundResource(mWorkingNote.getTitleBgResId());
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (isFinishing()) {
            return true;
        }
        //清除设置状态
        clearSettingState();
        //清除菜单项
        menu.clear();
        /**
         * 根据文件夹类型，则调用相应的目录填充方法
         */
        if (mWorkingNote.getFolderId() == Notes.ID_CALL_RECORD_FOLDER) {
            getMenuInflater().inflate(R.menu.call_note_edit, menu);
        } else {
            getMenuInflater().inflate(R.menu.note_edit, menu);
        }
        /**
         * 根据工作便签属性，则调用相应的目录填充方法
         */
        if (mWorkingNote.getCheckListMode() == TextNote.MODE_CHECK_LIST) {
            menu.findItem(R.id.menu_list_mode).setTitle(R.string.menu_normal_mode);
        } else {
            menu.findItem(R.id.menu_list_mode).setTitle(R.string.menu_list_mode);
        }
        /**
         * 将工作便签中的时钟提醒设置为不可见
         */
        if (mWorkingNote.hasClockAlert()) {
            menu.findItem(R.id.menu_alert).setVisible(false);
        } else {
            menu.findItem(R.id.menu_delete_remind).setVisible(false);
        }

        if (mWorkingNote.hasPassword()) {
            menu.findItem(R.id.menu_encrypt).setVisible(false);
        } else {
            menu.findItem(R.id.menu_delete_encrypt).setVisible(false);
        }

        //menu.findItem(R.id.menu_set_importance).setVisible(false);
        return true;
    }

    /**
     * 监听菜单中的项是否被选择
     * @param item
     * @return
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
                //选择新建便签
            case R.id.menu_new_note:
                createNewNote();
                break;
                //选择删除便签
            case R.id.menu_delete:

                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(getString(R.string.alert_title_delete));
                //设置提醒图标
                builder.setIcon(android.R.drawable.ic_dialog_alert);
                //设置删除提醒消息
                builder.setMessage(getString(R.string.alert_message_delete_note));
                //设置选择按钮
                builder.setPositiveButton(android.R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                deleteCurrentNote();
                                finish();
                            }
                        });
                //设置取消选择按钮
                builder.setNegativeButton(android.R.string.cancel, null);
                builder.show();
                break;
                //选择设置字号
            case R.id.menu_font_size:
                mFontSizeSelector.setVisibility(View.VISIBLE);
                findViewById(sFontSelectorSelectionMap.get(mFontSizeId)).setVisibility(View.VISIBLE);
                break;
                //选择设置清单模式
            case R.id.menu_list_mode:
                mWorkingNote.setCheckListMode(mWorkingNote.getCheckListMode() == 0 ?
                        TextNote.MODE_CHECK_LIST : 0);
                break;
                //选择分享
            case R.id.menu_share:
                getWorkingText();
                sendTo(this, mWorkingNote.getContent());
                break;
                //选择分享到桌面
            case R.id.menu_send_to_desktop:
                sendToDesktop();
                break;
                //选择提醒
            case R.id.menu_alert:
                setReminder();
                break;
                //选择删除提醒
            case R.id.menu_delete_remind:
                mWorkingNote.setAlertDate(0, false);
                break;
            case R.id.menu_encrypt:
                setEncryption();
                break;
            case R.id.menu_delete_encrypt:
                deleteEncryption();
                break;
            case R.id.menu_set_importance:
                setImportance();
                break;
            default:
                break;
        }
        return true;
    }
    private class RatingBarLayout extends LinearLayout{
        Context context;
        private RatingBar ratingBar;
        public RatingBarLayout(Context context) {
            super(context);
            this.context = context;
            View view;
            view = View.inflate(context, R.layout.note_importance, null);
            ratingBar = (RatingBar) view.findViewById(R.id.ratingBar);
            ratingBar.setOnRatingBarChangeListener(new RatingBar.OnRatingBarChangeListener(){

                @Override
                public void onRatingChanged(RatingBar ratingBar, float r, boolean fromUser) {
                    ratingBar.setRating(r);
                }
            });
            addView(view);
        }

        public int getStars(){
            return (int) ratingBar.getRating();
        }
    }
    private void setImportance(){
        final RatingBarLayout rbLayout = new RatingBarLayout(this);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.alert_title_importance));
        builder.setIcon(android.R.drawable.ic_dialog_info);
        builder.setView(rbLayout);
        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(NoteEditActivity.this, "Rating:"+String.valueOf(rbLayout.getStars()), Toast.LENGTH_SHORT).show();
                mWorkingNote.setImportance(rbLayout.getStars());
                finish();
            }
        });
        builder.show();
    }

    private void setEncryption(){
        final PasswordView pwdView = new PasswordView(this, PasswordView.NEW_PWD_VIEW);
        final PasswordView pwdViewRepeat = new PasswordView(this, PasswordView.RPT_PWD_VIEW);
        setContentView(pwdView);
        pwdView.setOnFinishInput(new OnPasswordInputFinish() {
            @Override
            public void inputFinish() {
                setContentView(pwdViewRepeat);
                Toast.makeText(NoteEditActivity.this, pwdView.getStrPassword(), Toast.LENGTH_SHORT).show();
            }
        });
        pwdView.getCancelImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NoteEditActivity.this, "Return from Step1", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        pwdViewRepeat.setOnFinishInput(new OnPasswordInputFinish() {
            @Override
            public void inputFinish() {

                final AlertDialog.Builder builder_c1 = new AlertDialog.Builder(NoteEditActivity.this);
                builder_c1.setTitle(getString(R.string.alert_title_encrypt));
                builder_c1.setIcon(android.R.drawable.ic_dialog_info);
                if( pwdView.getStrPassword().equals(pwdViewRepeat.getStrPassword()) ){
                    builder_c1.setMessage(getString(R.string.alert_message_encrypt,pwdViewRepeat.getStrPassword()));
                    mWorkingNote.setPassword(pwdViewRepeat.getStrPassword());
                }else{
                    builder_c1.setMessage(getString(R.string.alert_message_encrypt_error));
                }

                builder_c1.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                builder_c1.show();
            }
        });

//        /**
//         *  可以用自finish();定义控件中暴露出来的cancelImageView方法，重新提供相应
//         *  如果写了，会覆盖我们在自定义控件中提供的响应
//         *  可以看到这里toast显示 "Biu Biu Biu"而不是"Cancel"*/
        pwdViewRepeat.getCancelImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(NoteEditActivity.this, "Return from Step2", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }

    private void deleteEncryption(){
        mWorkingNote.setPassword("");
        AlertDialog.Builder builder_c2 = new AlertDialog.Builder(this);
        builder_c2.setTitle(getString(R.string.alert_title_encrypt));
        builder_c2.setIcon(android.R.drawable.ic_dialog_info);
        builder_c2.setMessage(getString(R.string.alert_message_remove_encrypt));
        builder_c2.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener(){
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finish();
            }
        });
        builder_c2.show();
    }

    /**
     * 设置提醒
     */
    private void setReminder() {
        //实例化一个时间日期选择器的对话框，进行提醒日期的设置
        DateTimePickerDialog d = new DateTimePickerDialog(this, System.currentTimeMillis());
        d.setOnDateTimeSetListener(new OnDateTimeSetListener() {
            public void OnDateTimeSet(AlertDialog dialog, long date) {
                mWorkingNote.setAlertDate(date	, true);
            }
        });
        //显示该控件
        d.show();
    }

    /**
     * Share note to apps that support {@link Intent#ACTION_SEND} action
     * and {@text/plain} type
     */
    private void sendTo(Context context, String info) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.putExtra(Intent.EXTRA_TEXT, info);
        intent.setType("text/plain");
        context.startActivity(intent);
    }

    /**
     * 创建新便签
     */
    private void createNewNote() {
        // Firstly, save current editing notes
        //首先保存当前编辑的便签
        saveNote();

        // For safety, start a new NoteEditActivity
        //结束当前活动周期，重新激活便签编辑Activity
        finish();
        Intent intent = new Intent(this, NoteEditActivity.class);
        intent.setAction(Intent.ACTION_INSERT_OR_EDIT);
        intent.putExtra(Notes.INTENT_EXTRA_FOLDER_ID, mWorkingNote.getFolderId());
        startActivity(intent);
    }

    /**
     * 删除当前便签
     */
    private void deleteCurrentNote() {
        if (mWorkingNote.existInDatabase()) {
            HashSet<Long> ids = new HashSet<Long>();
            long id = mWorkingNote.getNoteId();
            /**
             * 如果删除的便签Id不是根目录的ID，否则提示错误
             */
            if (id != Notes.ID_ROOT_FOLDER) {
                ids.add(id);
            } else {
                Log.d(TAG, "Wrong note id, should not happen");
            }
            /**
             * 如果不是同步模式，删除便签
             * 否则，将便签移入垃圾箱中
             */
            if (!isSyncMode()) {
                if (!DataUtils.batchDeleteNotes(getContentResolver(), ids)) {
                    Log.e(TAG, "Delete Note error");
                }
            } else {
                if (!DataUtils.batchMoveToFolder(getContentResolver(), ids, Notes.ID_TRASH_FOLER)) {
                    Log.e(TAG, "Move notes to trash folder error, should not happens");
                }
            }
        }
        //更改工作便签的删除标记
        mWorkingNote.markDeleted(true);
    }

    /**
     * 判断是否为同步模式
     * @return boolean
     */
    private boolean isSyncMode() {
        //从NotesPreferenceActivity中获取同步账号名，如果存在则返回true
        return NotesPreferenceActivity.getSyncAccountName(this).trim().length() > 0;
    }

    /**
     * 监听时钟提醒的变化
     * @param date 提醒时间
     * @param set
     */
    public void onClockAlertChanged(long date, boolean set) {
        /**
         * User could set clock to an unsaved note, so before setting the
         * alert clock, we should save the note first
         * 如果设置提醒的便签未保存，则先保存
         */
        if (!mWorkingNote.existInDatabase()) {
            saveNote();
        }
        /**
         * 如果便签Id存在（worthsaving，已编辑过）
         */
        if (mWorkingNote.getNoteId() > 0) {
            //设置向AlarmReceiver发送的Intent
            Intent intent = new Intent(this, AlarmReceiver.class);
            //设置其Data域为附加便签Id后的表示内容的Uri
            intent.setData(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, mWorkingNote.getNoteId()));
            //设置延迟触发Intent
            PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0);
            //获得系统的提醒服务
            AlarmManager alarmManager = ((AlarmManager) getSystemService(ALARM_SERVICE));
            //显示提醒标头
            showAlertHeader();
            //如果未设置，则取消当前设置，否则调用系统的提醒服务设置该提醒，在其被唤醒后解析之前设置的pendingIntent
            if(!set) {
                alarmManager.cancel(pendingIntent);
            } else {
                alarmManager.set(AlarmManager.RTC_WAKEUP, date, pendingIntent);
            }
        } else {
            /**
             * There is the condition that user has input nothing (the note is
             * not worthy saving), we have no note id, remind the user that he
             * should input something
             * 便签未编辑错误
             */
            Log.e(TAG, "Clock alert setting error");
            //显示消息提示框，为未编辑的便签设置闹钟
            showToast(R.string.error_note_empty_for_clock);
        }
    }

    /**
     * 监听挂件的变化，如果有变化更新挂件
     */
    public void onWidgetChanged() {
        updateWidget();
    }

    /**
     * 监听文本编辑过程中的删除
     * @param index 删除文本的位置
     * @param text 当前位置后的文本
     */
    public void onEditTextDelete(int index, String text) {
        //获取文本列表的总项数
        int childCount = mEditTextList.getChildCount();
        if (childCount == 1) {
            return;
        }
        //将删除文本后面项的索引减1
        for (int i = index + 1; i < childCount; i++) {
            ((NoteEditText) mEditTextList.getChildAt(i).findViewById(R.id.et_edit_text))
                    .setIndex(i - 1);
        }
        //移除当前项的视图
        mEditTextList.removeViewAt(index);
        NoteEditText edit = null;
        //如果原来编辑的索引值为0则编辑当前行，否则编辑上一行
        if(index == 0) {
            edit = (NoteEditText) mEditTextList.getChildAt(0).findViewById(
                    R.id.et_edit_text);
        } else {
            edit = (NoteEditText) mEditTextList.getChildAt(index - 1).findViewById(
                    R.id.et_edit_text);
        }
        //获取当前项的长度
        int length = edit.length();
        //在当前位置后附加文本
        edit.append(text);
        //获取当前控件的焦点
        edit.requestFocus();
        //选中该文本块
        edit.setSelection(length);
    }

    /**
     * 监听文本编辑过程中的换行
     * @param index 换行的位置
     * @param text 当前位置后的文本
     */
    public void onEditTextEnter(int index, String text) {
        /**
         * Should not happen, check for debug
         * 如果便签长度超过了限制，则提示错误
         */
        if(index > mEditTextList.getChildCount()) {
            Log.e(TAG, "Index out of mEditTextList boundrary, should not happen");
        }

        View view = getListItem(text, index);
        //添加当前项的视图
        mEditTextList.addView(view, index);
        NoteEditText edit = (NoteEditText) view.findViewById(R.id.et_edit_text);
        //获取控件焦点
        edit.requestFocus();
        //选中当前行的首个位置
        edit.setSelection(0);
        //将当前后面项的索引加1
        for (int i = index + 1; i < mEditTextList.getChildCount(); i++) {
            ((NoteEditText) mEditTextList.getChildAt(i).findViewById(R.id.et_edit_text))
                    .setIndex(i);
        }
    }

    /**
     * 转换到列表模式
     * @param text
     */
    private void switchToListMode(String text) {
        //清除所有视图
        mEditTextList.removeAllViews();
        //将文本以\n分离
        String[] items = text.split("\n");
        int index = 0;
        //从第一个索引块开始添加列表项的视图
        for (String item : items) {
            if(!TextUtils.isEmpty(item)) {
                mEditTextList.addView(getListItem(item, index));
                index++;
            }
        }
        //添加最后一行的视图
        mEditTextList.addView(getListItem("", index));
        //获取当前列表项焦点
        mEditTextList.getChildAt(index).findViewById(R.id.et_edit_text).requestFocus();
        //文本编辑器设置为不可见
        mNoteEditor.setVisibility(View.GONE);
        //文本列表编辑器设置为可见
        mEditTextList.setVisibility(View.VISIBLE);
    }

    /**
     * 获取匹配到的高亮结果
     * @param fullText 完整的文本
     * @param userQuery 用户的请求
     * @return Spannable类型的文本
     */
    private Spannable getHighlightQueryResult(String fullText, String userQuery) {
        //初始化spannable为空字符串
        SpannableString spannable = new SpannableString(fullText == null ? "" : fullText);
        if (!TextUtils.isEmpty(userQuery)) {
            //将用户的请求编译为正则表达式
            mPattern = Pattern.compile(userQuery);
            //在全文本中匹配该请求
            Matcher m = mPattern.matcher(fullText);
            int start = 0;
            /**
             * 将匹配到的部分显示为高亮
             */
            while (m.find(start)) {
                spannable.setSpan(
                        new BackgroundColorSpan(this.getResources().getColor(
                                R.color.user_query_highlight)), m.start(), m.end(),
                        Spannable.SPAN_INCLUSIVE_EXCLUSIVE);
                start = m.end();
            }
        }
        return spannable;
    }

    /**
     * 获取列表项
     * @param item 以字符串格式存储的列表项
     * @param index 当前项的索引
     * @return View 列表项的视图
     */
    private View getListItem(String item, int index) {
        View view = LayoutInflater.from(this).inflate(R.layout.note_edit_list_item, null);
        final NoteEditText edit = (NoteEditText) view.findViewById(R.id.et_edit_text);
        edit.setTextAppearance(this, TextAppearanceResources.getTexAppearanceResource(mFontSizeId));
        //实例化标签框
        CheckBox cb = ((CheckBox) view.findViewById(R.id.cb_edit_item));
        //监听是否已标识
        cb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    edit.setPaintFlags(edit.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    edit.setPaintFlags(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
                }
            }
        });
        /**
         * 如果列表项含有的标识创建相应的标签框
         */
        if (item.startsWith(TAG_CHECKED)) {
            cb.setChecked(true);
            edit.setPaintFlags(edit.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            item = item.substring(TAG_CHECKED.length(), item.length()).trim();
        } else if (item.startsWith(TAG_UNCHECKED)) {
            cb.setChecked(false);
            edit.setPaintFlags(Paint.ANTI_ALIAS_FLAG | Paint.DEV_KERN_TEXT_FLAG);
            item = item.substring(TAG_UNCHECKED.length(), item.length()).trim();
        }
        //监听文本视图的变化
        edit.setOnTextViewChangeListener(this);
        //将索引值设置为当前索引
        edit.setIndex(index);
        //设置高亮
        edit.setText(getHighlightQueryResult(item, mUserQuery));
        return view;
    }

    /**
     * 监听文本的变化
     * @param index 发生变化位置的索引
     * @param hasText 是否有文本
     */
    public void onTextChange(int index, boolean hasText) {
        //如果索引超过了当前文本内容，报告错误
        if (index >= mEditTextList.getChildCount()) {
            Log.e(TAG, "Wrong index, should not happen");
            return;
        }
        /**
         * 如果当前位置有文本则设置为可见，否则设置为不可见
         */
        if(hasText) {
            mEditTextList.getChildAt(index).findViewById(R.id.cb_edit_item).setVisibility(View.VISIBLE);
        } else {
            mEditTextList.getChildAt(index).findViewById(R.id.cb_edit_item).setVisibility(View.GONE);
        }
    }

    /**
     * 监听清单模式的变化
     * @param oldMode is previous mode before change
     * @param newMode is new mode
     */
    public void onCheckListModeChanged(int oldMode, int newMode) {
        //如果新模式为列表模式，则调用switchToListMode()方法进行转化
        if (newMode == TextNote.MODE_CHECK_LIST) {
            switchToListMode(mNoteEditor.getText().toString());
        } else {
            //如果没有保存工作文本，则进行保存该部分文本
            if (!getWorkingText()) {
                mWorkingNote.setWorkingText(mWorkingNote.getContent().replace(TAG_UNCHECKED + " ",
                        ""));
            }
            //设置高亮文本
            mNoteEditor.setText(getHighlightQueryResult(mWorkingNote.getContent(), mUserQuery));
            //取消文本列表视图
            mEditTextList.setVisibility(View.GONE);
            //设置文本编辑为可见
            mNoteEditor.setVisibility(View.VISIBLE);
        }
    }

    /**
     * 获取当前活动便签的文本并保存到数据库中
     * @return boolean
     */
    private boolean getWorkingText() {
        //初始化hasChecked变量
        boolean hasChecked = false;
        /**
         * 如果当前工作便签为清单模式，转化为文字形式保存其数据
         * 否则直接保存文字数据到数据库中
         */
        if (mWorkingNote.getCheckListMode() == TextNote.MODE_CHECK_LIST) {
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mEditTextList.getChildCount(); i++) {
                //获取当前文本编辑列表的第i项
                View view = mEditTextList.getChildAt(i);
                NoteEditText edit = (NoteEditText) view.findViewById(R.id.et_edit_text);
                /**
                 * 如果当前便签文本编辑部分不为空
                 */
                if (!TextUtils.isEmpty(edit.getText())) {
                    /**
                     * 如果已标记当前项，则在文本前添加“TAG_CHECKED”
                     * 如果未标记当前项，则在文本前添加“TAG_CHECKED”
                     */
                    if (((CheckBox) view.findViewById(R.id.cb_edit_item)).isChecked()) {
                        sb.append(TAG_CHECKED).append(" ").append(edit.getText()).append("\n");
                        hasChecked = true;
                    } else {
                        sb.append(TAG_UNCHECKED).append(" ").append(edit.getText()).append("\n");
                    }
                }
            }
            //使用建立好的文本内容设置当前活动便签的内容
            mWorkingNote.setWorkingText(sb.toString());
        } else {
            mWorkingNote.setWorkingText(mNoteEditor.getText().toString());
        }
        //返回便签是否已标记
        return hasChecked;
    }

    /**
     * 保存便签
     * @return boolean
     */
    private boolean saveNote() {
        //获取当前文本并存储
        getWorkingText();
        //调用WorkingNote的saveNote()方法保存便签
        boolean saved = mWorkingNote.saveNote();
        //如果已保存，则设置结果为已保存
        if (saved) {
            /**
             * There are two modes from List view to edit view, open one note,
             * create/edit a node. Opening node requires to the original
             * position in the list when back from edit view, while creating a
             * new node requires to the top of the list. This code
             * {@link #RESULT_OK} is used to identify the create/edit state
             */
            setResult(RESULT_OK);
        }
        return saved;
    }

    /**
     * 将便签发送至桌面
     */
    private void sendToDesktop() {
        /**
         * Before send message to home, we should make sure that current
         * editing note is exists in databases. So, for new note, firstly
         * save it
         * 在发送消息至主页之前，先保存当前编辑的便签
         */
        if (!mWorkingNote.existInDatabase()) {
            saveNote();
        }
        /**
         * 如果当前便签具有Id（已编辑）
         */
        if (mWorkingNote.getNoteId() > 0) {
            Intent sender = new Intent();
            Intent shortcutIntent = new Intent(this, NoteEditActivity.class);
            //设置快捷访问的Intent，将其保存至sender中，以便直接快速调用NoteEditActivity
            shortcutIntent.setAction(Intent.ACTION_VIEW);
            shortcutIntent.putExtra(Intent.EXTRA_UID, mWorkingNote.getNoteId());
            sender.putExtra(Intent.EXTRA_SHORTCUT_INTENT, shortcutIntent);
            sender.putExtra(Intent.EXTRA_SHORTCUT_NAME,
                    makeShortcutIconTitle(mWorkingNote.getContent()));
            sender.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE,
                    Intent.ShortcutIconResource.fromContext(this, R.drawable.icon_app));
            sender.putExtra("duplicate", true);
            sender.setAction("com.android.launcher.action.INSTALL_SHORTCUT");
            //显示消息提示框，表示文本以发送至桌面
            showToast(R.string.info_note_enter_desktop);
            //将sender以广播的形式发出
            sendBroadcast(sender);
        } else {
            /**
             * There is the condition that user has input nothing (the note is
             * not worthy saving), we have no note id, remind the user that he
             * should input something
             */
            Log.e(TAG, "Send to desktop error");
            //显示消息提示框，将空便签发送到桌面
            showToast(R.string.error_note_empty_for_send_to_desktop);
        }
    }

    /**
     * 制作快捷访问图标的题目，如果其长度大于最大表示长度，则截断
     * @param content 便签内容
     * @return 返回修剪过的内容字符串
     */
    private String makeShortcutIconTitle(String content) {
        content = content.replace(TAG_CHECKED, "");
        content = content.replace(TAG_UNCHECKED, "");
        return content.length() > SHORTCUT_ICON_TITLE_MAX_LEN ? content.substring(0,
                SHORTCUT_ICON_TITLE_MAX_LEN) : content;
    }

    /**
     * 显示消息提示框
     * @param resId 结果标识（不同的消息）
     */
    private void showToast(int resId) {
        showToast(resId, Toast.LENGTH_SHORT);
    }


    /**
     * 显示消息提示框
     * @param resId 结果标识（不同的消息）
     * @param duration 显示时长
     */
    private void showToast(int resId, int duration) {
        Toast.makeText(this, resId, duration).show();
    }
}
