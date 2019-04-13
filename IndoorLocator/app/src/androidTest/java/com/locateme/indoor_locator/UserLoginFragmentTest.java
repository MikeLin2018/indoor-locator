package com.locateme.indoor_locator;

import android.content.res.Resources;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import junit.framework.TestCase;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.closeSoftKeyboard;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class UserLoginFragmentTest {

    @Rule
    public ActivityTestRule<UserLoginActivity> mActivityTestRule = new ActivityTestRule<>(UserLoginActivity.class);
    private Resources resources;
    private UserLoginFragment userLoginFragment;

    @Before
    public void init(){
        mActivityTestRule.getActivity()
                .getSupportFragmentManager().beginTransaction();
        resources = mActivityTestRule.getActivity().getResources();
        userLoginFragment = new UserLoginFragment();
    }


    @Test
    public void textViewTest() throws Exception {

        onView(withId(R.id.title)).check(matches(withText("Login")));

    }



//    // check on button click email/password validation
//    @Test
//    public void textViewValueTest() throws Exception {
//
//        onView(withId(R.id.editText)).perform(typeText("sreejathoomgmail.com"));
//        onView(withId(R.id.editText2)).perform(typeText("123"));
//
//        closeSoftKeyboard();
//
//        onView(withId(R.id.loginButton)).perform(click());
//
//        String s = resources.getString(R.string.error_invalid_email);
//        onView(withId(R.id.errorMessage_login)).check(matches(withText(s)));
//    }

    @Test
    public void emailValidityTest() throws Exception {
        TestCase.assertTrue(userLoginFragment.validEmailForm("sreejathoom@gmail.com"));
        TestCase.assertFalse(userLoginFragment.validEmailForm("sreejathoomgmail.com"));
        TestCase.assertFalse(userLoginFragment.validEmailForm("sreejathoom@gmailcom"));
    }


    //set the value to edit text
    @Test
    public void editTextSetTest() throws Exception {
        onView(withId(R.id.editText)).perform(typeText("sreejathoom@gmail.com"));
        onView(withId(R.id.editText2)).perform(typeText("123456"));

        closeSoftKeyboard();

        onView(withId(R.id.editText))
                .check(matches(hasValueEqualTo("sreejathoom@gmail.com")));
        onView(withId(R.id.editText2))
                .check(matches(hasValueEqualTo("123456")));

    }



    Matcher<View> hasValueEqualTo(final String content) {

        return new TypeSafeMatcher<View>() {

            @Override
            public void describeTo(Description description) {
                description.appendText("Has EditText/TextView the value:  " + content);
            }

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof TextView) && !(view instanceof EditText)) {
                    return false;
                }
                if (view != null) {
                    String text;
                    if (view instanceof TextView) {
                        text = ((TextView) view).getText().toString();
                    } else {
                        text = ((EditText) view).getText().toString();
                    }

                    return (text.equalsIgnoreCase(content));
                }
                return false;
            }
        };


    }


}