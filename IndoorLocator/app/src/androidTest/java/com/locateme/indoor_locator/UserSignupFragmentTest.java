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
public class UserSignupFragmentTest {

    @Rule
    public ActivityTestRule<UserSignupActivity> mActivityTestRule = new ActivityTestRule<>(UserSignupActivity.class);
    private Resources resources;
    private UserSignupFragment userSignupFragment;

    @Before
    public void init(){
        mActivityTestRule.getActivity()
                .getSupportFragmentManager().beginTransaction();
        resources = mActivityTestRule.getActivity().getResources();
        userSignupFragment = new UserSignupFragment();
    }


    @Test
    public void textViewTest() throws Exception {

        onView(withId(R.id.title)).check(matches(withText("Sign up")));
    }

//    @Test
//    public void textViewValueTest() throws Exception {
//
//        onView(withId(R.id.signup_username)).perform(typeText("sreeja"));
//        onView(withId(R.id.signup_email)).perform(typeText("sreejathoomgmail.com"));
//        onView(withId(R.id.signup_password)).perform(typeText("123456"));
//        onView(withId(R.id.signup_password)).perform(typeText("123456"));
//
//        closeSoftKeyboard();
//
//        onView(withId(R.id.signupButton)).perform(click());
//
//        TimeUnit.SECONDS.sleep(5);
//        String s = resources.getString(R.string.error_invalid_email);
//
//        onView(withId(R.id.errorMessage2)).check(matches(withText(s)));
//    }


    //set the value to editte
    @Test
    public void editTextSetTest() throws Exception {

        onView(withId(R.id.signup_username)).perform(typeText("sreeja"));
        onView(withId(R.id.signup_email)).perform(typeText("sreejathoom@gmail.com"));
        onView(withId(R.id.signup_password)).perform(typeText("123456"));
        onView(withId(R.id.signup_confirmPassword)).perform(typeText("123456"));

        closeSoftKeyboard();

        onView(withId(R.id.signup_username))
                .check(matches(hasValueEqualTo("sreeja")));
        onView(withId(R.id.signup_email))
                .check(matches(hasValueEqualTo("sreejathoom@gmail.com")));
        onView(withId(R.id.signup_password))
                .check(matches(hasValueEqualTo("123456")));
        onView(withId(R.id.signup_confirmPassword))
                .check(matches(hasValueEqualTo("123456")));
    }

    @Test
    public void emailValiditySignupTest() throws Exception{
        TestCase.assertTrue(userSignupFragment.validEmailForm("sreejathoom@gmail.com"));
        TestCase.assertFalse(userSignupFragment.validEmailForm("sreejathoomgmail.com"));
        TestCase.assertFalse(userSignupFragment.validEmailForm("sreejathoom@gmailcom"));
    }

    @Test
    public void passwordValiditySignupTest() throws Exception{
        TestCase.assertTrue(userSignupFragment.validPasswordForm("Hello1"));
        TestCase.assertFalse(userSignupFragment.validPasswordForm("Hell1"));
        TestCase.assertFalse(userSignupFragment.validPasswordForm(""));
        TestCase.assertFalse(userSignupFragment.validPasswordForm("hello"));
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
