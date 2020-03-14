package cn.atomdu.beautifulstring;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.ColorRes;
import android.support.annotation.StringRes;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.style.BackgroundColorSpan;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.text.style.RelativeSizeSpan;
import android.text.style.StyleSpan;
import android.view.View;

import java.util.LinkedList;

/**
 * SpannableString 的包装类
 * <p>
 * TextView.setText(BeautifulString.get(Context,"content").build());
 */
public class BeautifulString {
    //Spanned.SPAN_INCLUSIVE_EXCLUSIVE 从起始下标到终了下标，包括起始下标
    //Spanned.SPAN_INCLUSIVE_INCLUSIVE 从起始下标到终了下标，同时包括起始下标和终了下标
    //Spanned.SPAN_EXCLUSIVE_EXCLUSIVE 从起始下标到终了下标，但都不包括起始下标和终了下标
    //Spanned.SPAN_EXCLUSIVE_INCLUSIVE 从起始下标到终了下标，包括终了下标

    //what： 对应的各种Span，不同的Span对应不同的样式。已知的可用类有：
    //BackgroundColorSpan : 文本背景色
    //ForegroundColorSpan : 文本颜色
    //MaskFilterSpan : 修饰效果，如模糊(BlurMaskFilter)浮雕
    //RasterizerSpan : 光栅效果
    //StrikethroughSpan : 删除线
    //SuggestionSpan : 相当于占位符
    //UnderlineSpan : 下划线
    //AbsoluteSizeSpan : 文本字体（绝对大小）
    //DynamicDrawableSpan : 设置图片，基于文本基线或底部对齐。
    //ImageSpan : 图片
    //RelativeSizeSpan : 相对大小（文本字体）
    //ScaleXSpan : 基于x轴缩放
    //StyleSpan : 字体样式：粗体、斜体等
    //SubscriptSpan : 下标（数学公式会用到）
    //SuperscriptSpan : 上标（数学公式会用到）
    //TextAppearanceSpan : 文本外貌（包括字体、大小、样式和颜色）
    //TypefaceSpan : 文本字体
    //URLSpan : 文本超链接
    //ClickableSpan : 点击事件

    public static final int MAX_LENGTH = -1; // 最大长度

    private Context context;
    private LinkedList<Item> items = new LinkedList<>();

    private static final class Item {
        String target;
        int start; // 相对起点
        int end; // 相对终点
        int globalStart; // 绝对起点
        int globalEnd; // 绝对终点
        LinkedList<Style> styles = new LinkedList<>();
    }

    private static final class Style {
        Object what;
        int start;
        int end;
        int flags;
        boolean isGlobal = false; // 是否是按照绝对位置计算
    }

    public BeautifulString(Context context) {
        this.context = context;
        append("");
    }

    public BeautifulString(Context context, String str) {
        this.context = context;
        append(str);
    }

    public static BeautifulString get(Context context) {
        return new BeautifulString(context, "");
    }

    public static BeautifulString get(Context context, String str) {
        return new BeautifulString(context, str);
    }

    public static BeautifulString get(Context context, @StringRes int resId) {
        return new BeautifulString(context, context.getResources().getString(resId));
    }

    public BeautifulString append(@StringRes int targetId) {
        append(context.getResources().getString(targetId));
        return this;
    }

    public BeautifulString append(String target) {
        // 创建一个item，记录字符以及字符的开始与结束位置
        Item item = new Item();
        item.target = target;
        item.start = 0;
        item.end = target.length();
        item.globalStart = 0;
        item.globalEnd = target.length();
        // 如果有前一个Item，追加记录当前字符的开始与结束位置
        if (!items.isEmpty()) {
            Item before = items.getLast();
            if (before != null) {
                //获取在整体中的位置
                item.globalStart = before.globalEnd + item.start;
                item.globalEnd = before.globalEnd + item.end;
            }
        }
        // 计算完成以后，入栈
        items.add(item);
        return this;
    }

    private void setSpan(Object what, int start, int end, int flags, boolean isGlobal) {
        if (!items.isEmpty()) {
            Item item = items.getLast();
            if (item != null) {
                Style style = new Style();
                style.what = what;
                if (isGlobal) {
                    style.start = start;
                    style.end = end;
                } else {
                    style.start = item.globalStart + start;
                    style.end = item.globalStart + end;
                }
                style.flags = flags;
                style.isGlobal = isGlobal;
                item.styles.add(style);
            }
        }
    }

    public SpannableString build() {
        StringBuilder sb = new StringBuilder();
        for (Item item : items) {
            sb.append(item.target);
        }
        SpannableString ss = new SpannableString(sb.toString());
        for (Item item : items) {
            for (Style style : item.styles) {
                if (style.end == MAX_LENGTH) {
                    if (style.isGlobal) {
                        ss.setSpan(style.what, style.start, sb.length(), style.flags);
                    } else {
                        ss.setSpan(style.what, style.start, item.globalStart + item.target.length(), style.flags);
                    }
                } else {
                    ss.setSpan(style.what, style.start, style.end, style.flags);
                }
            }
        }
        return ss;
    }

    /**
     * 设置文字的前景色颜色
     *
     * @param color
     * @param start
     * @param end
     * @return
     */
    public BeautifulString color(String color, int start, int end, boolean isGlobal) {
        ForegroundColorSpan span = new ForegroundColorSpan(Color.parseColor(color));
        setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE, isGlobal);
        return this;
    }

    /**
     * 设置文字的前景色颜色
     *
     * @param color
     * @param start
     * @param end
     * @return
     */
    public BeautifulString color(String color, int start, int end) {
        color(color, start, end, false);
        return this;
    }

    /**
     * 设置文字的前景色颜色
     *
     * @param color
     * @return
     */
    public BeautifulString color(String color) {
        color(color, 0, items.getLast().end);
        return this;
    }

    /**
     * 设置文字的前景色颜色
     *
     * @param colorId
     * @param start
     * @param end
     * @return
     */
    public BeautifulString color(@ColorRes int colorId, int start, int end, boolean isGlobal) {
        ForegroundColorSpan span = new ForegroundColorSpan(context.getResources().getColor(colorId));
        setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE, isGlobal);
        return this;
    }

    /**
     * 设置文字的前景色颜色
     *
     * @param colorId
     * @param start
     * @param end
     * @return
     */
    public BeautifulString color(@ColorRes int colorId, int start, int end) {
        color(colorId, start, end, false);
        return this;
    }

    /**
     * 设置文字的前景色颜色
     *
     * @param colorId
     * @return
     */
    public BeautifulString color(@ColorRes int colorId) {
        color(colorId, 0, items.getLast().end);
        return this;
    }

    /**
     * 设置文字的背景色颜色
     *
     * @param color
     * @param start
     * @param end
     * @return
     */
    public BeautifulString backgroundColor(String color, int start, int end, boolean isGlobal) {
        BackgroundColorSpan span = new BackgroundColorSpan(Color.parseColor(color));
        setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE, isGlobal);
        return this;
    }

    /**
     * 设置文字的背景色颜色
     *
     * @param color
     * @param start
     * @param end
     * @return
     */
    public BeautifulString backgroundColor(String color, int start, int end) {
        backgroundColor(color, start, end, false);
        return this;
    }

    /**
     * 设置文字的背景色颜色
     *
     * @param color
     * @return
     */
    public BeautifulString backgroundColor(String color) {
        backgroundColor(color, 0, items.getLast().end);
        return this;
    }

    /**
     * 设置文字的背景色颜色
     *
     * @param colorId
     * @param start
     * @param end
     * @return
     */
    public BeautifulString backgroundColor(@ColorRes int colorId, int start, int end, boolean isGlobal) {
        BackgroundColorSpan span = new BackgroundColorSpan(context.getResources().getColor(colorId));
        setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE, isGlobal);
        return this;
    }

    /**
     * 设置文字的背景色颜色
     *
     * @param colorId
     * @param start
     * @param end
     * @return
     */
    public BeautifulString backgroundColor(@ColorRes int colorId, int start, int end) {
        backgroundColor(colorId, start, end, false);
        return this;
    }

    /**
     * 设置文字的背景色颜色
     *
     * @param colorId
     * @return
     */
    public BeautifulString backgroundColor(@ColorRes int colorId) {
        backgroundColor(colorId, 0, items.getLast().end);
        return this;
    }

    /**
     * 设置文字的文字大小
     *
     * @param size
     * @param start
     * @param end
     * @return
     */
    public BeautifulString size(float size, int start, int end, boolean isGlobal) {
        RelativeSizeSpan span = new RelativeSizeSpan(size);
        setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE, isGlobal);
        return this;
    }

    /**
     * 设置文字的文字大小
     *
     * @param size
     * @param start
     * @param end
     * @return
     */
    public BeautifulString size(float size, int start, int end) {
        size(size, start, end, false);
        return this;
    }

    /**
     * 设置文字的文字大小
     *
     * @param size
     * @return
     */
    public BeautifulString size(float size) {
        size(size, 0, items.getLast().end);
        return this;
    }

    /**
     * @param style An integer constant describing the style for this span. Examples
     *              include bold, italic, and normal. Values are constants defined
     *              in {@link android.graphics.Typeface}.
     */
    public BeautifulString style(int style, int start, int end, boolean isGlobal) {
        StyleSpan span = new StyleSpan(style);
        setSpan(span, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE, isGlobal);
        return this;
    }

    /**
     * @param style An integer constant describing the style for this span. Examples
     *              include bold, italic, and normal. Values are constants defined
     *              in {@link android.graphics.Typeface}.
     */
    public BeautifulString style(int style, int start, int end) {
        style(style, start, end, false);
        return this;
    }

    /**
     * @param style An integer constant describing the style for this span. Examples
     *              include bold, italic, and normal. Values are constants defined
     *              in {@link android.graphics.Typeface}.
     */
    public BeautifulString style(int style) {
        style(style, 0, items.getLast().end);
        return this;
    }

    /**
     * 设置点击区域
     * <p>
     * 如果要响应点击区域，需要TextView初始化的时候调用一下此方法
     * TextView.setMovementMethod(LinkMovementMethod.getInstance())
     *
     * @param clickableSpan
     * @param start
     * @param end
     * @return
     */
    public BeautifulString onClick(ClickableSpan clickableSpan, int start, int end, boolean isGlobal) {
        setSpan(clickableSpan, start, end, Spanned.SPAN_INCLUSIVE_EXCLUSIVE, isGlobal);
        return this;
    }

    /**
     * 设置点击区域
     * <p>
     * 如果要响应点击区域，需要TextView初始化的时候调用一下此方法
     * TextView.setMovementMethod(LinkMovementMethod.getInstance())
     *
     * @param clickableSpan
     * @param start
     * @param end
     * @return
     */
    public BeautifulString onClick(ClickableSpan clickableSpan, int start, int end) {
        onClick(clickableSpan, start, end, false);
        return this;
    }

    /**
     * 设置点击区域
     * <p>
     * 如果要响应点击区域，需要TextView初始化的时候调用一下此方法
     * TextView.setMovementMethod(LinkMovementMethod.getInstance())
     *
     * @param clickableSpan
     * @return
     */
    public BeautifulString onClick(ClickableSpan clickableSpan) {
        onClick(clickableSpan, 0, items.getLast().end);
        return this;
    }

    /**
     * 设置点击区域
     * <p>
     * 如果要响应点击区域，需要TextView初始化的时候调用一下此方法
     * TextView.setMovementMethod(LinkMovementMethod.getInstance())
     *
     * @param onClickListener
     * @param start
     * @param end
     * @return
     */
    public BeautifulString onClick(View.OnClickListener onClickListener, int start, int end, boolean isGlobal) {
        onClick(new Clickable(onClickListener), start, end, isGlobal);
        return this;
    }

    /**
     * 设置点击区域
     * <p>
     * 如果要响应点击区域，需要TextView初始化的时候调用一下此方法
     * TextView.setMovementMethod(LinkMovementMethod.getInstance())
     *
     * @param onClickListener
     * @param start
     * @param end
     * @return
     */
    public BeautifulString onClick(View.OnClickListener onClickListener, int start, int end) {
        onClick(new Clickable(onClickListener), start, end);
        return this;
    }

    /**
     * 设置点击区域
     * <p>
     * 如果要响应点击区域，需要TextView初始化的时候调用一下此方法
     * TextView.setMovementMethod(LinkMovementMethod.getInstance())
     *
     * @param onClickListener
     * @return
     */
    public BeautifulString onClick(View.OnClickListener onClickListener) {
        onClick(new Clickable(onClickListener), 0, items.getLast().end);
        return this;
    }

    /**
     * 点击区域写法
     */
    class Clickable extends ClickableSpan {
        private final View.OnClickListener mListener;
        private Context context;

        public Clickable(View.OnClickListener l) {
            mListener = l;
        }

        public Clickable(Context context, View.OnClickListener l) {
            mListener = l;
        }

        /**
         * 重写父类点击事件
         */
        @Override
        public void onClick(View v) {
            mListener.onClick(v);
        }

        @Override
        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setUnderlineText(false);// 设置文字下划线不显示
            //ds.setColor(context.getResources().getColor(R.color.colorAccent));// 设置字体颜色
        }
    }
}
