package com.locateme.indoor_locator;

import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.rule.ActivityTestRule;

@RunWith(AndroidJUnit4.class)
public class UserLoginFragmentTest {


    @Rule
    public ActivityTestRule<UserSignupActivity> mActivityTestRule = new ActivityTestRule<>(UserSignupActivity.class);

    @Before
    public void init(){
        mActivityTestRule.getActivity()
                .getSupportFragmentManager().beginTransaction();
    }


//    @Test
//    public void testFragment() {
//        //onView(withId(R.id.loginButton)).perform(click());
//        onView(allOf(withId(R.id.textView),withEffectiveVisibility(VISIBLE))).
//                check(matches(isDisplayed()));
//    }

//    @Test
//    public void fragment_can_be_instantiated() {
//        mActivityTestRule.getActivity().runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                UserLoginFragment userLoginFragment = startVoiceFragment();
//            }
//        });
//        // Then use Espresso to test the Fragment
//        onView(withId(R.id.iv_record_image)).check(matches(isDisplayed()));
//    }

    @Before
    public void setUp() throws Exception {
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void onCreateView() {
    }

    @Test
    public void validEmailForm() {
    }


}