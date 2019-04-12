package com.locateme.indoor_locator;

import android.support.test.runner.AndroidJUnit4;

import junit.framework.TestCase;

import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class UserSignupFragmentTest {

    UserSignupFragment userSignupFragment = new UserSignupFragment();
    @Test
    public void onCreateView() {
    }

    @Test
    public void validEmailFormTest() {

        TestCase.assertTrue(userSignupFragment.validEmailForm("name@email.com"));
        TestCase.assertFalse(userSignupFragment.validEmailForm("name@.com"));
    }

    @Test
    public void signupTest() {

        User user = new User("sreejathoom@gmail.com","sreeja","123");
        userSignupFragment.signup(user);
        TestCase.assertEquals("sreeja", user.getPassword());

    }
}
