package com.duyi.avatarautoscrollview.view

import android.animation.ValueAnimator
import android.content.Context
import android.os.Build
import android.os.Handler
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.widget.RelativeLayout

class AvatarAutoScrollView : RelativeLayout {
    companion object {
        const val TAG = "AvatarAutoScrollView"
    }


    private val mHandler = Handler()

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )


    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        oneChildWidth = getChildAt(0).measuredWidth
        doOnLayoutLayout(0f)
        startPostTask()
    }

    var showItemNum = 3
    var leftOffsetNum = 60
    var postDelayTime = 5000L

    var childOffsetDistence = 0
    var oneChildWidth = 0
    var fistViewItem = 0
    var viewSortList = mutableListOf<View>()

    private fun doOnLayoutLayout(animateFraction: Float) {
        var showLeft = 0
        showLeft += childOffsetDistence


        viewSort()
        for (i in 0 until viewSortList.size) {
            val child = viewSortList[i]
            child.layout(0, 0, 0, 0)
        }

        var realShowItemNum = showItemNum
        if (animateFraction == 1f) {
            realShowItemNum -= 1
        }
        for (i in 0 until minOf(realShowItemNum, viewSortList.size)) {
            val child = viewSortList[i]
            val childWidth = child.measuredWidth
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                child.z = i.toFloat()
            }
            if (i != 0) {
                showLeft -= leftOffsetNum
            }

            layoutChild(child, showLeft)


            showLeft += childWidth
        }
        val lastChild = viewSortList[viewSortList.size - 1]
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            lastChild.z = -1f
        }
        layoutChild(lastChild, 0)
    }


    fun layoutChild(child:View, showLeft:Int) {
        //根据宽度来设置具体的左边偏移量
        val left = showLeft
        val right = left + child.measuredWidth
        val top = 0
        val bottom = top + child.measuredHeight

        child.layout(left, top, right, bottom)
    }

    private fun viewSort() {
        viewSortList.clear()
        for (i in fistViewItem until childCount) {
            val child = getChildAt(i)
            viewSortList.add(child)
        }
        for(i in 0 until fistViewItem) {
            val child = getChildAt(i)
            viewSortList.add(child)
        }
    }

    fun startPostTask() {
        mHandler.postDelayed({
            startAnimation()
        }, postDelayTime)
    }

    //定义动画
    private var animator: ValueAnimator? = null

    fun startAnimation() {
        var lastAnimatedValue = 0
        childOffsetDistence = 0
        if (animator == null) {
            val animateLength = oneChildWidth - leftOffsetNum
            animator = ValueAnimator.ofInt(0, animateLength)
            animator?.duration = 1000
            animator?.repeatCount = 0
            animator?.addUpdateListener {animator->
                val animatedValue = animator.animatedValue as Int
                val animateFraction = animator.animatedFraction
                if (animatedValue == 0) {
                    lastAnimatedValue = 0
                }
                Log.i(TAG, "$animatedValue,$lastAnimatedValue")
                val changeValue = animatedValue - lastAnimatedValue
                lastAnimatedValue = animatedValue
                childOffsetDistence += changeValue
                doOnLayoutLayout(animateFraction)

                if (animatedValue == animateLength) {
                    fistViewItem -= 1
                    if (fistViewItem < 0) {
                        fistViewItem = childCount -1
                    }
                    startPostTask()
                }
            }
        }
        animator?.start()
    }


}