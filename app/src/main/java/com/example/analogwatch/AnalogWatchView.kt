package com.example.analogwatch

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PointF
import android.graphics.Rect
import android.os.Handler
import android.os.Looper
import android.os.Parcel
import android.os.Parcelable
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorInt
import java.util.Calendar
import kotlin.math.min
import kotlin.properties.Delegates


class AnalogWatchView(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    //    NOTE: черная полоска на часах из примера
    private val watchCasePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 30f
    }

    private val watchPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val delimitersPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
    }

    private val timeNumbersPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
    }

    private val arrowsPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeCap = Paint.Cap.ROUND
    }

    private var widthView: Int = 512
    private var heightView: Int = 512
    private var watchViewCenter = PointF()

    private lateinit var hourArrow: ArrowData
    private lateinit var minuteArrow: ArrowData
    private lateinit var secondArrow: ArrowData

    private var numberCenterPoint = PointF(0f, 0f)

    private var watchFullRadius: Float = 0.0f
    private var watchBodyRadius: Float = 0f

    private var delimiterPoint = PointF(0f, 0f)

    private var hour by Delegates.notNull<Int>()
    private var minute by Delegates.notNull<Int>()
    private var second by Delegates.notNull<Int>()

    private val timeCounter = Handler(Looper.getMainLooper())
    private val timeRunnable: Runnable = object : Runnable {
        override fun run() {
            updateCurrentTime()
            timeCounter.postDelayed(this, 1000)
        }
    }

    private fun updateCurrentTime() {
        val calendar = Calendar.getInstance()
        hour = calendar.get(Calendar.HOUR)
        minute = calendar.get(Calendar.MINUTE)
        second = calendar.get(Calendar.SECOND)
    }


    init {
        updateCurrentTime()
        timeCounter.post(timeRunnable)

        initAttributes(attrs, context)

    }

    private fun initAttributes(attrs: AttributeSet?, context: Context) {
        val setXmlAttributes = context.obtainStyledAttributes(attrs, R.styleable.AnalogWatchView)

        watchCasePaint.color =
            setXmlAttributes.getColor(R.styleable.AnalogWatchView_caseColor, DEFAULT_CASE_COLOR)
        watchPaint.color =
            setXmlAttributes.getColor(R.styleable.AnalogWatchView_bodyColor, DEFAULT_BODY_COLOR)
        delimitersPaint.color = setXmlAttributes.getColor(
            R.styleable.AnalogWatchView_delimitersColor,
            DEFAULT_DELIMITERS_COLOR
        )
        timeNumbersPaint.color = setXmlAttributes.getColor(
            R.styleable.AnalogWatchView_timeNumbersColor,
            DEFAULT_NUMBERS_COLOR
        )
        val hourArrowColor = setXmlAttributes.getColor(
            R.styleable.AnalogWatchView_hourArrowColor,
            DEFAULT_HOUR_ARROW_COLOR
        )
        val minuteArrowColor = setXmlAttributes.getColor(
            R.styleable.AnalogWatchView_minuteArrowColor,
            DEFAULT_MIN_ARROW_COLOR
        )
        val secondArrowColor = setXmlAttributes.getColor(
            R.styleable.AnalogWatchView_secondArrowColor,
            DEFAULT_SEC_ARROW_COLOR
        )
        hourArrow = ArrowData(color = hourArrowColor)
        minuteArrow = ArrowData(color = minuteArrowColor)
        secondArrow = ArrowData(color = secondArrowColor)

        setXmlAttributes.recycle()
    }

    override fun onSaveInstanceState(): Parcelable {
        val superState = super.onSaveInstanceState()
        val savedState = AnalogWatchViewSavedState(superState)
        savedState.watchCaseColor = watchCasePaint.color
        savedState.watchBodyColor = watchPaint.color
        savedState.delimitersColor = delimitersPaint.color
        savedState.numbersColor = timeNumbersPaint.color
        savedState.hourArrowColor = hourArrow.color
        savedState.minuteArrowColor = minuteArrow.color
        savedState.secondArrowColor = secondArrow.color
        return savedState
    }

    override fun onRestoreInstanceState(state: Parcelable?) {
        var superState = state
        if (state is AnalogWatchViewSavedState) {
            superState = state.superState
            watchCasePaint.color = state.watchCaseColor
            watchPaint.color = state.watchBodyColor
            delimitersPaint.color = state.delimitersColor
            timeNumbersPaint.color = state.numbersColor
            hourArrow.color = state.hourArrowColor
            minuteArrow.color = state.minuteArrowColor
            secondArrow.color = state.secondArrowColor
        }
        super.onRestoreInstanceState(superState)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)

        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)

        val width = when (MeasureSpec.getMode(widthMeasureSpec)) {
            MeasureSpec.EXACTLY -> widthSize
            MeasureSpec.AT_MOST -> min(widthSize, widthView)
            else -> widthView
        }

        val height = when (MeasureSpec.getMode(heightMeasureSpec)) {
            MeasureSpec.EXACTLY -> heightSize
            MeasureSpec.AT_MOST -> min(heightSize, heightView)
            else -> heightView
        }

        setMeasuredDimension(width, height)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        widthView = w - paddingStart - paddingEnd
        heightView = h - paddingTop - paddingBottom
        watchViewCenter = PointF(
            (paddingStart + widthView / 2).toFloat(), (paddingTop + heightView / 2).toFloat()
        )
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        watchFullRadius = min(widthView / 2, heightView / 2).toFloat()
        watchBodyRadius = watchFullRadius - watchCasePaint.strokeWidth

        delimiterPoint.y = -11 * watchBodyRadius / 12
        numberCenterPoint.y = -4 * watchBodyRadius / 5

        hourArrow.start.y = watchBodyRadius / 15
        hourArrow.end.y = -watchBodyRadius / 2

        minuteArrow.start.y = watchBodyRadius / 10
        minuteArrow.end.y = -11 * watchBodyRadius / 14

        secondArrow.start.y = watchBodyRadius / 11
        secondArrow.end.y = -5 * watchBodyRadius / 7
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.translate(watchViewCenter.x, watchViewCenter.y)

        drawWatchFound(canvas)

        drawDelimiters(canvas)

        drawTimeNumbers(canvas)

        drawArrows(canvas)
// NOTE: Так как часы не обязательно обновлять каждый кадр, можно поставить задержку например раз в секунду,
// но в таком случае возможно запоздание анимации сек стрелки вплоть до секунды, поэтому не решился добавлять, а вместо этого считать время раз в секунду
        invalidate()
    }

    private fun drawWatchFound(canvas: Canvas) {
        canvas.drawCircle(0f, 0f, watchBodyRadius, watchPaint)
        canvas.drawCircle(0f, 0f, watchBodyRadius, watchCasePaint)
    }

    private fun drawDelimiters(canvas: Canvas) {
        for (i in 0 until countDelimiters) {
            delimitersPaint.strokeWidth =
                if (i % 5 == 0) watchFullRadius / 30f
                else watchFullRadius / 60f

            canvas.drawPoint(
                delimiterPoint.x,
                delimiterPoint.y,
                delimitersPaint
            )
            canvas.rotate((360.0 / countDelimiters).toFloat())
        }
    }

    private fun drawTimeNumbers(canvas: Canvas) {
        val textBound = Rect()
        for (i in 12 downTo 1) {
            canvas.save()
            canvas.translate(numberCenterPoint.x, numberCenterPoint.y)
            val text = i.toString()
            timeNumbersPaint.getTextBounds(text, 0, text.length, textBound)
            timeNumbersPaint.textSize = watchBodyRadius / 6

            canvas.rotate((12 - i) * 30f)
            canvas.drawText(
                text,
                (-textBound.width() / 2).toFloat(),
                (textBound.height() / 2).toFloat(),
                timeNumbersPaint
            )
            canvas.restore()
            canvas.rotate(-30f)
        }
    }

    private fun drawArrows(canvas: Canvas) {
        hourArrow.degree = (hour + minute * 1f / 60f + second * 1f / 3600f) * 30f
        minuteArrow.degree = (minute + second * 1f / 60f) * 6f
        secondArrow.degree = second * 6f

        canvas.save()
        arrowsPaint.strokeWidth = watchFullRadius / 20f
        arrowsPaint.color = hourArrow.color
        canvas.rotate(hourArrow.degree)
        canvas.drawLine(
            hourArrow.start.x,
            hourArrow.start.y,
            hourArrow.end.x,
            hourArrow.end.y,
            arrowsPaint
        )
        canvas.restore()

        canvas.save()
        arrowsPaint.strokeWidth = watchFullRadius / 25f
        arrowsPaint.color = minuteArrow.color
        canvas.rotate(minuteArrow.degree)
        canvas.drawLine(
            minuteArrow.start.x,
            minuteArrow.start.y,
            minuteArrow.end.x,
            minuteArrow.end.y,
            arrowsPaint
        )
        canvas.restore()

        canvas.save()
        arrowsPaint.strokeWidth = watchFullRadius / 60f
        arrowsPaint.color = secondArrow.color
        canvas.rotate(secondArrow.degree)
        canvas.drawLine(
            secondArrow.start.x,
            secondArrow.start.y,
            secondArrow.end.x,
            secondArrow.end.y,
            arrowsPaint
        )
        canvas.restore()
        canvas.drawCircle(0f, 0f, watchFullRadius / 60, watchPaint)
    }

    //    Note: Для программного изменения
    fun changeWatchBodyColor(@ColorInt color: Int) {
        watchPaint.color = color
    }

    fun changeWatchCaseColor(@ColorInt color: Int) {
        watchCasePaint.color = color
    }

    fun changeTimeNumbersColor(@ColorInt color: Int) {
        timeNumbersPaint.color = color
    }

    fun changeDelimitersColor(@ColorInt color: Int) {
        delimitersPaint.color = color
    }

    fun changeSecArrowColor(@ColorInt color: Int) {
        secondArrow.color = color
    }

    fun changeMinArrowColor(@ColorInt color: Int) {
        minuteArrow.color = color
    }

    fun changeHourArrowColor(@ColorInt color: Int) {
        hourArrow.color = color
    }

    companion object {
        private const val DEFAULT_HOUR_ARROW_COLOR = Color.BLACK
        private const val DEFAULT_MIN_ARROW_COLOR = Color.BLACK
        private const val DEFAULT_SEC_ARROW_COLOR = Color.BLACK
        private const val DEFAULT_CASE_COLOR = Color.BLACK
        private const val DEFAULT_BODY_COLOR = Color.WHITE
        private const val DEFAULT_DELIMITERS_COLOR = Color.BLACK
        private const val DEFAULT_NUMBERS_COLOR = Color.BLACK
        private const val countDelimiters: Int = 60
    }

    class AnalogWatchViewSavedState : BaseSavedState {
        var watchCaseColor: Int = Color.BLACK
        var watchBodyColor: Int = Color.BLACK
        var delimitersColor: Int = Color.BLACK
        var numbersColor: Int = Color.BLACK
        var hourArrowColor: Int = Color.BLACK
        var minuteArrowColor: Int = Color.BLACK
        var secondArrowColor: Int = Color.BLACK

        constructor(superState: Parcelable?) : super(superState)

        private constructor(parcel: Parcel) : super(parcel) {
            watchCaseColor = parcel.readInt()
            watchBodyColor = parcel.readInt()
            delimitersColor = parcel.readInt()
            numbersColor = parcel.readInt()
            hourArrowColor = parcel.readInt()
            minuteArrowColor = parcel.readInt()
            secondArrowColor = parcel.readInt()
        }

        override fun writeToParcel(out: Parcel, flags: Int) {
            super.writeToParcel(out, flags)
            out.writeInt(watchCaseColor)
            out.writeInt(watchBodyColor)
            out.writeInt(delimitersColor)
            out.writeInt(numbersColor)
            out.writeInt(hourArrowColor)
            out.writeInt(minuteArrowColor)
            out.writeInt(secondArrowColor)
        }

        companion object CREATOR : Parcelable.Creator<AnalogWatchViewSavedState> {
            override fun createFromParcel(parcel: Parcel): AnalogWatchViewSavedState {
                return AnalogWatchViewSavedState(parcel)
            }

            override fun newArray(size: Int): Array<AnalogWatchViewSavedState?> {
                return arrayOfNulls(size)
            }
        }
    }

    data class ArrowData(
        var start: PointF = PointF(0f, 0f),
        var end: PointF = PointF(0f, 0f),
        var degree: Float = 0.0f,
        var color: Int
    )
}

