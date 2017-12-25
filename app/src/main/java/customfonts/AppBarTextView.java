package customfonts;


import android.content.Context;
import android.graphics.Typeface;
import android.util.AttributeSet;

/**
 * Created by mesutgenc on 14.07.2017.
 */

public class AppBarTextView extends android.support.v7.widget.AppCompatTextView {

    public AppBarTextView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init();
    }

    public AppBarTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public AppBarTextView(Context context) {
        super(context);
        init();
    }

    private void init() {
        if (!isInEditMode()) {
            Typeface tf = Typeface.createFromAsset(getContext().getAssets(), "fonts/Spiderman-Homecoming.otf");
            setTypeface(tf);
        }
    }
}
