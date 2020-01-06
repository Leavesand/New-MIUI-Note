package net.micode.notes.ui;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.RelativeLayout;
import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import net.micode.notes.R;

import java.util.HashMap;
import java.util.ArrayList;
import java.util.Map;

public class PasswordView extends RelativeLayout implements View.OnClickListener {
    Context context;

    private String strPassword; //输入的密码
    private TextView[] tvList;  //用数组保存4个TextView
    private GridView gridView;  //GridView布局键盘
    private ArrayList<Map<String, String>> valueList;   //要用Adapter中适配，用数组不能往adapter中填充

    private ImageView imgCancel;

    private int currentIndex = -1;    //用于记录当前输入密码格位置

    /**
     * 声明整型常量用于描述三种不同的口令输入界面
     */
    public static final int NEW_PWD_VIEW = 0; //表示“设置便签访问密码”界面的序号
    public static final int RPT_PWD_VIEW = 1; //表示“确认设置访问密码”界面的序号
    public static final int CUR_PWD_VIEW = 2; //表示“根据密码访问便签”界面的序号


    public PasswordView(Context context, int mode) {
        this(context, null, mode);
    }

    /**
     * 该构造方法用于在界面上方创建便签输入的界面
     * @param context 便签密码输入界面的上下文
     * @param attrs 便签密码输入界面的控件参数
     * @param mode 便签密码输入界面的序号
     */
    public PasswordView(Context context, AttributeSet attrs, int mode) {
        //调用父类方法处理参数
        super(context, attrs);
        this.context = context;
        View view;
        /**
         * 根据输入参数mode跳转到不同的界面
         */
        switch (mode) {
            case NEW_PWD_VIEW:
                view = View.inflate(context, R.layout.note_encryption, null);
                break;
            case RPT_PWD_VIEW:
                view = View.inflate(context, R.layout.note_encryption_repeat, null);
                break;
            case CUR_PWD_VIEW:
                view = View.inflate(context, R.layout.note_decryption, null);
                break;
            default:
                view = View.inflate(context, R.layout.homepage, null);
        }

        valueList = new ArrayList<Map<String, String>>();
        tvList = new TextView[4];

        imgCancel = (ImageView) view.findViewById(R.id.img_cancel);
        imgCancel.setOnClickListener(this);

        tvList[0] = (TextView) view.findViewById(R.id.tv_pass1);
        tvList[1] = (TextView) view.findViewById(R.id.tv_pass2);
        tvList[2] = (TextView) view.findViewById(R.id.tv_pass3);
        tvList[3] = (TextView) view.findViewById(R.id.tv_pass4);

        gridView = (GridView) view.findViewById(R.id.gv_keyboard);

        setView();
        addView(view);      //必须要，不然不显示控件
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.img_cancel:
                Toast.makeText(context, "Cancel", Toast.LENGTH_SHORT).show();
                break;
        }
    }

    private void setView() {
        /* 初始化按钮上应该显示的数字 */
        for (int i = 1; i < 13; i++) {
            Map<String, String> map = new HashMap<String, String>();
            if (i < 10) {
                map.put("name", String.valueOf(i));
            } else if (i == 10) {
                map.put("name", "");
            } else if (i == 12) {
                map.put("name", "<<-");
            } else if (i == 11) {
                map.put("name", String.valueOf(0));
            }
            valueList.add(map);
        }

        gridView.setAdapter(adapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if (position < 11 && position != 9) {    //点击0~9按钮
                    if (currentIndex >= -1 && currentIndex < 3) {      //判断输入位置————要小心数组越界
                        tvList[++currentIndex].setText(valueList.get(position).get("name"));
                    }
                } else {
                    if (position == 11) {      //点击退格键
                        if (currentIndex - 1 >= -1) {      //判断是否删除完毕————要小心数组越界
                            tvList[currentIndex--].setText("");
                        }
                    }
                }
            }
        });
    }

    //设置监听方法，在第6位输入完成后触发
    public void setOnFinishInput(final OnPasswordInputFinish pass) {
        tvList[3].addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.toString().length() == 1) {
                    strPassword = "";     //每次触发都要先将strPassword置空，再重新获取，避免由于输入删除再输入造成混乱
                    for (int i = 0; i < 4; i++) {
                        strPassword += tvList[i].getText().toString().trim();
                    }
                    pass.inputFinish();    //接口中要实现的方法，完成密码输入完成后的响应逻辑
                }
            }
        });
    }

    /* 获取输入的密码 */
    public String getStrPassword() {
        return strPassword;
    }

    /* 暴露取消支付的按钮，可以灵活改变响应 */
    public ImageView getCancelImageView() {
        return imgCancel;
    }

    //GridView的适配器
    BaseAdapter adapter = new BaseAdapter() {
        @Override
        public int getCount() {
            return valueList.size();
        }

        @Override
        public Object getItem(int position) {
            return valueList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.item_grid, null);
                viewHolder = new ViewHolder();
                viewHolder.btnKey = (TextView) convertView.findViewById(R.id.btn_keys);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }
            viewHolder.btnKey.setText(valueList.get(position).get("name"));
            if(position == 9){
                viewHolder.btnKey.setBackgroundResource(R.drawable.selector_key_del);
                viewHolder.btnKey.setEnabled(false);
            }
            if(position == 11){
                viewHolder.btnKey.setBackgroundResource(R.drawable.selector_key_del);
            }

            return convertView;
        }
    };

    /**
     * 存放控件
     */
    public final class ViewHolder {
        public TextView btnKey;
    }
}
