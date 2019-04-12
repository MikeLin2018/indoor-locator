package com.locateme.indoor_locator;

import android.support.test.rule.ActivityTestRule;
import android.support.test.rule.GrantPermissionRule;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ApDataScanFragmentTest extends ApDataScanFragment{

    @Rule
    public ActivityTestRule<ApDataScanActivity> mActivityTestRule = new ActivityTestRule<>(ApDataScanActivity.class);

    @Rule public GrantPermissionRule permissionRule = GrantPermissionRule.grant(android.Manifest.permission.ACCESS_FINE_LOCATION);
    private ApDataScanFragment apDataScanFragment;

    @Before
    public void init(){
        mActivityTestRule.getActivity()
                .getSupportFragmentManager().beginTransaction();
        apDataScanFragment = new ApDataScanFragment();
    }

//    @Test
//    public void arrayadapTerTest(){
//        onData(is(instanceOf(String.class)), is("Americano")));
//    }
//
//
//    @Test
//    public void hasLocationPermissionTest(){
//
//        TestCase.assertTrue(apDataScanFragment.has
//    }
//
//    public static Matcher withName(Matcher nameMatcher){
//        return new TypeSafeMatcher<Person>(){
//            @Override
//            public boolean matchesSafely(Person person) {
//                return nameMatcher.matches(person.getName());
//            }
//
//            @Override
//            public void describeTo(Description description) {
//            ...
//            }
//        }
//    }

}
