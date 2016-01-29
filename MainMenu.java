package wooio.artistblock;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;


public class MainMenu extends AppCompatActivity {

    //TODO: fix start button animation for lollipop

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        //initiates views
        final ImageView start_button = (ImageView) findViewById(R.id.menu_start_button);
        final ImageView project_button = (ImageView) findViewById(R.id.menu_projects_button);
        final ImageView settings_button = (ImageView) findViewById(R.id.menu_settings_button);

        ArrayList<group> words = new ArrayList();
        words.add(new group("Oil", "Mammal", "11/10"));
        words.add(new group("Pastel", "Tree", "3/12"));
        words.add(new group("Digital", "Golden Gate", "6/21"));

        //ListView stuff
        ListView accepted_projects = (ListView) findViewById(R.id.menu_recent_list);
        wordsAdapter adapter = new wordsAdapter(this, words);
        accepted_projects.setAdapter(adapter);
        //ListView stuff

        //initiates animations and the timer handler
        final Animation whileClicked = AnimationUtils.loadAnimation(this, R.anim.shrink_rotate);  //gets the shrink and rotate animation from anim
        final Handler timer = new Handler();                                                      //timer used during <button> pressed

        /*          creates the start button touch listener          */
        /*     will start animations and send to random activity     */
        start_button.setOnTouchListener(new View.OnTouchListener() {
            //initiates the final integers used for organization purposes
            final int DURATION = 1000, DEGREES_ROTATED = 45, IDLE = 0, DOWN = 1, UP = 2;          //DURATION, DEGREES_ROTATED, and SCALE are default integers used in shrink_rotate anim
            final double SCALE = 0.8;                                                             //IDLE, DOWN, and UP are numbers used for the inside of the timer

            //initiates variables used to determine the release animation
            float oScaleX, oScaleY;
            long lastDown = 0;
            int actionFlag = IDLE, moveFlag = 0;
            Rect IMAGE_BOUNDS;

            @Override
            public boolean onTouch(View arg0, final MotionEvent arg1) {
                if (arg1.getAction() == MotionEvent.ACTION_DOWN) {                                //checks if button is pressed

                    IMAGE_BOUNDS = new Rect(
                            arg0.getLeft(), arg0.getTop(), arg0.getRight(), arg0.getBottom()      //gets the bounds of the image
                    );

                    //sets variables properly (view right side comments)
                    actionFlag = DOWN;                                                            //sets flag to: button pressed
                    lastDown = System.currentTimeMillis();                                        //captures time when button pressed

                    oScaleX = 1;                                           //captures original scale X
                    oScaleY = 1;                                           //captures original scale y

                    //begins the clicked animation
                    start_button.startAnimation(whileClicked);

                    /*      creates timer set to go off after DURATION      */
                    /*  if the button is still pressed after the duration:  */
                    /*   then it keeps the data the same as end of pressed  */
                    /*                      animation                       */
                    timer.postDelayed(new Runnable() {
                        @Override
                        public void run() {                                                       //FIXME: click multiple times and hold leads to very small image
                            if (actionFlag == DOWN && moveFlag == 0) {                            //if the button is still pressed
                                start_button.setRotation(DEGREES_ROTATED);                        //image rotation set to DEGREES_ROTATED
                                start_button.setScaleX(start_button.getScaleX() * (float) SCALE); //image scale X set to final scale
                                start_button.setScaleY(start_button.getScaleY() * (float) SCALE); //image scale Y set to final scale
                            }
                        }
                    }, DURATION + 1);                                                             //timer set to go off 1 milli after DURATION

                } else if (arg1.getAction() == MotionEvent.ACTION_MOVE){
                    if(!IMAGE_BOUNDS.contains(arg0.getLeft() + (int) arg1.getX(), arg0.getTop() + (int) arg1.getY()) && moveFlag == 0){

                        actionFlag = UP;                                                          //sets flag to: button released (or in this case, not pressed)

                    /*       Creates new Animation Set for released button       */
                    /*   Finds current position of the image and animates it     */
                    /*                      based on data                        */
                        AnimationSet growRotate = new AnimationSet(true);

                        //Below gets how long the button was held down
                        long time = System.currentTimeMillis() - lastDown;                        //subtracts current time from the time captured at press of button
                        if (time >= DURATION)
                            time = DURATION;                                                      //if the time exceeds the duration, set the time to the duration
                        int timePressed = (int) time;                                             //turns the float into an integer


                        float sX = oScaleX - ((oScaleX - oScaleX * (float) SCALE) / DURATION)
                                * timePressed;                                                    //finds where the Scale of X was left
                        float sY = oScaleY - ((oScaleY - oScaleY * (float) SCALE) / DURATION)
                                * timePressed;                                                    //finds where the Scale of Y was left
                        float rot = ((float) DEGREES_ROTATED / DURATION) * timePressed;           //finds where the Rotation of the image was left

                        //sets the data back to original in case of data change
                        start_button.setRotation(0);
                        start_button.setScaleX(oScaleX);
                        start_button.setScaleY(oScaleY);

                        //creates the rotation animation
                        RotateAnimation rotate = new RotateAnimation(rot, 0, 50, 50);             //starts where the rotation was left (see "float rot")
                        rotate.setInterpolator(new LinearInterpolator());
                        rotate.setDuration(timePressed);                                          //sets duration to same time elapsed during button pressed


                        ScaleAnimation scale =
                                new ScaleAnimation(sX, oScaleX, sY, oScaleY, 50, 50);             //starts where scales were left (see "float sX" or "float sY")
                        scale.setDuration(timePressed);                                           //sets duration to same time elapsed during button pressed

                        //adds both rotation and scale to the animation set
                        growRotate.addAnimation(rotate);
                        growRotate.addAnimation(scale);

                        //starts the animation using the animation set
                        start_button.startAnimation(growRotate);

                        moveFlag = 1;

                    }
                } else if (arg1.getAction() == MotionEvent.ACTION_UP) {                           //checks if button is released
                    if(     IMAGE_BOUNDS.contains(arg0.getLeft() + (int) arg1.getX(),
                            arg0.getTop() + (int) arg1.getY()) && moveFlag == 0 ) {

                        moveFlag = 0;
                        actionFlag = UP;                                                          //sets flag to: button released

                    /*       Creates new Animation Set for released button       */
                    /*   Finds current position of the image and animates it     */
                    /*                      based on data                        */
                        AnimationSet growRotate = new AnimationSet(true);

                        //Below gets how long the button was held down
                        long time = System.currentTimeMillis() - lastDown;                        //subtracts current time from the time captured at press of button
                        if (time >= DURATION)
                            time = DURATION;                                                      //if the time exceeds the duration, set the time to the duration
                        int timePressed = (int) time;                                             //turns the float into an integer


                        float sX = oScaleX - ((oScaleX - oScaleX * (float) SCALE) / DURATION)
                                * timePressed;                                                    //finds where the Scale of X was left
                        float sY = oScaleY - ((oScaleY - oScaleY * (float) SCALE) / DURATION)
                                * timePressed;                                                    //finds where the Scale of Y was left
                        float rot = ((float) DEGREES_ROTATED / DURATION) * timePressed;           //finds where the Rotation of the image was left

                        //sets the data back to original in case of data change
                        start_button.setRotation(0);
                        start_button.setScaleX(oScaleX);
                        start_button.setScaleY(oScaleY);

                        //creates the rotation animation
                        RotateAnimation rotate = new RotateAnimation(rot, -720, 50, 50);          //starts where the rotation was left (see "float rot") ends after 2 rotations
                        rotate.setInterpolator(new OvershootInterpolator());
                        rotate.setDuration(timePressed + DURATION);                               //sets duration to same time elapsed during button pressed + the original duration


                        ScaleAnimation scale =
                                new ScaleAnimation(sX, oScaleX, sY, oScaleY, 50, 50);             //starts where scales were left (see "float sX" or "float sY")
                        scale.setDuration(timePressed);                                           //sets duration to same time elapsed during button pressed

                        //adds both rotation and scale to the animation set
                        growRotate.addAnimation(rotate);
                        growRotate.addAnimation(scale);

                        //starts the animation using the animation set
                        start_button.startAnimation(growRotate);

                        //TODO:intent to open the randomization class
                    }
                    moveFlag=0;
                }
                return true;
            }
        });

        /*         creates the project button touch listener        */
        /*     will start animation and send to project activity    */
        project_button.setOnTouchListener(sideButtonListener);

        /*         creates the setting button touch listener        */
        /*     will start animation and send to setting activity    */
        settings_button.setOnTouchListener(sideButtonListener);
    }

    View.OnTouchListener sideButtonListener =new View.OnTouchListener() {
        final float SCALE_FROM = 1, SCALE_TO = SCALE_FROM*((float)0.8);
        final int DURATION = 1000, IDLE = 0, PRESSED = 1, RELEASED = 2;
        int actionFlag = IDLE, moveFlag = 0, timePressed;
        Handler timer = new Handler();
        Rect IMAGE_BOUNDS;

        @Override
        public boolean onTouch(final View v, final MotionEvent event) {
            if (event.getAction() == MotionEvent.ACTION_DOWN) {
                timePressed = (int) System.currentTimeMillis();
                actionFlag = PRESSED;
                moveFlag=0;

                IMAGE_BOUNDS = new Rect(v.getLeft(), v.getTop(), v.getRight(), v.getBottom());

                ScaleAnimation scale =
                        new ScaleAnimation(SCALE_FROM, SCALE_TO, SCALE_FROM, SCALE_TO, 50, 50);
                scale.setDuration(DURATION);
                scale.setInterpolator(new LinearInterpolator());
                v.startAnimation(scale);

                timer.postDelayed(new Runnable() {
                    @Override
                    public void run() {                                                       //FIXME: click multiple times and hold leads to very small image
                        if (actionFlag == PRESSED && moveFlag==0) {                                          //if the button is still pressed

                            float currentTime = System.currentTimeMillis() - timePressed;
                            if(currentTime>1000)currentTime=1000;

                            float correctScale = SCALE_FROM - ((SCALE_FROM - SCALE_TO)/DURATION * currentTime);

                            v.setScaleX(correctScale);                               //image scale X set to final scale
                            v.setScaleY(correctScale);                               //image scale Y set to final scale

                            ScaleAnimation scale = new ScaleAnimation(SCALE_TO+(float)0.1, SCALE_TO, SCALE_TO+(float)0.1, SCALE_TO, 50, 50);
                            scale.setDuration(1000);
                            scale.setRepeatCount(Animation.INFINITE);
                            scale.setRepeatMode(Animation.REVERSE);

                        }
                    }
                }, DURATION + 1);
            } else if (event.getAction() == MotionEvent.ACTION_MOVE) {                        //TODO: once it reaches past a certain point (going left) activity starts
                if(!IMAGE_BOUNDS.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY()) && moveFlag==0) {
                    actionFlag = RELEASED;
                    moveFlag=1;

                    int currentTime = (int) System.currentTimeMillis() - timePressed;
                    if(currentTime>1000)currentTime=1000;

                    float currentScale = SCALE_FROM - ((SCALE_FROM - SCALE_FROM * SCALE_TO) / DURATION) * currentTime;

                    ScaleAnimation scale = new ScaleAnimation(currentScale, SCALE_FROM, currentScale, SCALE_FROM, 50, 50);
                    scale.setDuration(currentTime);
                    scale.setInterpolator(new LinearInterpolator());

                    v.setScaleX(SCALE_FROM);
                    v.setScaleY(SCALE_FROM);

                    v.startAnimation(scale);
                }
            } else if (event.getAction() == MotionEvent.ACTION_UP){
                if(IMAGE_BOUNDS.contains(v.getLeft() + (int) event.getX(), v.getTop() + (int) event.getY())){
                    actionFlag = RELEASED;

                    v.setScaleX(SCALE_FROM);
                    v.setScaleY(SCALE_FROM);

                    int currentTime = (int) System.currentTimeMillis() - timePressed;
                    if(currentTime>1000)currentTime=1000;

                    float currentScale = SCALE_FROM - ((SCALE_FROM - SCALE_FROM * SCALE_TO) / DURATION) * currentTime;

                    ScaleAnimation scale;

                    if(currentTime > 100) {
                        scale = new ScaleAnimation(currentScale, SCALE_FROM + (float) 0.1, currentScale, SCALE_FROM + (float) 0.1, 50, 50);
                        scale.setDuration(currentTime+DURATION/10);
                    }else {
                        scale = new ScaleAnimation(SCALE_FROM, SCALE_FROM + (float) 0.1, SCALE_FROM, SCALE_FROM + (float) 0.1, 50, 50);
                        scale.setDuration(currentTime + DURATION / 4);
                    }
                    scale.setInterpolator(new OvershootInterpolator());
                    scale.setRepeatMode(Animation.REVERSE);
                    scale.setRepeatCount(3);

                    v.startAnimation(scale);
                    
                    //TODO: make menu move up and fade away the menu title
                    if(v.getId() == R.id.menu_projects_button){
                        //TODO open project activity
                    }
                    if(v.getId() == R.id.menu_settings_button){
                        //TODO open settings activity
                    }

                }

            }
            return true;
        }
    };

    public class wordsAdapter extends BaseAdapter {
        Context mContext;
        ArrayList<group> choice = new ArrayList<group>();
        LayoutInflater mInflater;

        public wordsAdapter(Context c, ArrayList<group> ch) {
            mContext = c;
            choice = ch;
            mInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        public int getCount() {
            return choice.size();
        }

        public Object getItem(int position) {
            return choice.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ViewHolder viewHolder;
            if (convertView == null) {
                convertView = mInflater.inflate(R.layout.layout_recent_projects, null);
                viewHolder = new ViewHolder();
                viewHolder.layout_medium = (TextView) convertView.findViewById(R.id.accepted_menu_med);
                viewHolder.layout_subject = (TextView) convertView.findViewById(R.id.accepted_menu_sub);
                viewHolder.layout_date = (TextView) convertView.findViewById(R.id.accepted_menu_date);
                viewHolder.layout_box = (RelativeLayout) convertView.findViewById(R.id.accepted_menu_back);
                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            String sub = choice.get(position).getSub();
            String med = choice.get(position).getMed();
            String date = choice.get(position).getDate();
            viewHolder.layout_medium.setText(med);
            viewHolder.layout_subject.setText(sub);
            viewHolder.layout_date.setText(date);

            return convertView;
        }
    }

    private static class ViewHolder {
        public TextView layout_medium;
        public TextView layout_subject;
        public TextView layout_date;
        public RelativeLayout layout_box;
    }

    private static class group {
        String medium, subject, date;
        public group(String m, String s, String d){
            medium = m;
            subject = s;
            date = d;
        }

        public String getSub() {return subject;}
        public String getMed() { return medium;}
        public String getDate() { return date;}
    }

}