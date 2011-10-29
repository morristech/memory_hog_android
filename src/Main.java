package nz.gen.geek_central.MemoryHog;
/*
    Memory-hog test app for Android. The purpose of this is to
    grab enough memory to force paused activities in the back
    stack to be killed after their onSaveInstanceState methods
    are called. This way you can make sure they correctly restore
    their UI state as they are restarted when the Back key is pressed.

    Unfortunately, this app does not work gracefully; instead
    of reporting that it has run out of memory, the system kills it.
    But that's OK, you were going to return to the previous activity
    anyway...

    Then you discover that everything prior to that has also been
    killed, including the launcher. Bit of a sledgehammer, eh wot...

    Copyright 2011 by Lawrence D'Oliveiro <ldo@geek-central.gen.nz>.

    Licensed under the Apache License, Version 2.0 (the "License"); you
    may not use this file except in compliance with the License. You may
    obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
    implied. See the License for the specific language governing
    permissions and limitations under the License.
*/

public class Main extends android.app.Activity
  {

    protected android.widget.TextView Message;
    protected android.widget.ArrayAdapter<String> Digits;
    protected DigitSpinner Hundreds, Tens, Units;
    protected HoggerTask Hogging = null;

    protected class HoggerTask extends android.os.AsyncTask<Void, Void, Void>
      {
        private final int ToGrab;
        private boolean Success;

        public HoggerTask
          (
            int ToGrab
          )
          {
            this.ToGrab = ToGrab;
          } /*HoggerTask*/

        @Override
        protected Void doInBackground
          (
            Void... Unused
          )
          {
            Success = GrabIt((long)ToGrab * 1048576L);
            return
                null;
          } /*doInBackground*/

        @Override
        protected void onPostExecute
          (
            Void Unused
          )
          {
            Message.setText
              (
                String.format
                  (
                    Success ?
                        "Grabbed %d MiB"
                    :
                        "Failed to grab %d MiB",
                    ToGrab
                  )
              );
            Hogging = null;
          } /*onPostExecute*/

      } /*HoggerTask*/

    protected native boolean GrabIt
      (
        long HowMuch
      );
    protected native void FreeIt();

    static
      {
        System.loadLibrary("hogger");
      } /*static*/

    @Override
    public void onCreate
      (
        android.os.Bundle savedInstanceState
      )
      {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        Message = (android.widget.TextView)findViewById(R.id.message);
        Hundreds = (DigitSpinner)findViewById(R.id.hundreds);
        Tens = (DigitSpinner)findViewById(R.id.tens);
        Units = (DigitSpinner)findViewById(R.id.units);
        ((android.widget.Button)findViewById(R.id.doit)).setOnClickListener
          (
            new android.view.View.OnClickListener()
              {
                public void onClick
                  (
                    android.view.View TheView
                  )
                  {
                    final int HowMuch =
                            Hundreds.GetDigit() * 100
                        +
                            Tens.GetDigit() * 10
                        +
                            Units.GetDigit();
                    Message.setText(String.format("Grabbing %d MiB...", HowMuch));
                    Hogging = new HoggerTask(HowMuch);
                    Hogging.execute((Void)null);
                  } /*onClick*/
              } /*OnClickListener*/
          );
        Message.setText("Choose Size");
      } /*onCreate*/

    @Override
    public void onPause()
      {
        if (Hogging != null)
          {
            Hogging.cancel(true);
            Hogging = null;
          } /*if*/
        FreeIt();
        super.onPause();
      } /*onPause*/

  } /*Main*/
