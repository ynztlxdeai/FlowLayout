package com.luoxiang.flowlibrary;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * projectName: 	    FlowLayout
 * packageName:	        com.luoxiang.flowlibrary
 * className:	        FlowLayout
 * author:	            Luoxiang
 * time:	            2016/6/21	8:07
 * desc:	            TODO
 *
 * svnVersion:	        $Rev
 * upDateAuthor:	    Lxiang
 * upDate:	            2016/6/21
 * upDateDesc:	        TODO
 */
public class FlowLayout
        extends ViewGroup
{
    //一个装下行的集合
    private List<Line> mLines = new ArrayList<>();

    //行的水平方向上的空隙
    private int mHorizontalSpace = 15;
    //行的垂直方向上的空隙
    private int mVerticalSpace   = 15;

    private Line mCurrentLine;

    public FlowLayout(Context context) {
        this(context, null);
    }

    public FlowLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //设置数值方向和水平方向的间隙
    public void setSpace(int horizontal, int vertical) {
        this.mHorizontalSpace = horizontal;
        this.mVerticalSpace = vertical;
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        /**
         * 测量全部的孩子的话 会比较麻烦 因此 我们值需要测量行进行布局就可以了
         *
         * 然后行里面的孩子 交给行自己去测量
         */


        //初始的顶部高度就是边距高度
        int top = getPaddingTop();
        for (int i = 0; i < mLines.size(); i++) {
            Line line = mLines.get(i);
            //行的孩子的布局 交给行自行完成
            //因为对于左边来说的话 是一直不会变动的 左边开始位置一直都是起始位置
            line.layout(getPaddingLeft(), top);
            //因为每次都会向下布局 因此高度需要记录到已经布局完成的高度的地方
            top += line.mLineHeight + mVerticalSpace;
        }

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        /**
         * 根据手机性能的不同 onmeasure方法不是一次星全部执行完成 而是上一个测量完成之后才开始测量下一个
         *
         * 所以在测量的开始 需要把之前的清空
         */

        mLines.clear();
        mCurrentLine = null;

        /**
         * 第一步 : 测量孩子的宽高
         */
        //获得当前的大小
        int width = MeasureSpec.getSize(widthMeasureSpec);
        //计算孩子的最大宽度
        int childMaxWidth = width - getPaddingLeft() - getPaddingRight();
        //拿到孩子的总数量
        int count = getChildCount();
        for (int i = 0; i < count; i++) {
            //获得当前的孩子
            View child = getChildAt(i);
            //如果孩子是GONE的状态 跳过执行
            if (child.getVisibility() == View.GONE) {
                continue;
            }
            //测量孩子 按照父容器对孩子的期望值确定
            measureChild(child, widthMeasureSpec, heightMeasureSpec);
            if (mCurrentLine == null) {
                //如果当前行是一个空行 new出来以后直接添加 如果是空行的话 无论当前孩子有多大 都必须添加
                mCurrentLine = new Line(childMaxWidth, mHorizontalSpace);
                //添加到行集合中
                mLines.add(mCurrentLine);
                //把当前的孩子添加到行中
                mCurrentLine.addChild(child);
            } else {
                //如果当前还可以添加孩子的话 直接添加
                if (mCurrentLine.addable(child)) {
                    mCurrentLine.addChild(child);
                } else {
                    //如果不能添加孩子了的话 就直接添加到下一行
                    mCurrentLine = new Line(childMaxWidth, mHorizontalSpace);
                    //添加到行集合中
                    mLines.add(mCurrentLine);
                    //把当前的孩子添加到行中
                    mCurrentLine.addChild(child);
                }

            }
        }

        /**
         * 第二步 : 测量自己的宽高
         *
         * 自己高度并不是一个确定的值 自己的高度随着孩子的行数增加而增加
         *
         * 自己的高度首先包括上下的一个padding边距
         *
         * 然后包括孩子之间的间隙高度(间隙个数等于孩子个数-1)
         *
         * 最后就是孩子本身的高度
         */

        //拿到控件的上下边距
        int height = getPaddingTop() + getPaddingBottom();
        //对孩子进行遍历 计算高度
        for (int i = 0; i < mLines.size(); i++) {
            Line line = mLines.get(i);

            //当前父布局的高度等于已知高度加上孩子的高度
            height += line.mLineHeight;
            //只要不是最后一个孩子 就需要加上数值方向上的高度间隙
            if (i != mLines.size() - 1) {
                height += mVerticalSpace;
            }

        }

        setMeasuredDimension(width, height);
    }


    public class Line {
        //一个装View的集合
        private List<View> mViews = new ArrayList<>();
        //行用了的宽度
        private int        mLineUserdWidth;
        //行的总宽度
        private int        mLineTotleWidth;
        //行的高度
        private int        mLineHeight;
        //控件间隙 横向
        private int        mSpace;

        //创建的时候给定赋值
        public Line(int lineTotleWidth, int space) {
            mLineTotleWidth = lineTotleWidth;
            mSpace = space;
        }

        //查询是否可以添加
        public boolean addable(View view) {
            //如果控件里没有孩子的话 可以直接添加
            if (mViews.size() == 0) {
                return true;
            }
            //否则需要计算是否宽度超过现在所有的宽度
            //获得控件测量后的宽度
            int childWidth = view.getMeasuredWidth();
            //如果这个控件加上之前用了的宽度 还小于总的宽度 就可以继续添加
            if (childWidth + mLineUserdWidth + mSpace < mLineTotleWidth) {
                return true;
            }
            //其他清空下不允许添加孩子
            return false;
        }

        public void addChild(View view) {
            //获得控件测量后的宽度
            int childWidth = view.getMeasuredWidth();
            //获得控件的测量后的高度
            int childHeight = view.getMeasuredHeight();

            if (mViews.size() == 0) {
                //如果这一行一个控件都没有的话  直接放入 并计算宽高
                //计算行的宽度
                mLineUserdWidth = childWidth;
                //行的高度
                mLineHeight = childHeight;
            } else {
                //这一行已经有别的控件了 需要对控件测量之后才能放入
                //如果这一行已经有东西了的话 就需要加上原本的控件的宽度和现在控件的间隙
                mLineUserdWidth += childWidth + mSpace;
                //高度计算 如果行的高度大于孩子的行的高度 就用现有的高度 否则用孩子的高度
                mLineHeight = mLineHeight > childHeight
                              ? mLineHeight
                              : childHeight;
            }


            mViews.add(view);
        }

        //行对自己的孩子进行自行布局
        public void layout(int left, int top) {
            //判断是否还有剩余的宽度
            int unusedWidth = mLineTotleWidth - mLineUserdWidth;
            //由于会出现填充的时候 刚刚最后一个放不下 换到下一行的时候 但是这个时候会出现大量的空白区域
            //因此 计算这个空白区域的大小 并把这个大小平均分配给这一行所有的孩子
            //获得每个可以分到的平均值
            int avgWidth = (int) (unusedWidth * 1f / mViews.size() + 0.5f);
            for (int i = 0; i <mViews.size() ; i++) {
                View view = mViews.get(i);

                int measuredHeight = view.getMeasuredHeight();
                int measuredWidth  = view.getMeasuredWidth();

                //先不去布局,先期望孩子的宽高
                if (avgWidth > 0) {
                    int childWidthSpec = MeasureSpec.makeMeasureSpec(measuredWidth + avgWidth,
                                                                     MeasureSpec.EXACTLY);
                    int childHeightSpec = MeasureSpec.makeMeasureSpec(measuredHeight,
                                                                      MeasureSpec.EXACTLY);
                    view.measure(childWidthSpec, childHeightSpec);

                    //获取测量后新的值
                    measuredWidth = view.getMeasuredWidth();
                    measuredHeight = view.getMeasuredHeight();
                }

                int t = (int) (top + (mLineHeight - measuredHeight) / 2f + 0.5f);
                int l = left;
                int r = l + measuredWidth;
                int b = t + measuredHeight;

                view.layout(l, t, r, b);

                //因为下一个的left是改变的 因此要一直记录这个left值
                left += measuredWidth + mSpace;
            }
        }
    }
}
