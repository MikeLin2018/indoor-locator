package com.locateme.indoor_locator;

import org.junit.Test;
import org.mockito.Mockito;

import static org.junit.Assert.assertFalse;

public class DialogTest {

    @Test
    public void dialogTest() {
        LoginErrorDialogFragment a = Mockito.mock(LoginErrorDialogFragment.class);
        assertFalse(a.isVisible());
    }
}
