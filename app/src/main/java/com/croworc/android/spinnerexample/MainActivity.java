package com.croworc.android.spinnerexample;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private static final String LOG_TAG = MainActivity.class.getSimpleName();

    private Context mContext = this;
    private Spinner mSpinner;

    String[] mCelebrities = {
            "Chris Hemsworth",
            "Jennifer Lawrence",
            "Jessica Alba",
            "Brad Pitt",
            "Tom Cruise",
            "Johnny Depp",
            "Megan Fox",
            "Paul Walker",
            "Vin Diesel"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupSpinner();
    }

    /**
     * Configures the Spinner to use an ArrayAdapter bound to the dynamically created string array,
     * set the DropDownViewResource to use a layout provided by the framework, to set the adapter
     * and two set two event listener (which will be removed during onDestroy() ).
     */
    private void setupSpinner() {
        // Fetch and hold a reference to the Spinner widget.
        mSpinner = (Spinner) findViewById(R.id.spinner);

        // Create a new array adapter and base it on the dynamically created celebrities array.
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item, // built-in layout to use when not expanded
                mCelebrities
        );

        // Specify the layout to use when the list of choices appears.
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        // Apply the adapter to the spinner.
        mSpinner.setAdapter(adapter);

        // Attach two event listener to the spinner:
        //   - OnTouchEventListener
        //   - OnItemSelectedListener
        // so that we can run our code (e.g. show a toast message) *only when the user* has selected
        // an item from the drop-down list, but avoid the code being run when the OnItemSelected event
        // just gets fired after a device rotation.
        // Such a config change will cause the activity to be destroyed and re-created, which in turn
        // sets the spinner adapter, which would finally cause this event to be fired without any
        // user interaction. On my device it got fired even *twice*!
        // Have a look at how the inner class SpinnerInteractionHandler takes care of this distinction
        // by setting a "use has touched the spinner" flag in the onTouch() method and examining this
        // flag's value within the onItemSelected() method - which is guaranteed) to be fired
        // *after* onTouch().
        SpinnerInteractionHandler spinnerInteractionHandler = new SpinnerInteractionHandler();
        mSpinner.setOnTouchListener(spinnerInteractionHandler);
        mSpinner.setOnItemSelectedListener(spinnerInteractionHandler);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Remove both listeners as not to leak the activity.
        mSpinner.setOnTouchListener(null);
        mSpinner.setOnItemSelectedListener(null);
        // Log.d(LOG_TAG, "in method onDestroy(): both listeners were removed");
    }


    /**
     * This helper class implements the event handlers for two events:
     *   - OnItemSelected, and
     *   - OnTouch
     *
     *   The OnTouch listener solves the problem that the OnItemSelected event gets fired
     *   without any user intervention whenever the device is rotated and thus the activity re-
     *   created. That's because onCreate() sets the spinner's adapter, which will cause the event
     *   to be fired. That's by design.
     *   Some answers on stackoverflow.com suggested to use a boolean flag to indicate
     *   whether the onItemSelected() method was called just the first time after activity re-creation,
     *   which would indeed work if the event would get fired only once.
     *   In my case, however, it got called *twice*, which would render this approach useless.
     *   A pretty elegant solution was provided by Andres Q. who suggested to attach an additional
     *   onTouch listener to the spinner, which can be used to set a flag, indicating that indeed
     *   a user (and not only the rotation itself) had fired the OnItemSelected event.
     *
     *   Here's the link to the relevant question:
     *   http://stackoverflow.com/questions/14560733/spinners-onitemselected-callback-called-twice-after-a-rotation-if-non-zero-posi
     */
    class SpinnerInteractionHandler implements AdapterView.OnItemSelectedListener,
            View.OnTouchListener {

        private final String LOG_TAG = SpinnerInteractionHandler.class.getSimpleName();

        private boolean mHasUserSelected = false;

        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            // Log.d(LOG_TAG, "in onTouch()");
            mHasUserSelected = true; // User has clicked the spinner, so set this flag to true.
            return false;
        }

        @Override
        public void onItemSelected(
                AdapterView<?> adapterView,
                View view,
                int adapterPosition,
                long arrayIdx) {
            // Log.d(LOG_TAG, "in onItemSelected()");
            if (mHasUserSelected) { // Only run our code if it's really the user who has clicked the
                              // spinner, not Android itself due to device rotation.

                // We'll demonstrate *two* different methods of accessing the celebrity name:
                // First, by pulling the data out of the array ourselves, using the passed-in array
                //   index.
                // Secondly by simply using the passed-in AdapterView's method
                //   getItemAtPosition(adapterPosition).

                // Method 1:
                //   Get the celebrity string from the celebrities array at position arrayIdx.
                String celebrity_by_arrayIdx = mCelebrities[(int) arrayIdx];

                // Method 2:
                //   Get the celebrity string using a built-in method of the AdapterView.
                String celebrity_by_adapterPosition = (String)
                        adapterView.getItemAtPosition(adapterPosition);

                // Display the celebrity with a toast.
                String msg = "Celebrity name via array: " + celebrity_by_arrayIdx + "\n"
                        +    "Celebrity name via method 'getItemAtPosition(): '"
                        +    celebrity_by_adapterPosition;
                // Toast.makeText(mContext, msg, Toast.LENGTH_SHORT).show();

                mHasUserSelected = false; // Don't forget to reset the flag for the next click / rotation.
            } // end if(mHasUserSelected)
        } // end method onItemSelected()

        @Override
        public void onNothingSelected(AdapterView<?> adapterView) {
            // not used in this example
        }
    } // end class SpinnerInteractionHandler


} // end class MainActivity